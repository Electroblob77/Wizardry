package electroblob.wizardry;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.living.EntitySummonedCreature;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketPlayerSync;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.None;
import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants.NBT;

/** IExtendedEntityProperties for players. Everything player-data-related that is needed by wizardry is handled here,
 * including the ADS, soulbinding, conjured items, summoned wolves and horses, transportation and clairvoyance. */
public class ExtendedPlayer implements IExtendedEntityProperties {

	public final static String name = "WizardryExtendedPlayer";
	
	private final EntityPlayer player;
	
	public int damageToApply;
	
	// These were originally done using the item damage directly, but this caused the annoying re-equip animation
	// to keep happening so they were moved to here instead.
	public int conjuredSwordDuration;
	public int conjuredPickaxeDuration;
	public int conjuredBowDuration;
	public int conjuredArmourDuration;
	public int flamingAxeDuration;
	public int frostAxeDuration;
	public int magicWeaponDuration;
	public int flamingWeaponDuration;
	public int freezingWeaponDuration;
	public boolean hasSpiritWolf;
	public boolean hasSpiritHorse;
	
	/** Whether this player is currently casting a continuous spell via commands. Not saved over world reload
	 * and reset on player death. */
	private Spell currentlyCasting;
	/** The time for which this player has been casting a continuous spell via commands. Increments by 1 each tick.
	 * Not saved over world reload and reset on player death. */
	private int castingTick;
	/** The damage multiplier for the current continuous spell cast via commands. Not saved over world reload and
	 * reset on player death. */
	private float spellDamageMultiplier;
	/** The range multiplier for the current continuous spell cast via commands. Not saved over world reload and
	 * reset on player death. */
	private float spellRangeMultiplier;
	/** The duration multiplier for the current continuous spell cast via commands. Not saved over world reload and
	 * reset on player death. */
	private float spellDurationMultiplier;
	/** The blast multiplier for the current continuous spell cast via commands. Not saved over world reload and
	 * reset on player death. */
	private float spellBlastMultiplier;
	
	/** Coordinate for the saved transportation stone location. Will be -1 if no location is saved. */
	public int transportX, transportY, transportZ;
	/** Dimension id which the saved stone circle is in. */
	public int transportDimension;
	/** Time left until the player teleports under the effect of transportation */
	public int tpTimer;
	
	/** Coordinate for the saved clairvoyance location. Will be -1 if no location is saved. */
	public int clairvoyanceX, clairvoyanceY, clairvoyanceZ;
	/** Dimension id which the saved clairvoyance point is in. */
	public int clairvoyanceDimension;

	public EntityShield shield;
	
	public WeakReference<EntitySummonedCreature> selectedMinion;

	public HashSet<Spell> spellsDiscovered;
	
	private HashSet<UUID> allies;
	/** List of usernames of this player's allies. May not be accurate 100% of the time. This is here so that a player
	 * can view the usernames of their allies even when those allies are not online.
	 * <b> Do not use this for any other purpose than displaying the names! */
	public HashSet<String> allyNames;
	
	private HashSet<UUID> soulboundCreatures;
	
	public ExtendedPlayer(EntityPlayer player){
		this.player = player;
		this.damageToApply = 0;
		this.conjuredSwordDuration = 0;
		this.conjuredPickaxeDuration = 0;
		this.conjuredBowDuration = 0;
		this.conjuredArmourDuration = 0;
		this.flamingAxeDuration = 0;
		this.frostAxeDuration = 0;
		this.magicWeaponDuration = 0;
		this.flamingWeaponDuration = 0;
		this.freezingWeaponDuration = 0;
		this.spellsDiscovered = new HashSet<Spell>();
		// All players can recognise magic missile. This is not done using discoverSpell because that seems to cause
		// a crash on load occasionally (probably something to do with achievements being initalised)
		this.spellsDiscovered.add(WizardryRegistry.magicMissile);
		this.hasSpiritWolf = false;
		this.hasSpiritHorse = false;
		this.currentlyCasting = WizardryRegistry.none;
		this.spellDamageMultiplier = 1;
		this.spellRangeMultiplier = 1;
		this.spellDurationMultiplier = 1;
		this.spellBlastMultiplier = 1;
		this.castingTick = 0;
		this.transportX = -1;
		this.transportY = -1;
		this.transportZ = -1;
		this.transportDimension = 0;
		this.clairvoyanceX = -1;
		this.clairvoyanceY = -1;
		this.clairvoyanceZ = -1;
		this.clairvoyanceDimension = 0;
		this.tpTimer = 0;
		this.allies = new HashSet<UUID>();
		this.allyNames = new HashSet<String>();
		this.soulboundCreatures = new HashSet<UUID>();
	}
	
	public boolean hasSpellBeenDiscovered(Spell spell){
		return spellsDiscovered.contains(spell) || spell instanceof None;
	}
	
	/**
	 * Adds the spell to the list of discovered spells for this player. Automatically takes into account
	 * whether the spell has been discovered. Use this method rather than adding directly to the list
	 * because it handles the mage of all trades achievement.
	 * @param spell
	 */
	public void discoverSpell(Spell spell){
		
		if(spellsDiscovered == null){
			spellsDiscovered = new HashSet<Spell>();
		}
		
		if(!(spell instanceof None)){
			spellsDiscovered.add(spell);
		}
		
		if(spellsDiscovered.containsAll(Spell.getSpells(Spell.enabledSpells))){
			this.player.triggerAchievement(Wizardry.allSpells);
		}
		
		for(EnumElement element : EnumElement.values()){
			if(element != EnumElement.MAGIC && spellsDiscovered.containsAll(Spell.getSpells(new Spell.TierElementFilter(null, element)))){
				this.player.triggerAchievement(Wizardry.elementMaster);
			}
		}
	}
	
	/** Sets the player's saved transportation stone location. */
	public void setStoneLocation(int x, int y, int z, int dimensionID){
		this.transportX = x;
		this.transportY = y;
		this.transportZ = z;
		this.transportDimension = dimensionID;
	}
	
	/** Sets the player's saved clairvoyance location. */
	public void setClairvoyancePoint(int x, int y, int z, int dimensionID){
		this.clairvoyanceX = x;
		this.clairvoyanceY = y;
		this.clairvoyanceZ = z;
		this.clairvoyanceDimension = dimensionID;
	}

	/** Adds the given player to the list of allies belonging to the associated player, or removes the player if
	 * they are already in the list of allies. Returns true if the player was added, false if they were removed. */
	public boolean toggleAlly(EntityPlayer player){
		if(this.isPlayerAlly(player)){
			this.allies.remove(player.getUniqueID());
			// The remove method uses .equals() rather than == so this will work fine.
			this.allyNames.remove(player.getCommandSenderName());
			return false;
		}else{
			this.allies.add(player.getUniqueID());
			this.allyNames.add(player.getCommandSenderName());
			return true;
		}
	}
	
	/** Returns whether the given player is in this player's list of allies, or is on the same team as this player. */
	public boolean isPlayerAlly(EntityPlayer player){
		return this.allies.contains(player.getUniqueID()) || this.player.isOnSameTeam(player);
	}
	
	/** Adds the given entity to this player's list of soulbound creatures, and returns whether it succeeded. */
	public boolean soulbind(EntityLivingBase target){
		return this.soulboundCreatures.add(target.getUniqueID());
	}
	
	/** Returns whether the given entity has been soulbound to this player. */
	public boolean isCreatureSoulbound(EntityPlayer target){
		return this.soulboundCreatures.contains(target.getUniqueID());
	}
	
	/** Damages all creatures soulbound to this player by the given amount, and removes from the list any that no
	 * longer exist. */
	public void damageAllSoulboundCreatures(float damage){
		
		for(Iterator<UUID> iterator = this.soulboundCreatures.iterator(); iterator.hasNext();){
			
			Entity entity = WizardryUtilities.getEntityByUUID(this.player.worldObj, iterator.next());
			
			if(entity == null) iterator.remove();
			
			if(entity instanceof EntityLivingBase){
				if(entity.attackEntityFrom(MagicDamage.causeDirectMagicDamage(this.player, DamageType.MAGIC), damage)){
					// Sound only plays if the damage succeeds
					player.worldObj.playSoundAtEntity(entity, "mob.wither.hurt", 1.0F, player.worldObj.rand.nextFloat() * 0.2F + 1.0F);
				}
			}
		}
	}
	
	/** Starts casting the given spell with the given modifiers. */
	public void startCastingContinuousSpell(Spell spell, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		this.currentlyCasting = spell;
		this.spellDamageMultiplier = damageMultiplier;
		this.spellRangeMultiplier = rangeMultiplier;
		this.spellDurationMultiplier = durationMultiplier;
		this.spellBlastMultiplier = blastMultiplier;
		
		if(!this.player.worldObj.isRemote){
			PacketCastContinuousSpell.Message message = new PacketCastContinuousSpell.Message(this.player.getEntityId(), spell.id(), damageMultiplier, rangeMultiplier, blastMultiplier);
			WizardryPacketHandler.net.sendToDimension(message, this.player.worldObj.provider.dimensionId);
		}
	}
	
	/** Stops casting the current spell. */
	public void stopCastingContinuousSpell(){
		this.currentlyCasting = WizardryRegistry.none;
		this.castingTick = 0;
		this.spellDamageMultiplier = 1;
		this.spellRangeMultiplier = 1;
		this.spellDurationMultiplier = 1;
		this.spellBlastMultiplier = 1;
		
		if(!this.player.worldObj.isRemote){
			PacketCastContinuousSpell.Message message = new PacketCastContinuousSpell.Message(this.player.getEntityId(), WizardryRegistry.none.id(), 1, 1, 1);
			WizardryPacketHandler.net.sendToDimension(message, this.player.worldObj.provider.dimensionId);
		}
	}
	
	/** Returns whether this player is currently casting a continuous spell via commands. */
	public boolean isCasting(){
		return this.currentlyCasting != null && this.currentlyCasting != WizardryRegistry.none;
	}
	
	/** Returns the continuous spell this player is currently casting via commands, or the 'none' spell if they aren't
	 * casting anything. */
	public Spell currentlyCasting(){
		return currentlyCasting;
	}
	
	/** Called from the event handler each time the associated player is updated. */
	public void update(EntityPlayer entityplayer){
		
		if(this.selectedMinion != null && this.selectedMinion.get() == null) this.selectedMinion = null;
		
		Random random = entityplayer.worldObj.rand;
		
		// Annoyingly, it seems that the armour inventory is dealt with completely separately.
		if(entityplayer.inventory.hasItem(Wizardry.spectralHelmet)
				|| entityplayer.inventory.hasItem(Wizardry.spectralChestplate)
				|| entityplayer.inventory.hasItem(Wizardry.spectralLeggings)
				|| entityplayer.inventory.hasItem(Wizardry.spectralBoots)
				|| (entityplayer.inventory.armorInventory[3] != null && entityplayer.inventory.armorInventory[3].getItem() == Wizardry.spectralHelmet)
				|| (entityplayer.inventory.armorInventory[2] != null && entityplayer.inventory.armorInventory[2].getItem() == Wizardry.spectralChestplate)
				|| (entityplayer.inventory.armorInventory[1] != null && entityplayer.inventory.armorInventory[1].getItem() == Wizardry.spectralLeggings)
				|| (entityplayer.inventory.armorInventory[0] != null && entityplayer.inventory.armorInventory[0].getItem() == Wizardry.spectralBoots)){
			
			this.conjuredArmourDuration++;
			
		}else{
			this.conjuredArmourDuration = 0;
		}
		
		if(!entityplayer.worldObj.isRemote){
			if(tpTimer == 1){
				entityplayer.setPositionAndUpdate(this.transportX + 0.5, this.transportY, this.transportZ + 0.5);
				entityplayer.worldObj.playSoundAtEntity(entityplayer, "portal.travel", 1.0f, 1.0f);
				entityplayer.addPotionEffect(new PotionEffect(Potion.blindness.id, 50, 0));
				IMessage msg = new PacketTransportation.Message(entityplayer.getEntityId());
		    	WizardryPacketHandler.net.sendToDimension(msg, entityplayer.worldObj.provider.dimensionId);
			}
			
			if(tpTimer > 0){
				tpTimer--;
			}
		}
		
		if(this.currentlyCasting != null && this.currentlyCasting.isContinuous){
			this.currentlyCasting.cast(player.worldObj, player, castingTick++, spellDamageMultiplier, spellRangeMultiplier, spellDurationMultiplier, spellBlastMultiplier);
		}else{
			this.castingTick = 0;
		}
	}
	
	/** Called from the event handler each time the associated player dies. Used to reset any variables that shouldn't 
	 * persist over player death. */
	public void onPlayerDeath(){
		this.conjuredSwordDuration = 0;
		this.conjuredPickaxeDuration = 0;
		this.conjuredBowDuration = 0;
		this.conjuredArmourDuration = 0;
		this.flamingAxeDuration = 0;
		this.frostAxeDuration = 0;
		this.magicWeaponDuration = 0;
		this.flamingWeaponDuration = 0;
		this.freezingWeaponDuration = 0;
		// Death cancels transportation spell.
		this.tpTimer = 0;
		this.soulboundCreatures = new HashSet<UUID>();
		this.currentlyCasting = WizardryRegistry.none;
		this.castingTick = 0;
		this.spellDamageMultiplier = 1;
		this.spellRangeMultiplier = 1;
		this.spellDurationMultiplier = 1;
		this.spellBlastMultiplier = 1;
	}
	
	/**
	* Returns the string key to be used when saving and loading the extended player data on respawn.
	*/
	public static String getSaveKey(EntityPlayer player) {
		return player.getUniqueID().toString() + ":" + name;
	}
	
	/**
	* Used to register these extended properties for the player during EntityConstructing event
	* This method is for convenience only; it will make your code look nicer
	*/
	public static final void register(EntityPlayer player){
		player.registerExtendedProperties(ExtendedPlayer.name, new ExtendedPlayer(player));
	}
	
	/**
	* Returns the ExtendedPlayer properties for the specified player.
	*/
	public static final ExtendedPlayer get(EntityPlayer player){
		return (ExtendedPlayer) player.getExtendedProperties(name);
	}
	
	/** Sends a packet to this player's client to synchronise necessary information. Only called server side. */
	public void sync(){
		if(this.player instanceof EntityPlayerMP){
			int id = -1;
			if(this.selectedMinion != null && this.selectedMinion.get() != null) id = this.selectedMinion.get().getEntityId();
			IMessage msg = new PacketPlayerSync.Message(this.spellsDiscovered, id);
			WizardryPacketHandler.net.sendTo(msg, (EntityPlayerMP)this.player);
		}
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		
		// NBTTagCompound is for Maps, NBTTagList is for Lists/Sets
		
		NBTTagCompound properties = new NBTTagCompound();
		properties.setInteger("conjuredSwordDuration", this.conjuredSwordDuration);
		properties.setInteger("conjuredPickaxeDuration", this.conjuredPickaxeDuration);
		properties.setInteger("conjuredBowDuration", this.conjuredBowDuration);
		properties.setInteger("conjuredArmourDuration", this.conjuredArmourDuration);
		properties.setInteger("flamingAxeDuration", this.flamingAxeDuration);
		properties.setInteger("frostAxeDuration", this.frostAxeDuration);
		properties.setInteger("magicWeaponDuration", this.magicWeaponDuration);
		properties.setInteger("flamingWeaponDuration", this.flamingWeaponDuration);
		properties.setInteger("freezingWeaponDuration", this.freezingWeaponDuration);
		properties.setBoolean("hasSpiritWolf", this.hasSpiritWolf);
		properties.setBoolean("hasSpiritHorse", this.hasSpiritHorse);
		properties.setInteger("x", this.transportX);
		properties.setInteger("y", this.transportY);
		properties.setInteger("z", this.transportZ);
		properties.setInteger("dimension", this.transportDimension);
		properties.setInteger("x2", this.clairvoyanceX);
		properties.setInteger("y2", this.clairvoyanceY);
		properties.setInteger("z2", this.clairvoyanceZ);
		properties.setInteger("dimension2", this.clairvoyanceDimension);
		properties.setInteger("tpTimer", this.tpTimer);
		
		NBTTagList tagList = new NBTTagList();
		for(UUID id : this.allies){
			tagList.appendTag(new NBTTagString(id.toString()));
		}
		properties.setTag("allies", tagList);
		
		NBTTagList tagList2 = new NBTTagList();
		for(String name : this.allyNames){
			tagList2.appendTag(new NBTTagString(name));
		}
		properties.setTag("allyNames", tagList2);
		
		NBTTagList tagList3 = new NBTTagList();
		for(UUID id : this.soulboundCreatures){
			tagList3.appendTag(new NBTTagString(id.toString()));
		}
		properties.setTag("soulboundCreatures", tagList3);
		
		int[] spells = new int[this.spellsDiscovered.size()];
		int i=0;
		for(Spell spell : this.spellsDiscovered){
			spells[i] = spell.id();
			i++;
		}
		properties.setIntArray("discoveredSpells", spells);
		
		compound.setTag(name, properties);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		NBTTagCompound properties = (NBTTagCompound) compound.getTag(name);
		if(properties != null){
			this.conjuredSwordDuration = properties.getInteger("conjuredSwordDuration");
			this.conjuredPickaxeDuration = properties.getInteger("conjuredPickaxeDuration");
			this.conjuredBowDuration = properties.getInteger("conjuredBowDuration");
			this.conjuredArmourDuration = properties.getInteger("conjuredArmourDuration");
			this.flamingAxeDuration = properties.getInteger("flamingAxeDuration");
			this.frostAxeDuration = properties.getInteger("frostAxeDuration");
			this.magicWeaponDuration = properties.getInteger("magicWeaponDuration");
			this.flamingWeaponDuration = properties.getInteger("flamingWeaponDuration");
			this.freezingWeaponDuration = properties.getInteger("freezingWeaponDuration");
			this.hasSpiritWolf = properties.getBoolean("hasSpiritWolf");
			this.hasSpiritHorse = properties.getBoolean("hasSpiritHorse");
			this.transportX = properties.getInteger("x");
			this.transportY = properties.getInteger("y");
			this.transportZ = properties.getInteger("z");
			this.transportDimension = properties.getInteger("dimension");
			this.clairvoyanceX = properties.getInteger("x2");
			this.clairvoyanceY = properties.getInteger("y2");
			this.clairvoyanceZ = properties.getInteger("z2");
			this.clairvoyanceDimension = properties.getInteger("dimension2");
			this.tpTimer = properties.getInteger("tpTimer");
			
			this.allies = new HashSet<UUID>();
			NBTTagList tagList = properties.getTagList("allies", NBT.TAG_STRING);
			for(int i=0; i<tagList.tagCount(); i++){
				allies.add(UUID.fromString(tagList.getStringTagAt(i)));
			}
			
			this.allyNames = new HashSet<String>();
			NBTTagList tagList2 = properties.getTagList("allyNames", NBT.TAG_STRING);
			for(int i=0; i<tagList2.tagCount(); i++){
				allyNames.add(tagList2.getStringTagAt(i));
			}
			
			this.soulboundCreatures = new HashSet<UUID>();
			NBTTagList tagList3 = properties.getTagList("soulboundCreatures", NBT.TAG_STRING);
			for(int i=0; i<tagList3.tagCount(); i++){
				soulboundCreatures.add(UUID.fromString(tagList3.getStringTagAt(i)));
			}
			
			this.spellsDiscovered = new HashSet<Spell>();
			for(int id : properties.getIntArray("discoveredSpells")){
				spellsDiscovered.add(Spell.get(id));
			}
		}
	}

	@Override
	public void init(Entity entity, World world) {
		// For reference, apparently there is a strange error that causes 'this' to be null here.
	}

}
