package electroblob.wizardry.spell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.entity.living.EntityWizard;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/** Generic spell class which is the superclass to all spells in wizardry. When extending this class, you must do
 * the following:
 * <p>
 * - Have a constructor which passes all necessary constants into the super constructor. I define the constants here
 * so that the constructor for an individual spell has no parameters, but you may prefer to pass in the parameters when
 * the spell is registered, so all the mana costs etc. are in one place like a sort of sandbox.
 * <p> 
 * - Implement the {@link Spell#cast(World, EntityPlayer, int, float, float, float, float)} method, in which you should
 * execute the code that makes the spell work, and return true or false depending on whether the spell succeeded and
 * therefore whether mana should be used up.
 * <p>
 * - Register the spell using {@link Spell#register(Spell)} during the init() method of your main mod class. Each spell
 * should have a single instance, like blocks and items. Unlike with blocks and items however, it is usually not
 * necessary to keep a reference to this instance unless something specific requires it (for example, the
 * {@link Transportation} spell instance is stored in {@link WizardryRegistry} so that its name can be used in chat 
 * messages). 
 * <p>
 * Also note that you can override some other methods from this class. For example, to add a
 * specific kind of formatting to a spell name or description, you can override {@link Spell#getDisplayName()},
 * {@link Spell#getDisplayNameWithFormatting()} or {@link Spell#getDescription()} and append the formatting code (though
 * you will have to call super() to get the name itself, since the unlocalised name is private). See
 * {@link SummonShadowWraith#getDescription()} for an example.
 * <hr>
 * This class is also home to some useful static methods for interacting with the spell registry:<p>
 * {@link Spell#get(int)} gets a spell instance from its integer id, which corresponds to the metadata of its spell book.<br>
 * {@link Spell#get(String)} gets a spell instance from its unlocalised name.<br>
 * {@link Spell#getSpells(Filter)} returns a list of spell instances the match the given {@link Filter}.
 * {@link Spell#getTotalSpellCount()} returns the total number of registered spells.
 * <hr>
 * Spell implements the {@link Comparable} interface, and as such, any collection of spells can be sorted. Spells are
 * sorted by increasing tier (see {@link EnumTier}), from novice to master. Within each tier, spells are sorted by element,
 * the order of which is as defined in {@link EnumElement} (i.e. magic, fire, ice, lightning, necromancy, earth, sorcery, healing).
 * <hr>
 * @since Wizardry 1.0
 * @see electroblob.wizardry.item.ItemSpellBook ItemSpellBook
 * @see electroblob.wizardry.item.ItemScroll ItemScroll
 * @see WizardryRegistry
 */

public abstract class Spell implements Comparable<Spell> {
	
	/** The internal list of all registered spells. This is deliberately made private since it shouldn't be fiddled with. */
	private static ArrayList<Spell> spellsList = new ArrayList<Spell>(1);
	
	/** The internal list of all registered spells' unlocalised names, used in commands. This is deliberately made
	 * private since it shouldn't be fiddled with. */
	private static ArrayList<String> unlocalisedNames = new ArrayList<String>(1);
	
	/** The tier this spell belongs to. */
	public final EnumTier tier;
	/** Mana cost of the spell. If it is a continuous spell the cost is per second. */
	public final int cost;
	/** The element this spell belongs to. */
	public final EnumElement element;
	/** The unlocalised name of the spell. */
	private final String unlocalisedName;
	/** The type of spell this is classified as. */
	public final EnumSpellType type;
	/** Cooldown for the spell in ticks */
	public final int cooldown;
	/** The action the player does when this spell is cast. */
	public final EnumAction action;
	/** Whether or not the spell is continuous (keeps going as long as the mouse button is held) */
	public final boolean isContinuous;
	
	/** ResourceLocation of the spell icon. This is set up within the constructor; don't change it otherwise. */
	public ResourceLocation icon;
	/** Mod ID of the mod that added this spell; defaults to Wizardry.MODID if not specified. This is set up within the
	 * constructor; don't change it otherwise. */
	public String modID;
	
	/** False if the spell has been disabled in the config file, true otherwise. This is now encapsulated to stop it
	 * being fiddled with. */
	private boolean isEnabled = true;
	
	/**
	 * This constructor should be called from any subclasses, either feeding in the constants directly or through
	 * their own constructor from wherever the spell is registered. This is the constructor for original spells;
	 * spells added by other mods should use {@link Spell#Spell(EnumTier, int, EnumElement, String, EnumSpellType, int, EnumAction, boolean, String)}.
	 * @param tier The tier this spell belongs to.
	 * @param cost The amount of mana used to cast the spell. If this is a continuous spell, it represents mana cost
	 * per second and should be a multiple of 5.
	 * @param element The element this spell belongs to.
	 * @param name The unlocalised name of the spell This will also be the name of the icon file.
	 * @param cooldown The cooldown time for this spell in ticks.
	 * @param action The vanilla usage action to be displayed when casting this spell.
	 * @param isContinuous Whether this spell is continuous, meaning you cast it for a length of time by holding the
	 * right mouse button.
	 */
	public Spell(EnumTier tier, int cost, EnumElement element, String name, EnumSpellType type, int cooldown, EnumAction action, boolean isContinuous){
		this.tier = tier;
		this.cost = cost;
		this.element = element;
		this.unlocalisedName = name;
		this.type = type;
		this.cooldown = cooldown;
		this.action = action;
		this.isContinuous = isContinuous;
		this.modID = Wizardry.MODID;
		this.icon = new ResourceLocation(this.modID, "textures/spells/" + this.unlocalisedName + ".png");
	}
	
	/**
	 * This constructor should be called from any subclasses, either feeding in the constants directly or through
	 * their own constructor from wherever the spell is registered.
	 * @param tier The tier this spell belongs to.
	 * @param cost The amount of mana used to cast the spell. If this is a continuous spell, it represents mana cost
	 * per second and should be a multiple of 5.
	 * @param element The element this spell belongs to.
	 * @param name The unlocalised name of the spell. This will also be the name of the icon file.
	 * @param cooldown The cooldown time for this spell in ticks.
	 * @param action The vanilla usage action to be displayed when casting this spell (see {@link}EnumAction)
	 * @param isContinuous Whether this spell is continuous, meaning you cast it for a length of time by holding the
	 * right mouse button.
	 * @param modID The mod id of the mod that added this spell. This allows wizardry to use the correct file
	 * path for the spell icon, and also more generally to distinguish between original and addon spells.
	 */
	public Spell(EnumTier tier, int cost, EnumElement element, String name, EnumSpellType type, int cooldown, EnumAction action, boolean isContinuous, String modID){
		this(tier, cost, element, name, type, cooldown, action, isContinuous);
		this.modID = modID;
		this.icon = new ResourceLocation(this.modID, "textures/spells/" + this.unlocalisedName + ".png");
	}
	
	/**
	 * Casts the spell. Each subclass must override this method and within it execute the code to make the spell work.
	 * Returns a boolean so that the main onItemRightClick or onUsingItemTick method can check if the spell
	 * was actually cast or whether a spell specific condition caused it not to be (for example, heal won't work if the player is on
	 * full health), preventing unfair drain of mana.
	 * <p>
	 * Each spell must return true when it works or the spell will not use up mana. Note that (!world.isRemote) does not count
	 * as a condition; return true should be outside it - in other words, return a value on both the client and the server.
	 * <p>
	 * It's worth noting that on the client side, this method only gets called if the server side
	 * cast() method succeeded, so you can put any particle spawning code outside of any success conditions if there
	 * are discrepancies between client and server.
	 * @param world A reference to the world object. Again this is for convenience, you can also use caster.worldObj.
	 * @param caster The EntityPlayer that cast the spell.
	 * @param ticksInUse The number of ticks the spell has already been cast for. For all non-continuous spells, this is 0 and
	 * is not used. For continuous spells, it is passed in as the maximum use duration of the item minus the count parameter in
	 * onUsingItemTick and therefore it increases by 1 each tick.
	 * @param damageMultiplier The damage multiplier to apply; pass in 1 if no multiplier is needed.
	 * @param rangeMultiplier The range multiplier to apply; pass in 1 if no multiplier is needed.
	 * @param durationMultiplier The duration multiplier to apply; pass in 1 if no multiplier is needed.
	 * @param blastMultiplier The blast radius multiplier to apply; pass in 1 if no multiplier is needed.
	 * 
	 * @return True if the spell succeeded and mana should be used up, false if not.
	 */
	public abstract boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier);
	
	/** Casts the spell, but with an EntityLiving as the caster. Each subclass can optionally override this method and
	 * within it execute the code to make the spell work. Returns a boolean to allow whatever calls this method to check if the spell
	 * was actually cast or whether a spell specific condition caused it not to be (for example, heal won't work if the caster is on
	 * full health).
	 * <p>
	 * This method is intended for use by NPCs (see {@link EntityWizard}) so that they can cast spells. Override it if you want
	 * a spell to be cast by wizards. Note that you must also override {@link Spell#canBeCastByNPCs()} to return true to allow wizards
	 * to select the spell. For some spells, this method may well be exactly the same as the regular cast method; for
	 * others it won't be - for example, projectile-based spells are normally done using the player's look vector, but
	 * NPCs need to use a target-based method instead.
	 * <p>
	 * Each spell must return true when it works. Note that (!world.isRemote) does not count as a condition; return true should be outside
	 * it - in other words, return a value on both the client and the server.
	 * <p>
	 * It's worth noting that on the client side, this method only gets called if the server side
	 * cast() method succeeded, so you can put any particle spawning code outside of any success conditions if there
	 * are discrepancies between client and server.
	 * 
	 * @param world A reference to the world object. This is for convenience, you can also use caster.worldObj.
	 * @param caster The EntityLiving that cast the spell.
	 * @param target The EntityLivingBase that is targeted by the spell. You must check if this parameter is null when overriding.
	 * @param damageMultiplier The damage multiplier to apply; pass in 1 if no multiplier is needed.
	 * @param rangeMultiplier The range multiplier to apply; pass in 1 if no multiplier is needed.
	 * @param durationMultiplier The duration multiplier to apply; pass in 1 if no multiplier is needed.
	 * @param blastMultiplier The blast radius multiplier to apply; pass in 1 if no multiplier is needed.
	 * @return True if the spell succeeded, false if not. Returns false by default.
	 */
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		return false;
	}
	
	/** Whether NPCs such as wizards can cast this spell. If you have overridden {@link Spell#cast(World, EntityLiving, EntityLivingBase, float, float, float, float)},
	 * you should override this to return true. */
	public boolean canBeCastByNPCs(){
		return false;
	}
	
	/** Whether this spell requires a packet to be sent when it is cast. Returns true by default, but can be overridden
	 * to return false <b>if</b> the spell's cast() method does not use any code that must be executed client-side (i.e.
	 * particle spawning). Does nothing for continuous spells, because they never need to send packets.
	 * <p>
	 * <i>If in doubt, leave this method as is; it is purely an optimisation.</i>
	 * @return <b>false</b> if the spell code should only be run on the server and the client of the player casting it<br>
	 * <b>true</b> if the spell code should be run on the server and all clients in the dimension */
	// Edit: Turns out that swingItem() actually sends packets to all nearby clients, but not the client doing the swinging.
	// Also, now I think about it, this method isn't going to make the slightest bit of difference to the item usage
	// actions since setItemInUse() is called in ItemWand, not the spell class - so the only thing that matters here is
	// the particles.
	public boolean doesSpellRequirePacket(){
		return true;
	}
	
	/** Returns this spell's id number, which now corresponds to its position in the spells ArrayList. 
	 * Returns -1 if the spell has not been registered. */
	// This is final so nothing can override it, because that would cause all kinds of problems!
	public final int id(){
		return spellsList.indexOf(this);
	}
	
	/** Returns the unlocalised name of the spell. */
	public String getUnlocalisedName(){
		return unlocalisedName;
	}

	/** Returns the translated display name of the spell, without formatting (i.e. not coloured). */
	public String getDisplayName(){
		return StatCollector.translateToLocal("spell." + unlocalisedName);
	}

	/** Returns the translated display name of the spell, with formatting (i.e. coloured). */
	public String getDisplayNameWithFormatting(){
		return this.element.colour + StatCollector.translateToLocal("spell." + unlocalisedName);
	}

	/** Returns the translated description of the spell, without formatting. */
	public String getDescription(){
		return StatCollector.translateToLocal("spell." + unlocalisedName + ".desc");
	}

	/** Returns whether the spell is enabled in the config. */
	public final boolean isEnabled() {
		return isEnabled;
	}

	/** Sets whether the spell is enabled or not. */
	public final void setEnabled(boolean isEnabled) {
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
	 * Registers a spell with the wizardry mod. Each spell should have a single instance, like blocks and items (this
	 * instance does not usually need to be stored, but you will have to store it if you want to reference a specific
	 * spell directly, for example transportation requires an accessible instance for printing of chat messages). 
	 * Spells must be registered in the <b>preInit</b> method in your main mod class for them to be added to the config
	 * file automatically.
	 * 
	 * @param spell The spell instance to register 
	 * @throws NullPointerException if the passed in spell is null (why would you ever do that?).
	 */
	public static void register(Spell spell){
		if(spell == null){
			// If the null value was added to the list, it would cause an NPE later when it tries to sort the list.
			// If the null value was ignored and not added, there would be no record of what went wrong.
			// Therefore the best way is to throw an NPE here so whoever caused it knows why it went wrong.
			throw new NullPointerException("Tried to register a spell, but the passed in spell was null.");
		}else{
			spellsList.add(spell);
			if(!(spell instanceof None)) unlocalisedNames.add(spell.unlocalisedName);
		}
	}
	
	/** Called from the postInit method in the main wizardry class to sort all the spells. 
	 * <p> Deprecated in favour of sorting the books and scrolls themselves in the creative tab. */
	@Deprecated
	public static void sortSpells(){
		Collections.sort(spellsList);
	}
	
	/** Returns the total number of registered spells, excluding the 'None' spell. Returns the same number that would
	 * be returned by Spell.getSpells(Spell.allSpells).size(), but this method is more efficient. */
	public static int getTotalSpellCount(){
		return spellsList.size() - 1;
	}
	
	/** Gets a spell instance from its integer id, which now corresponds to its position in the spells ArrayList. 
	 * If the given id has no spell (i.e. is less than 0 or greater than the total number of spells - 1)
	 * then it will return the {@link None} spell.
	 * <p>
	 * If you are calling this from inside a loop in which you are iterating through the spells, there is
	 * probably a better way; see {@link Spell#getSpells(Filter)}. */
	public static Spell get(int id){
		return id < 0 || id >= spellsList.size() ? WizardryRegistry.none : spellsList.get(id);
	}
	
	/** Returns the spell with the given unlocalised name, or null if no such spell exists. */
	public static Spell get(String unlocalisedName){
		for(Spell spell : Spell.getSpells(allSpells)){
			if(spell.unlocalisedName.equals(unlocalisedName)){
				return spell;
			}
		}
		return null;
	}
	
	/** Returns a list of all registered spells' unlocalised names, excluding the 'none' spell. Used in commands. */
	public static String[] getUnlocalisedNames(){
		return unlocalisedNames.toArray(new String[unlocalisedNames.size()]);
	}
	
	/* The following method is an example of where I would have liked to use the java 8 predicate and removeIf
	 * functionality. However, since people don't update their java, I am trying to stick to java 6, annoying as that
	 * is... so this is the most flexible I can make it within the constraints of java 6. The aim is to remove the need
	 * to EVER iterate through the spells by id number and use spell.get(id).[condition], because this is cumbersome and
	 * inefficient and often leads to timeouts. It's far better to restrict the RNG to only the spells you want it
	 * to select from. Even when you're not using RNG, the for each loop is nicer and more efficient than constantly
	 * calling Spell.get(i). */
	
	/**
	 * Returns a list containing all spells matching the given filter. The returned list is separate from the
	 * internal spells list; any changes you make to the returned list will have no effect on wizardry since the
	 * returned list is local to this method. Never includes the 'none' spell. For convenience, there are some
	 * predefined filters in the Spell class:
	 * <p>
	 * {@link Spell#allSpells} will allow all spells to be returned<br>
	 * {@link Spell#enabledSpells} will filter out any spells that are disabled in the config<br>
	 * {@link Spell#npcSpells} will only allow enabled spells that can be cast by NPCs (see {@link Spell#canBeCastByNPCs()})<br>
	 * {@link Spell#nonContinuousSpells} will filter out continuous spells but not disabled spells<br>
	 * {@link Spell.TierElementFilter} will only allow enabled spells of the specified tier and element
	 * 
	 * @param filter see {@link Spell.Filter}
	 * 
	 * @return A list of spells matching the given filter. <i>Note that this list may be empty.
	 * */
	public static List<Spell> getSpells(Filter filter){
		
		ArrayList<Spell> spells = new ArrayList<Spell>(1);
		
		for(Spell spell : spellsList){
			if(filter.test(spell) && spell != WizardryRegistry.none) spells.add(spell);
		}
		
		return spells;
	}
	
	/** To allow Java 6 compatibility, this is defined here instead of using the jdk 8 predicate. Function is exactly
	 * the same, but specifically for spells. For convenience, there are some predefined filters in the {@link Spell}
	 * class:
	 * <p>
	 * {@link Spell#allSpells} will allow all spells to be returned<br>
	 * {@link Spell#enabledSpells} will filter out any spells that are disabled in the config<br>
	 * {@link Spell#npcSpells} will only allow enabled spells that can be cast by NPCs (see {@link Spell#canBeCastByNPCs()})<br>
	 * {@link Spell#nonContinuousSpells} will filter out continuous spells but not disabled spells<br>
	 * {@link Spell.TierElementFilter} will only allow enabled spells of the specified tier and element
	 * <p>
	 * Edit: Turns out the google stuff has a predicate class. Oh well. */
	public static interface Filter {
		public boolean test(Spell spell);
	}
	
	/** Filter which allows all spells. */
	public static Filter allSpells = new Filter(){
		@Override
		public boolean test(Spell spell) {
			return true;
		}
	};
	
	/** Filter which allows all enabled spells. */
	public static Filter enabledSpells = new Filter(){
		@Override
		public boolean test(Spell spell) {
			return spell.isEnabled();
		}
	};
	
	/** Filter which allows all non-continuous spells, even those that have been disabled. */
	public static Filter nonContinuousSpells = new Filter(){
		@Override
		public boolean test(Spell spell) {
			return !spell.isContinuous;
		}
	};
	
	/** Filter which allows all enabled spells for which {@link Spell#canBeCastByNPCs()} returns true. */
	public static Filter npcSpells = new Filter(){
		@Override
		public boolean test(Spell spell) {
			return spell.isEnabled() && spell.canBeCastByNPCs();
		}
	};
	
	/** Filter type which allows all enabled spells of the given tier and element (create an instance of this class
	 * each time you want to use it). */
	public static class TierElementFilter implements Filter {
		
		private EnumTier tier;
		private EnumElement element;
		
		/** 
		 * Creates a new TierElementFilter that checks for the given tier and element. Does not allow spells that have
		 * been disabled in the config.
		 * @param tier The EnumTier to check for. Pass in null to allow all tiers.
		 * @param element The EnumElement to check for. Pass in null to allow all elements.
		 */
		public TierElementFilter(EnumTier tier, EnumElement element){
			this.tier = tier;
			this.element = element;
		}
		
		@Override
		public boolean test(Spell spell) {
			return spell.isEnabled() && (this.tier == null || spell.tier == this.tier)
					&& (this.element == null || spell.element == this.element);
		}
	};
}
