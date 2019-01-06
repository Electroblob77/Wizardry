package electroblob.wizardry.registry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArmourUpgrade;
import electroblob.wizardry.item.ItemBlankScroll;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

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
	
	/** Keeping the material fields in here means {@code @ObjectHolder} ignores them. In actual fact, I could have just
	 * made them private since wizardry only uses them within this class, but in case someone needs them elsewhere I've
	 * used this trick instead to keep them public. */
	public static final class Materials {

		public static final ToolMaterial MAGICAL = EnumHelper.addToolMaterial("MAGICAL", 3, 1000, 8.0f, 4.0f, 0);
		
		public static final ArmorMaterial SILK = EnumHelper.addArmorMaterial("SILK", "wizardry/textures/armour/wizard_armour",
		15, new int[]{0, 0, 0, 0}, 0, WizardrySounds.ITEM_ARMOUR_EQUIP_SILK, 0.0F);
		
	}
	
	// This is the most concise way I can think of to register the items. Really, I'd prefer it if there was only one
	// point where all the items were listed, but that's not possible within the current system unless you use an array,
	// which means you lose the individual fields...

	public static final Item magic_crystal = null;

	public static final Item magic_wand = null;
	public static final Item apprentice_wand = null;
	public static final Item advanced_wand = null;
	public static final Item master_wand = null;

	public static final Item arcane_tome = null;
	public static final Item wizard_handbook = null;
	public static final Item spell_book = null;

	public static final Item basic_fire_wand = null;
	public static final Item basic_ice_wand = null;
	public static final Item basic_lightning_wand = null;
	public static final Item basic_necromancy_wand = null;
	public static final Item basic_earth_wand = null;
	public static final Item basic_sorcery_wand = null;
	public static final Item basic_healing_wand = null;

	public static final Item apprentice_fire_wand = null;
	public static final Item apprentice_ice_wand = null;
	public static final Item apprentice_lightning_wand = null;
	public static final Item apprentice_necromancy_wand = null;
	public static final Item apprentice_earth_wand = null;
	public static final Item apprentice_sorcery_wand = null;
	public static final Item apprentice_healing_wand = null;

	public static final Item advanced_fire_wand = null;
	public static final Item advanced_ice_wand = null;
	public static final Item advanced_lightning_wand = null;
	public static final Item advanced_necromancy_wand = null;
	public static final Item advanced_earth_wand = null;
	public static final Item advanced_sorcery_wand = null;
	public static final Item advanced_healing_wand = null;

	public static final Item master_fire_wand = null;
	public static final Item master_ice_wand = null;
	public static final Item master_lightning_wand = null;
	public static final Item master_necromancy_wand = null;
	public static final Item master_earth_wand = null;
	public static final Item master_sorcery_wand = null;
	public static final Item master_healing_wand = null;

	public static final Item spectral_sword = null;
	public static final Item spectral_pickaxe = null;
	public static final Item spectral_bow = null;

	public static final Item medium_mana_flask = null;

	public static final Item storage_upgrade = null;
	public static final Item siphon_upgrade = null;
	public static final Item condenser_upgrade = null;
	public static final Item range_upgrade = null;
	public static final Item duration_upgrade = null;
	public static final Item cooldown_upgrade = null;
	public static final Item blast_upgrade = null;
	public static final Item attunement_upgrade = null;

	public static final Item magic_silk = null;

	public static final Item flaming_axe = null;
	public static final Item frost_axe = null;

	public static final Item firebomb = null;
	public static final Item poison_bomb = null;

	public static final Item blank_scroll = null;
	public static final Item scroll = null;
	
	public static final Item armour_upgrade = null;
	
	public static final Item wizard_hat = null;
	public static final Item wizard_robe = null;
	public static final Item wizard_leggings = null;
	public static final Item wizard_boots = null;

	public static final Item wizard_hat_fire = null;
	public static final Item wizard_robe_fire = null;
	public static final Item wizard_leggings_fire = null;
	public static final Item wizard_boots_fire = null;

	public static final Item wizard_hat_ice = null;
	public static final Item wizard_robe_ice = null;
	public static final Item wizard_leggings_ice = null;
	public static final Item wizard_boots_ice = null;

	public static final Item wizard_hat_lightning = null;
	public static final Item wizard_robe_lightning = null;
	public static final Item wizard_leggings_lightning = null;
	public static final Item wizard_boots_lightning = null;

	public static final Item wizard_hat_necromancy = null;
	public static final Item wizard_robe_necromancy = null;
	public static final Item wizard_leggings_necromancy = null;
	public static final Item wizard_boots_necromancy = null;

	public static final Item wizard_hat_earth = null;
	public static final Item wizard_robe_earth = null;
	public static final Item wizard_leggings_earth = null;
	public static final Item wizard_boots_earth = null;

	public static final Item wizard_hat_sorcery = null;
	public static final Item wizard_robe_sorcery = null;
	public static final Item wizard_leggings_sorcery = null;
	public static final Item wizard_boots_sorcery = null;

	public static final Item wizard_hat_healing = null;
	public static final Item wizard_robe_healing = null;
	public static final Item wizard_leggings_healing = null;
	public static final Item wizard_boots_healing = null;

	public static final Item spectral_helmet = null;
	public static final Item spectral_chestplate = null;
	public static final Item spectral_leggings = null;
	public static final Item spectral_boots = null;
	
	public static final Item smoke_bomb = null;

	public static final Item identification_scroll = null;

	private static final Map<Pair<Tier, Element>, Item> WAND_MAP = new HashMap<>();
	private static final Map<Pair<EntityEquipmentSlot, Element>, Item> ARMOUR_MAP = new HashMap<>();

	static {

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

	// TODO: These methods need a rethink. What are we trying to achieve with them? Should each use case look in the
	// same pool of items? For example, might we (or someone else) want to have a wand which can generate in chests, but
	// is not used by wizards?

	// I reckon this should be strictly for cases where we only ever want the standard wand set, i.e. wizards' gear,
	// etc.

	/**
	 * Helper method to return the appropriate wand based on tier and element. As of Wizardry 2.1, this uses the
	 * immutable map stored in {@link WizardryItems#WAND_MAP}. Currently used for upgrading wands, for chest generation
	 * and to iterate through wands for charging recipes.
	 * 
	 * @param tier The tier of the wand required.
	 * @param element The element of the wand required. Null will be converted to {@link Element#MAGIC}.
	 * @return The wand item which corresponds to the given element and slot, or null if no such item exists.
	 * @throws NullPointerException if the given tier is null.
	 */
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
	 */
	public static Item getArmour(Element element, EntityEquipmentSlot slot){
		if(slot == null || slot.getSlotType() != Type.ARMOR)
			throw new IllegalArgumentException("Must be a valid armour slot");
		if(element == null) element = Element.MAGIC;
		return ARMOUR_MAP.get(ImmutablePair.of(slot, element));
	}
	
	/**
	 * Sets both the registry and unlocalised names of the given item, then registers it with the given registry. Use
	 * this instead of {@link Item#setRegistryName(String)} and {@link Item#setUnlocalizedName(String)} during
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
	 * this instead of {@link Item#setRegistryName(String)} and {@link Item#setUnlocalizedName(String)} during
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
		
		if(block.getCreativeTabToDisplayOn() instanceof CreativeTabListed){
			((CreativeTabListed)block.getCreativeTabToDisplayOn()).order.add(itemblock);
		}
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
		registerItemBlock(registry, WizardryBlocks.crystal_block);

		// Items
		
		registerItem(registry, "magic_crystal", 				new ItemCrystal());

		registerItem(registry, "magic_wand", 					new ItemWand(Tier.BASIC, null));
		registerItem(registry, "apprentice_wand", 				new ItemWand(Tier.APPRENTICE, null));
		registerItem(registry, "advanced_wand", 				new ItemWand(Tier.ADVANCED, null));
		registerItem(registry, "master_wand", 					new ItemWand(Tier.MASTER, null));

		registerItem(registry, "arcane_tome", 					new ItemArcaneTome());
		registerItem(registry, "wizard_handbook", 				new ItemWizardHandbook(), true);
		registerItem(registry, "spell_book", 					new ItemSpellBook(), true);

		registerItem(registry, "basic_fire_wand", 				new ItemWand(Tier.BASIC, Element.FIRE));
		registerItem(registry, "apprentice_fire_wand", 		new ItemWand(Tier.APPRENTICE, Element.FIRE));
		registerItem(registry, "advanced_fire_wand", 			new ItemWand(Tier.ADVANCED, Element.FIRE));
		registerItem(registry, "master_fire_wand", 			new ItemWand(Tier.MASTER, Element.FIRE));
		
		registerItem(registry, "basic_ice_wand", 				new ItemWand(Tier.BASIC, Element.ICE));
		registerItem(registry, "apprentice_ice_wand", 			new ItemWand(Tier.APPRENTICE, Element.ICE));
		registerItem(registry, "advanced_ice_wand", 			new ItemWand(Tier.ADVANCED, Element.ICE));
		registerItem(registry, "master_ice_wand", 				new ItemWand(Tier.MASTER, Element.ICE));
		
		registerItem(registry, "basic_lightning_wand", 		new ItemWand(Tier.BASIC, Element.LIGHTNING));
		registerItem(registry, "apprentice_lightning_wand", 	new ItemWand(Tier.APPRENTICE, Element.LIGHTNING));
		registerItem(registry, "advanced_lightning_wand", 		new ItemWand(Tier.ADVANCED, Element.LIGHTNING));
		registerItem(registry, "master_lightning_wand", 		new ItemWand(Tier.MASTER, Element.LIGHTNING));
		
		registerItem(registry, "basic_necromancy_wand", 		new ItemWand(Tier.BASIC, Element.NECROMANCY));
		registerItem(registry, "apprentice_necromancy_wand", 	new ItemWand(Tier.APPRENTICE, Element.NECROMANCY));
		registerItem(registry, "advanced_necromancy_wand", 	new ItemWand(Tier.ADVANCED, Element.NECROMANCY));
		registerItem(registry, "master_necromancy_wand", 		new ItemWand(Tier.MASTER, Element.NECROMANCY));
		
		registerItem(registry, "basic_earth_wand", 			new ItemWand(Tier.BASIC, Element.EARTH));
		registerItem(registry, "apprentice_earth_wand", 		new ItemWand(Tier.APPRENTICE, Element.EARTH));
		registerItem(registry, "advanced_earth_wand", 			new ItemWand(Tier.ADVANCED, Element.EARTH));
		registerItem(registry, "master_earth_wand", 			new ItemWand(Tier.MASTER, Element.EARTH));
		
		registerItem(registry, "basic_sorcery_wand", 			new ItemWand(Tier.BASIC, Element.SORCERY));
		registerItem(registry, "apprentice_sorcery_wand", 		new ItemWand(Tier.APPRENTICE, Element.SORCERY));
		registerItem(registry, "advanced_sorcery_wand", 		new ItemWand(Tier.ADVANCED, Element.SORCERY));
		registerItem(registry, "master_sorcery_wand", 			new ItemWand(Tier.MASTER, Element.SORCERY));
		
		registerItem(registry, "basic_healing_wand", 			new ItemWand(Tier.BASIC, Element.HEALING));
		registerItem(registry, "apprentice_healing_wand", 		new ItemWand(Tier.APPRENTICE, Element.HEALING));
		registerItem(registry, "advanced_healing_wand", 		new ItemWand(Tier.ADVANCED, Element.HEALING));
		registerItem(registry, "master_healing_wand", 			new ItemWand(Tier.MASTER, Element.HEALING));

		registerItem(registry, "spectral_sword", 				new ItemSpectralSword(ToolMaterial.IRON));
		registerItem(registry, "spectral_pickaxe", 			new ItemSpectralPickaxe(ToolMaterial.IRON));
		registerItem(registry, "spectral_bow", 				new ItemSpectralBow());

		registerItem(registry, "medium_mana_flask", 			new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "storage_upgrade", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "siphon_upgrade", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "condenser_upgrade", 			new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "range_upgrade", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "duration_upgrade", 			new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "cooldown_upgrade", 			new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "blast_upgrade", 				new Item().setCreativeTab(WizardryTabs.WIZARDRY));
		registerItem(registry, "attunement_upgrade", 			new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "magic_silk", 					new Item().setCreativeTab(WizardryTabs.WIZARDRY));

		registerItem(registry, "flaming_axe", 					new ItemFlamingAxe(Materials.MAGICAL));
		registerItem(registry, "frost_axe",					new ItemFrostAxe(Materials.MAGICAL));

		registerItem(registry, "firebomb", 					new ItemFirebomb());
		registerItem(registry, "poison_bomb", 					new ItemPoisonBomb());

		registerItem(registry, "blank_scroll",					new ItemBlankScroll());
		registerItem(registry, "scroll", 						new ItemScroll());

		// The only way to get these is in dungeon chests (They are legendary, after all. Wizards don't just have them.
		// Also if they were sold you could buy four from the same wizard - and that's no fun at all!)
		registerItem(registry, "armour_upgrade", 				new ItemArmourUpgrade());
		
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
		
		registerItem(registry, "smoke_bomb", 					new ItemSmokeBomb());

		registerItem(registry, "identification_scroll", 		new ItemIdentificationScroll());
		
		registerItem(registry, "test_artefact", 				new ItemArtefact(EnumRarity.UNCOMMON));
	}
	}

}