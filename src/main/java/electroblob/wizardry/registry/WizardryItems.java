package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityPoisonBomb;
import electroblob.wizardry.entity.projectile.EntitySmokeBomb;
import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.item.*;
import electroblob.wizardry.item.ItemWizardArmour.ArmourClass;
import electroblob.wizardry.misc.BehaviourSpellDispense;
import electroblob.wizardry.registry.WizardryTabs.CreativeTabListed;
import electroblob.wizardry.registry.WizardryTabs.CreativeTabSorted;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockPlanks;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Locale;

/**
 * Class responsible for defining, storing and registering all of wizardry's items. Also registers the ItemBlocks for
 * wizardry's blocks.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@ObjectHolder(Wizardry.MODID)
@Mod.EventBusSubscriber
public final class WizardryItems {

	private WizardryItems(){} // No instances!

	/** Keeping the material fields in here means {@code @ObjectHolder} ignores them. In actual fact, I could have just
	 * made them private since wizardry only uses them within this class, but in case someone needs them elsewhere I've
	 * used this trick instead to keep them public. */
	public static final class Materials {

		public static final ToolMaterial MAGICAL = EnumHelper.addToolMaterial("MAGICAL", 3, 1000, 8.0f, 4.0f, 0);

		// The parameter textureName is incorrectly-named, it's actually the name field in ArmorMaterial
		public static final ArmorMaterial SILK = EnumHelper.addArmorMaterial("SILK", "silk",
		15, new int[]{2, 4, 5, 2}, 15, WizardrySounds.ITEM_ARMOUR_EQUIP_SILK, 0.0F);
		public static final ArmorMaterial SAGE = EnumHelper.addArmorMaterial("SAGE", "sage",
				15, new int[]{2, 5, 6, 3}, 25, WizardrySounds.ITEM_ARMOUR_EQUIP_SAGE, 0.0F);
		public static final ArmorMaterial BATTLEMAGE = EnumHelper.addArmorMaterial("BATTLEMAGE", "battlemage",
				15, new int[]{3, 6, 8, 3}, 15, WizardrySounds.ITEM_ARMOUR_EQUIP_BATTLEMAGE, 1.0F);
		public static final ArmorMaterial WARLOCK = EnumHelper.addArmorMaterial("WARLOCK", "warlock",
				20, new int[]{2, 4, 5, 2}, 15, WizardrySounds.ITEM_ARMOUR_EQUIP_WARLOCK, 0.0F);

	}

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder(){ return null; }

	// This is the most concise way I can think of to register the items. Really, I'd prefer it if there was only one
	// point where all the items were listed, but that's not possible within the current system unless you use an array,
	// which means you lose the individual fields...

	public static final Item magic_crystal = placeholder();
	public static final Item earth_crystal = placeholder();
	public static final Item fire_crystal = placeholder();
	public static final Item healing_crystal = placeholder();
	public static final Item ice_crystal = placeholder();
	public static final Item lightning_crystal = placeholder();
	public static final Item necromancy_crystal = placeholder();
	public static final Item sorcery_crystal = placeholder();

	public static final Item grand_crystal = placeholder();
	public static final Item crystal_shard = placeholder();

	public static final Item wizard_handbook = placeholder();
	public static final Item arcane_tome_apprentice = placeholder();
	public static final Item arcane_tome_advanced = placeholder();
	public static final Item arcane_tome_master = placeholder();
	public static final Item spell_book = placeholder();
	public static final Item scroll = placeholder();
	public static final Item ruined_spell_book = placeholder();

	public static final Item magic_wand = placeholder();
	public static final Item apprentice_wand = placeholder();
	public static final Item advanced_wand = placeholder();
	public static final Item master_wand = placeholder();

	public static final Item novice_fire_wand = placeholder();
	public static final Item novice_ice_wand = placeholder();
	public static final Item novice_lightning_wand = placeholder();
	public static final Item novice_necromancy_wand = placeholder();
	public static final Item novice_earth_wand = placeholder();
	public static final Item novice_sorcery_wand = placeholder();
	public static final Item novice_healing_wand = placeholder();

	public static final Item apprentice_fire_wand = placeholder();
	public static final Item apprentice_ice_wand = placeholder();
	public static final Item apprentice_lightning_wand = placeholder();
	public static final Item apprentice_necromancy_wand = placeholder();
	public static final Item apprentice_earth_wand = placeholder();
	public static final Item apprentice_sorcery_wand = placeholder();
	public static final Item apprentice_healing_wand = placeholder();

	public static final Item advanced_fire_wand = placeholder();
	public static final Item advanced_ice_wand = placeholder();
	public static final Item advanced_lightning_wand = placeholder();
	public static final Item advanced_necromancy_wand = placeholder();
	public static final Item advanced_earth_wand = placeholder();
	public static final Item advanced_sorcery_wand = placeholder();
	public static final Item advanced_healing_wand = placeholder();

	public static final Item master_fire_wand = placeholder();
	public static final Item master_ice_wand = placeholder();
	public static final Item master_lightning_wand = placeholder();
	public static final Item master_necromancy_wand = placeholder();
	public static final Item master_earth_wand = placeholder();
	public static final Item master_sorcery_wand = placeholder();
	public static final Item master_healing_wand = placeholder();

	public static final Item spectral_sword = placeholder();
	public static final Item spectral_pickaxe = placeholder();
	public static final Item spectral_bow = placeholder();

	public static final Item blank_scroll = placeholder();
	public static final Item magic_silk = placeholder();

	public static final Item small_mana_flask = placeholder();
	public static final Item medium_mana_flask = placeholder();
	public static final Item large_mana_flask = placeholder();

	public static final Item storage_upgrade = placeholder();
	public static final Item siphon_upgrade = placeholder();
	public static final Item condenser_upgrade = placeholder();
	public static final Item range_upgrade = placeholder();
	public static final Item duration_upgrade = placeholder();
	public static final Item cooldown_upgrade = placeholder();
	public static final Item blast_upgrade = placeholder();
	public static final Item attunement_upgrade = placeholder();
	public static final Item melee_upgrade = placeholder();

	public static final Item flaming_axe = placeholder();
	public static final Item frost_axe = placeholder();

	public static final Item identification_scroll = placeholder();
	public static final Item resplendent_thread = placeholder();
	public static final Item crystal_silver_plating = placeholder();
	public static final Item ethereal_crystalweave = placeholder();
	public static final Item astral_diamond = placeholder();
	public static final Item purifying_elixir = placeholder();

	public static final Item firebomb = placeholder();
	public static final Item poison_bomb = placeholder();
	public static final Item smoke_bomb = placeholder();
	public static final Item spark_bomb = placeholder();

	public static final Item spectral_dust_earth = placeholder();
	public static final Item spectral_dust_fire = placeholder();
	public static final Item spectral_dust_healing = placeholder();
	public static final Item spectral_dust_ice = placeholder();
	public static final Item spectral_dust_lightning = placeholder();
	public static final Item spectral_dust_necromancy = placeholder();
	public static final Item spectral_dust_sorcery = placeholder();

	public static final Item wizard_hat = placeholder();
	public static final Item wizard_robe = placeholder();
	public static final Item wizard_leggings = placeholder();
	public static final Item wizard_boots = placeholder();

	public static final Item wizard_hat_fire = placeholder();
	public static final Item wizard_robe_fire = placeholder();
	public static final Item wizard_leggings_fire = placeholder();
	public static final Item wizard_boots_fire = placeholder();

	public static final Item wizard_hat_ice = placeholder();
	public static final Item wizard_robe_ice = placeholder();
	public static final Item wizard_leggings_ice = placeholder();
	public static final Item wizard_boots_ice = placeholder();

	public static final Item wizard_hat_lightning = placeholder();
	public static final Item wizard_robe_lightning = placeholder();
	public static final Item wizard_leggings_lightning = placeholder();
	public static final Item wizard_boots_lightning = placeholder();

	public static final Item wizard_hat_necromancy = placeholder();
	public static final Item wizard_robe_necromancy = placeholder();
	public static final Item wizard_leggings_necromancy = placeholder();
	public static final Item wizard_boots_necromancy = placeholder();

	public static final Item wizard_hat_earth = placeholder();
	public static final Item wizard_robe_earth = placeholder();
	public static final Item wizard_leggings_earth = placeholder();
	public static final Item wizard_boots_earth = placeholder();

	public static final Item wizard_hat_sorcery = placeholder();
	public static final Item wizard_robe_sorcery = placeholder();
	public static final Item wizard_leggings_sorcery = placeholder();
	public static final Item wizard_boots_sorcery = placeholder();

	public static final Item wizard_hat_healing = placeholder();
	public static final Item wizard_robe_healing = placeholder();
	public static final Item wizard_leggings_healing = placeholder();
	public static final Item wizard_boots_healing = placeholder();

	public static final Item sage_hat = placeholder();
	public static final Item sage_robe = placeholder();
	public static final Item sage_leggings = placeholder();
	public static final Item sage_boots = placeholder();

	public static final Item sage_hat_fire = placeholder();
	public static final Item sage_robe_fire = placeholder();
	public static final Item sage_leggings_fire = placeholder();
	public static final Item sage_boots_fire = placeholder();

	public static final Item sage_hat_ice = placeholder();
	public static final Item sage_robe_ice = placeholder();
	public static final Item sage_leggings_ice = placeholder();
	public static final Item sage_boots_ice = placeholder();

	public static final Item sage_hat_lightning = placeholder();
	public static final Item sage_robe_lightning = placeholder();
	public static final Item sage_leggings_lightning = placeholder();
	public static final Item sage_boots_lightning = placeholder();

	public static final Item sage_hat_necromancy = placeholder();
	public static final Item sage_robe_necromancy = placeholder();
	public static final Item sage_leggings_necromancy = placeholder();
	public static final Item sage_boots_necromancy = placeholder();

	public static final Item sage_hat_earth = placeholder();
	public static final Item sage_robe_earth = placeholder();
	public static final Item sage_leggings_earth = placeholder();
	public static final Item sage_boots_earth = placeholder();

	public static final Item sage_hat_sorcery = placeholder();
	public static final Item sage_robe_sorcery = placeholder();
	public static final Item sage_leggings_sorcery = placeholder();
	public static final Item sage_boots_sorcery = placeholder();

	public static final Item sage_hat_healing = placeholder();
	public static final Item sage_robe_healing = placeholder();
	public static final Item sage_leggings_healing = placeholder();
	public static final Item sage_boots_healing = placeholder();

	public static final Item battlemage_helmet = placeholder();
	public static final Item battlemage_chestplate = placeholder();
	public static final Item battlemage_leggings = placeholder();
	public static final Item battlemage_boots = placeholder();

	public static final Item battlemage_helmet_fire = placeholder();
	public static final Item battlemage_chestplate_fire = placeholder();
	public static final Item battlemage_leggings_fire = placeholder();
	public static final Item battlemage_boots_fire = placeholder();

	public static final Item battlemage_helmet_ice = placeholder();
	public static final Item battlemage_chestplate_ice = placeholder();
	public static final Item battlemage_leggings_ice = placeholder();
	public static final Item battlemage_boots_ice = placeholder();

	public static final Item battlemage_helmet_lightning = placeholder();
	public static final Item battlemage_chestplate_lightning = placeholder();
	public static final Item battlemage_leggings_lightning = placeholder();
	public static final Item battlemage_boots_lightning = placeholder();

	public static final Item battlemage_helmet_necromancy = placeholder();
	public static final Item battlemage_chestplate_necromancy = placeholder();
	public static final Item battlemage_leggings_necromancy = placeholder();
	public static final Item battlemage_boots_necromancy = placeholder();

	public static final Item battlemage_helmet_earth = placeholder();
	public static final Item battlemage_chestplate_earth = placeholder();
	public static final Item battlemage_leggings_earth = placeholder();
	public static final Item battlemage_boots_earth = placeholder();

	public static final Item battlemage_helmet_sorcery = placeholder();
	public static final Item battlemage_chestplate_sorcery = placeholder();
	public static final Item battlemage_leggings_sorcery = placeholder();
	public static final Item battlemage_boots_sorcery = placeholder();

	public static final Item battlemage_helmet_healing = placeholder();
	public static final Item battlemage_chestplate_healing = placeholder();
	public static final Item battlemage_leggings_healing = placeholder();
	public static final Item battlemage_boots_healing = placeholder();
	public static final Item warlock_hood = placeholder();
	public static final Item warlock_robe = placeholder();
	public static final Item warlock_leggings = placeholder();
	public static final Item warlock_boots = placeholder();

	public static final Item warlock_hood_fire = placeholder();
	public static final Item warlock_robe_fire = placeholder();
	public static final Item warlock_leggings_fire = placeholder();
	public static final Item warlock_boots_fire = placeholder();

	public static final Item warlock_hood_ice = placeholder();
	public static final Item warlock_robe_ice = placeholder();
	public static final Item warlock_leggings_ice = placeholder();
	public static final Item warlock_boots_ice = placeholder();

	public static final Item warlock_hood_lightning = placeholder();
	public static final Item warlock_robe_lightning = placeholder();
	public static final Item warlock_leggings_lightning = placeholder();
	public static final Item warlock_boots_lightning = placeholder();

	public static final Item warlock_hood_necromancy = placeholder();
	public static final Item warlock_robe_necromancy = placeholder();
	public static final Item warlock_leggings_necromancy = placeholder();
	public static final Item warlock_boots_necromancy = placeholder();

	public static final Item warlock_hood_earth = placeholder();
	public static final Item warlock_robe_earth = placeholder();
	public static final Item warlock_leggings_earth = placeholder();
	public static final Item warlock_boots_earth = placeholder();

	public static final Item warlock_hood_sorcery = placeholder();
	public static final Item warlock_robe_sorcery = placeholder();
	public static final Item warlock_leggings_sorcery = placeholder();
	public static final Item warlock_boots_sorcery = placeholder();

	public static final Item warlock_hood_healing = placeholder();
	public static final Item warlock_robe_healing = placeholder();
	public static final Item warlock_leggings_healing = placeholder();
	public static final Item warlock_boots_healing = placeholder();


	public static final Item spectral_helmet = placeholder();
	public static final Item spectral_chestplate = placeholder();
	public static final Item spectral_leggings = placeholder();
	public static final Item spectral_boots = placeholder();

	public static final Item lightning_hammer = placeholder();
	public static final Item flamecatcher = placeholder();

	public static final Item ring_condensing = placeholder();
	public static final Item ring_siphoning = placeholder();
	public static final Item ring_battlemage = placeholder();
	public static final Item ring_combustion = placeholder();
	public static final Item ring_fire_melee = placeholder();
	public static final Item ring_fire_biome = placeholder();
	public static final Item ring_disintegration = placeholder();
	public static final Item ring_meteor = placeholder();
	public static final Item ring_ice_melee = placeholder();
	public static final Item ring_ice_biome = placeholder();
	public static final Item ring_arcane_frost = placeholder();
	public static final Item ring_shattering = placeholder();
	public static final Item ring_lightning_melee = placeholder();
	public static final Item ring_storm = placeholder();
	public static final Item ring_seeking = placeholder();
	public static final Item ring_hammer = placeholder();
	public static final Item ring_stormcloud = placeholder();
	public static final Item ring_soulbinding = placeholder();
	public static final Item ring_leeching = placeholder();
	public static final Item ring_necromancy_melee = placeholder();
	public static final Item ring_mind_control = placeholder();
	public static final Item ring_poison = placeholder();
	public static final Item ring_earth_melee = placeholder();
	public static final Item ring_earth_biome = placeholder();
	public static final Item ring_full_moon = placeholder();
	public static final Item ring_evoker = placeholder();
	public static final Item ring_extraction = placeholder();
	public static final Item ring_mana_return = placeholder();
	public static final Item ring_blockwrangler = placeholder();
	public static final Item ring_conjurer = placeholder();
	public static final Item ring_defender = placeholder();
	public static final Item ring_paladin = placeholder();
	public static final Item ring_interdiction = placeholder();

	public static final Item amulet_arcane_defence = placeholder();
	public static final Item amulet_warding = placeholder();
	public static final Item amulet_wisdom = placeholder();
	public static final Item amulet_fire_protection = placeholder();
	public static final Item amulet_fire_cloaking = placeholder();
	public static final Item amulet_ice_immunity = placeholder();
	public static final Item amulet_ice_protection = placeholder();
	public static final Item amulet_frost_warding = placeholder();
	public static final Item amulet_potential = placeholder();
	public static final Item amulet_channeling = placeholder();
	public static final Item amulet_lich = placeholder();
	public static final Item amulet_wither_immunity = placeholder();
	public static final Item amulet_glide = placeholder();
	public static final Item amulet_banishing = placeholder();
	public static final Item amulet_anchoring = placeholder();
	public static final Item amulet_recovery = placeholder();
	public static final Item amulet_transience = placeholder();
	public static final Item amulet_resurrection = placeholder();
	public static final Item amulet_auto_shield = placeholder();
	public static final Item amulet_absorption = placeholder();

	public static final Item charm_haggler = placeholder();
	public static final Item charm_experience_tome = placeholder();
	public static final Item charm_move_speed = placeholder();
	public static final Item charm_spell_discovery = placeholder();
	public static final Item charm_auto_smelt = placeholder();
	public static final Item charm_lava_walking = placeholder();
	public static final Item charm_storm = placeholder();
	public static final Item charm_minion_health = placeholder();
	public static final Item charm_minion_variants = placeholder();
	public static final Item charm_undead_helmets = placeholder();
	public static final Item charm_hunger_casting = placeholder();
	public static final Item charm_flight = placeholder();
	public static final Item charm_growth = placeholder();
	public static final Item charm_abseiling = placeholder();
	public static final Item charm_silk_touch = placeholder();
	public static final Item charm_sixth_sense = placeholder();
	public static final Item charm_stop_time = placeholder();
	public static final Item charm_light = placeholder();
	public static final Item charm_transportation = placeholder();
	public static final Item charm_black_hole = placeholder();
	public static final Item charm_mount_teleporting = placeholder();
	public static final Item charm_feeding = placeholder();

	/**
	 * Helper method to return the appropriate wand based on tier and element. As of Wizardry 4.3.3, this delegates to
	 * the cleaner replacement method {@link ItemWand#getWand(Tier, Element)}, which accesses the item registry
	 * dynamically by generating the registry name on the fly.
	 *
	 * @param tier The tier of the wand required.
	 * @param element The element of the wand required. Null will be converted to {@link Element#MAGIC}.
	 * @return The wand item which corresponds to the given tier and element, or null if no such item exists.
	 * @throws NullPointerException if the given tier is null.
	 * @deprecated This is being phased out; it is now only used for wizard gear and trades. To add an item to charging
	 * recipes, use {@link WizardryRecipes#addToManaFlaskCharging(Item)}. If you absolutely must use this method, usages
	 * may be replaced with {@link ItemWand#getWand(Tier, Element)}. However, this should only be done in cases where it
	 * is appropriate to ignore all wands that are not in the base mod.
	 */
	@Deprecated
	public static Item getWand(Tier tier, Element element){
		return ItemWand.getWand(tier, element); // Delegate to replacement method
	}

	/**
	 * Helper method to return the appropriate armour item based on element and slot. As of Wizardry 4.3.3, this
	 * delegates to the cleaner replacement method {@link ItemWizardArmour#getArmour(Element, ArmourClass, EntityEquipmentSlot)},
	 * which accesses the item registry dynamically by generating the registry name on the fly.
	 * <p></p>
	 * <i>This method only returns the original, basic armour set ({@link ArmourClass#WIZARD}).</i>
	 *
	 * @param element The EnumElement of the armour required. Null will be converted to {@link Element#MAGIC}.
	 * @param slot EntityEquipmentSlot of the armour piece required
	 * @return The armour item which corresponds to the given element and slot, or null if no such item exists.
	 * @throws IllegalArgumentException if the given slot is not an armour slot.
	 * @deprecated This is being phased out; it is now only used for wizard gear and trades. To add an item to
	 * charging recipes, use {@link WizardryRecipes#addToManaFlaskCharging(Item)}. If you absolutely must use this
	 * method, usages may be replaced with {@link ItemWand#getWand(Tier, Element)}. However, this should only be done in
	 * cases where it is appropriate to ignore all armour that is not in the base mod.
	 */
	@Deprecated
	public static Item getArmour(Element element, EntityEquipmentSlot slot){
		return ItemWizardArmour.getArmour(element, ArmourClass.WIZARD, slot); // Delegate to replacement method
	}

	/**
	 * Sets both the registry and unlocalised names of the given item, then registers it with the given registry. Use
	 * this instead of {@link Item#setRegistryName(String)} and {@link Item#setTranslationKey(String)} during
	 * construction, for convenience and consistency. As of wizardry 4.2, this also automatically adds it to the order
	 * list for its creative tab if that tab is a {@link CreativeTabListed}, meaning the order can be defined simply
	 * by the order in which the items are registered in this class.
	 *
	 * @param registry The registry to register the given item to.
	 * @param name The name of the item, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code item.ebwizardry:[name].name}.
	 * @param item The item to register.
	 */
	// It now makes sense to have the name first, since it's shorter than an entire item declaration.
	public static void registerItem(IForgeRegistry<Item> registry, String name, Item item){
		registerItem(registry, name, item, false);
	}

	/**
	 * Sets both the registry and unlocalised names of the given item, then registers it with the given registry. Use
	 * this instead of {@link Item#setRegistryName(String)} and {@link Item#setTranslationKey(String)} during
	 * construction, for convenience and consistency. As of wizardry 4.2, this also automatically adds it to the order
	 * list for its creative tab if that tab is a {@link CreativeTabListed}, meaning the order can be defined simply
	 * by the order in which the items are registered in this class.
	 *
	 * @param registry The registry to register the given item to.
	 * @param name The name of the item, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code item.ebwizardry:[name].name}.
	 * @param item The item to register.
	 * @param setTabIcon True to set this item as the icon for its creative tab.
	 */
	// It now makes sense to have the name first, since it's shorter than an entire item declaration.
	public static void registerItem(IForgeRegistry<Item> registry, String name, Item item, boolean setTabIcon){

		item.setRegistryName(Wizardry.MODID, name);
		item.setTranslationKey(item.getRegistryName().toString());
		registry.register(item);

		if(setTabIcon && item.getCreativeTab() instanceof CreativeTabSorted){
			((CreativeTabSorted)item.getCreativeTab()).setIconItem(new ItemStack(item));
		}

		if(item.getCreativeTab() instanceof CreativeTabListed){
			((CreativeTabListed)item.getCreativeTab()).order.add(item);
		}
	}

	/** Registers an ItemBlock for the given block, with the same registry name as that block. As of wizardry 4.2, this
	 * also automatically adds it to the order list for its creative tab if that tab is a {@link CreativeTabListed},
	 * meaning the order can be defined simply by the order in which the items are registered in this class. */
	private static void registerItemBlock(IForgeRegistry<Item> registry, Block block){
		registerItemBlock(registry, block, new ItemBlock(block));
	}

	/** Registers the given ItemBlock for the given block, with the same registry name as that block. This
	 * also automatically adds it to the order list for its creative tab if that tab is a {@link CreativeTabListed},
	 * meaning the order can be defined simply by the order in which the items are registered in this class. */
	private static void registerItemBlock(IForgeRegistry<Item> registry, Block block, ItemBlock itemblock){
		// We don't need to keep a reference to the ItemBlock
		itemblock.setRegistryName(block.getRegistryName());
		registry.register(itemblock);

		if(block.getCreativeTab() instanceof CreativeTabListed){
			((CreativeTabListed)block.getCreativeTab()).order.add(itemblock);
		}
	}

	private static void registerMultiTexturedItemBlock(IForgeRegistry<Item> registry, Block block, boolean separateNames, String... prefixes){
		// We don't need to keep a reference to the ItemBlock
		Item itemblock = new ItemBlockMultiTextured(block, separateNames, prefixes).setRegistryName(block.getRegistryName());
		registry.register(itemblock);

		if(block.getCreativeTab() instanceof CreativeTabListed){
			((CreativeTabListed)block.getCreativeTab()).order.add(itemblock);
		}
	}


	@SubscribeEvent
	public static void register(RegistryEvent.Register<Item> event){

		IForgeRegistry<Item> registry = event.getRegistry();

		// ItemBlocks

		// Not all blocks need an ItemBlock
		registerItemBlock(registry, WizardryBlocks.arcane_workbench);
		registerItemBlock(registry, WizardryBlocks.crystal_ore);
		registerItemBlock(registry, WizardryBlocks.crystal_flower, new ItemCrystalFlower());
		registerItemBlock(registry, WizardryBlocks.transportation_stone);

		String[] elements = Arrays.stream(Element.values()).map(Element::getName).toArray(String[]::new);
		String[] woodTypes = Arrays.stream(BlockPlanks.EnumType.values()).map(BlockPlanks.EnumType::getName).toArray(String[]::new);

		registerItemBlock(registry, WizardryBlocks.magic_crystal_block);
		registerItemBlock(registry, WizardryBlocks.fire_crystal_block);
		registerItemBlock(registry, WizardryBlocks.ice_crystal_block);
		registerItemBlock(registry, WizardryBlocks.lightning_crystal_block);
		registerItemBlock(registry, WizardryBlocks.necromancy_crystal_block);
		registerItemBlock(registry, WizardryBlocks.earth_crystal_block);
		registerItemBlock(registry, WizardryBlocks.sorcery_crystal_block);
		registerItemBlock(registry, WizardryBlocks.healing_crystal_block);

		registerItemBlock(registry, WizardryBlocks.fire_runestone);
		registerItemBlock(registry, WizardryBlocks.ice_runestone);
		registerItemBlock(registry, WizardryBlocks.lightning_runestone);
		registerItemBlock(registry, WizardryBlocks.necromancy_runestone);
		registerItemBlock(registry, WizardryBlocks.earth_runestone);
		registerItemBlock(registry, WizardryBlocks.sorcery_runestone);
		registerItemBlock(registry, WizardryBlocks.healing_runestone);

		registerMultiTexturedItemBlock(registry, WizardryBlocks.gilded_wood, true, woodTypes);

		registerItemBlock(registry, WizardryBlocks.oak_bookshelf);
		registerItemBlock(registry, WizardryBlocks.spruce_bookshelf);
		registerItemBlock(registry, WizardryBlocks.birch_bookshelf);
		registerItemBlock(registry, WizardryBlocks.jungle_bookshelf);
		registerItemBlock(registry, WizardryBlocks.acacia_bookshelf);
		registerItemBlock(registry, WizardryBlocks.dark_oak_bookshelf);

		registerItemBlock(registry, WizardryBlocks.oak_lectern);
		registerItemBlock(registry, WizardryBlocks.spruce_lectern);
		registerItemBlock(registry, WizardryBlocks.birch_lectern);
		registerItemBlock(registry, WizardryBlocks.jungle_lectern);
		registerItemBlock(registry, WizardryBlocks.acacia_lectern);
		registerItemBlock(registry, WizardryBlocks.dark_oak_lectern);

		registerItemBlock(registry, WizardryBlocks.receptacle);
		registerItemBlock(registry, WizardryBlocks.imbuement_altar);

		// Items

		registerItem(registry, "magic_crystal",					new ItemCrystal(Element.MAGIC));
		registerItem(registry, "earth_crystal",					new ItemCrystal(Element.EARTH));
		registerItem(registry, "fire_crystal",					new ItemCrystal(Element.FIRE));
		registerItem(registry, "healing_crystal",					new ItemCrystal(Element.HEALING));
		registerItem(registry, "ice_crystal",						new ItemCrystal(Element.ICE));
		registerItem(registry, "lightning_crystal",				new ItemCrystal(Element.LIGHTNING));
		registerItem(registry, "necromancy_crystal",				new ItemCrystal(Element.NECROMANCY));
		registerItem(registry, "sorcery_crystal",					new ItemCrystal(Element.SORCERY));

		registerItem(registry, "crystal_shard", 					new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "grand_crystal", 					new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "wizard_handbook", 				new ItemWizardHandbook(), true);
		registerItem(registry, "arcane_tome_apprentice", 			new ItemArcaneTome(EnumRarity.UNCOMMON, Tier.APPRENTICE));
		registerItem(registry, "arcane_tome_advanced", 			new ItemArcaneTome(EnumRarity.RARE, Tier.ADVANCED));
		registerItem(registry, "arcane_tome_master", 				new ItemArcaneTome(EnumRarity.EPIC, Tier.MASTER));
		registerItem(registry, "spell_book", 						new ItemSpellBook(), true);
		registerItem(registry, "scroll", 							new ItemScroll());
		registerItem(registry, "ruined_spell_book", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY).setMaxStackSize(16));

		registerItem(registry, "magic_wand", 						new ItemWand(Tier.NOVICE, null));
		registerItem(registry, "apprentice_wand", 				new ItemWand(Tier.APPRENTICE, null));
		registerItem(registry, "advanced_wand", 					new ItemWand(Tier.ADVANCED, null));
		registerItem(registry, "master_wand", 					new ItemWand(Tier.MASTER, null));

		registerItem(registry, "novice_fire_wand", 				new ItemWand(Tier.NOVICE, Element.FIRE));
		registerItem(registry, "apprentice_fire_wand", 			new ItemWand(Tier.APPRENTICE, Element.FIRE));
		registerItem(registry, "advanced_fire_wand", 				new ItemWand(Tier.ADVANCED, Element.FIRE));
		registerItem(registry, "master_fire_wand", 				new ItemWand(Tier.MASTER, Element.FIRE));

		registerItem(registry, "novice_ice_wand", 				new ItemWand(Tier.NOVICE, Element.ICE));
		registerItem(registry, "apprentice_ice_wand", 			new ItemWand(Tier.APPRENTICE, Element.ICE));
		registerItem(registry, "advanced_ice_wand", 				new ItemWand(Tier.ADVANCED, Element.ICE));
		registerItem(registry, "master_ice_wand", 				new ItemWand(Tier.MASTER, Element.ICE));

		registerItem(registry, "novice_lightning_wand", 			new ItemWand(Tier.NOVICE, Element.LIGHTNING));
		registerItem(registry, "apprentice_lightning_wand", 		new ItemWand(Tier.APPRENTICE, Element.LIGHTNING));
		registerItem(registry, "advanced_lightning_wand", 		new ItemWand(Tier.ADVANCED, Element.LIGHTNING));
		registerItem(registry, "master_lightning_wand", 			new ItemWand(Tier.MASTER, Element.LIGHTNING));

		registerItem(registry, "novice_necromancy_wand", 			new ItemWand(Tier.NOVICE, Element.NECROMANCY));
		registerItem(registry, "apprentice_necromancy_wand", 		new ItemWand(Tier.APPRENTICE, Element.NECROMANCY));
		registerItem(registry, "advanced_necromancy_wand", 		new ItemWand(Tier.ADVANCED, Element.NECROMANCY));
		registerItem(registry, "master_necromancy_wand", 			new ItemWand(Tier.MASTER, Element.NECROMANCY));

		registerItem(registry, "novice_earth_wand", 				new ItemWand(Tier.NOVICE, Element.EARTH));
		registerItem(registry, "apprentice_earth_wand", 			new ItemWand(Tier.APPRENTICE, Element.EARTH));
		registerItem(registry, "advanced_earth_wand", 			new ItemWand(Tier.ADVANCED, Element.EARTH));
		registerItem(registry, "master_earth_wand", 				new ItemWand(Tier.MASTER, Element.EARTH));

		registerItem(registry, "novice_sorcery_wand", 			new ItemWand(Tier.NOVICE, Element.SORCERY));
		registerItem(registry, "apprentice_sorcery_wand", 		new ItemWand(Tier.APPRENTICE, Element.SORCERY));
		registerItem(registry, "advanced_sorcery_wand", 			new ItemWand(Tier.ADVANCED, Element.SORCERY));
		registerItem(registry, "master_sorcery_wand", 			new ItemWand(Tier.MASTER, Element.SORCERY));

		registerItem(registry, "novice_healing_wand", 			new ItemWand(Tier.NOVICE, Element.HEALING));
		registerItem(registry, "apprentice_healing_wand", 		new ItemWand(Tier.APPRENTICE, Element.HEALING));
		registerItem(registry, "advanced_healing_wand", 			new ItemWand(Tier.ADVANCED, Element.HEALING));
		registerItem(registry, "master_healing_wand", 			new ItemWand(Tier.MASTER, Element.HEALING));

		registerItem(registry, "spectral_sword", 					new ItemSpectralSword(ToolMaterial.IRON));
		registerItem(registry, "spectral_pickaxe", 				new ItemSpectralPickaxe(ToolMaterial.IRON));
		registerItem(registry, "spectral_bow", 					new ItemSpectralBow());

		registerItem(registry, "blank_scroll",					new ItemBlankScroll());
		registerItem(registry, "magic_silk", 						new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "small_mana_flask", 				new ItemManaFlask(ItemManaFlask.Size.SMALL));
		registerItem(registry, "medium_mana_flask", 				new ItemManaFlask(ItemManaFlask.Size.MEDIUM));
		registerItem(registry, "large_mana_flask", 				new ItemManaFlask(ItemManaFlask.Size.LARGE));

		registerItem(registry, "storage_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "siphon_upgrade", 					new ItemWandUpgrade());
		registerItem(registry, "condenser_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "range_upgrade", 					new ItemWandUpgrade());
		registerItem(registry, "duration_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "cooldown_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "blast_upgrade", 					new ItemWandUpgrade());
		registerItem(registry, "attunement_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "melee_upgrade", 					new ItemWandUpgrade());

		registerItem(registry, "flaming_axe", 					new ItemFlamingAxe(Materials.MAGICAL));
		registerItem(registry, "frost_axe",						new ItemFrostAxe(Materials.MAGICAL));

		registerItem(registry, "identification_scroll", 			new ItemIdentificationScroll());
		registerItem(registry, "resplendent_thread", 				new ItemArmourUpgrade());
		registerItem(registry, "crystal_silver_plating", 			new ItemArmourUpgrade());
		registerItem(registry, "ethereal_crystalweave", 			new ItemArmourUpgrade());
		registerItem(registry, "astral_diamond", 					new Item(){ @Override public EnumRarity getRarity(ItemStack stack){ return EnumRarity.RARE; }}.setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "purifying_elixir",				new ItemPurifyingElixir());

		registerItem(registry, "firebomb", 						new ItemFirebomb());
		registerItem(registry, "poison_bomb", 					new ItemPoisonBomb());
		registerItem(registry, "smoke_bomb", 						new ItemSmokeBomb());
		registerItem(registry, "spark_bomb", 						new ItemSparkBomb());

		registerItem(registry, "spectral_dust_earth",		 		new ItemSpectralDust());
		registerItem(registry, "spectral_dust_fire",		 		new ItemSpectralDust());
		registerItem(registry, "spectral_dust_healing",		 	new ItemSpectralDust());
		registerItem(registry, "spectral_dust_ice",		 		new ItemSpectralDust());
		registerItem(registry, "spectral_dust_lightning",		 	new ItemSpectralDust());
		registerItem(registry, "spectral_dust_necromancy",	 	new ItemSpectralDust());
		registerItem(registry, "spectral_dust_sorcery",		 	new ItemSpectralDust());

		registerItem(registry, "wizard_hat", 						new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, null), true);
		registerItem(registry, "wizard_robe", 					new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, null));
		registerItem(registry, "wizard_leggings", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, null));
		registerItem(registry, "wizard_boots", 					new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, null));

		registerItem(registry, "wizard_hat_fire", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.FIRE));
		registerItem(registry, "wizard_robe_fire", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.FIRE));
		registerItem(registry, "wizard_leggings_fire", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.FIRE));
		registerItem(registry, "wizard_boots_fire", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.FIRE));

		registerItem(registry, "wizard_hat_ice", 					new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.ICE));
		registerItem(registry, "wizard_robe_ice", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.ICE));
		registerItem(registry, "wizard_leggings_ice", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.ICE));
		registerItem(registry, "wizard_boots_ice", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.ICE));

		registerItem(registry, "wizard_hat_lightning", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.LIGHTNING));
		registerItem(registry, "wizard_robe_lightning", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.LIGHTNING));
		registerItem(registry, "wizard_leggings_lightning", 		new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.LIGHTNING));
		registerItem(registry, "wizard_boots_lightning", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.LIGHTNING));

		registerItem(registry, "wizard_hat_necromancy", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.NECROMANCY));
		registerItem(registry, "wizard_robe_necromancy", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.NECROMANCY));
		registerItem(registry, "wizard_leggings_necromancy", 		new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.NECROMANCY));
		registerItem(registry, "wizard_boots_necromancy", 		new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.NECROMANCY));

		registerItem(registry, "wizard_hat_earth", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.EARTH));
		registerItem(registry, "wizard_robe_earth", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.EARTH));
		registerItem(registry, "wizard_leggings_earth", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.EARTH));
		registerItem(registry, "wizard_boots_earth", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.EARTH));

		registerItem(registry, "wizard_hat_sorcery", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.SORCERY));
		registerItem(registry, "wizard_robe_sorcery", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.SORCERY));
		registerItem(registry, "wizard_leggings_sorcery", 		new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.SORCERY));
		registerItem(registry, "wizard_boots_sorcery", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.SORCERY));

		registerItem(registry, "wizard_hat_healing", 				new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.HEAD, Element.HEALING));
		registerItem(registry, "wizard_robe_healing", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.CHEST, Element.HEALING));
		registerItem(registry, "wizard_leggings_healing", 		new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.LEGS, Element.HEALING));
		registerItem(registry, "wizard_boots_healing", 			new ItemWizardArmour(ArmourClass.WIZARD, EntityEquipmentSlot.FEET, Element.HEALING));

		registerItem(registry, "sage_hat", 						new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, null));
		registerItem(registry, "sage_robe", 						new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, null));
		registerItem(registry, "sage_leggings", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, null));
		registerItem(registry, "sage_boots", 						new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, null));

		registerItem(registry, "sage_hat_fire", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.FIRE));
		registerItem(registry, "sage_robe_fire", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.FIRE));
		registerItem(registry, "sage_leggings_fire", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.FIRE));
		registerItem(registry, "sage_boots_fire", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.FIRE));

		registerItem(registry, "sage_hat_ice", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.ICE));
		registerItem(registry, "sage_robe_ice", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.ICE));
		registerItem(registry, "sage_leggings_ice", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.ICE));
		registerItem(registry, "sage_boots_ice", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.ICE));

		registerItem(registry, "sage_hat_lightning", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.LIGHTNING));
		registerItem(registry, "sage_robe_lightning", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.LIGHTNING));
		registerItem(registry, "sage_leggings_lightning", 		new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.LIGHTNING));
		registerItem(registry, "sage_boots_lightning", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.LIGHTNING));

		registerItem(registry, "sage_hat_necromancy", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.NECROMANCY));
		registerItem(registry, "sage_robe_necromancy", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.NECROMANCY));
		registerItem(registry, "sage_leggings_necromancy", 		new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.NECROMANCY));
		registerItem(registry, "sage_boots_necromancy", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.NECROMANCY));

		registerItem(registry, "sage_hat_earth", 					new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.EARTH));
		registerItem(registry, "sage_robe_earth", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.EARTH));
		registerItem(registry, "sage_leggings_earth", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.EARTH));
		registerItem(registry, "sage_boots_earth", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.EARTH));

		registerItem(registry, "sage_hat_sorcery", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.SORCERY));
		registerItem(registry, "sage_robe_sorcery", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.SORCERY));
		registerItem(registry, "sage_leggings_sorcery", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.SORCERY));
		registerItem(registry, "sage_boots_sorcery", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.SORCERY));

		registerItem(registry, "sage_hat_healing", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.HEAD, Element.HEALING));
		registerItem(registry, "sage_robe_healing", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.CHEST, Element.HEALING));
		registerItem(registry, "sage_leggings_healing", 			new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.LEGS, Element.HEALING));
		registerItem(registry, "sage_boots_healing", 				new ItemWizardArmour(ArmourClass.SAGE, EntityEquipmentSlot.FEET, Element.HEALING));

		registerItem(registry, "battlemage_helmet", 				new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, null));
		registerItem(registry, "battlemage_chestplate", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, null));
		registerItem(registry, "battlemage_leggings", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, null));
		registerItem(registry, "battlemage_boots", 				new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, null));

		registerItem(registry, "battlemage_helmet_fire", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.FIRE));
		registerItem(registry, "battlemage_chestplate_fire", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.FIRE));
		registerItem(registry, "battlemage_leggings_fire", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.FIRE));
		registerItem(registry, "battlemage_boots_fire", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.FIRE));

		registerItem(registry, "battlemage_helmet_ice", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.ICE));
		registerItem(registry, "battlemage_chestplate_ice", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.ICE));
		registerItem(registry, "battlemage_leggings_ice", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.ICE));
		registerItem(registry, "battlemage_boots_ice", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.ICE));

		registerItem(registry, "battlemage_helmet_lightning", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.LIGHTNING));
		registerItem(registry, "battlemage_chestplate_lightning", new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.LIGHTNING));
		registerItem(registry, "battlemage_leggings_lightning", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.LIGHTNING));
		registerItem(registry, "battlemage_boots_lightning", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.LIGHTNING));

		registerItem(registry, "battlemage_helmet_necromancy", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.NECROMANCY));
		registerItem(registry, "battlemage_chestplate_necromancy", new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.NECROMANCY));
		registerItem(registry, "battlemage_leggings_necromancy", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.NECROMANCY));
		registerItem(registry, "battlemage_boots_necromancy", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.NECROMANCY));

		registerItem(registry, "battlemage_helmet_earth", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.EARTH));
		registerItem(registry, "battlemage_chestplate_earth", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.EARTH));
		registerItem(registry, "battlemage_leggings_earth", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.EARTH));
		registerItem(registry, "battlemage_boots_earth", 			new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.EARTH));

		registerItem(registry, "battlemage_helmet_sorcery", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.SORCERY));
		registerItem(registry, "battlemage_chestplate_sorcery", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.SORCERY));
		registerItem(registry, "battlemage_leggings_sorcery", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.SORCERY));
		registerItem(registry, "battlemage_boots_sorcery", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.SORCERY));

		registerItem(registry, "battlemage_helmet_healing", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.HEAD, Element.HEALING));
		registerItem(registry, "battlemage_chestplate_healing", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.CHEST, Element.HEALING));
		registerItem(registry, "battlemage_leggings_healing", 	new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.LEGS, Element.HEALING));
		registerItem(registry, "battlemage_boots_healing", 		new ItemWizardArmour(ArmourClass.BATTLEMAGE, EntityEquipmentSlot.FEET, Element.HEALING));

		registerItem(registry, "warlock_hood", 					new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, null));
		registerItem(registry, "warlock_robe", 					new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, null));
		registerItem(registry, "warlock_leggings", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, null));
		registerItem(registry, "warlock_boots", 					new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, null));

		registerItem(registry, "warlock_hood_fire", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.FIRE));
		registerItem(registry, "warlock_robe_fire", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.FIRE));
		registerItem(registry, "warlock_leggings_fire", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.FIRE));
		registerItem(registry, "warlock_boots_fire", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.FIRE));

		registerItem(registry, "warlock_hood_ice", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.ICE));
		registerItem(registry, "warlock_robe_ice", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.ICE));
		registerItem(registry, "warlock_leggings_ice", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.ICE));
		registerItem(registry, "warlock_boots_ice", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.ICE));

		registerItem(registry, "warlock_hood_lightning", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.LIGHTNING));
		registerItem(registry, "warlock_robe_lightning", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.LIGHTNING));
		registerItem(registry, "warlock_leggings_lightning", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.LIGHTNING));
		registerItem(registry, "warlock_boots_lightning", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.LIGHTNING));

		registerItem(registry, "warlock_hood_necromancy", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.NECROMANCY));
		registerItem(registry, "warlock_robe_necromancy", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.NECROMANCY));
		registerItem(registry, "warlock_leggings_necromancy", 	new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.NECROMANCY));
		registerItem(registry, "warlock_boots_necromancy", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.NECROMANCY));

		registerItem(registry, "warlock_hood_earth", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.EARTH));
		registerItem(registry, "warlock_robe_earth", 				new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.EARTH));
		registerItem(registry, "warlock_leggings_earth", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.EARTH));
		registerItem(registry, "warlock_boots_earth", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.EARTH));

		registerItem(registry, "warlock_hood_sorcery", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.SORCERY));
		registerItem(registry, "warlock_robe_sorcery", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.SORCERY));
		registerItem(registry, "warlock_leggings_sorcery", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.SORCERY));
		registerItem(registry, "warlock_boots_sorcery", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.SORCERY));

		registerItem(registry, "warlock_hood_healing", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.HEAD, Element.HEALING));
		registerItem(registry, "warlock_robe_healing", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.CHEST, Element.HEALING));
		registerItem(registry, "warlock_leggings_healing", 		new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.LEGS, Element.HEALING));
		registerItem(registry, "warlock_boots_healing", 			new ItemWizardArmour(ArmourClass.WARLOCK, EntityEquipmentSlot.FEET, Element.HEALING));

		registerItem(registry, "spectral_helmet", 				new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.HEAD));
		registerItem(registry, "spectral_chestplate", 			new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.CHEST));
		registerItem(registry, "spectral_leggings", 				new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.LEGS));
		registerItem(registry, "spectral_boots", 					new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.FEET));

		registerItem(registry, "lightning_hammer", 				new ItemLightningHammer());
		registerItem(registry, "flamecatcher", 					new ItemFlamecatcher());

		registerItem(registry, "ring_condensing", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_siphoning", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_battlemage", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_combustion", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_fire_melee", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_fire_biome", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_disintegration", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_meteor", 					new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_ice_melee", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_ice_biome", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_arcane_frost", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_shattering", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_lightning_melee", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_storm", 						new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_seeking", 					new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_hammer", 					new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_stormcloud", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_soulbinding", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_leeching", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_necromancy_melee", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_mind_control", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_poison", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_earth_melee", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_earth_biome", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_full_moon", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_evoker", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_extraction", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_mana_return", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_blockwrangler", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_conjurer", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_defender", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.RING));
		registerItem(registry, "ring_paladin", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.RING));
		registerItem(registry, "ring_interdiction", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.RING));

		registerItem(registry, "amulet_arcane_defence", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_warding", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_wisdom", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_fire_protection", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_fire_cloaking", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_ice_immunity", 			new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_ice_protection", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_frost_warding", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_potential", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_channeling", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_lich", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_wither_immunity", 			new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_glide", 					new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_banishing", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_anchoring", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_recovery", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_transience", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_resurrection", 			new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_auto_shield", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_absorption", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.AMULET));

		registerItem(registry, "charm_haggler", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_experience_tome", 			new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_move_speed", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_spell_discovery", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_auto_smelt", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_lava_walking", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_storm", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_minion_health", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_minion_variants", 			new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_undead_helmets", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_hunger_casting", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_flight", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_growth", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_abseiling", 				new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_silk_touch", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_sixth_sense", 				new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_stop_time", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_light", 					new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_transportation", 			new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_black_hole", 				new ItemArtefact(EnumRarity.EPIC, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_mount_teleporting", 		new ItemArtefact(EnumRarity.RARE, 		ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_feeding", 					new ItemArtefact(EnumRarity.UNCOMMON, 	ItemArtefact.Type.CHARM));

	}

	/** Called from init() in the main mod class to register wizardry's dispenser behaviours. */
	public static void registerDispenseBehaviours(){

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(firebomb, new BehaviorProjectileDispense(){

			@Override
			protected IProjectile getProjectileEntity(World world, IPosition position, ItemStack stack){
				EntityFirebomb entity = new EntityFirebomb(world);
				entity.setPosition(position.getX(), position.getY(), position.getZ());
				return entity;
			}

		});

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(poison_bomb, new BehaviorProjectileDispense(){

			@Override
			protected IProjectile getProjectileEntity(World world, IPosition position, ItemStack stack){
				EntityPoisonBomb entity = new EntityPoisonBomb(world);
				entity.setPosition(position.getX(), position.getY(), position.getZ());
				return entity;
			}

		});

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(smoke_bomb, new BehaviorProjectileDispense(){

			@Override
			protected IProjectile getProjectileEntity(World world, IPosition position, ItemStack stack){
				EntitySmokeBomb entity = new EntitySmokeBomb(world);
				entity.setPosition(position.getX(), position.getY(), position.getZ());
				return entity;
			}

		});

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(spark_bomb, new BehaviorProjectileDispense(){

			@Override
			protected IProjectile getProjectileEntity(World world, IPosition position, ItemStack stack){
				EntitySparkBomb entity = new EntitySparkBomb(world);
				entity.setPosition(position.getX(), position.getY(), position.getZ());
				return entity;
			}

		});

		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scroll, new BehaviourSpellDispense());

//		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(spectral_dust, new Bootstrap.BehaviorDispenseOptional(){
//
//			// TODO: Needs packets of some kind, may be able to get away with using the tile entity sync packet
//
//			@Override
//			protected ItemStack dispenseStack(IBlockSource source, ItemStack stack){
//
//				World world = source.getWorld();
//				EnumFacing direction = source.getBlockState().getValue(BlockDispenser.FACING);
//				BlockPos pos = source.getBlockPos().offset(direction);
//				TileEntity tileEntity = world.getTileEntity(pos);
//
//				if(tileEntity instanceof TileEntityReceptacle && ((TileEntityReceptacle)tileEntity).getElement() == null){
//					((TileEntityReceptacle)tileEntity).setElement(Element.values()[stack.getMetadata()]);
//					stack.shrink(1);
//					world.checkLight(pos);
//					// TESTME: Do we need this?
//					world.notifyBlockUpdate(pos, source.getBlockState(), source.getBlockState(), 3);
//					return stack;
//				}
//
//				return super.dispenseStack(source, stack);
//			}
//
//			@Override
//			protected void playDispenseSound(IBlockSource source){
//				EnumFacing direction = source.getBlockState().getValue(BlockDispenser.FACING);
//				BlockPos pos = source.getBlockPos().offset(direction);
//				source.getWorld().playSound(pos.getX(), pos.getY(), pos.getZ(), WizardrySounds.BLOCK_RECEPTACLE_IGNITE,
//						SoundCategory.BLOCKS, 0.7f, 0.7f, false);
//			}
//		});

	}

	/** Called from init() in the main mod class to register wizardry's banner patterns. */
	public static void registerBannerPatterns(){
		for(Element element : Element.values()){
			if (element != Element.MAGIC) {
				addBannerPattern("WIZARDRY_" + element.getName().toUpperCase(Locale.ROOT),
						"wizardry_" + element.getName(), "eb" + element.getName().charAt(0),
						new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID, "crystal_" + element.name().toLowerCase()))));
			}
		}
	}

	private static void addBannerPattern(String codeName, String fileName, String hashName, ItemStack patternItem){
		EnumHelper.addEnum(BannerPattern.class, codeName, new Class[]{String.class, String.class, ItemStack.class}, fileName, hashName, patternItem);
	}

}