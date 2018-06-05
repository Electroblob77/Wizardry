package electroblob.wizardry;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.FreezingWeapon;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**
 * As of Wizardry 2.1, most of the code in this class has been relocated somewhere sensible, leaving only a few
 * miscellaneous things that don't make much sense anywhere else, or that are better kept together. Previously, this was
 * a gigantic class with about half of the entire mod's logic in it!
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public final class WizardryEventHandler {

	// IDEA: Config option allowing users to specify loot locations
	private static final String[] LOOT_INJECTION_LOCATIONS = {"minecraft:chests/simple_dungeon",
			"minecraft:chests/abandoned_mineshaft", "minecraft:chests/desert_pyramid", "minecraft:chests/jungle_temple",
			"minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing",
			"minecraft:chests/stronghold_library", "minecraft:chests/igloo_chest", "minecraft:chests/woodland_mansion",
			"minecraft:chests/end_city_treasure"};

	@SubscribeEvent
	public static void onLootTableLoadEvent(LootTableLoadEvent event){
		if(Wizardry.settings.generateLoot){
			for(String location : LOOT_INJECTION_LOCATIONS){
				if(event.getName().toString().matches(location)){
					event.getTable().addPool(getAdditive(Wizardry.MODID + ":chests/dungeon_additions"));
				}
			}
		}
	}

	private static LootPool getAdditive(String entryName){
		return new LootPool(new LootEntry[]{getAdditiveEntry(entryName, 1)}, new LootCondition[0],
				new RandomValueRange(1), new RandomValueRange(0, 1), Wizardry.MODID + "_additive_pool");
	}

	private static LootEntryTable getAdditiveEntry(String name, int weight){
		return new LootEntryTable(new ResourceLocation(name), weight, 0, new LootCondition[0],
				Wizardry.MODID + "_additive_entry");
	}

	@SubscribeEvent
	public static void onPlayerLoggedInEvent(PlayerLoggedInEvent event){
		// When a player logs in, they are sent the glyph data and the server's settings.
		if(event.player instanceof EntityPlayerMP){
			SpellGlyphData.get(event.player.world).sync((EntityPlayerMP)event.player);
			Wizardry.settings.sync((EntityPlayerMP)event.player);
		}
	}

	@SubscribeEvent
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// If a spell is disabled in the config, it will not work.
		if(!event.getSpell().isEnabled()){
			if(!event.getEntityLiving().world.isRemote) event.getEntity().sendMessage(
					new TextComponentTranslation("spell.disabled", event.getSpell().getNameForTranslationFormatted()));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onSpellCastPostEvent(SpellCastEvent.Post event){

		// Spell discovery (only players can discover spells, obviously)
		if(event.getEntity() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getEntity();

			WizardData data = WizardData.get(player);

			if(data != null){
				// Data is updated on both sides (This line was added client-side to fix a bug back in 1.1.3, so now
				// it's in common code, which is nice!)
				// Short-circuiting AND means that discoverSpell is only called if the event isn't cancelled.
				if(!MinecraftForge.EVENT_BUS
						.post(new DiscoverSpellEvent(player, event.getSpell(), DiscoverSpellEvent.Source.CASTING))
						&& data.discoverSpell(event.getSpell())){

					// If the spell wasn't already discovered, other stuff happens:
					if(event.getSource() == SpellCastEvent.Source.COMMAND){
						// If the spell didn't send a packet itself, the extended player needs to be synced so the
						// spell discovery updates on the client.
						if(!event.getSpell().doesSpellRequirePacket()) data.sync();

					}else if(!event.getEntity().world.isRemote && !player.capabilities.isCreativeMode
							&& Wizardry.settings.discoveryMode){
						// Sound and text only happen server-side, in survival, with discovery mode on, and only when
						// the spell wasn't cast using commands.
						WizardryUtilities.playSoundAtPlayer(player, SoundEvents.ENTITY_PLAYER_LEVELUP, 1.25f, 1);
						player.sendMessage(new TextComponentTranslation("spell.discover",
								event.getSpell().getNameForTranslationFormatted()));
					}
				}
			}
		}
	}

	/* There is a subtle but important difference between LivingAttackEvent and LivingHurtEvent - LivingAttackEvent
	 * fires immediately when attackEntityFrom is called, whereas LivingHurtEvent only fires if the attack actually
	 * succeeded, i.e. if the entity in question takes damage (though the event is fired before that so you can cancel
	 * the damage). Things are processed in the following order: * LivingAttackEvent * - Invulnerability -
	 * Already-dead-ness - Fire resistance - Helmets vs. falling things - Hurt resistant time - Invulnerability (again)
	 * * LivingHurtEvent * - Armour - Potions - Health is finally changed Of course, there are no guarantees that other
	 * mods hooking into these two events will be called before or after yours, but you can have some degree of control
	 * by choosing which event to use. EDIT: Actually, there are. Firstly, you can set a priority in the @SubscribeEvent
	 * annotation which defines how early (higher priority) or late (lower priority) the method is called. Methods with
	 * the same priority are sorted alphabetically by mod id (so it's safe to assume wizardry would be fairly late on!).
	 * I wonder if there are any conventions for what sort of things take what priority...? */

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){

		// Prevents any damage to allies from magic if friendly fire is disabled
		if(!Wizardry.settings.friendlyFire && event.getSource() != null
				&& event.getSource().getTrueSource() instanceof EntityPlayer && event.getEntity() instanceof EntityPlayer
				&& event.getSource() instanceof IElementalDamage){
			if(WizardryUtilities.isPlayerAlly((EntityPlayer)event.getSource().getTrueSource(),
					(EntityPlayer)event.getEntity())){
				event.setCanceled(true);
				// This needs to be here, since if the event is cancelled nothing else needs to happen.
				return;
			}
		}

		// Retaliatory effects
		// These are better off here because the revenge effects are pretty similar, and I'd rather keep the (lengthy)
		// if statement in one place.
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof EntityLivingBase
				&& !event.getSource().isProjectile() && !(event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory())){

			EntityLivingBase attacker = (EntityLivingBase)event.getSource().getTrueSource();
			World world = event.getEntityLiving().world;

			// Fireskin
			if(event.getEntityLiving().isPotionActive(WizardryPotions.fireskin)
					&& !MagicDamage.isEntityImmune(DamageType.FIRE, event.getEntityLiving()))
				attacker.setFire(5);

			// Ice Shroud
			if(event.getEntityLiving().isPotionActive(WizardryPotions.ice_shroud)
					&& !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
				attacker.addPotionEffect(new PotionEffect(WizardryPotions.frost, 100, 0));

			// Static Aura
			if(event.getEntityLiving().isPotionActive(WizardryPotions.static_aura)){

				if(!world.isRemote){
					EntityArc arc = new EntityArc(world);
					arc.setEndpointCoords(event.getEntityLiving().posX, event.getEntityLiving().posY + 1,
							event.getEntityLiving().posZ, attacker.posX, attacker.posY + attacker.height / 2,
							attacker.posZ);
					world.spawnEntity(arc);
				}else{
					for(int i = 0; i < 8; i++){
						ParticleBuilder.create(Type.SPARK).pos(attacker.posX + world.rand.nextFloat() - 0.5,
								attacker.getEntityBoundingBox().minY + attacker.height / 2 + world.rand.nextFloat() * 2 - 1,
								attacker.posZ + world.rand.nextFloat() - 0.5).spawn(world);
						world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, attacker.posX + world.rand.nextFloat() - 0.5,
								attacker.getEntityBoundingBox().minY + attacker.height / 2 + world.rand.nextFloat() * 2
								- 1,
								attacker.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
					}
				}

				attacker.attackEntityFrom(
						MagicDamage.causeDirectMagicDamage(event.getEntityLiving(), DamageType.SHOCK, true), 4.0f);
				attacker.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			}
		}

	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		// Flaming and freezing swords
		if(event.getSource().getTrueSource() instanceof EntityLivingBase){

			EntityLivingBase attacker = (EntityLivingBase)event.getSource().getTrueSource();

			// Players can only ever attack with their main hand, so this is the right method to use here.
			if(!attacker.getHeldItemMainhand().isEmpty() && attacker.getHeldItemMainhand().getItem() instanceof ItemSword){

				int level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.flaming_weapon,
						attacker.getHeldItemMainhand());

				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FIRE, event.getEntityLiving()))
					event.getEntityLiving().setFire(level * 4);

				level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.freezing_weapon,
						attacker.getHeldItemMainhand());
				// Frost lasts for longer because it doesn't do any actual damage
				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
					event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, level * 200, 0));
			}
		}

		// Freezing bow
		if(event.getSource().getImmediateSource() instanceof EntityArrow
				&& event.getSource().getImmediateSource().getEntityData() != null){

			int level = event.getSource().getImmediateSource().getEntityData()
					.getInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY);

			if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
				event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, level * 150, 0));
		}

		// Damage scaling
		if(event.getSource() != null && event.getSource() instanceof IElementalDamage){

			if(event.getSource().getTrueSource() instanceof EntityPlayer){
				event.setAmount((float)(event.getAmount() * Wizardry.settings.playerDamageScale));
			}else{
				event.setAmount((float)(event.getAmount() * Wizardry.settings.npcDamageScale));
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		if(event.getEntityLiving() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getEntityLiving();

			if(player.world.isRemote) hackilyFixContinuousSpellCasting(player);

			// Mana flask crafting
			if(player.openContainer instanceof ContainerWorkbench){
				
				IInventory craftMatrix = ((ContainerWorkbench)player.openContainer).craftMatrix;
				ItemStack output = ((ContainerWorkbench)player.openContainer).craftResult.getStackInSlot(0);
				processManaFlaskCrafting(craftMatrix, output);
				
			}else if(player.openContainer instanceof ContainerPlayer){
				
				IInventory craftMatrix = ((ContainerPlayer)player.openContainer).craftMatrix;
				ItemStack output = ((ContainerPlayer)player.openContainer).craftResult.getStackInSlot(0);
				// Unfortunately I have no choice but to call this method every tick when the player isn't using another
				// inventory, since the only thing tracking whether the player is looking at their inventory is the GUI
				// itself, which is client-side only.
				processManaFlaskCrafting(craftMatrix, output);
			}

		}

		if(event.getEntityLiving().world.isRemote){

			// Client-side continuous spell casting for NPCs

			if(event.getEntity() instanceof ISpellCaster && event.getEntity() instanceof EntityLiving){

				Spell spell = ((ISpellCaster)event.getEntity()).getContinuousSpell();
				SpellModifiers modifiers = ((ISpellCaster)event.getEntity()).getModifiers();

				if(spell != null && spell != Spells.none){

					if(!MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(event.getEntityLiving(), spell, modifiers,
							SpellCastEvent.Source.NPC, 0))){

						spell.cast(event.getEntity().world, (EntityLiving)event.getEntity(), EnumHand.MAIN_HAND, 0,
								// TODO: This implementation of modifiers relies on them being accessible client-side.
								((EntityLiving)event.getEntity()).getAttackTarget(), modifiers);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event){

		if(event.getSource().getTrueSource() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();

			for(ItemStack stack : WizardryUtilities.getPrioritisedHotbarAndOffhand(player)){

				if(stack.getItem() instanceof ItemWand && stack.isItemDamaged()
						&& WandHelper.getUpgradeLevel(stack, WizardryItems.siphon_upgrade) > 0){
					int damage = stack.getItemDamage()
							- Constants.SIPHON_MANA_PER_LEVEL
							* WandHelper.getUpgradeLevel(stack, WizardryItems.siphon_upgrade)
							- player.world.rand.nextInt(Constants.SIPHON_MANA_PER_LEVEL);
					if(damage < 0) damage = 0;
					stack.setItemDamage(damage);
					break;
				}
			}

			if(event.getEntityLiving() == player && event.getSource() instanceof IElementalDamage){
				WizardryAdvancementTriggers.self_destruct.triggerFor(player);
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
				&& !(event.getEntityLiving() instanceof ISummonedCreature)
				&& event.getSource().getTrueSource() instanceof EntityPlayer && Wizardry.settings.spellBookDropChance > 0){

			// This does exactly what the entity drop method does, but with a different random number so that the
			// spell book doesn't always drop with other rare drops.
			int rareDropNumber = event.getEntity().world.rand.nextInt(200) - event.getLootingLevel();
			if(rareDropNumber < Wizardry.settings.spellBookDropChance){
				// Drops a spell book
				int id = WizardryUtilities.getStandardWeightedRandomSpellId(event.getEntity().world.rand);

				event.getDrops()
				.add(new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX,
						event.getEntityLiving().posY, event.getEntityLiving().posZ,
						new ItemStack(WizardryItems.spell_book, 1, id)));
			}
		}
	}

	// Private helper methods
	// ================================================================================================================

	/**
	 * Detects inconsistencies between player.getActiveItemStack and the actual itemstack and forces them to be equal.
	 * Fixes issue #25.
	 * 
	 * @param player
	 */
	private static void hackilyFixContinuousSpellCasting(EntityPlayer player){
		if(player.isHandActive() && player.getHeldItem(player.getActiveHand()).getItem() instanceof ItemWand
				&& WandHelper.getCurrentSpell(player.getHeldItem(player.getActiveHand())).isContinuous){
			if(player.getActiveItemStack() != player.getHeldItem(player.getActiveHand())){
				player.setHeldItem(player.getActiveHand(), player.getActiveItemStack());
			}
		}
	}

	private static void processManaFlaskCrafting(IInventory craftMatrix, ItemStack output){

		// Charges wand using mana flask. It is here rather than in the crafting handler so the result displays
		// the proper damage before it is actually crafted.

		boolean flag = false;
		ItemStack wand = ItemStack.EMPTY;
		ItemStack armour = ItemStack.EMPTY;

		for(int i = 0; i < craftMatrix.getSizeInventory(); i++){
			
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

		if(output.getItem() instanceof ItemWand && flag && !wand.isEmpty()){
			output.setTagCompound((wand.getTagCompound()));
			if(wand.getItemDamage() - Constants.MANA_PER_FLASK < 0){
				output.setItemDamage(0);
			}else{
				output.setItemDamage(wand.getItemDamage() - Constants.MANA_PER_FLASK);
			}
		}

		if(output.getItem() instanceof ItemWizardArmour && flag && !armour.isEmpty()){
			output.setTagCompound((armour.getTagCompound()));
			if(armour.getItemDamage() - Constants.MANA_PER_FLASK < 0){
				output.setItemDamage(0);
			}else{
				output.setItemDamage(wand.getItemDamage() - Constants.MANA_PER_FLASK);
			}
		}
	}

}