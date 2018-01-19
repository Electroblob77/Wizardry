package electroblob.wizardry;

import java.util.List;
import java.util.Map;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.enchantment.Imbuement;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.potion.ICustomPotionParticles;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAchievements;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Clairvoyance;
import electroblob.wizardry.spell.FreezingWeapon;
import electroblob.wizardry.spell.Intimidate;
import electroblob.wizardry.spell.MindControl;
import electroblob.wizardry.spell.ShadowWard;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.IndirectMinionDamage;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.MinionDamage;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

// TODO: This class is waaaay too long. We need to either:
// - Split it into several separate handlers, each with a logical area to deal with
// - Remove most of the stuff, and have individual spell, item, block, entity, etc. classes handle their own events
// - Keep all the methods, but delegate near-repeated behaviours to their individual spell/potion/whatever classes

/**
 * Wizardry's main event handler class for common code.
 * @author Electroblob
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public final class WizardryEventHandler {
	
	@SubscribeEvent
	// The type parameter here has to be Entity, not EntityPlayer, or the event won't get fired.
	public static void onCapabilityLoad(AttachCapabilitiesEvent<Entity> event){

		if(event.getObject() instanceof EntityPlayer)
			event.addCapability(new ResourceLocation(Wizardry.MODID, "WizardData"), new WizardData.Provider((EntityPlayer)event.getObject()));

		// This demonstrates why capabilities are badly structured: The following code compiles, but what it does is put
		// a player into a CapabilityDispatcher, which is in turn stored in that very same player, which makes no sense
		// at all!
		//event.addCapability(new ResourceLocation(Wizardry.MODID, "WizardData"), event.getObject());
	}

	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event){

		WizardData newData = WizardData.get(event.getEntityPlayer());
		WizardData oldData = WizardData.get(event.getOriginal());

		newData.copyFrom(oldData, event.isWasDeath());
	}

	@SubscribeEvent
	public static void onWorldLoadEvent(WorldEvent.Load event){
		if(!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0){
			// Called to initialise the spell glyph data when a world loads, if it isn't already.
			// NOTE: Do we actually need this, or can we just let it initialise the first time it is needed? (see below)
			SpellGlyphData.get(event.getWorld());
		}
	}
	
	// IDEA: Config option allowing users to specify loot locations
	private static final String[] LOOT_INJECTION_LOCATIONS = {
			"minecraft:chests/simple_dungeon",
			"minecraft:chests/abandoned_mineshaft",
			"minecraft:chests/desert_pyramid",
			"minecraft:chests/jungle_temple",
			"minecraft:chests/stronghold_corridor",
			"minecraft:chests/stronghold_crossing",
			"minecraft:chests/stronghold_library",
			"minecraft:chests/igloo_chest"
	};
	
	@SubscribeEvent
	public static void onLootTableLoadEvent(LootTableLoadEvent event){
		if(Wizardry.settings.generateLoot){
			for(String location : LOOT_INJECTION_LOCATIONS){
				if(event.getName().toString().matches(location)){
					event.getTable().addPool(getAdditive("wizardry:chests/dungeon_additions"));
				}
			}
		}
	}

	private static LootPool getAdditive(String entryName){
		return new LootPool(new LootEntry[] { getAdditiveEntry(entryName, 1) }, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), Wizardry.MODID + "_additive_pool");
	}

	private static LootEntryTable getAdditiveEntry(String name, int weight){
		return new LootEntryTable(new ResourceLocation(name), weight, 0, new LootCondition[0], Wizardry.MODID + "_additive_entry");
	}

	/* There is a subtle but important difference between LivingAttackEvent and LivingHurtEvent - LivingAttackEvent
	 * fires immediately when attackEntityFrom is called, whereas LivingHurtEvent only fires if the attack actually
	 * succeeded, i.e. if the entity in question takes damage (though the event is fired before that so you can cancel
	 * the damage). Things are processed in the following order:
	 * * LivingAttackEvent *
	 * - Invulnerability
	 * - Already-dead-ness
	 * - Fire resistance
	 * - Helmets vs. falling things
	 * - Hurt resistant time
	 * - Invulnerability (again)
	 * * LivingHurtEvent *
	 * - Armour
	 * - Potions
	 * - Health is finally changed
	 * Of course, there are no guarantees that other mods hooking into these two events will be called before or after
	 * yours, but you can have some degree of control by choosing which event to use.
	 * EDIT: Actually, there are. Firstly, you can set a priority in the @SubscribeEvent annotation which defines how
	 * early (higher priority) or late (lower priority) the method is called. Methods with the same priority are sorted
	 * alphabetically by mod id (so it's safe to assume wizardry would be fairly late on!). I wonder if there are any
	 * conventions for what sort of things take what priority...? */

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){

		// Rather than bother overriding entire attack methods in ISummonedCreature implementations, it's easier (and
		// more robust) to use LivingAttackEvent to modify the damage source.
		if(event.getSource().getEntity() instanceof ISummonedCreature){

			EntityLivingBase summoner = ((ISummonedCreature)event.getSource().getEntity()).getCaster();

			if(summoner != null){

				event.setCanceled(true);
				DamageSource newSource = event.getSource();
				// Copies over the original DamageType if appropriate.
				DamageType type = event.getSource() instanceof IElementalDamage ? ((IElementalDamage)event.getSource()).getType() : DamageType.MAGIC;
				// Copies over the original isRetaliatory flag if appropriate.
				boolean isRetaliatory = event.getSource() instanceof IElementalDamage && ((IElementalDamage)event.getSource()).isRetaliatory();

				// All summoned creatures are classified as magic, so it makes sense to do it this way.
				if(event.getSource() instanceof EntityDamageSourceIndirect){
					newSource = new IndirectMinionDamage(event.getSource().damageType, event.getSource().getSourceOfDamage(), event.getSource().getEntity(), summoner, type, isRetaliatory);
				}else if(event.getSource() instanceof EntityDamageSource){
					// Name is copied over so it uses the appropriate vanilla death message
					newSource = new MinionDamage(event.getSource().damageType, event.getSource().getEntity(), summoner, type, isRetaliatory);
				}

				// Copy over any relevant 'attributes' the original DamageSource might have had.
				if(event.getSource().isExplosion()) newSource.setExplosion();
				if(event.getSource().isFireDamage()) newSource.setFireDamage();
				if(event.getSource().isProjectile()) newSource.setProjectile();

				// For some reason Minecraft calculates knockback relative to DamageSource#getEntity. In vanilla this
				// is unnoticeable, but it looks a bit weird with summoned creatures involved - so let's fix that!
				if(WizardryUtilities.attackEntityWithoutKnockback(event.getEntity(), newSource, event.getAmount())){
					WizardryUtilities.applyStandardKnockback(event.getSource().getEntity(), event.getEntityLiving());
					((ISummonedCreature)event.getSource().getEntity()).onSuccessfulAttack(event.getEntityLiving());
				}

				return;
			}
		}

		// Prevents any damage to allies from magic if friendly fire is enabled
		if(!Wizardry.settings.friendlyFire && event.getSource() != null && event.getSource().getEntity() instanceof EntityPlayer
				&& event.getEntity() instanceof EntityPlayer && event.getSource() instanceof IElementalDamage){
			if(WizardryUtilities.isPlayerAlly((EntityPlayer)event.getSource().getEntity(), (EntityPlayer)event.getEntity())){
				event.setCanceled(true);
				// I think this ought to be here, since if the event is cancelled nothing else needs to happen.
				return;
			}
		}

		if(event.getSource() instanceof IElementalDamage){
			if(MagicDamage.isEntityImmune(((IElementalDamage)event.getSource()).getType(), event.getEntity())){
				event.setCanceled(true);
				// I would have liked to have done the 'resist' chat message here, but I overlooked the fact that I
				// would need an instance of the spell to get its display name!
				return;
			}
			// One convenient side effect of the new damage type system is that I can get rid of all the places where
			// creepers are charged and just put them here under shock damage - this is precisely the sort of
			// repetitive code I was trying to get rid of, since errors can (and did!) occur.
			if(event.getEntityLiving() instanceof EntityCreeper && !((EntityCreeper)event.getEntityLiving()).getPowered()
					&& ((IElementalDamage)event.getSource()).getType() == DamageType.SHOCK){
				// Charges creepers when they are hit by shock damage
				WizardryUtilities.chargeCreeper((EntityCreeper)event.getEntityLiving());
				// Gives the player that caused the shock damage the 'It's Gonna Blow' achievement
				if(event.getSource().getEntity() instanceof EntityPlayer){
					((EntityPlayer)event.getSource().getEntity()).addStat(WizardryAchievements.charge_creeper);
				}
			}
		}

		// Bursts bubble when the creature inside takes damage
		if(event.getEntityLiving().getRidingEntity() instanceof EntityBubble &&
				!((EntityBubble)event.getEntityLiving().getRidingEntity()).isDarkOrb){
			event.getEntityLiving().getRidingEntity().playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.5f, 1.0f);
			event.getEntityLiving().getRidingEntity().setDead();
		}

		// Prevents all unblockable damage while transience is active
		if(event.getEntityLiving().isPotionActive(WizardryPotions.transience) && event.getSource() != null && !event.getSource().isUnblockable()){
			event.setCanceled(true);
			// Again, I think this ought to be here, since if the event is cancelled nothing else needs to happen.
			return;
		}

		if(event.getSource() != null && event.getSource().getEntity() instanceof EntityLivingBase){

			// Cancels the mind trick effect if the creature takes damage
			// This has been moved to within the (event.getSource().getEntity() instanceof EntityLivingBase) check so it doesn't
			// crash the game with a ConcurrentModificationException. If you think about it, mind trick only ought to be
			// cancelled if something attacks the entity since potions, drowning, cacti etc. don't affect the targeting.
			if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_trick)){
				event.getEntityLiving().removePotionEffect(WizardryPotions.mind_trick);
			}

			// 'Revenge' effects

			EntityLivingBase attacker = (EntityLivingBase)event.getSource().getEntity();
			World world = event.getEntityLiving().worldObj;

			if(event.getEntityLiving().isPotionActive(WizardryPotions.fireskin) && !event.getSource().isProjectile()){
				if(!MagicDamage.isEntityImmune(DamageType.FIRE, event.getEntityLiving())) attacker.setFire(5);
			}

			if(event.getEntityLiving().isPotionActive(WizardryPotions.ice_shroud) && !event.getSource().isProjectile()){
				if(!MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
					attacker.addPotionEffect(new PotionEffect(WizardryPotions.frost, 100, 0));
			}

			if(event.getEntityLiving().isPotionActive(WizardryPotions.static_aura) && !event.getSource().isProjectile()){
				if(!world.isRemote){
					EntityArc arc = new EntityArc(world);
					arc.setEndpointCoords(event.getEntityLiving().posX, event.getEntityLiving().posY + 1, event.getEntityLiving().posZ,
							attacker.posX, attacker.posY + attacker.height/2, attacker.posZ);
					world.spawnEntityInWorld(arc);
				}else{
					for(int i=0;i<8;i++){
						Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world, attacker.posX + world.rand.nextFloat() - 0.5, attacker.getEntityBoundingBox().minY + attacker.height/2 + world.rand.nextFloat()*2 - 1, attacker.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
						world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, attacker.posX + world.rand.nextFloat() - 0.5, attacker.getEntityBoundingBox().minY + attacker.height/2 + world.rand.nextFloat()*2 - 1, attacker.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
					}
				}
				
				attacker.attackEntityFrom(MagicDamage.causeDirectMagicDamage(event.getEntityLiving(), DamageType.SHOCK, true), 4.0f);
				attacker.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			}

			// Shadow ward
			if(event.getEntityLiving() instanceof EntityPlayer){

				ItemStack wand = event.getEntityLiving().getActiveItemStack();

				if(wand != null && wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
						&& WandHelper.getCurrentSpell(wand) instanceof ShadowWard && !event.getSource().isUnblockable()){

					event.setCanceled(true);
					// This DamageSource.magic and not event.getSource() because the latter would cause an infinite loop.
					event.getEntityLiving().attackEntityFrom(DamageSource.magic, event.getAmount()/2);
					attacker.attackEntityFrom(MagicDamage.causeDirectMagicDamage(event.getEntityLiving(), DamageType.MAGIC, true), event.getAmount()/2);
				}
			}

			// Transience
			if(attacker.isPotionActive(WizardryPotions.transience)){
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		// Curse of soulbinding
		if(!event.getEntity().worldObj.isRemote && event.getEntityLiving() instanceof EntityPlayer && !event.getSource().isUnblockable()){
			WizardData properties = WizardData.get((EntityPlayer)event.getEntityLiving());
			if(properties != null){
				properties.damageAllSoulboundCreatures(event.getAmount());
			}
		}

		// Flaming and freezing swords
		if(event.getSource().getEntity() instanceof EntityLivingBase){

			EntityLivingBase attacker = (EntityLivingBase)event.getSource().getEntity();

			// Players can only ever attack with their main hand, so this is the right method to use here.
			if(attacker.getHeldItemMainhand() != null && attacker.getHeldItemMainhand().getItem() instanceof ItemSword){

				int level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.flaming_weapon, attacker.getHeldItemMainhand());

				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FIRE, event.getEntityLiving()))
					event.getEntityLiving().setFire(level*4);

				level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.freezing_weapon, attacker.getHeldItemMainhand());
				// Frost lasts for longer because it doesn't do any actual damage
				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
					event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, level*200, 0));
			}
		}

		// Freezing bow
		if(event.getSource().getSourceOfDamage() instanceof EntityArrow && event.getSource().getSourceOfDamage().getEntityData() != null){

			int level = event.getSource().getSourceOfDamage().getEntityData().getInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY);

			if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
				event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, level*150, 0));
		}

		// Damage scaling
		if(event.getSource() != null && event.getSource() instanceof IElementalDamage){

			if(event.getSource().getEntity() instanceof EntityPlayer){
				event.setAmount((float)(event.getAmount() * Wizardry.settings.playerDamageScale));
			}else{
				event.setAmount((float)(event.getAmount() * Wizardry.settings.npcDamageScale));
			}
		}
	}

	@SubscribeEvent
	public static void onBlockPlaceEvent(BlockEvent.PlaceEvent event){

		if(event.getPlayer().isPotionActive(WizardryPotions.transience)){
			event.setCanceled(true);
			return;
		}

		// Spectral blocks cannot be built on
		if(event.getPlacedAgainst() == WizardryBlocks.spectral_block){
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public static void onBlockBreakEvent(BlockEvent.BreakEvent event){

		if(event.getPlayer().isPotionActive(WizardryPotions.transience)){
			event.setCanceled(true);
			return;
		}

		// Makes wizards angry if a player breaks a block in their tower
		if(!(event.getPlayer() instanceof FakePlayer)){

			List<EntityWizard> wizards = WizardryUtilities.getEntitiesWithinRadius(64, event.getPos().getX(),
					event.getPos().getY(), event.getPos().getZ(), event.getWorld(), EntityWizard.class);

			if(!wizards.isEmpty()){
				for(EntityWizard wizard : wizards){
					if(wizard.isBlockPartOfTower(event.getPos())){
						wizard.setRevengeTarget(event.getPlayer());
						event.getPlayer().addStat(WizardryAchievements.anger_wizard);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onEntityStruckByLightningEvent(EntityStruckByLightningEvent event){

		if(event.getLightning().getEntityData() != null && event.getLightning().getEntityData().hasKey("summoningPlayer")){

			EntityPlayer player = (EntityPlayer)WizardryUtilities.getEntityByUUID(event.getLightning().worldObj, event.getLightning().getEntityData().getUniqueId("summoningPlayer"));

			if(event.getEntity() instanceof EntityCreeper){
				player.addStat(WizardryAchievements.charge_creeper);
			}

			if(event.getEntity() instanceof EntityPig){
				player.addStat(WizardryAchievements.frankenstein);
			}
		}

	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		// TODO: Move the player stuff to a PlayerTickEvent and separate methods out for clarity

		if(event.getEntityLiving() instanceof EntityPlayer){

			EntityPlayer entityplayer = (EntityPlayer)event.getEntityLiving();

			if(WizardData.get(entityplayer) != null){
				WizardData.get(entityplayer).update();
			}

			if(entityplayer.openContainer instanceof ContainerWorkbench){
				craftingTableTick(entityplayer);
			}

			if(entityplayer.openContainer instanceof ContainerPlayer){
				// Unfortunately I have no choice but to call this method every tick when the player isn't using another
				// inventory, since the only thing tracking whether the player is looking at their inventory is the GUI
				// itself, which is client-side only.
				playerInventoryTick(entityplayer);
			}

			testForArmourSet:{
				for(ItemStack stack : entityplayer.getArmorInventoryList()){
					if(stack == null || !(stack.getItem() instanceof ItemWizardArmour)){
						break testForArmourSet;
					}
				}
				entityplayer.addStat(WizardryAchievements.armour_set);
			}

		}

		if(event.getEntityLiving().worldObj.isRemote){
			// Behold the power of interfaces! TODO: Backport.
			for(PotionEffect effect : event.getEntityLiving().getActivePotionEffects()){

				if(effect.getPotion() instanceof ICustomPotionParticles && effect.doesShowParticles()){

					double x = event.getEntityLiving().posX + (event.getEntityLiving().worldObj.rand.nextDouble() - 0.5)*event.getEntityLiving().width;
					double y = event.getEntityLiving().getEntityBoundingBox().minY + event.getEntityLiving().worldObj.rand.nextDouble()*event.getEntityLiving().height;
					double z = event.getEntityLiving().posZ + (event.getEntityLiving().worldObj.rand.nextDouble() - 0.5)*event.getEntityLiving().width;

					((ICustomPotionParticles)effect.getPotion()).spawnCustomParticle(event.getEntityLiving().worldObj, x, y, z);
				}
			}

			// Client-side continuous spell casting for NPCs
			
			if(event.getEntity() instanceof ISpellCaster && event.getEntity() instanceof EntityLiving){

				Spell spell = ((ISpellCaster)event.getEntity()).getContinuousSpell();

				if(spell != null && spell != Spells.none){
					spell.cast(event.getEntity().worldObj, (EntityLiving)event.getEntity(), EnumHand.MAIN_HAND, 0,
							// TODO: This implementation of modifiers relies on them being accessible client-side.
							((EntityLiving)event.getEntity()).getAttackTarget(), ((ISpellCaster)event.getEntity()).getModifiers());
				}
			}

		}else{

			if(event.getEntityLiving().isPotionActive(WizardryPotions.decay) && event.getEntityLiving().onGround && event.getEntityLiving().ticksExisted % Constants.DECAY_SPREAD_INTERVAL == 0){

				List<Entity> list = event.getEntityLiving().worldObj.getEntitiesWithinAABBExcludingEntity(event.getEntityLiving(), event.getEntityLiving().getEntityBoundingBox());

				boolean flag = true;

				for(Object object : list){
					if(object instanceof EntityDecay) flag = false;
				}

				if(flag){
					// The victim spreading the decay is the 'caster' here, so that it can actually wear off, otherwise it just gets infected with its own decay and the effect lasts forever.
					event.getEntityLiving().worldObj.spawnEntityInWorld(new EntityDecay(event.getEntityLiving().worldObj, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, event.getEntityLiving()));
				}
			}
		}
		
		// Mind Control
		// This was added because something got changed in the AI classes which means LivingSetAttackTargetEvent doesn't
		// get fired when I want it to... so I'm firing it myself.
		if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_control) && event.getEntityLiving() instanceof EntityLiving
				&& ((EntityLiving)event.getEntityLiving()).getAttackTarget() != null
				&& !((EntityLiving)event.getEntityLiving()).getAttackTarget().isEntityAlive())
			((EntityLiving)event.getEntityLiving()).setAttackTarget(null); // Causes the event to be fired

		/* Old AI no longer exists!

		// Mind trick

		if(event.getEntityLiving().isPotionActive(Wizardry.mindTrick) && event.getEntityLiving() instanceof EntityLiving){
			// Old AI (this can't be done in onLivingSetAttackTargetEvent because that only fires for the new AI).
			if(event.getEntityLiving() instanceof EntityCreature) ((EntityCreature)event.getEntityLiving()).setTarget(null);
		}


		// Mind control - old AI (this can't be done in onLivingSetAttackTargetEvent because that only fires for the new AI).
		mindcontrol:
			if(event.getEntityLiving().isPotionActive(Wizardry.mindControl) && event.getEntityLiving() instanceof EntityLiving){

				NBTTagCompound entityNBT = event.getEntityLiving().getEntityData();

				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){

					Entity caster = WizardryUtilities.getEntityByUUID(event.getEntity().worldObj, UUID.fromString(entityNBT.getString(MindControl.NBT_KEY)));

					if(caster instanceof EntityLivingBase){

						if(MindControl.findMindControlTarget((EntityLiving)event.getEntityLiving(), (EntityLivingBase)caster, event.getEntity().worldObj)){
							// If it worked, skip setting the target to null.
							break mindcontrol;
						}
					}
				}
				// If the caster couldn't be found or no valid target was found, this just acts like mind trick.
				((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
			}
		 */

		// Intimidate
		if(event.getEntityLiving().isPotionActive(WizardryPotions.fear) && event.getEntityLiving() instanceof EntityCreature){

			NBTTagCompound entityNBT = event.getEntityLiving().getEntityData();
			EntityCreature creature = (EntityCreature)event.getEntityLiving();

			if(entityNBT != null && entityNBT.hasKey(Intimidate.NBT_KEY)){

				Entity caster = WizardryUtilities.getEntityByUUID(creature.worldObj, entityNBT.getUniqueId(Intimidate.NBT_KEY));

				if(caster instanceof EntityLivingBase){
					Intimidate.runAway(creature, (EntityLivingBase)caster);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBreakSpeedEvent(BreakSpeed event){
		if(event.getEntityPlayer().isPotionActive(WizardryPotions.frost)){
			// Amplifier + 1 because it starts at 0
			event.setNewSpeed(event.getOriginalSpeed() * (1 - Constants.FROST_FATIGUE_PER_LEVEL*(event.getEntityPlayer().getActivePotionEffect(WizardryPotions.frost).getAmplifier() + 1)));
		}
	}

	private static void playerInventoryTick(EntityPlayer player) {

		// Charges wand using mana flask. It is here rather than in the crafting handler so the result displays
		// the proper damage before it is actually crafted.

		boolean flag = false;
		ItemStack wand = null;
		ItemStack armour = null;
		IInventory craftMatrix = ((ContainerPlayer)player.openContainer).craftMatrix;
		ItemStack outputItem = ((ContainerPlayer)player.openContainer).craftResult.getStackInSlot(0);

		for(int i = 0; i < craftMatrix.getSizeInventory(); i++){
			if(craftMatrix.getStackInSlot(i) != null){
				ItemStack itemstack = craftMatrix.getStackInSlot(i);

				if(itemstack.getItem() == WizardryItems.mana_flask){
					flag = true;
				}

				if(itemstack.getItem() instanceof ItemWand){
					wand = itemstack;
				}

				if(itemstack.getItem() instanceof ItemWizardArmour){
					armour = itemstack;
				}
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWand && flag && wand != null){
			outputItem.setTagCompound((wand.getTagCompound()));
			if(wand.getItemDamage()-Constants.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Constants.MANA_PER_FLASK);
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWizardArmour && flag && armour != null){
			outputItem.setTagCompound((armour.getTagCompound()));
			if(armour.getItemDamage()-Constants.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Constants.MANA_PER_FLASK);
			}
		}
	}

	private static void craftingTableTick(EntityPlayer player) {

		// Charges wand using mana flask. It is here rather than in the crafting handler so the result displays
		// the proper damage before it is actually crafted.

		boolean flag = false;
		ItemStack wand = null;
		ItemStack armour = null;
		IInventory craftMatrix = ((ContainerWorkbench)player.openContainer).craftMatrix;
		ItemStack outputItem = ((ContainerWorkbench)player.openContainer).craftResult.getStackInSlot(0);

		for (int i = 0; i < craftMatrix.getSizeInventory(); i++){
			if(craftMatrix.getStackInSlot(i) != null){
				ItemStack itemstack = craftMatrix.getStackInSlot(i);

				if(itemstack.getItem() == WizardryItems.mana_flask){
					flag = true;
				}

				if(itemstack.getItem() instanceof ItemWand){
					wand = itemstack;
				}

				if(itemstack.getItem() instanceof ItemWizardArmour){
					armour = itemstack;
				}
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWand && flag && wand != null){
			outputItem.setTagCompound((wand.getTagCompound()));
			if(wand.getItemDamage()-Constants.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Constants.MANA_PER_FLASK);
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWizardArmour && flag && armour != null){
			outputItem.setTagCompound((armour.getTagCompound()));
			if(armour.getItemDamage()-Constants.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Constants.MANA_PER_FLASK);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event){

		if(event.getSource().getEntity() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getSource().getEntity();

			for(ItemStack stack : WizardryUtilities.getPrioritisedHotbarAndOffhand(player)){
				
				if(stack != null && stack.getItem() instanceof ItemWand && stack.isItemDamaged() && WandHelper.getUpgradeLevel(stack, WizardryItems.siphon_upgrade) > 0){
					int damage = stack.getItemDamage() - Constants.SIPHON_MANA_PER_LEVEL*WandHelper.getUpgradeLevel(stack, WizardryItems.siphon_upgrade) - player.worldObj.rand.nextInt(Constants.SIPHON_MANA_PER_LEVEL);
					if(damage < 0) damage = 0;
					stack.setItemDamage(damage);
					break;
				}
			}

			if(event.getEntityLiving() == player && event.getSource() instanceof IElementalDamage){
				player.addStat(WizardryAchievements.self_destruct);
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedInEvent(PlayerLoggedInEvent event){
		// When a player logs in, they are sent the glyph data and the server's settings.
		if(event.player instanceof EntityPlayerMP){
			SpellGlyphData.get(event.player.worldObj).sync((EntityPlayerMP)event.player);
			Wizardry.settings.sync((EntityPlayerMP)event.player);
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event){

		if(!event.getEntity().worldObj.isRemote && event.getEntity() instanceof EntityPlayerMP){
			// Synchronises wizard data after loading.
			WizardData data = WizardData.get((EntityPlayer)event.getEntity());
			if(data != null) data.sync();
		}

		// Rather long-winded (but necessary) way of getting an arrow just after it has been fired, checking if the bow
		// that fired it has the imbuement enchantment, and applying extra damage accordingly.
		if(!event.getEntity().worldObj.isRemote && event.getEntity() instanceof EntityArrow){

			EntityArrow arrow = (EntityArrow)event.getEntity();

			magicBow:
				if(arrow.shootingEntity instanceof EntityLivingBase){

					EntityLivingBase archer = (EntityLivingBase)arrow.shootingEntity;

					ItemStack bow = archer.getHeldItemMainhand();

					if(bow == null || !(bow.getItem() instanceof ItemBow)){
						bow = archer.getHeldItemOffhand();
						// Break used because return would skip the entire method, bypassing anything that might be added
						// further down.
						if(bow == null || !(bow.getItem() instanceof ItemBow)) break magicBow;
					}

					// Taken directly from ItemBow, so it works exactly the same as the power enchantment.
					int level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.magic_bow, bow);

					if(level > 0){
						arrow.setDamage(arrow.getDamage() + (double)level * 0.5D + 0.5D);
					}

					if(EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.flaming_weapon, bow) > 0){
						// Again, this is exactly what happens in ItemBow (flame is flame; level does nothing).
						arrow.setFire(100);
					}

					level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.freezing_weapon, bow);

					if(level > 0){
						if(arrow.getEntityData() != null){
							arrow.getEntityData().setInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY, level);
						}
					}
				}

		}
	}

	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event){
		// TODO: Really, this should be in a loot table (mob_additions), however I can't seem to find a way of
		// automatically adding it to all subclasses of IMob.
		// Evil wizards drop spell books themselves
		if(event.getEntityLiving() instanceof IMob && !(event.getEntityLiving() instanceof EntityEvilWizard)
				// TODO: Backport when you backport the new summoned creature system.
				&& !(event.getEntityLiving() instanceof ISummonedCreature) && event.getSource().getEntity() instanceof EntityPlayer
				&& Wizardry.settings.spellBookDropChance > 0){

			// This does exactly what the entity drop method does, but with a different random number so that the
			// spell book doesn't always drop with other rare drops.
			int rareDropNumber = event.getEntity().worldObj.rand.nextInt(200) - event.getLootingLevel();
			if(rareDropNumber < Wizardry.settings.spellBookDropChance){
				// Drops a spell book
				int id = WizardryUtilities.getStandardWeightedRandomSpellId(event.getEntity().worldObj.rand);

				event.getDrops().add(new EntityItem(event.getEntityLiving().worldObj, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ,
						new ItemStack(WizardryItems.spell_book, 1, id)));
			}
		}

		for(EntityItem item : event.getDrops()){

			// Destroys conjured items if their caster dies.
			if(item.getEntityItem().getItem() instanceof IConjuredItem){
				item.setDead();
			}

			// Instantly disenchants an imbued weapon if it is dropped when the player dies.
			if(item.getEntityItem().isItemEnchanted()){

				// No need to check what enchantments the item has, since remove() does nothing if the element does not exist.
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(item.getEntityItem());

				// Removes the magic weapon enchantments from the enchantment map
				// An excellent demonstration of the usefulness of both interfaces and Java 8.
				enchantments.entrySet().removeIf(entry -> entry.getKey() instanceof Imbuement);

				// Applies the new enchantment map to the item
				EnchantmentHelper.setEnchantments(enchantments, item.getEntityItem());
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// Mind trick
		// If the target is null already, no need to set it to null, or infinite loops will occur.
		if((event.getEntityLiving().isPotionActive(WizardryPotions.mind_trick) || event.getEntityLiving().isPotionActive(WizardryPotions.fear)) && event.getEntityLiving() instanceof EntityLiving && event.getTarget() != null){
			((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}

		// Mind control
		mindcontrol:
			if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_control) && event.getEntityLiving() instanceof EntityLiving){

				NBTTagCompound entityNBT = event.getEntityLiving().getEntityData();

				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY + "Most")){

					Entity caster = WizardryUtilities.getEntityByUUID(event.getEntity().worldObj, entityNBT.getUniqueId(MindControl.NBT_KEY));

					// If the target that the event tried to set is already a valid mind control target, nothing happens.
					if(event.getTarget() != null && WizardryUtilities.isValidTarget(caster, event.getTarget())) break mindcontrol;

					if(caster instanceof EntityLivingBase){

						if(MindControl.findMindControlTarget((EntityLiving)event.getEntityLiving(), (EntityLivingBase)caster, event.getEntity().worldObj)){
							// If it worked, skip setting the target to null.
							break mindcontrol;
						}
					}
				}
				// If the caster couldn't be found or no valid target was found, this just acts like mind trick.
				// If the target is null already, no need to set it to null, or infinite loops will occur.
				if(event.getTarget() != null) ((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
			}
	}

	@SubscribeEvent
	public static void onItemPickupEvent(EntityItemPickupEvent event){
		if(event.getItem().getEntityItem().getItem() == WizardryItems.magic_crystal){
			event.getEntityPlayer().addStat(WizardryAchievements.crystal, 1);
		}
	}

	@SubscribeEvent
	public static void onItemTossEvent(ItemTossEvent event){

		// Prevents conjured items being thrown by dragging and dropping outside the inventory.
		if(event.getEntityItem().getEntityItem().getItem() instanceof IConjuredItem){
			event.setCanceled(true);
			event.getPlayer().inventory.addItemStackToInventory(event.getEntityItem().getEntityItem());
		}

		// Instantly disenchants an imbued weapon if it is thrown on the ground.
		if(event.getEntityItem().getEntityItem().isItemEnchanted()){

			// No need to check what enchantments the item has, since remove() does nothing if the element does not exist.
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(event.getEntityItem().getEntityItem());

			// Removes the magic weapon enchantments from the enchantment map
			enchantments.entrySet().removeIf(entry -> entry.getKey() instanceof Imbuement);

			// Applies the new enchantment map to the item
			EnchantmentHelper.setEnchantments(enchantments, event.getEntityItem().getEntityItem());
		}
	}

	@SubscribeEvent
	public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event){

		if(event.getEntityPlayer().isSneaking()){

			// The event now has an ItemStack, which greatly simplifies hand-related stuff.
			ItemStack wand = event.getItemStack();

			if(wand != null && wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof Clairvoyance){

				WizardData properties = WizardData.get(event.getEntityPlayer());

				if(properties != null){
					// THIS is why BlockPos is a thing - in 1.7.10 this requires a clumsy switch statement.
					BlockPos pos = event.getPos().offset(event.getFace());

					properties.setClairvoyancePoint(pos, event.getWorld().provider.getDimension());

					if(!event.getWorld().isRemote){
						event.getEntityPlayer().addChatMessage(new TextComponentTranslation("spell.clairvoyance.confirm", Spells.clairvoyance.getNameForTranslationFormatted()));
					}
					
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onBonemealEvent(BonemealEvent event){
		// Grows crystal flowers when bonemeal is used on grass
		if(event.getBlock().getBlock() == Blocks.GRASS){

			BlockPos pos = event.getPos().add(event.getWorld().rand.nextInt(8) - event.getWorld().rand.nextInt(8),
					event.getWorld().rand.nextInt(4) - event.getWorld().rand.nextInt(4), 
					event.getWorld().rand.nextInt(8) - event.getWorld().rand.nextInt(8));

			if (event.getWorld().isAirBlock(new BlockPos(pos)) && (!event.getWorld().provider.getHasNoSky() || pos.getY() < 127) && WizardryBlocks.crystal_flower.canPlaceBlockAt(event.getWorld(), pos))
			{
				event.getWorld().setBlockState(pos, WizardryBlocks.crystal_flower.getDefaultState(), 2);
			}
		}
	}
	
}