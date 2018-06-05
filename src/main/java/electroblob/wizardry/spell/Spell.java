package electroblob.wizardry.spell;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Generic spell class which is the superclass to all spells in wizardry. When extending this class, you must do the
 * following:
 * <p>
 * - Have a constructor which passes all necessary constants into the super constructor. I define the constants here so
 * that the constructor for an individual spell has no parameters, but you may prefer to pass in the parameters when the
 * spell is registered, so all the mana costs etc. are in one place like a sort of sandbox.
 * <p>
 * - Implement the {@link Spell#cast(World, EntityPlayer, EnumHand, int, SpellModifiers)} method, in which you should
 * execute the code that makes the spell work, and return true or false depending on whether the spell succeeded and
 * therefore whether mana should be used up.
 * <p>
 * - Register the spell using {@link RegistryEvent.Register}, with {@link Spell} as the type parameter. Each spell
 * should have a single instance, like blocks and items. <i>As of Wizardry 2.1, spells use the Forge registry system.
 * Related methods such as {@link Spell#id()} and {@link Spell#get(int)} have been re-routed to use this system, leaving
 * minimal external changes. Note also that the constructor automatically sets the registry name for you, though you may
 * change it afterwards if necessary.</i>
 * <p>
 * Also note that you can override some other methods from this class. For example, to add a specific kind of formatting
 * to a spell name or description, you can override {@link Spell#getDisplayName()},
 * {@link Spell#getDisplayNameWithFormatting()} or {@link Spell#getDescription()} and append the formatting code (though
 * you will have to call super() to get the name itself, since the unlocalised name is private). See
 * {@link SummonShadowWraith#getDescription()} for an example.
 * <hr>
 * This class is also home to some useful static methods for interacting with the spell registry:
 * <p>
 * {@link Spell#get(int)} gets a spell instance from its integer id, which corresponds to the metadata of its spell
 * book.<br>
 * {@link Spell#get(String)} gets a spell instance from its unlocalised name.<br>
 * {@link Spell#getSpells(Predicate)} returns a list of spell instances that match the given {@link Predicate}.<br>
 * {@link Spell#getTotalSpellCount()} returns the total number of registered spells.
 * <hr>
 * Spell implements the {@link Comparable} interface, and as such, any collection of spells can be sorted. Spells are
 * sorted by increasing tier (see {@link Tier}), from novice to master. Within each tier, spells are sorted by element,
 * the order of which is as defined in {@link Element} (i.e. magic, fire, ice, lightning, necromancy, earth, sorcery,
 * healing).
 * <hr>
 * 
 * @since Wizardry 1.0
 * @see electroblob.wizardry.item.ItemSpellBook ItemSpellBook
 * @see electroblob.wizardry.item.ItemScroll ItemScroll
 * @see Spells
 */
public abstract class Spell extends IForgeRegistryEntry.Impl<Spell> implements Comparable<Spell> {

	/** Forge registry-based replacement for the internal spells list. */
	public static IForgeRegistry<Spell> registry;

	/** The tier this spell belongs to. */
	public final Tier tier;
	/** Mana cost of the spell. If it is a continuous spell the cost is per second. */
	public final int cost;
	/** The element this spell belongs to. */
	public final Element element;
	/** The unlocalised name of the spell. */
	private final String unlocalisedName;
	/** The type of spell this is classified as. */
	public final SpellType type;
	/** Cooldown for the spell in ticks */
	public final int cooldown;
	/** The action the player does when this spell is cast. */
	public final EnumAction action;
	/** Whether or not the spell is continuous (keeps going as long as the mouse button is held) */
	public final boolean isContinuous;

	/** ResourceLocation of the spell icon. */
	private final ResourceLocation icon;
	/** Mod ID of the mod that added this spell; defaults to {@link Wizardry#MODID} if not specified. */
	private final String modID;

	/**
	 * False if the spell has been disabled in the config file, true otherwise. This is now encapsulated to stop it
	 * being fiddled with.
	 */
	private boolean isEnabled = true;

	/**
	 * This constructor should be called from any subclasses, either feeding in the constants directly or through their
	 * own constructor from wherever the spell is registered. This is the constructor for wizardry's own spells; spells
	 * added by other mods should use
	 * {@link Spell#Spell(Tier, int, Element, String, SpellType, int, EnumAction, boolean, String)}.
	 * 
	 * @param tier The tier this spell belongs to.
	 * @param cost The amount of mana used to cast the spell. If this is a continuous spell, it represents mana cost per
	 *        second and should be a multiple of 5.
	 * @param element The element this spell belongs to.
	 * @param name The <i>registry name</i> of the spell. This will also be the name of the icon file. The spell's
	 *        unlocalised name will be a resource location with the format [modid]:[name].
	 * @param cooldown The cooldown time for this spell in ticks.
	 * @param action The vanilla usage action to be displayed when casting this spell.
	 * @param isContinuous Whether this spell is continuous, meaning you cast it for a length of time by holding the
	 *        right mouse button.
	 */
	public Spell(Tier tier, int cost, Element element, String name, SpellType type, int cooldown, EnumAction action,
			boolean isContinuous){
		this(tier, cost, element, name, type, cooldown, action, isContinuous, Wizardry.MODID);
	}

	/**
	 * This constructor should be called from any subclasses, either feeding in the constants directly or through their
	 * own constructor from wherever the spell is registered.
	 * 
	 * @param tier The tier this spell belongs to.
	 * @param cost The amount of mana used to cast the spell. If this is a continuous spell, it represents mana cost per
	 *        second and should be a multiple of 5.
	 * @param element The element this spell belongs to.
	 * @param name The <i>registry name</i> of the spell, excluding the mod id. This will also be the name of the icon
	 *        file. The spell's unlocalised name will be a resource location with the format [modid]:[name].
	 * @param cooldown The cooldown time for this spell in ticks.
	 * @param action The vanilla usage action to be displayed when casting this spell (see {@link}EnumAction)
	 * @param isContinuous Whether this spell is continuous, meaning you cast it for a length of time by holding the
	 *        right mouse button.
	 * @param modID The mod id of the mod that added this spell. This allows wizardry to use the correct file path for
	 *        the spell icon, and also more generally to distinguish between original and addon spells.
	 */
	public Spell(Tier tier, int cost, Element element, String name, SpellType type, int cooldown, EnumAction action,
			boolean isContinuous, String modID){
		this.tier = tier;
		this.cost = cost;
		this.element = element;
		this.type = type;
		this.cooldown = cooldown;
		this.action = action;
		this.isContinuous = isContinuous;
		this.modID = modID;
		this.setRegistryName(modID, name);
		this.unlocalisedName = this.getRegistryName().toString();
		this.icon = new ResourceLocation(this.modID, "textures/spells/" + name + ".png");
	}

	/**
	 * Casts the spell. Each subclass must override this method and within it execute the code to make the spell work.
	 * Returns a boolean so that the main onItemRightClick or onUsingItemTick method can check if the spell was actually
	 * cast or whether a spell specific condition caused it not to be (for example, heal won't work if the player is on
	 * full health), preventing unfair drain of mana.
	 * <p>
	 * Each spell must return true when it works or the spell will not use up mana. Note that (!world.isRemote) does not
	 * count as a condition; return true should be outside it - in other words, return a value on both the client and
	 * the server.
	 * <p>
	 * It's worth noting that on the client side, this method only gets called if the server side cast() method
	 * succeeded, so you can put any particle spawning code outside of any success conditions if there are discrepancies
	 * between client and server.
	 * 
	 * @param world A reference to the world object. Again this is for convenience, you can also use caster.world.
	 * @param caster The EntityPlayer that cast the spell.
	 * @param hand The hand that is holding the item used to cast the spell. If no item was used, this will be the main
	 *        hand.
	 * @param ticksInUse The number of ticks the spell has already been cast for. For all non-continuous spells, this is
	 *        0 and is not used. For continuous spells, it is passed in as the maximum use duration of the item minus
	 *        the count parameter in onUsingItemTick and therefore it increases by 1 each tick.
	 * @param modifiers A {@link SpellModifiers} object containing the modifiers that have been applied to the spell.
	 *        See the javadoc for that class for more information. If no modifiers are required, pass in
	 *        {@code new SpellModifiers()}.
	 * @return True if the spell succeeded and mana should be used up, false if not.
	 */
	public abstract boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse,
			SpellModifiers modifiers);

	/**
	 * Casts the spell, but with an EntityLiving as the caster. Each subclass can optionally override this method and
	 * within it execute the code to make the spell work. Returns a boolean to allow whatever calls this method to check
	 * if the spell was actually cast or whether a spell specific condition caused it not to be (for example, heal won't
	 * work if the caster is on full health).
	 * <p>
	 * This method is intended for use by NPCs (see {@link EntityWizard}) so that they can cast spells. Override it if
	 * you want a spell to be cast by wizards. Note that you must also override {@link Spell#canBeCastByNPCs()} to
	 * return true to allow wizards to select the spell. For some spells, this method may well be exactly the same as
	 * the regular cast method; for others it won't be - for example, projectile-based spells are normally done using
	 * the player's look vector, but NPCs need to use a target-based method instead.
	 * <p>
	 * Each spell must return true when it works. Note that (!world.isRemote) does not count as a condition; return true
	 * should be outside it - in other words, return a value on both the client and the server.
	 * <p>
	 * It's worth noting that on the client side, this method only gets called if the server side cast() method
	 * succeeded, so you can put any particle spawning code outside of any success conditions if there are discrepancies
	 * between client and server.
	 * 
	 * @param world A reference to the world object. This is for convenience, you can also use caster.world.
	 * @param caster The EntityLiving that cast the spell.
	 * @param hand The hand that is holding the item used to cast the spell. This will almost certainly be the main
	 *        hand.
	 * @param ticksInUse The number of ticks the spell has already been cast for. For all non-continuous spells, this is
	 *        0 and is not used.
	 * @param target The EntityLivingBase that is targeted by the spell. May be null in some cases.
	 * @param modifiers A {@link SpellModifiers} object containing the modifiers that have been applied to the spell.
	 *        See the javadoc for that class for more information. If no modifiers are required, pass in
	 *        {@code new SpellModifiers()}.
	 * @return True if the spell succeeded, false if not. Returns false by default.
	 */
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){
		return false;
	}

	/**
	 * Whether NPCs such as wizards can cast this spell. If you have overridden
	 * {@link Spell#cast(World, EntityLiving, EnumHand, int, EntityLivingBase, SpellModifiers)}, you should override
	 * this to return true.
	 */
	public boolean canBeCastByNPCs(){
		return false;
	}

	/**
	 * Whether this spell requires a packet to be sent when it is cast. Returns true by default, but can be overridden
	 * to return false <b>if</b> the spell's cast() method does not use any code that must be executed client-side (i.e.
	 * particle spawning). Does nothing for continuous spells, because they never need to send packets.
	 * <p>
	 * <i>If in doubt, leave this method as is; it is purely an optimisation.</i>
	 * 
	 * @return <b>false</b> if the spell code should only be run on the server and the client of the player casting
	 *         it<br>
	 *         <b>true</b> if the spell code should be run on the server and all clients in the dimension
	 */
	// Edit: Turns out that swingItem() actually sends packets to all nearby clients, but not the client doing the
	// swinging.
	// Also, now I think about it, this method isn't going to make the slightest bit of difference to the item usage
	// actions since setItemInUse() is called in ItemWand, not the spell class - so the only thing that matters here is
	// the particles.
	public boolean doesSpellRequirePacket(){
		return true;
	}

	/**
	 * Returns this spell's id number, which now corresponds to its position in the spell registry. Returns -1 if the
	 * spell has not been registered.
	 */
	// This is final so nothing can override it, because that would cause all kinds of problems!
	public final int id(){
		return ((ForgeRegistry<Spell>)registry).getID(this);
	}

	/**
	 * Returns the mod ID for this spell, which should be the ID of the mod that added it. The mod ID is used to tell
	 * wizardry which filepath to use for the spell's icon. As of Wizardry 1.2, the field itself is private, but this
	 * getter is provided for external use (though it is never called in the main Wizardry mod).
	 */
	public final String getModID(){
		return modID;
	}

	/** Returns the ResourceLocation for this spell's icon. */
	public final ResourceLocation getIcon(){
		return icon;
	}

	/**
	 * Returns the unlocalised name of the spell, without any prefixes or suffixes, e.g. "flame_ray". <b>This should
	 * only be used for translation purposes.</b>
	 */
	public final String getUnlocalisedName(){
		return unlocalisedName;
	}

	/* The general idea with translation is to use net.minecraft.client.resources.I18n directly on the client side (and
	 * just prepend formatting codes where necessary), and to use TextComponentTranslation on the server (setting the
	 * style as necessary). TextComponentTranslation effectively stores what needs to be translated, without actually
	 * translating it. If, for whatever reason, you need to supply an ITextComponent but don't want it translated
	 * (perhaps a name?), you can use TextComponentString, which will simply keep the raw string it is given. */

	/**
	 * Returns the translated display name of the spell, without formatting (i.e. not coloured). <b>Client-side
	 * only!</b> On the server side, use {@link TextComponentTranslation} (see {@link Spell#getNameForTranslation()}).
	 */
	@SideOnly(Side.CLIENT)
	public String getDisplayName(){
		return I18n.format("spell." + unlocalisedName);
	}

	/**
	 * Returns a {@code TextComponentTranslation} which will be translated to the display name of the spell, without
	 * formatting (i.e. not coloured).
	 */
	public TextComponentTranslation getNameForTranslation(){
		return new TextComponentTranslation("spell." + unlocalisedName);
	}

	/**
	 * Returns the translated display name of the spell, with formatting (i.e. coloured). <b>Client-side only!</b> On
	 * the server side, use {@link TextComponentTranslation} (see {@link Spell#getNameForTranslationFormatted()}).
	 */
	@SideOnly(Side.CLIENT)
	public String getDisplayNameWithFormatting(){
		return this.element.getFormattingCode()	+ I18n.format("spell." + unlocalisedName);
	}

	/**
	 * Returns a {@code TextComponentTranslation} which will be translated to the display name of the spell, with
	 * formatting (i.e. coloured).
	 */
	public ITextComponent getNameForTranslationFormatted(){
		return new TextComponentTranslation("spell." + unlocalisedName).setStyle(this.element.getColour());
	}

	/**
	 * Returns the translated description of the spell, without formatting. <b>Client-side only!</b> You should not need
	 * to use this on the server side.
	 */
	@SideOnly(Side.CLIENT)
	public String getDescription(){
		return I18n.format("spell." + unlocalisedName + ".desc");
	}

	/** Returns whether the spell is enabled in the config. */
	public final boolean isEnabled(){
		return isEnabled;
	}

	/** Sets whether the spell is enabled or not. */
	public final void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
	}

	// Spells are sorted according to tier and element. Where several spells have the same tier and element,
	// they will remain in the order they were registered.
	@Override
	public final int compareTo(Spell spell){

		if(this.tier.ordinal() > spell.tier.ordinal()){
			return 1;
		}else if(this.tier.ordinal() < spell.tier.ordinal()){
			return -1;
		}else{
			if(this.element.ordinal() > spell.element.ordinal()){
				return 1;
			}else if(this.element.ordinal() < spell.element.ordinal()){
				return -1;
			}else{
				return 0;
			}
		}
	}

	// ================================================ Static methods ==================================================

	/**
	 * Returns the total number of registered spells, excluding the 'None' spell. Returns the same number that would be
	 * returned by Spell.getSpells(Spell.allSpells).size(), but this method is more efficient.
	 */
	public static int getTotalSpellCount(){
		return registry.getValuesCollection().size() - 1;
	}

	/**
	 * Gets a spell instance from its integer id, which now corresponds to its id in the spell registry. If the given id
	 * has no spell (i.e. is less than 0 or greater than the total number of spells - 1) then it will return the
	 * {@link None} spell.
	 * <p>
	 * <i>If you are calling this from inside a loop in which you are iterating through the spells, there is probably a
	 * better way; see </i>{@link Spell#getSpells(Predicate)}.
	 */
	public static Spell get(int id){
		if(id < 0 || id >= registry.getValuesCollection().size()){
			return Spells.none;
		}
		Spell spell = ((ForgeRegistry<Spell>)registry).getValue(id);
		return spell == null ? Spells.none : spell;
	}

	/**
	 * Returns the spell with the given registry name, or null if no such spell exists.
	 * 
	 * @param name The registry name of the spell, in the form [mod id]:[spell name]. If no mod id is specified, it
	 *        defaults to {@link Wizardry#MODID}.
	 */
	public static Spell get(String name){
		ResourceLocation key = new ResourceLocation(name);
		if(key.getResourceDomain().equals("minecraft")) key = new ResourceLocation(Wizardry.MODID, name);
		return ((ForgeRegistry<Spell>)registry).getValue(key);
	}

	/** Returns a list of all registered spells' registry names, excluding the 'none' spell. Used in commands. */
	public static Collection<ResourceLocation> getSpellNames(){
		// Maybe it would be better to store all of this statically?
		Set<ResourceLocation> keys = new HashSet<ResourceLocation>(registry.getKeys());
		keys.remove(registry.getKey(Spells.none));
		return keys;
	}

	/**
	 * Returns a list containing all spells matching the given {@link Predicate}. The returned list is separate from the
	 * internal spells list; any changes you make to the returned list will have no effect on wizardry since the
	 * returned list is local to this method. Never includes the {@link None} spell. For convenience, there are some
	 * predefined predicates in the Spell class (some of these really aren't shortcuts any more):
	 * <p>
	 * {@link Spell#allSpells} will allow all spells to be returned<br>
	 * {@link Spell#enabledSpells} will filter out any spells that are disabled in the config<br>
	 * {@link Spell#npcSpells} will only allow enabled spells that can be cast by NPCs (see
	 * {@link Spell#canBeCastByNPCs()})<br>
	 * {@link Spell#nonContinuousSpells} will filter out continuous spells but not disabled spells<br>
	 * {@link Spell.TierElementFilter} will only allow enabled spells of the specified tier and element
	 * 
	 * @param filter A <code>Predicate&ltSpell&gt</code> that the returned spells must satisfy.
	 * 
	 * @return A <b>local, modifiable</b> list of spells matching the given predicate. <i>Note that this list may be
	 *         empty.</i>
	 */
	public static List<Spell> getSpells(Predicate<Spell> filter){
		return registry.getValuesCollection().stream().filter(filter.and(p -> p != Spells.none)).collect(Collectors.toList());
	}

	/** Predicate which allows all spells. */
	public static Predicate<Spell> allSpells = s -> true;

	/** Predicate which allows all enabled spells. */
	public static Predicate<Spell> enabledSpells = Spell::isEnabled;

	/** Predicate which allows all non-continuous spells, even those that have been disabled. */
	public static Predicate<Spell> nonContinuousSpells = s -> !s.isContinuous;

	/** Predicate which allows all enabled spells for which {@link Spell#canBeCastByNPCs()} returns true. */
	public static Predicate<Spell> npcSpells = s -> s.isEnabled() && s.canBeCastByNPCs();

	/**
	 * Predicate which allows all enabled spells of the given tier and element (create an instance of this class each
	 * time you want to use it). This is somewhat useless now that Wizardry uses Java 8, but it is more readable than a
	 * lambda expression and you don't need to remember to check that the spell is enabled every time.
	 */
	public static class TierElementFilter implements Predicate<Spell> {

		private Tier tier;
		private Element element;

		/**
		 * Creates a new TierElementFilter that checks for the given tier and element. Does not allow spells that have
		 * been disabled in the config.
		 * 
		 * @param tier The EnumTier to check for. Pass in null to allow all tiers.
		 * @param element The EnumElement to check for. Pass in null to allow all elements.
		 */
		public TierElementFilter(Tier tier, Element element){
			this.tier = tier;
			this.element = element;
		}

		@Override
		public boolean test(Spell spell){
			return spell.isEnabled() && (this.tier == null || spell.tier == this.tier)
					&& (this.element == null || spell.element == this.element);
		}
	};
}
