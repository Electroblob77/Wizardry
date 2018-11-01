package electroblob.wizardry;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.enchantment.Imbuement;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketPlayerSync;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.spell.None;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Capability-based replacement for the old ExtendedPlayer class from 1.7.10. This has been reworked to leave minimum
 * external changes (for my own sanity, mainly!). Turns out the only major difference between an internal capability and
 * an IEEP is a couple of redundant classes and a different way of registering it.
 * <p>
 * Forge seems to have separate classes to hold the Capability<...> instance ('key') and methods for getting the
 * capability, but in my opinion there are already too many classes to deal with, so I'm not adding any more than are
 * necessary, meaning those constants and values are kept here instead.
 * 
 * @since Wizardry 2.1
 * @author Electroblob
 */
// On the plus side, having to rethink this class allowed me to clean it up a lot.
@Mod.EventBusSubscriber
public class WizardData implements INBTSerializable<NBTTagCompound> {

	/** Static instance of what I like to refer to as the capability key. Private because, well, it's internal! */
	// This annotation does some crazy Forge magic behind the scenes and assigns this field a value.
	@CapabilityInject(WizardData.class)
	private static final Capability<WizardData> WIZARD_DATA_CAPABILITY = null;

	/** The player this WizardData instance belongs to. */
	private final EntityPlayer player;

	// This one is still necessary, because I can't override the equip animation for items that aren't from Wizardry.
	private Map<Imbuement, Integer> imbuementDurations;

	public boolean hasSpiritWolf;
	public boolean hasSpiritHorse;

	/**
	 * Whether this player is currently casting a continuous spell via commands. Not saved over world reload and reset
	 * on player death.
	 */
	private Spell currentlyCasting;
	/**
	 * The time for which this player has been casting a continuous spell via commands. Increments by 1 each tick. Not
	 * saved over world reload and reset on player death.
	 */
	private int castingTick;
	/**
	 * SpellModifiers object for the current continuous spell cast via commands. Not saved over world reload and reset
	 * on player death.
	 */
	private SpellModifiers spellModifiers;
	/** Coordinates for the saved transportation stone circle location. Will be null if no location is saved. */
	private BlockPos stoneCircleLocation;
	/** Dimension id which the saved stone circle is in. */
	private int stoneCircleDimension;
	/** Time left until the player teleports under the effect of transportation */
	private int tpCountdown;

	/** Coordinates for the saved clairvoyance location. Will be null if no location is saved. */
	private BlockPos clairvoyanceLocation;
	/** Dimension id which the saved clairvoyance point is in. */
	private int clairvoyanceDimension;

	public EntityShield shield;

	public WeakReference<ISummonedCreature> selectedMinion;

	/**
	 * Set of this player's discovered spells. <b>Do not write to this list directly</b>, use
	 * {@link WizardData#discoverSpell(Spell)} instead.
	 */
	public Set<Spell> spellsDiscovered;

	private Set<UUID> allies;
	/**
	 * List of usernames of this player's allies. May not be accurate 100% of the time. This is here so that a player
	 * can view the usernames of their allies even when those allies are not online. <b> Do not use this for any other
	 * purpose than displaying the names!
	 */
	public Set<String> allyNames;

	private Set<UUID> soulboundCreatures;
	
	public WizardData(){
		this(null); // Nullary constructor for the registration method factory parameter
	}

	public WizardData(EntityPlayer player){
		this.player = player;
		this.imbuementDurations = new HashMap<Imbuement, Integer>();
		this.spellsDiscovered = new HashSet<Spell>();
		// All players can recognise magic missile. This is not done using discoverSpell because that seems to cause
		// a crash on load occasionally (probably something to do with achievements being initalised)
		this.spellsDiscovered.add(Spells.magic_missile);
		this.hasSpiritWolf = false;
		this.hasSpiritHorse = false;
		this.currentlyCasting = Spells.none;
		this.spellModifiers = new SpellModifiers();
		this.castingTick = 0;
		this.stoneCircleDimension = 0;
		this.clairvoyanceDimension = 0;
		this.setTpCountdown(0);
		this.allies = new HashSet<UUID>();
		this.allyNames = new HashSet<String>();
		this.soulboundCreatures = new HashSet<UUID>();
	}

	public boolean hasSpellBeenDiscovered(Spell spell){
		return spellsDiscovered.contains(spell) || spell instanceof None;
	}

	/**
	 * Adds the given spell to the list of discovered spells for this player. Automatically takes into account whether
	 * the spell has been discovered. Use this method rather than adding directly to the list because it handles
	 * achievements.
	 * 
	 * @param spell The spell to be discovered
	 * @return True if the spell had not already been discovered; false otherwise.
	 */
	public boolean discoverSpell(Spell spell){

		if(spellsDiscovered == null){
			spellsDiscovered = new HashSet<Spell>();
		}
		// The 'none' spell cannot be discovered
		if(spell instanceof None) return false;
		// Tries to add the spell to the list of discovered spells, and returns false if it was already present
		if(!spellsDiscovered.add(spell)) return false;
		// If the spell had not already been discovered, achievements can be triggered and the method returns true
		if(spellsDiscovered.containsAll(Spell.getSpells(Spell::isEnabled))){
			WizardryAdvancementTriggers.all_spells.triggerFor(this.player);
		}

		for(Element element : Element.values()){
			if(element != Element.MAGIC
					&& spellsDiscovered.containsAll(Spell.getSpells(new Spell.TierElementFilter(null, element)))){
				WizardryAdvancementTriggers.element_master.triggerFor(this.player);
			}
		}

		return true;
	}

	/** Sets the player's saved transportation stone location and dimension. */
	public void setStoneCircleLocation(BlockPos pos, int dimensionID){
		this.stoneCircleLocation = pos;
		this.stoneCircleDimension = dimensionID;
	}

	/** Returns the coordinates of the associated player's saved transportation stone circle. */
	public BlockPos getStoneCircleLocation(){
		return stoneCircleLocation;
	}

	/** Returns the dimension ID of the associated player's saved transportation stone circle. */
	public int getStoneCircleDimension(){
		return stoneCircleDimension;
	}

	public int getTpCountdown(){
		return tpCountdown;
	}

	public void setTpCountdown(int tpCountdown){
		this.tpCountdown = tpCountdown;
	}

	/** Sets the player's saved clairvoyance location. */
	public void setClairvoyancePoint(BlockPos pos, int dimensionID){
		this.clairvoyanceLocation = pos;
		this.clairvoyanceDimension = dimensionID;
	}

	/** Returns the coordinates for the saved clairvoyance location. Will be null if no location is saved. */
	public BlockPos getClairvoyanceLocation(){
		return clairvoyanceLocation;
	}

	/** Returns the dimension ID for the saved clairvoyance location. Will be null if no location is saved. */
	public int getClairvoyanceDimension(){
		return clairvoyanceDimension;
	}

	/**
	 * Overwrites the imbuement duration associated with the given imubement for this player, or creates it if there was
	 * none previously.
	 * 
	 * @throws IllegalArgumentException if the given {@link Enchantment} is not an {@link Imbuement}.
	 */
	public void setImbuementDuration(Enchantment enchantment, int duration){
		// It is best to throw an exception here, because otherwise the error would either go unnoticed (if
		// non-imbuements
		// were ignored) or cause a ClassCastException later (if non-imbuements were allowed to be added).
		if(enchantment instanceof Imbuement){
			this.imbuementDurations.put((Imbuement)enchantment, duration);
		}else{
			throw new IllegalArgumentException(
					"Attempted to set an imbuement duration for something that isn't an Imbuement! (This exception has been thrown now to prevent a ClassCastException from occurring later.)");
		}
	}

	/**
	 * Returns the imbuement duration associated with the given imbuement for this player, or 0 if it does not exist.
	 */
	@SuppressWarnings("unlikely-arg-type")
	public int getImbuementDuration(Enchantment enchantment){
		// Need to check that i is not null, otherwise it throws an NPE when Java auto-unboxes it.
		// What's nice here is that the map simply accepts objects as keys, so there's no need to cast or throw
		// exceptions.
		Integer i = this.imbuementDurations.get(enchantment);
		// If i is null, returns 0; otherwise returns i, auto-unboxed to an int.
		return i == null ? 0 : i;
	}

	/**
	 * Decrements the duration for each conjured item by 1, and removes from the map any that are 0 or less or that the
	 * player no longer has. Also deletes the item from the player's inventory if it runs out of time.
	 */
	private void updateImbuedItems(){

		Set<Imbuement> activeImbuements = new HashSet<Imbuement>();

		// For each item in the player's inventory
		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.isItemEnchanted()){

				NBTTagList enchantmentList = stack.getItem() == Items.ENCHANTED_BOOK ?
						ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();

				Iterator<NBTBase> iterator =enchantmentList.iterator();
				// For each of the item's enchantments
				while(iterator.hasNext()){
					NBTTagCompound enchantmentTag = (NBTTagCompound) iterator.next();
					Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentTag.getShort("id"));
					// Ignores the enchantment unless it is an imbuement
					if(enchantment instanceof Imbuement){
						int duration = this.getImbuementDuration(enchantment);
						// If the imbuement is still active:
						if(duration > 0){
							// Decrements the timer
							this.imbuementDurations.put((Imbuement)enchantment, duration - 1);
							// Adds this imbuement to the set of imbuements that need to be kept
							activeImbuements.add((Imbuement)enchantment);
							// Otherwise:
						}else{
							// Removes the enchantment from the item
							iterator.remove();
						}
					}
				}
			}
		}
		// Removes all imbuements from the map that are no longer active
		this.imbuementDurations.keySet().retainAll(activeImbuements);
	}

	/**
	 * Adds the given player to the list of allies belonging to the associated player, or removes the player if they are
	 * already in the list of allies. Returns true if the player was added, false if they were removed.
	 */
	public boolean toggleAlly(EntityPlayer player){
		if(this.isPlayerAlly(player)){
			this.allies.remove(player.getUniqueID());
			// The remove method uses .equals() rather than == so this will work fine.
			this.allyNames.remove(player.getName());
			return false;
		}else{
			this.allies.add(player.getUniqueID());
			this.allyNames.add(player.getName());
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

	/**
	 * Damages all creatures soulbound to this player by the given amount, and removes from the list any that no longer
	 * exist.
	 */
	public void damageAllSoulboundCreatures(float damage){

		for(Iterator<UUID> iterator = this.soulboundCreatures.iterator(); iterator.hasNext();){

			Entity entity = WizardryUtilities.getEntityByUUID(this.player.world, iterator.next());

			if(entity == null) iterator.remove();

			if(entity instanceof EntityLivingBase){
				// Retaliatory effect
				if(entity.attackEntityFrom(MagicDamage.causeDirectMagicDamage(this.player, DamageType.MAGIC, true),
						damage)){
					// Sound only plays if the damage succeeds
					player.playSound(SoundEvents.ENTITY_WITHER_HURT, 1.0F, player.world.rand.nextFloat() * 0.2F + 1.0F);
				}
			}
		}
	}

	/** Starts casting the given spell with the given modifiers. */
	public void startCastingContinuousSpell(Spell spell, SpellModifiers modifiers){

		this.currentlyCasting = spell;
		this.spellModifiers = modifiers;

		if(!this.player.world.isRemote){
			PacketCastContinuousSpell.Message message = new PacketCastContinuousSpell.Message(this.player.getEntityId(),
					spell.id(), this.spellModifiers);
			WizardryPacketHandler.net.sendToDimension(message, this.player.world.provider.getDimension());
		}
	}

	/** Stops casting the current spell. */
	public void stopCastingContinuousSpell(){

		this.currentlyCasting = Spells.none;
		this.castingTick = 0;
		this.spellModifiers.reset();

		if(!this.player.world.isRemote){
			PacketCastContinuousSpell.Message message = new PacketCastContinuousSpell.Message(this.player.getEntityId(),
					Spells.none.id(), this.spellModifiers);
			WizardryPacketHandler.net.sendToDimension(message, this.player.world.provider.getDimension());
		}
	}

	/** Casts the current continuous spell, fires relevant events and updates the castingTick field. */
	public void updateContinuousSpellCasting(){

		if(this.currentlyCasting != null && this.currentlyCasting.isContinuous){

			if(MinecraftForge.EVENT_BUS.post(
					new SpellCastEvent.Tick(player, currentlyCasting, spellModifiers, Source.COMMAND, castingTick))){
				this.stopCastingContinuousSpell();
				return;
			}

			if(this.currentlyCasting.cast(player.world, player, EnumHand.MAIN_HAND, castingTick, this.spellModifiers)
					&& this.castingTick == 0){
				// On the first tick casting a continuous spell via commands, SpellCastEvent.Post is fired.
				MinecraftForge.EVENT_BUS
						.post(new SpellCastEvent.Post(player, currentlyCasting, spellModifiers, Source.COMMAND));
			}

			castingTick++;

		}else{
			// Why is this here? Surely castingTick will always be 0 if currentlyCasting is null?
			this.castingTick = 0;
		}
	}

	/** Returns whether this player is currently casting a continuous spell via commands. */
	public boolean isCasting(){
		return this.currentlyCasting != null && this.currentlyCasting != Spells.none;
	}

	/**
	 * Returns the continuous spell this player is currently casting via commands, or the 'none' spell if they aren't
	 * casting anything.
	 */
	public Spell currentlyCasting(){
		return currentlyCasting;
	}

	/** Called each time the associated player is updated. */
	private void update(){

		if(this.selectedMinion != null && this.selectedMinion.get() == null) this.selectedMinion = null;

		// This new system removes a lot of repetitive event handler code and inflexible variables which had duplicate
		// functions, just for different enchantments.
		updateImbuedItems();

		if(!player.world.isRemote){
			if(getTpCountdown() == 1){
				player.setPositionAndUpdate(this.stoneCircleLocation.getX() + 0.5, this.stoneCircleLocation.getY(),
						this.stoneCircleLocation.getZ() + 0.5);
				player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 50, 0));
				IMessage msg = new PacketTransportation.Message(player.getEntityId());
				WizardryPacketHandler.net.sendToDimension(msg, player.world.provider.getDimension());
			}

			if(getTpCountdown() > 0){
				setTpCountdown(getTpCountdown() - 1);
			}
		}

		updateContinuousSpellCasting();
	}

	/**
	 * Returns the WizardData instance for the specified player.
	 */
	public static final WizardData get(EntityPlayer player){
		return player.getCapability(WIZARD_DATA_CAPABILITY, null);
	}

	/**
	 * Called from the event handler each time the associated player entity is cloned, i.e. on respawn or when
	 * travelling to a different dimension. Used to copy over any variables that should persist over player death. This
	 * is the inverse of the old onPlayerDeath method, which reset the variables that shouldn't persist.
	 * 
	 * @param data The old WizardData whose variables are to be copied over.
	 * @param respawn True if the player died and is respawning, false if they are just travelling between dimensions.
	 */
	public void copyFrom(WizardData data, boolean respawn){
		// TODO: What happens with spirit wolf and spirit horse?
		this.hasSpiritHorse = data.hasSpiritHorse;
		this.hasSpiritWolf = data.hasSpiritWolf;
		this.allies = data.allies;
		this.allyNames = data.allyNames;
		this.clairvoyanceDimension = data.clairvoyanceDimension;
		this.clairvoyanceLocation = data.clairvoyanceLocation;
		this.selectedMinion = data.selectedMinion;
		// Curse of soulbinding is lifted when the caster dies, but not when they switch dimensions.
		if(!respawn) this.soulboundCreatures = data.soulboundCreatures;
		this.spellsDiscovered = data.spellsDiscovered;
		this.stoneCircleDimension = data.stoneCircleDimension;
		this.stoneCircleLocation = data.stoneCircleLocation;

		// Imbuements are lost on death so their durations do not persist.
		// Command spell casting is reset on death so the associated variables do not persist.
		// tpCountdown is reset both when the player dies and when they switch dimensions.

	}

	/** Sends a packet to this player's client to synchronise necessary information. Only called server side. */
	public void sync(){
		if(this.player instanceof EntityPlayerMP){
			int id = -1;
			if(this.selectedMinion != null && this.selectedMinion.get() instanceof Entity)
				id = ((Entity)this.selectedMinion.get()).getEntityId();
			IMessage msg = new PacketPlayerSync.Message(this.spellsDiscovered, id);
			WizardryPacketHandler.net.sendTo(msg, (EntityPlayerMP)this.player);
		}
	}

	@Override
	public NBTTagCompound serializeNBT(){

		NBTTagCompound properties = new NBTTagCompound();

		// ...so Java 8 allows you to do stuff like this:
		properties.setTag("imbuements", WizardryUtilities.mapToNBT(this.imbuementDurations,
				imbuement -> new NBTTagInt(Enchantment.getEnchantmentID((Enchantment)imbuement)), NBTTagInt::new));

		properties.setBoolean("hasSpiritWolf", this.hasSpiritWolf);
		properties.setBoolean("hasSpiritHorse", this.hasSpiritHorse);

		if(this.stoneCircleLocation != null)
			properties.setLong("stoneCircleLocation", this.stoneCircleLocation.toLong());
		properties.setInteger("stoneCircleDimension", this.stoneCircleDimension);
		properties.setInteger("tpCountdown", this.tpCountdown);

		if(this.clairvoyanceLocation != null)
			properties.setLong("clairvoyanceLocation", this.clairvoyanceLocation.toLong());
		properties.setInteger("clairvoyanceDimension", this.getClairvoyanceDimension());

		// THIS is why I wrote the list/map <-> NBT methods. Look how neat this is!
		properties.setTag("allies", WizardryUtilities.listToNBT(this.allies, WizardryUtilities::UUIDtoTagCompound));
		properties.setTag("allyNames", WizardryUtilities.listToNBT(this.allyNames, NBTTagString::new));
		properties.setTag("soulboundCreatures",
				WizardryUtilities.listToNBT(this.soulboundCreatures, WizardryUtilities::UUIDtoTagCompound));

		// Might be worth converting this over to WizardryUtilities.listToNBT.
		int[] spells = new int[this.spellsDiscovered.size()];
		int i = 0;
		for(Spell spell : this.spellsDiscovered){
			spells[i] = spell.id();
			i++;
		}
		properties.setIntArray("discoveredSpells", spells);

		return properties;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt){

		if(nbt != null){

			this.imbuementDurations = WizardryUtilities.NBTToMap(nbt.getTagList("imbuements", NBT.TAG_COMPOUND),
					(NBTTagInt tag) -> (Imbuement)Enchantment.getEnchantmentByID(tag.getInt()), NBTTagInt::getInt);

			this.hasSpiritWolf = nbt.getBoolean("hasSpiritWolf");
			this.hasSpiritHorse = nbt.getBoolean("hasSpiritHorse");

			this.stoneCircleLocation = BlockPos.fromLong(nbt.getLong("stoneCircleLocation"));
			this.stoneCircleDimension = nbt.getInteger("stoneCircleDimension");
			this.tpCountdown = nbt.getInteger("tpCountdown");

			this.clairvoyanceLocation = BlockPos.fromLong(nbt.getLong("clairvoyanceLocation"));
			this.clairvoyanceDimension = nbt.getInteger("clairvoyanceDimension");

			this.allies = new HashSet<UUID>(WizardryUtilities.NBTToList(nbt.getTagList("allies", NBT.TAG_COMPOUND),
					WizardryUtilities::tagCompoundToUUID));

			this.allyNames = new HashSet<String>(
					WizardryUtilities.NBTToList(nbt.getTagList("allyNames", NBT.TAG_STRING), NBTTagString::getString));

			this.soulboundCreatures = new HashSet<UUID>(WizardryUtilities.NBTToList(
					nbt.getTagList("soulboundCreatures", NBT.TAG_COMPOUND), WizardryUtilities::tagCompoundToUUID));

			this.spellsDiscovered = new HashSet<Spell>();
			for(int id : nbt.getIntArray("discoveredSpells")){
				spellsDiscovered.add(Spell.get(id));
			}
		}
	}

	// Event handlers

	@SubscribeEvent
	// The type parameter here has to be Entity, not EntityPlayer, or the event won't get fired.
	public static void onCapabilityLoad(AttachCapabilitiesEvent<Entity> event){

		if(event.getObject() instanceof EntityPlayer)
			event.addCapability(new ResourceLocation(Wizardry.MODID, "WizardData"),
					new WizardData.Provider((EntityPlayer)event.getObject()));

		// This demonstrates why capabilities are badly structured: The following code compiles, but what it does is put
		// a player into a CapabilityDispatcher, which is in turn stored in that very same player, which makes no sense
		// at all!
		// event.addCapability(new ResourceLocation(Wizardry.MODID, "WizardData"), event.getObject());
	}

	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event){

		WizardData newData = WizardData.get(event.getEntityPlayer());
		WizardData oldData = WizardData.get(event.getOriginal());

		newData.copyFrom(oldData, event.isWasDeath());
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event){
		if(!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayerMP){
			// Synchronises wizard data after loading.
			WizardData data = WizardData.get((EntityPlayer)event.getEntity());
			if(data != null) data.sync();
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		if(event.getEntityLiving() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getEntityLiving();

			if(WizardData.get(player) != null){
				WizardData.get(player).update();
			}
		}
	}

	/**
	 * This is a nested class for a few reasons: firstly, it makes sense because instances of this and WizardData go
	 * hand-in-hand; secondly, it's too short to be worth a separate file; and thirdly (and most importantly) it allows
	 * me to access WIZARD_DATA_CAPABILITY while keeping it private.
	 */
	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		private final WizardData data;

		public Provider(EntityPlayer player){
			data = new WizardData(player);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing){
			return capability == WIZARD_DATA_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing){

			if(capability == WIZARD_DATA_CAPABILITY){
				return WIZARD_DATA_CAPABILITY.cast(data);
			}

			return null;
		}

		@Override
		public NBTTagCompound serializeNBT(){
			return data.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt){
			data.deserializeNBT(nbt);
		}

	}

	// Ended up deleting IWizardData because it was unnecessary. This is the comment that was at the start of it:

	/* I'm not going to lie, I will never find the capabilities system even remotely intuitive so this is a bare-minimum
	 * approach just to get things working (four classes where one would have done?!) At one point I considered simply
	 * wrapping my old IEEP inside a single-field capability, but I eventually decided I would at least *try* to do it
	 * properly.
	 * 
	 * "...without having to directly implement many interfaces." - Forge Docs. I still can't see what's wrong with
	 * implementing many interfaces; surely that's what Java interfaces are designed for?
	 * 
	 * Other things I find annoying: - IStorage. It's completely redundant in the majority of cases, and I don't
	 * understand why we need yet another separate class. - Making an interface, only to implement it once and once
	 * only. This completely defeats the point of interfaces. - The EnumFacing parameter, which is again redundant for
	 * everything that isn't a tile entity. So much for a clean, neat system.
	 * 
	 * What Forge has effectively done is conflated two different functions: attaching data to stuff and cross-mod
	 * integration/soft dependencies. I think this is bad design; it would have been better to keep the two features
	 * separate.
	 * 
	 * Here's my current understanding of how the capability system works: - You make an interface which defines the
	 * things your capability can do (this class). I will call this the TEMPLATE. - You implement that interface with
	 * your default implementation (WizardData). This is the closest analog to your old IEEP implementation class. THIS
	 * CLASS STORES ALL THE VARIABLES, and hence has one instance for each instance of whatever it is attached to. I
	 * will call this the DATA. - The DATA class implements INBTSerializable (assuming you want it to be saved, which is
	 * nearly always the case) - Despite its name, Capability<T> does NOT represent a capability itself. Instead, it
	 * acts as a sort of identifier/key, the idea being that you can access a particular instance of your DATA given the
	 * key (which tells forge that you want a capability of type TEMPLATE) and the object you want the DATA for. This is
	 * what Entity.getCapability(...) does.
	 * 
	 * To really understand what's going on though, you need to sift through Forge's verbose data structures and find
	 * where capabilities are actually hooked into vanilla: - Anything that implements ICapabilityProvider will have a
	 * private CapabilityDispatcher field. This holds other ICapabilityProviders. (I know. This inheritance pattern DOES
	 * NOT MAKE SENSE, because these could, in theory, be OTHER ENTITIES!) - This field is assigned a value through
	 * Forge's event factory, which, as we are all familiar with, calls all the methods marked with @SubscribeEvent.
	 * These methods add individual ICapabilityProviders to a Map stored in the event, which the event factory then
	 * wraps in a CapabilityDispatcher (which is itself an ICapabilityProvider) for the object that called it. - In your
	 * event handler, you return a custom ICapabilityProvider which is effectively bolted on to the player, and
	 * duplicates the ICapabilityProvider methods so you can hook into them and return an instance of your DATA class. -
	 * Where before there was a simple collection of IEEPs stored in the player, there is now a tree of
	 * ICapabilityProviders:
	 * 
	 * - Entity/TileEntity/ItemStack - Vanilla ICapabilityProviders, mostly IItemHandlers, stored as fields. -
	 * CapabilityDispatcher, stored as a field. - Custom ICapabilityProviders - Custom CapabilityDispatchers - ...
	 * 
	 * Most importantly, EACH PLAYER HOLDS THEIR OWN INSTANCE OF THIS TREE.
	 * 
	 * When a capability is retrieved, the following process happens: 1. For the Entity/TileEntity/ItemStack instance,
	 * ICapabilityProvider.getCapability(...) is called. 2. The request propogates through the tree and finds the
	 * requested capability. */

}
