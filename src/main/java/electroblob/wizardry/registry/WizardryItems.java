package electroblob.wizardry.registry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.advancement.AdvancementHelper;
import electroblob.wizardry.advancement.AdvancementHelper.EnumAdvancement;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArmourUpgrade;
import electroblob.wizardry.item.ItemFirebomb;
import electroblob.wizardry.item.ItemFlamingAxe;
import electroblob.wizardry.item.ItemFrostAxe;
import electroblob.wizardry.item.ItemIdentificationScroll;
import electroblob.wizardry.item.ItemPoisonBomb;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSmokeBomb;
import electroblob.wizardry.item.ItemSpectralArmour;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemSpectralPickaxe;
import electroblob.wizardry.item.ItemSpectralSword;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.ItemWizardHandbook;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
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
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class responsible for defining, storing and registering all of wizardry's items. Also registers the ItemBlocks for
 * wizardry's blocks.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryItems {

	public static final Item magic_crystal = new Item().setCreativeTab(WizardryTabs.WIZARDRY);

	public static final Item magic_wand = new ItemWand(Tier.BASIC, null);
	public static final Item apprentice_wand = new ItemWand(Tier.APPRENTICE, null);
	public static final Item advanced_wand = new ItemWand(Tier.ADVANCED, null);
	public static final Item master_wand = new ItemWand(Tier.MASTER, null);

	public static final Item arcane_tome = new ItemArcaneTome();
	public static final Item wizard_handbook = new ItemWizardHandbook();
	public static final Item spell_book = new ItemSpellBook();

	public static final Item basic_fire_wand = new ItemWand(Tier.BASIC, Element.FIRE);
	public static final Item basic_ice_wand = new ItemWand(Tier.BASIC, Element.ICE);
	public static final Item basic_lightning_wand = new ItemWand(Tier.BASIC, Element.LIGHTNING);
	public static final Item basic_necromancy_wand = new ItemWand(Tier.BASIC, Element.NECROMANCY);
	public static final Item basic_earth_wand = new ItemWand(Tier.BASIC, Element.EARTH);
	public static final Item basic_sorcery_wand = new ItemWand(Tier.BASIC, Element.SORCERY);
	public static final Item basic_healing_wand = new ItemWand(Tier.BASIC, Element.HEALING);

	public static final Item apprentice_fire_wand = new ItemWand(Tier.APPRENTICE, Element.FIRE);
	public static final Item apprentice_ice_wand = new ItemWand(Tier.APPRENTICE, Element.ICE);
	public static final Item apprentice_lightning_wand = new ItemWand(Tier.APPRENTICE, Element.LIGHTNING);
	public static final Item apprentice_necromancy_wand = new ItemWand(Tier.APPRENTICE, Element.NECROMANCY);
	public static final Item apprentice_earth_wand = new ItemWand(Tier.APPRENTICE, Element.EARTH);
	public static final Item apprentice_sorcery_wand = new ItemWand(Tier.APPRENTICE, Element.SORCERY);
	public static final Item apprentice_healing_wand = new ItemWand(Tier.APPRENTICE, Element.HEALING);

	public static final Item advanced_fire_wand = new ItemWand(Tier.ADVANCED, Element.FIRE);
	public static final Item advanced_ice_wand = new ItemWand(Tier.ADVANCED, Element.ICE);
	public static final Item advanced_lightning_wand = new ItemWand(Tier.ADVANCED, Element.LIGHTNING);
	public static final Item advanced_necromancy_wand = new ItemWand(Tier.ADVANCED, Element.NECROMANCY);
	public static final Item advanced_earth_wand = new ItemWand(Tier.ADVANCED, Element.EARTH);
	public static final Item advanced_sorcery_wand = new ItemWand(Tier.ADVANCED, Element.SORCERY);
	public static final Item advanced_healing_wand = new ItemWand(Tier.ADVANCED, Element.HEALING);

	public static final Item master_fire_wand = new ItemWand(Tier.MASTER, Element.FIRE);
	public static final Item master_ice_wand = new ItemWand(Tier.MASTER, Element.ICE);
	public static final Item master_lightning_wand = new ItemWand(Tier.MASTER, Element.LIGHTNING);
	public static final Item master_necromancy_wand = new ItemWand(Tier.MASTER, Element.NECROMANCY);
	public static final Item master_earth_wand = new ItemWand(Tier.MASTER, Element.EARTH);
	public static final Item master_sorcery_wand = new ItemWand(Tier.MASTER, Element.SORCERY);
	public static final Item master_healing_wand = new ItemWand(Tier.MASTER, Element.HEALING);

	public static final Item spectral_sword = new ItemSpectralSword(ToolMaterial.IRON);
	public static final Item spectral_pickaxe = new ItemSpectralPickaxe(ToolMaterial.IRON);
	public static final Item spectral_bow = new ItemSpectralBow();

	public static final Item mana_flask = new Item().setCreativeTab(WizardryTabs.WIZARDRY);

	public static final Item storage_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item siphon_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item condenser_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item range_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item duration_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item cooldown_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item blast_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item attunement_upgrade = new Item().setCreativeTab(WizardryTabs.WIZARDRY);

	public static final Item magic_silk = new Item().setCreativeTab(WizardryTabs.WIZARDRY);

	public static final Item.ToolMaterial MAGICAL = EnumHelper.addToolMaterial("MAGICAL", 3, 1000, 8.0f, 4.0f, 0);

	public static final Item flaming_axe = new ItemFlamingAxe(MAGICAL);
	public static final Item frost_axe = new ItemFrostAxe(MAGICAL);

	public static final Item firebomb = new ItemFirebomb();
	public static final Item poison_bomb = new ItemPoisonBomb();

	public static final Item blank_scroll = new Item().setCreativeTab(WizardryTabs.WIZARDRY);
	public static final Item scroll = new ItemScroll().setCreativeTab(WizardryTabs.SPELLS);

	// The only way to get these is in dungeon chests (They are legendary, after all. Wizards don't just have them.
	// Also if they were sold you could buy four from the same wizard - and that's no fun at all!)
	public static final Item armour_upgrade = new ItemArmourUpgrade();

	public static final ArmorMaterial SILK = EnumHelper.addArmorMaterial("SILK",
			"wizardry/textures/armour/wizard_armour", 15, new int[]{2, 5, 4, 2}, 0,
			SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F);
	
	// Saw a post somewhere that said you have to put these in the init methods rather than defining them as constants.
	// I *think* that's because the post was for a newer Minecraft version, where custom armour has its own renderer or
	// something, meaning it would be done a lot like the entity rendering registry in ClientProxy. This is all working
	// fine it seems, so I'm not going to fiddle with it, but it might be useful to know if I update versions again.
	public static final Item wizard_hat = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, null);
	public static final Item wizard_robe = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, null);
	public static final Item wizard_leggings = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, null);
	public static final Item wizard_boots = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, null);

	public static final Item wizard_hat_fire = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.FIRE);
	public static final Item wizard_robe_fire = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.FIRE);
	public static final Item wizard_leggings_fire = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.FIRE);
	public static final Item wizard_boots_fire = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.FIRE);

	public static final Item wizard_hat_ice = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.ICE);
	public static final Item wizard_robe_ice = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.ICE);
	public static final Item wizard_leggings_ice = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.ICE);
	public static final Item wizard_boots_ice = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.ICE);

	public static final Item wizard_hat_lightning = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.LIGHTNING);
	public static final Item wizard_robe_lightning = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.LIGHTNING);
	public static final Item wizard_leggings_lightning = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.LIGHTNING);
	public static final Item wizard_boots_lightning = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.LIGHTNING);

	public static final Item wizard_hat_necromancy = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.NECROMANCY);
	public static final Item wizard_robe_necromancy = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.NECROMANCY);
	public static final Item wizard_leggings_necromancy = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.NECROMANCY);
	public static final Item wizard_boots_necromancy = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.NECROMANCY);

	public static final Item wizard_hat_earth = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.EARTH);
	public static final Item wizard_robe_earth = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.EARTH);
	public static final Item wizard_leggings_earth = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.EARTH);
	public static final Item wizard_boots_earth = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.EARTH);

	public static final Item wizard_hat_sorcery = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.SORCERY);
	public static final Item wizard_robe_sorcery = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.SORCERY);
	public static final Item wizard_leggings_sorcery = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.SORCERY);
	public static final Item wizard_boots_sorcery = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.SORCERY);

	public static final Item wizard_hat_healing = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.HEAD, Element.HEALING);
	public static final Item wizard_robe_healing = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.CHEST, Element.HEALING);
	public static final Item wizard_leggings_healing = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.LEGS, Element.HEALING);
	public static final Item wizard_boots_healing = new ItemWizardArmour(SILK, 1, EntityEquipmentSlot.FEET, Element.HEALING);

	public static final Item spectral_helmet = new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.HEAD);
	public static final Item spectral_chestplate = new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.CHEST);
	public static final Item spectral_leggings = new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.LEGS);
	public static final Item spectral_boots = new ItemSpectralArmour(ArmorMaterial.IRON, 1, EntityEquipmentSlot.FEET);

	public static final Map<EntityEquipmentSlot, Item> SPECTRAL_ARMOUR_MAP = ImmutableMap.of(
			EntityEquipmentSlot.HEAD, spectral_helmet, EntityEquipmentSlot.CHEST, spectral_chestplate,
			EntityEquipmentSlot.LEGS, spectral_leggings, EntityEquipmentSlot.FEET, spectral_boots);
	
	public static final Item smoke_bomb = new ItemSmokeBomb();

	public static final Item identification_scroll = new ItemIdentificationScroll();

	public static final Map<Pair<Tier, Element>, Item> WAND_MAP = new HashMap<>();
	public static final Map<Pair<EntityEquipmentSlot, Element>, Item> ARMOUR_MAP = new HashMap<>();

	static{

		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.MAGIC), magic_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.FIRE), basic_fire_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.ICE), basic_ice_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.LIGHTNING), basic_lightning_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.NECROMANCY), basic_necromancy_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.EARTH), basic_earth_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.SORCERY), basic_sorcery_wand);
		WAND_MAP.put(ImmutablePair.of(Tier.BASIC, Element.HEALING), basic_healing_wand);
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

	/**
	 * Sets both the registry and unlocalised names of the given item, then registers it with the given registry. Use
	 * this instead of {@link Item#setRegistryName(String)} and {@link Item#setUnlocalizedName(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given item to.
	 * @param item The item to register.
	 * @param name The name of the item, without the mod ID or the .name stuff. The registry name will be
	 *        {@code wizardry:[name]}. The unlocalised name will be {@code item.wizardry:[name].name}.
	 */
	public static void registerItem(IForgeRegistry<Item> registry, Item item, String name){
		item.setRegistryName(Wizardry.MODID, name);
		item.setUnlocalizedName(item.getRegistryName().toString());
		registry.register(item);
	}

	/** Registers an ItemBlock afor the given block, with the same registry name as that block. */
	private static void registerItemBlock(IForgeRegistry<Item> registry, Block block){
		// We don't need to keep a reference to the ItemBlock, so this can all be done in one line.
		registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
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
		registerItemBlock(registry, WizardryBlocks.crystal_block);

		// Items

		registerItem(registry, magic_crystal, "magic_crystal");

		registerItem(registry, magic_wand, "magic_wand");
		registerItem(registry, apprentice_wand, "apprentice_wand");
		registerItem(registry, advanced_wand, "advanced_wand");
		registerItem(registry, master_wand, "master_wand");

		registerItem(registry, spell_book, "spell_book");
		registerItem(registry, arcane_tome, "arcane_tome");
		registerItem(registry, wizard_handbook, "wizard_handbook");

		registerItem(registry, basic_fire_wand, "basic_fire_wand");
		registerItem(registry, basic_ice_wand, "basic_ice_wand");
		registerItem(registry, basic_lightning_wand, "basic_lightning_wand");
		registerItem(registry, basic_necromancy_wand, "basic_necromancy_wand");
		registerItem(registry, basic_earth_wand, "basic_earth_wand");
		registerItem(registry, basic_sorcery_wand, "basic_sorcery_wand");
		registerItem(registry, basic_healing_wand, "basic_healing_wand");

		registerItem(registry, apprentice_fire_wand, "apprentice_fire_wand");
		registerItem(registry, apprentice_ice_wand, "apprentice_ice_wand");
		registerItem(registry, apprentice_lightning_wand, "apprentice_lightning_wand");
		registerItem(registry, apprentice_necromancy_wand, "apprentice_necromancy_wand");
		registerItem(registry, apprentice_earth_wand, "apprentice_earth_wand");
		registerItem(registry, apprentice_sorcery_wand, "apprentice_sorcery_wand");
		registerItem(registry, apprentice_healing_wand, "apprentice_healing_wand");

		registerItem(registry, advanced_fire_wand, "advanced_fire_wand");
		registerItem(registry, advanced_ice_wand, "advanced_ice_wand");
		registerItem(registry, advanced_lightning_wand, "advanced_lightning_wand");
		registerItem(registry, advanced_necromancy_wand, "advanced_necromancy_wand");
		registerItem(registry, advanced_earth_wand, "advanced_earth_wand");
		registerItem(registry, advanced_sorcery_wand, "advanced_sorcery_wand");
		registerItem(registry, advanced_healing_wand, "advanced_healing_wand");

		registerItem(registry, master_fire_wand, "master_fire_wand");
		registerItem(registry, master_ice_wand, "master_ice_wand");
		registerItem(registry, master_lightning_wand, "master_lightning_wand");
		registerItem(registry, master_necromancy_wand, "master_necromancy_wand");
		registerItem(registry, master_earth_wand, "master_earth_wand");
		registerItem(registry, master_sorcery_wand, "master_sorcery_wand");
		registerItem(registry, master_healing_wand, "master_healing_wand");

		registerItem(registry, spectral_sword, "spectral_sword");
		registerItem(registry, spectral_pickaxe, "spectral_pickaxe");
		registerItem(registry, spectral_bow, "spectral_bow");

		registerItem(registry, mana_flask, "mana_flask");

		registerItem(registry, storage_upgrade, "storage_upgrade");
		registerItem(registry, siphon_upgrade, "siphon_upgrade");
		registerItem(registry, condenser_upgrade, "condenser_upgrade");
		registerItem(registry, range_upgrade, "range_upgrade");
		registerItem(registry, duration_upgrade, "duration_upgrade");
		registerItem(registry, cooldown_upgrade, "cooldown_upgrade");
		registerItem(registry, blast_upgrade, "blast_upgrade");
		registerItem(registry, attunement_upgrade, "attunement_upgrade");

		registerItem(registry, flaming_axe, "flaming_axe");
		registerItem(registry, frost_axe, "frost_axe");

		registerItem(registry, firebomb, "firebomb");
		registerItem(registry, poison_bomb, "poison_bomb");

		registerItem(registry, blank_scroll, "blank_scroll");
		registerItem(registry, scroll, "scroll");

		registerItem(registry, armour_upgrade, "armour_upgrade");

		registerItem(registry, magic_silk, "magic_silk");

		registerItem(registry, wizard_hat, "wizard_hat");
		registerItem(registry, wizard_robe, "wizard_robe");
		registerItem(registry, wizard_leggings, "wizard_leggings");
		registerItem(registry, wizard_boots, "wizard_boots");

		registerItem(registry, wizard_hat_fire, "wizard_hat_fire");
		registerItem(registry, wizard_robe_fire, "wizard_robe_fire");
		registerItem(registry, wizard_leggings_fire, "wizard_leggings_fire");
		registerItem(registry, wizard_boots_fire, "wizard_boots_fire");

		registerItem(registry, wizard_hat_ice, "wizard_hat_ice");
		registerItem(registry, wizard_robe_ice, "wizard_robe_ice");
		registerItem(registry, wizard_leggings_ice, "wizard_leggings_ice");
		registerItem(registry, wizard_boots_ice, "wizard_boots_ice");

		registerItem(registry, wizard_hat_lightning, "wizard_hat_lightning");
		registerItem(registry, wizard_robe_lightning, "wizard_robe_lightning");
		registerItem(registry, wizard_leggings_lightning, "wizard_leggings_lightning");
		registerItem(registry, wizard_boots_lightning, "wizard_boots_lightning");

		registerItem(registry, wizard_hat_necromancy, "wizard_hat_necromancy");
		registerItem(registry, wizard_robe_necromancy, "wizard_robe_necromancy");
		registerItem(registry, wizard_leggings_necromancy, "wizard_leggings_necromancy");
		registerItem(registry, wizard_boots_necromancy, "wizard_boots_necromancy");

		registerItem(registry, wizard_hat_earth, "wizard_hat_earth");
		registerItem(registry, wizard_robe_earth, "wizard_robe_earth");
		registerItem(registry, wizard_leggings_earth, "wizard_leggings_earth");
		registerItem(registry, wizard_boots_earth, "wizard_boots_earth");

		registerItem(registry, wizard_hat_sorcery, "wizard_hat_sorcery");
		registerItem(registry, wizard_robe_sorcery, "wizard_robe_sorcery");
		registerItem(registry, wizard_leggings_sorcery, "wizard_leggings_sorcery");
		registerItem(registry, wizard_boots_sorcery, "wizard_boots_sorcery");

		registerItem(registry, wizard_hat_healing, "wizard_hat_healing");
		registerItem(registry, wizard_robe_healing, "wizard_robe_healing");
		registerItem(registry, wizard_leggings_healing, "wizard_leggings_healing");
		registerItem(registry, wizard_boots_healing, "wizard_boots_healing");

		registerItem(registry, spectral_helmet, "spectral_helmet");
		registerItem(registry, spectral_chestplate, "spectral_chestplate");
		registerItem(registry, spectral_leggings, "spectral_leggings");
		registerItem(registry, spectral_boots, "spectral_boots");

		registerItem(registry, smoke_bomb, "smoke_bomb");

		registerItem(registry, identification_scroll, "identification_scroll");
	}

}