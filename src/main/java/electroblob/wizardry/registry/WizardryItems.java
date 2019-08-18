package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityPoisonBomb;
import electroblob.wizardry.entity.projectile.EntitySmokeBomb;
import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.item.*;
import electroblob.wizardry.misc.BehaviourSpellDispense;
import electroblob.wizardry.registry.WizardryTabs.CreativeTabListed;
import electroblob.wizardry.registry.WizardryTabs.CreativeTabSorted;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

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

		public static final ArmorMaterial SILK = EnumHelper.addArmorMaterial("SILK", "wizardry/textures/armour/wizard_armour",
		15, new int[]{0, 0, 0, 0}, 15, WizardrySounds.ITEM_ARMOUR_EQUIP_SILK, 0.0F);

	}

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder(){ return null; }

	// This is the most concise way I can think of to register the items. Really, I'd prefer it if there was only one
	// point where all the items were listed, but that's not possible within the current system unless you use an array,
	// which means you lose the individual fields...

	public static final Item magic_crystal = placeholder();

	public static final Item grand_crystal = placeholder();
	public static final Item crystal_shard = placeholder();

	public static final Item wizard_handbook = placeholder();
	public static final Item arcane_tome = placeholder();
	public static final Item spell_book = placeholder();
	public static final Item scroll = placeholder();

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
	public static final Item armour_upgrade = placeholder();
	public static final Item astral_diamond = placeholder();
	public static final Item purifying_elixir = placeholder();

	public static final Item firebomb = placeholder();
	public static final Item poison_bomb = placeholder();
	public static final Item smoke_bomb = placeholder();
	public static final Item spark_bomb = placeholder();

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

	public static final Item spectral_helmet = placeholder();
	public static final Item spectral_chestplate = placeholder();
	public static final Item spectral_leggings = placeholder();
	public static final Item spectral_boots = placeholder();

	public static final Item lightning_hammer = placeholder();

	public static final Item ring_condensing = placeholder();
	public static final Item ring_siphoning = placeholder();
	public static final Item ring_battlemage = placeholder();
	public static final Item ring_combustion = placeholder();
	public static final Item ring_fire_melee = placeholder();
	public static final Item ring_fire_biome = placeholder();
	public static final Item ring_disintegration = placeholder();
	public static final Item ring_ice_melee = placeholder();
	public static final Item ring_ice_biome = placeholder();
	public static final Item ring_arcane_frost = placeholder();
	public static final Item ring_shattering = placeholder();
	public static final Item ring_lightning_melee = placeholder();
	public static final Item ring_storm = placeholder();
	public static final Item ring_seeking = placeholder();
	public static final Item ring_hammer = placeholder();
	public static final Item ring_soulbinding = placeholder();
	public static final Item ring_leeching = placeholder();
	public static final Item ring_necromancy_melee = placeholder();
	public static final Item ring_mind_control = placeholder();
	public static final Item ring_poison = placeholder();
	public static final Item ring_earth_melee = placeholder();
	public static final Item ring_earth_biome = placeholder();
	public static final Item ring_full_moon = placeholder();
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

	public static final Item charm_haggler = placeholder();
	public static final Item charm_experience_tome = placeholder();
	public static final Item charm_auto_smelt = placeholder();
	public static final Item charm_lava_walking = placeholder();
	public static final Item charm_storm = placeholder();
	public static final Item charm_minion_health = placeholder();
	public static final Item charm_minion_variants = placeholder();
	public static final Item charm_flight = placeholder();
	public static final Item charm_growth = placeholder();
	public static final Item charm_abseiling = placeholder();
	public static final Item charm_silk_touch = placeholder();
	public static final Item charm_stop_time = placeholder();
	public static final Item charm_light = placeholder();
	public static final Item charm_transportation = placeholder();
	public static final Item charm_feeding = placeholder();

	private static final Map<Pair<Tier, Element>, Item> WAND_MAP = new HashMap<>();
	private static final Map<Pair<EntityEquipmentSlot, Element>, Item> ARMOUR_MAP = new HashMap<>();

	/**
	 * Helper method to return the appropriate wand based on tier and element. As of Wizardry 2.1, this uses the
	 * immutable map stored in {@link WizardryItems#WAND_MAP}. Currently used for upgrading wands, for chest generation
	 * and to iterate through wands for charging recipes.
	 *
	 * @param tier The tier of the wand required.
	 * @param element The element of the wand required. Null will be converted to {@link Element#MAGIC}.
	 * @return The wand item which corresponds to the given tier and element, or null if no such item exists.
	 * @throws NullPointerException if the given tier is null.
	 * @deprecated This is being phased out; it is now only used for wizard gear and trades. To add an item to
	 * charging recipes, use {@link WizardryRecipes#addToManaFlaskCharging(Item)}.
	 */
	@Deprecated
	public static Item getWand(Tier tier, Element element){
		if(tier == null) throw new NullPointerException("The given tier cannot be null.");
		if(element == null) element = Element.MAGIC;
		return WAND_MAP.get(ImmutablePair.of(tier, element));
	}

	/**
	 * Helper method to return the appropriate armour item based on element and slot. As of Wizardry 2.1, this uses the
	 * immutable map stored in {@link WizardryItems#ARMOUR_MAP}. Currently used to iterate through armour for
	 * registering charging recipes and for chest generation.
	 *
	 * @param element The EnumElement of the armour required. Null will be converted to {@link Element#MAGIC}.
	 * @param slot EntityEquipmentSlot of the armour piece required
	 * @return The armour item which corresponds to the given element and slot, or null if no such item exists.
	 * @throws IllegalArgumentException if the given slot is not an armour slot.
	 * @deprecated This is being phased out; it is now only used for wizard gear and trades. To add an item to
	 * charging recipes, use {@link WizardryRecipes#addToManaFlaskCharging(Item)}.
	 */
	@Deprecated
	public static Item getArmour(Element element, EntityEquipmentSlot slot){
		if(slot == null || slot.getSlotType() != Type.ARMOR)
			throw new IllegalArgumentException("Must be a valid armour slot");
		if(element == null) element = Element.MAGIC;
		return ARMOUR_MAP.get(ImmutablePair.of(slot, element));
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
		// We don't need to keep a reference to the ItemBlock
		Item itemblock = new ItemBlock(block).setRegistryName(block.getRegistryName());
		registry.register(itemblock);

		if(block.getCreativeTab() instanceof CreativeTabListed){
			((CreativeTabListed)block.getCreativeTab()).order.add(itemblock);
		}
	}

	private static void registerMultiTexturedItemBlock(IForgeRegistry<Item> registry, Block block, boolean separateNames){
		// We don't need to keep a reference to the ItemBlock
		Item itemblock = new ItemBlockMultiTexturedElemental(block, separateNames).setRegistryName(block.getRegistryName());
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
		registerItemBlock(registry, WizardryBlocks.crystal_flower);
		registerItemBlock(registry, WizardryBlocks.transportation_stone);
		registerMultiTexturedItemBlock(registry, WizardryBlocks.crystal_block, true);
		registerMultiTexturedItemBlock(registry, WizardryBlocks.runestone, false);
		registerMultiTexturedItemBlock(registry, WizardryBlocks.runestone_pedestal, false);

		// Items

		registerItem(registry, "magic_crystal", 				new ItemCrystal());

		registerItem(registry, "crystal_shard", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "grand_crystal", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "wizard_handbook", 				new ItemWizardHandbook(), true);
		registerItem(registry, "arcane_tome", 					new ItemArcaneTome());
		registerItem(registry, "spell_book", 					new ItemSpellBook(), true);
		registerItem(registry, "scroll", 						new ItemScroll());

		registerItem(registry, "magic_wand", 					new ItemWand(Tier.NOVICE, null));
		registerItem(registry, "apprentice_wand", 				new ItemWand(Tier.APPRENTICE, null));
		registerItem(registry, "advanced_wand", 				new ItemWand(Tier.ADVANCED, null));
		registerItem(registry, "master_wand", 					new ItemWand(Tier.MASTER, null));

		registerItem(registry, "novice_fire_wand", 			new ItemWand(Tier.NOVICE, Element.FIRE));
		registerItem(registry, "apprentice_fire_wand", 		new ItemWand(Tier.APPRENTICE, Element.FIRE));
		registerItem(registry, "advanced_fire_wand", 			new ItemWand(Tier.ADVANCED, Element.FIRE));
		registerItem(registry, "master_fire_wand", 			new ItemWand(Tier.MASTER, Element.FIRE));

		registerItem(registry, "novice_ice_wand", 				new ItemWand(Tier.NOVICE, Element.ICE));
		registerItem(registry, "apprentice_ice_wand", 			new ItemWand(Tier.APPRENTICE, Element.ICE));
		registerItem(registry, "advanced_ice_wand", 			new ItemWand(Tier.ADVANCED, Element.ICE));
		registerItem(registry, "master_ice_wand", 				new ItemWand(Tier.MASTER, Element.ICE));

		registerItem(registry, "novice_lightning_wand", 		new ItemWand(Tier.NOVICE, Element.LIGHTNING));
		registerItem(registry, "apprentice_lightning_wand", 	new ItemWand(Tier.APPRENTICE, Element.LIGHTNING));
		registerItem(registry, "advanced_lightning_wand", 		new ItemWand(Tier.ADVANCED, Element.LIGHTNING));
		registerItem(registry, "master_lightning_wand", 		new ItemWand(Tier.MASTER, Element.LIGHTNING));

		registerItem(registry, "novice_necromancy_wand", 		new ItemWand(Tier.NOVICE, Element.NECROMANCY));
		registerItem(registry, "apprentice_necromancy_wand", 	new ItemWand(Tier.APPRENTICE, Element.NECROMANCY));
		registerItem(registry, "advanced_necromancy_wand", 	new ItemWand(Tier.ADVANCED, Element.NECROMANCY));
		registerItem(registry, "master_necromancy_wand", 		new ItemWand(Tier.MASTER, Element.NECROMANCY));

		registerItem(registry, "novice_earth_wand", 			new ItemWand(Tier.NOVICE, Element.EARTH));
		registerItem(registry, "apprentice_earth_wand", 		new ItemWand(Tier.APPRENTICE, Element.EARTH));
		registerItem(registry, "advanced_earth_wand", 			new ItemWand(Tier.ADVANCED, Element.EARTH));
		registerItem(registry, "master_earth_wand", 			new ItemWand(Tier.MASTER, Element.EARTH));

		registerItem(registry, "novice_sorcery_wand", 			new ItemWand(Tier.NOVICE, Element.SORCERY));
		registerItem(registry, "apprentice_sorcery_wand", 		new ItemWand(Tier.APPRENTICE, Element.SORCERY));
		registerItem(registry, "advanced_sorcery_wand", 		new ItemWand(Tier.ADVANCED, Element.SORCERY));
		registerItem(registry, "master_sorcery_wand", 			new ItemWand(Tier.MASTER, Element.SORCERY));

		registerItem(registry, "novice_healing_wand", 			new ItemWand(Tier.NOVICE, Element.HEALING));
		registerItem(registry, "apprentice_healing_wand", 		new ItemWand(Tier.APPRENTICE, Element.HEALING));
		registerItem(registry, "advanced_healing_wand", 		new ItemWand(Tier.ADVANCED, Element.HEALING));
		registerItem(registry, "master_healing_wand", 			new ItemWand(Tier.MASTER, Element.HEALING));

		registerItem(registry, "spectral_sword", 				new ItemSpectralSword(ToolMaterial.IRON));
		registerItem(registry, "spectral_pickaxe", 			new ItemSpectralPickaxe(ToolMaterial.IRON));
		registerItem(registry, "spectral_bow", 				new ItemSpectralBow());

		registerItem(registry, "blank_scroll",					new ItemBlankScroll());
		registerItem(registry, "magic_silk", 					new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "small_mana_flask", 			new ItemManaFlask(ItemManaFlask.Size.SMALL));
		registerItem(registry, "medium_mana_flask", 			new ItemManaFlask(ItemManaFlask.Size.MEDIUM));
		registerItem(registry, "large_mana_flask", 			new ItemManaFlask(ItemManaFlask.Size.LARGE));

		registerItem(registry, "storage_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "siphon_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "condenser_upgrade", 			new ItemWandUpgrade());
		registerItem(registry, "range_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "duration_upgrade", 			new ItemWandUpgrade());
		registerItem(registry, "cooldown_upgrade", 			new ItemWandUpgrade());
		registerItem(registry, "blast_upgrade", 				new ItemWandUpgrade());
		registerItem(registry, "attunement_upgrade", 			new ItemWandUpgrade());
		registerItem(registry, "melee_upgrade", 				new ItemWandUpgrade());

		registerItem(registry, "flaming_axe", 					new ItemFlamingAxe(Materials.MAGICAL));
		registerItem(registry, "frost_axe",					new ItemFrostAxe(Materials.MAGICAL));

		registerItem(registry, "identification_scroll", 		new ItemIdentificationScroll());
		registerItem(registry, "armour_upgrade", 				new ItemArmourUpgrade());
		registerItem(registry, "astral_diamond", 				new Item(){ @Override public EnumRarity getRarity(ItemStack stack){ return EnumRarity.RARE; }}.setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "purifying_elixir",				new ItemPurifyingElixir());

		registerItem(registry, "firebomb", 					new ItemFirebomb());
		registerItem(registry, "poison_bomb", 					new ItemPoisonBomb());
		registerItem(registry, "smoke_bomb", 					new ItemSmokeBomb());
		registerItem(registry, "spark_bomb", 					new ItemSparkBomb());

		registerItem(registry, "wizard_hat", 					new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, null), true);
		registerItem(registry, "wizard_robe", 					new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, null));
		registerItem(registry, "wizard_leggings", 				new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, null));
		registerItem(registry, "wizard_boots", 				new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, null));

		registerItem(registry, "wizard_hat_fire", 				new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.FIRE));
		registerItem(registry, "wizard_robe_fire", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.FIRE));
		registerItem(registry, "wizard_leggings_fire", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.FIRE));
		registerItem(registry, "wizard_boots_fire", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.FIRE));

		registerItem(registry, "wizard_hat_ice", 				new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.ICE));
		registerItem(registry, "wizard_robe_ice", 				new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.ICE));
		registerItem(registry, "wizard_leggings_ice", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.ICE));
		registerItem(registry, "wizard_boots_ice", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.ICE));

		registerItem(registry, "wizard_hat_lightning", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.LIGHTNING));
		registerItem(registry, "wizard_robe_lightning", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.LIGHTNING));
		registerItem(registry, "wizard_leggings_lightning", 	new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.LIGHTNING));
		registerItem(registry, "wizard_boots_lightning", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.LIGHTNING));

		registerItem(registry, "wizard_hat_necromancy", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.NECROMANCY));
		registerItem(registry, "wizard_robe_necromancy", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.NECROMANCY));
		registerItem(registry, "wizard_leggings_necromancy", 	new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.NECROMANCY));
		registerItem(registry, "wizard_boots_necromancy", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.NECROMANCY));

		registerItem(registry, "wizard_hat_earth", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.EARTH));
		registerItem(registry, "wizard_robe_earth", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.EARTH));
		registerItem(registry, "wizard_leggings_earth", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.EARTH));
		registerItem(registry, "wizard_boots_earth", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.EARTH));

		registerItem(registry, "wizard_hat_sorcery", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.SORCERY));
		registerItem(registry, "wizard_robe_sorcery", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.SORCERY));
		registerItem(registry, "wizard_leggings_sorcery", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.SORCERY));
		registerItem(registry, "wizard_boots_sorcery", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.SORCERY));

		registerItem(registry, "wizard_hat_healing", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.HEAD, Element.HEALING));
		registerItem(registry, "wizard_robe_healing", 			new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.CHEST, Element.HEALING));
		registerItem(registry, "wizard_leggings_healing", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.LEGS, Element.HEALING));
		registerItem(registry, "wizard_boots_healing", 		new ItemWizardArmour(Materials.SILK, 1, EntityEquipmentSlot.FEET, Element.HEALING));

		registerItem(registry, "spectral_helmet", 				new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.HEAD));
		registerItem(registry, "spectral_chestplate", 			new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.CHEST));
		registerItem(registry, "spectral_leggings", 			new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.LEGS));
		registerItem(registry, "spectral_boots", 				new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.FEET));

		registerItem(registry, "lightning_hammer", 			new ItemLightningHammer());

		registerItem(registry, "ring_condensing", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_siphoning", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_battlemage", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_combustion", 				new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));
		registerItem(registry, "ring_fire_melee", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_fire_biome", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_disintegration", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_ice_melee", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_ice_biome", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_arcane_frost", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));
		registerItem(registry, "ring_shattering", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_lightning_melee", 		new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_storm", 					new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_seeking", 				new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));
		registerItem(registry, "ring_hammer", 					new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));
		registerItem(registry, "ring_soulbinding", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));
		registerItem(registry, "ring_leeching", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_necromancy_melee", 		new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_mind_control", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_poison", 					new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_earth_melee", 			new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_earth_biome", 			new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_full_moon", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_extraction", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_mana_return", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));
		registerItem(registry, "ring_blockwrangler", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_conjurer", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_defender", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.RING));
		registerItem(registry, "ring_paladin", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.RING));
		registerItem(registry, "ring_interdiction", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.RING));

		registerItem(registry, "amulet_arcane_defence", 		new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_warding", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_wisdom", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_fire_protection", 		new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_fire_cloaking", 		new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_ice_immunity", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_ice_protection", 		new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_potential", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_channeling", 			new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_lich", 					new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_wither_immunity", 		new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_glide", 				new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_banishing", 			new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_anchoring", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_recovery", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_transience", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_resurrection", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.AMULET));
		registerItem(registry, "amulet_auto_shield", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.AMULET));

		registerItem(registry, "charm_haggler", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_experience_tome", 		new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_auto_smelt", 			new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_lava_walking", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_storm", 					new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_minion_health", 			new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_minion_variants", 		new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_flight", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_growth", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_abseiling", 				new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_silk_touch", 			new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_stop_time", 				new ItemArtefact(EnumRarity.EPIC, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_light", 					new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_transportation", 		new ItemArtefact(EnumRarity.RARE, ItemArtefact.Type.CHARM));
		registerItem(registry, "charm_feeding", 				new ItemArtefact(EnumRarity.UNCOMMON, ItemArtefact.Type.CHARM));

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

	}

	public static void populateWandMap(){

		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.MAGIC), magic_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.FIRE), novice_fire_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.ICE), novice_ice_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.LIGHTNING), novice_lightning_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.NECROMANCY), novice_necromancy_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.EARTH), novice_earth_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.SORCERY), novice_sorcery_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.NOVICE, Element.HEALING), novice_healing_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.MAGIC), apprentice_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.FIRE), apprentice_fire_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.ICE), apprentice_ice_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.LIGHTNING), apprentice_lightning_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.NECROMANCY), apprentice_necromancy_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.EARTH), apprentice_earth_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.SORCERY), apprentice_sorcery_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.APPRENTICE, Element.HEALING), apprentice_healing_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.MAGIC), advanced_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.FIRE), advanced_fire_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.ICE), advanced_ice_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.LIGHTNING), advanced_lightning_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.NECROMANCY), advanced_necromancy_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.EARTH), advanced_earth_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.SORCERY), advanced_sorcery_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.ADVANCED, Element.HEALING), advanced_healing_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.MAGIC), master_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.FIRE), master_fire_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.ICE), master_ice_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.LIGHTNING), master_lightning_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.NECROMANCY), master_necromancy_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.EARTH), master_earth_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.SORCERY), master_sorcery_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.MASTER, Element.HEALING), master_healing_wand);
	}

	public static void populateArmourMap(){

		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.MAGIC), wizard_hat);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.FIRE), wizard_hat_fire);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.ICE), wizard_hat_ice);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.LIGHTNING), wizard_hat_lightning);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.NECROMANCY), wizard_hat_necromancy);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.EARTH), wizard_hat_earth);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.SORCERY), wizard_hat_sorcery);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.HEAD, Element.HEALING), wizard_hat_healing);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.MAGIC), wizard_robe);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.FIRE), wizard_robe_fire);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.ICE), wizard_robe_ice);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.LIGHTNING), wizard_robe_lightning);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.NECROMANCY), wizard_robe_necromancy);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.EARTH), wizard_robe_earth);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.SORCERY), wizard_robe_sorcery);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.CHEST, Element.HEALING), wizard_robe_healing);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.MAGIC), wizard_leggings);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.FIRE), wizard_leggings_fire);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.ICE), wizard_leggings_ice);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.LIGHTNING), wizard_leggings_lightning);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.NECROMANCY), wizard_leggings_necromancy);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.EARTH), wizard_leggings_earth);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.SORCERY), wizard_leggings_sorcery);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.LEGS, Element.HEALING), wizard_leggings_healing);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.MAGIC), wizard_boots);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.FIRE), wizard_boots_fire);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.ICE), wizard_boots_ice);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.LIGHTNING), wizard_boots_lightning);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.NECROMANCY), wizard_boots_necromancy);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.EARTH), wizard_boots_earth);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.SORCERY), wizard_boots_sorcery);
		ARMOUR_MAP.put(ImmutablePair.of(EntityEquipmentSlot.FEET, Element.HEALING), wizard_boots_healing);
	}

}