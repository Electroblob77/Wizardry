package electroblob.wizardry.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import electroblob.wizardry.event.SpellCastEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * Object that wraps any number of spell modifiers into one, allowing for expandability within the Spell#cast methods.
 * This class is essentially a glorified {@link Map} which can be written to and read from a {@link ByteBuf}. It is
 * possible to calculate spell modifiers from wand NBT within the cast methods, but this is cumbersome and does not
 * allow the modifiers to be sent to the client, which is sometimes necessary (for example, detonate needs to know about
 * range modifiers on the client side or the particles wouldn't show outside of the base range).
 * <p>
 * Most external interaction with SpellModifiers objects will be in {@link SpellCastEvent.Pre}, where you can add
 * additional modifiers to them if desired for use with your own spells, or modify the existing ones. If you have added
 * a wand upgrade, this is <b>not</b> done automatically for you; you will have to do it yourself (for the simple reason
 * that not all wand upgrades affect spells). SpellModifiers objects are <i>mutable</i>, so you can simply change the
 * values they contain to modify the spell.
 * <p>
 * To use a SpellModifiers object within the <code>Spell.cast</code> methods, simply retrieve the desired multiplier
 * using {@link SpellModifiers#get(Item)} for wand upgrades, or {@link SpellModifiers#get(String)} if the multiplier is
 * not from a wand upgrade.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 * @see WandHelper
 */
// I have made the decision that the USERS of this class must decide whether the modifiers need syncing or not, on a
// case-by-case basis. Why? Because assigning keys to either sync or not sync would be unnecessarily restrictive, and
// would mean they have to be registered, and part of the point of SpellModifiers is that they can be added to on the
// fly.
public final class SpellModifiers {

	/** Constant string identifier for the damage modifier. All the other modifiers in Wizardry have items. */
	public static final String DAMAGE = "damage";

	private Map<String, Float> multiplierMap;
	private Map<String, Float> syncedMultiplierMap;

	/**
	 * Creates an empty SpellModifiers object. All calls to <code>get(...)</code> on an empty SpellModifiers object will
	 * return a value of 1.
	 */
	public SpellModifiers(){
		multiplierMap = new HashMap<String, Float>();
		syncedMultiplierMap = new HashMap<String, Float>();
	}

	/**
	 * Adds the given multiplier to this SpellModifiers object, using the string identifier that the given wand upgrade
	 * item was registered with.
	 * 
	 * @throws IllegalArgumentException if the given item is not a registered special wand upgrade.
	 * @param upgrade The upgrade item the multiplier corresponds to.
	 * @param multiplier The multiplier value, with 1 being default. Usage of modifiers is up to individual spells to
	 *        implement.
	 * @param needsSyncing Whether this multiplier should be synchronised with the client via packets. <i>Only set this
	 *        to true if particles will be spawned which need to know the value of the multiplier.</i>
	 * @return The SpellModifiers object, allowing this method to be chained onto the constructor.
	 */
	public SpellModifiers set(Item upgrade, float multiplier, boolean needsSyncing){
		this.set(WandHelper.getIdentifier(upgrade), multiplier, needsSyncing);
		return this;
	}

	/**
	 * Adds the given multiplier to this SpellModifiers object, using the given string key. In most cases, the
	 * multiplier will correspond to a wand upgrade, in which case use {@link SpellModifiers#set(Item, float, boolean)}
	 * instead.
	 * 
	 * @param key The key used to identify the multiplier.
	 * @param multiplier The multiplier value, with 1 being default. Usage of modifiers is up to individual spells to
	 *        implement.
	 * @param needsSyncing Whether this multiplier should be synchronised with the client via packets. <i>Only set this
	 *        to true if particles will be spawned which depend on the multiplier.</i>
	 * @return The SpellModifiers object, allowing this method to be chained onto the constructor.
	 */
	public SpellModifiers set(String key, float multiplier, boolean needsSyncing){
		multiplierMap.put(key, multiplier);
		if(needsSyncing) syncedMultiplierMap.put(key, multiplier);
		return this;
	}

	/**
	 * Returns the multiplier corresponding to the given wand upgrade item, or 1 if no multiplier was stored.
	 * 
	 * @throws IllegalArgumentException if the given item is not a registered special wand upgrade.
	 */
	public float get(Item upgrade){
		return get(WandHelper.getIdentifier(upgrade));
	}

	/**
	 * Returns the multiplier corresponding to the given string key, or 1 if no multiplier was stored. In most cases,
	 * the multiplier will correspond to a wand upgrade, in which case use {@link SpellModifiers#get(Item)} instead.
	 */
	public float get(String key){
		Float value = multiplierMap.get(key);
		// Must check for null before unboxing, and if it is null, return the default 1.
		return value == null ? 1 : value;
	}

	/**
	 * Returns an unmodifiable map of the modifiers stored in this SpellModifiers object. Useful for iterating through
	 * the modifiers.
	 */
	public Map<String, Float> getModifiers(){
		return Collections.unmodifiableMap(this.multiplierMap);
	}

	/** Removes all modifiers from this SpellModifiers object, effectively resetting them all to 1. */
	public void reset(){
		this.multiplierMap.clear();
		this.syncedMultiplierMap.clear();
	}

	/** Reads this SpellModifiers object from the given ByteBuf. */
	public void read(ByteBuf buf){
		int entryCount = buf.readInt();
		for(int i = 0; i < entryCount; i++){
			this.set(ByteBufUtils.readUTF8String(buf), buf.readFloat(), false);
		}
	}

	/** Writes this SpellModifiers object to the given ByteBuf so it can be sent via packets. */
	public void write(ByteBuf buf){
		buf.writeInt(syncedMultiplierMap.size());
		for(Entry<String, Float> entry : syncedMultiplierMap.entrySet()){
			ByteBufUtils.writeUTF8String(buf, entry.getKey());
			buf.writeFloat(entry.getValue());
		}
	}

	/**
	 * Creates a new SpellModifiers object from the given NBTTagCompound. The NBTTagCompound should have 1 or more float
	 * tags, which will be stored as modifiers under the same name as the tag. For example, the following NBT tag (in
	 * command syntax) will create a SpellModifiers object with a damage modifier of 1.5 and a range modifier of 2:
	 * <p>
	 * <code>{damage:1.5, range:2}</code>
	 * <p>
	 * Note that needsSyncing is set to true for all returned modifiers.
	 */
	public static SpellModifiers fromNBT(NBTTagCompound nbt){
		SpellModifiers modifiers = new SpellModifiers();
		for(String key : nbt.getKeySet()){
			modifiers.set(key, nbt.getFloat(key), true);
		}
		return modifiers;
	}

}
