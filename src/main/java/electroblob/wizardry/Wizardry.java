package electroblob.wizardry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.block.BlockArcaneWorkbench;
import electroblob.wizardry.block.BlockCrystal;
import electroblob.wizardry.block.BlockCrystalFlower;
import electroblob.wizardry.block.BlockCrystalOre;
import electroblob.wizardry.block.BlockMagicLight;
import electroblob.wizardry.block.BlockSnare;
import electroblob.wizardry.block.BlockSpectral;
import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.block.BlockTransportationStone;
import electroblob.wizardry.block.BlockVanishingCobweb;
import electroblob.wizardry.command.CommandCastSpell;
import electroblob.wizardry.command.CommandDiscoverSpell;
import electroblob.wizardry.command.CommandSetAlly;
import electroblob.wizardry.command.CommandViewAllies;
import electroblob.wizardry.enchantment.EnchantmentMagicSword;
import electroblob.wizardry.enchantment.EnchantmentTimed;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArmourUpgrade;
import electroblob.wizardry.item.ItemFirebomb;
import electroblob.wizardry.item.ItemFlamingAxe;
import electroblob.wizardry.item.ItemFrostAxe;
import electroblob.wizardry.item.ItemIdentificationScroll;
import electroblob.wizardry.item.ItemPoisonBomb;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSmokeBomb;
import electroblob.wizardry.item.ItemSpawnWizard;
import electroblob.wizardry.item.ItemSpectralArmour;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemSpectralPickaxe;
import electroblob.wizardry.item.ItemSpectralSword;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.ItemWizardHandbook;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.potion.PotionDecay;
import electroblob.wizardry.potion.PotionFrost;
import electroblob.wizardry.potion.PotionMagicEffect;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.stats.Achievement;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.util.EnumHelper;

@Mod(modid = Wizardry.MODID, name = Wizardry.NAME, version = Wizardry.VERSION, guiFactory = "electroblob." + Wizardry.MODID + ".WizardryGuiFactory")
public class Wizardry {

	public static final String MODID = "wizardry";
	public static final String NAME = "Electroblob's Wizardry";
	public static final String VERSION = "1.1.5";

	// Constants
	/** The amount of mana each magic crystal is worth */
	public static final int MANA_PER_CRYSTAL = 100;
	/** The amount of mana each mana flask can hold */
	public static final int MANA_PER_FLASK = 700;
	/** The maximum number of one type of wand upgrade which can be applied to a wand. */
	public static final int UPGRADE_STACK_LIMIT = 3;
	/** The fraction by which cooldowns are reduced for each level of cooldown upgrade. */
	public static final float COOLDOWN_REDUCTION_PER_LEVEL = 0.15f;
	/** The fraction by which maximum charge is increased for each level of storage upgrade. */
	public static final float STORAGE_INCREASE_PER_LEVEL = 0.15f;
	/** The fraction by which damage is increased for each tier of matching wand.  */
	public static final float DAMAGE_INCREASE_PER_TIER = 0.15f;
	/** The fraction by which costs are reduced for each piece of matching armour. Note that changing this value will not
	 * affect continuous spells, since they are handled differently. */
	public static final float COST_REDUCTION_PER_ARMOUR = 0.2f;
	/** The fraction by which spell duration is increased for each level of duration upgrade. */
	public static final float DURATION_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which spell range is increased for each level of range upgrade. */
	public static final float RANGE_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which spell blast radius is increased for each level of range upgrade. */
	public static final float BLAST_RADIUS_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which movement speed is reduced per level of frost effect. */
	public static final double FROST_SLOWNESS_PER_LEVEL = 0.5;
	/** The fraction by which movement speed is reduced per level of decay effect. */
	public static final double DECAY_SLOWNESS_PER_LEVEL = 0.2;
	/** The fraction by which dig speed is reduced per level of frost effect. */
	public static final float FROST_FATIGUE_PER_LEVEL = 0.45f;
	/** The number of ticks between each mana increase for wands with the condenser upgrade. */
	public static final int CONDENSER_TICK_INTERVAL = 50;
	/** The amount of mana given for a kill for each level of siphon upgrade. A random amount from 0 to this number - 1
	 * is also added. See {@link WizardryEventHandler#onLivingDeathEvent} for more details. */
	public static final int SIPHON_MANA_PER_LEVEL = 3;
	/** The number of ticks between the spawning of patches of decay when an entity has the decay effect.
	 * Note that decay won't spawn again if something is already standing in it. */
	public static final int DECAY_SPREAD_INTERVAL = 8;
	
	// Category names
	/** The unlocalised name of the spells config category. */
	public static final String SPELLS_CATEGORY = "spells";
	/** The unlocalised name of the ids config category. */
	public static final String IDS_CATEGORY = "ids";
	/** The unlocalised name of the ids config category. */
	public static final String RESISTANCES_CATEGORY = "resistances";
	
	// Chest loot
	/** The frequency with which spell books will be generated in dungeon chests. Higher numbers are more common. */
	public static final int SPELL_BOOK_FREQUENCY = 20;
	/** The frequency with which (basic) wands will be generated in dungeon chests. Higher numbers are more common. */
	public static final int WAND_FREQUENCY = 3;
	/** The frequency with which wizard armour will be generated in dungeon chests. Higher numbers are more common. */
	public static final int ARMOUR_FREQUENCY = 6;
	/** The frequency with which spell books will be generated in dungeon chests. Higher numbers are more common. */
	public static final int SCROLL_FREQUENCY = 10;
	/** The frequency with which armour upgrades will be generated in dungeon chests. Higher numbers are more common. */
	public static final int WAND_UPGRADE_FREQUENCY = 2;
	/** The frequency with which spell books will be generated in dungeon chests. Higher numbers are more common. */
	public static final int CRYSTAL_FREQUENCY = 5;
	/** The frequency with which apprentice tomes of arcana will be generated in dungeon chests. Higher numbers are more common. */
	public static final int APPRENTICE_TOME_FREQUENCY = 3;
	/** The frequency with which advanced tomes of arcana will be generated in dungeon chests. Higher numbers are more common. */
	public static final int ADVANCED_TOME_FREQUENCY = 2;
	/** The frequency with which master tomes of arcana will be generated in dungeon chests. Higher numbers are more common. */
	public static final int MASTER_TOME_FREQUENCY = 1;
	/** The frequency with which armour upgrades will be generated in dungeon chests. Higher numbers are more common. */
	public static final int ARMOUR_UPGRADE_FREQUENCY = 1;
	/** The frequency with which firebombs will be generated in dungeon chests. Higher numbers are more common. */
	public static final int FIREBOMB_FREQUENCY = 4;
	/** The frequency with which poison bombs will be generated in dungeon chests. Higher numbers are more common. */
	public static final int POISON_BOMB_FREQUENCY = 4;
	/** The frequency with which smoke bombs will be generated in dungeon chests. Higher numbers are more common. */
	public static final int SMOKE_BOMB_FREQUENCY = 4;
	/** The frequency with which scrolls of identification will be generated in dungeon chests. Higher numbers are more common. */
	public static final int IDENTIFICATION_SCROLL_FREQUENCY = 3;

	// Global options
	/** The rarity of wizard towers, used by the world generator. Larger numbers are rarer. */
	public static int towerRarity = 8;
	/** List of dimension ids in which to generate crystal ore. */
	public static int[] oreDimensions = {0};
	/** List of dimension ids in which to generate crystal ore. */
	public static int[] flowerDimensions = {0};
	/** List of dimension ids in which to generate crystal ore. */
	public static int[] towerDimensions = {0};
	/** Chance (out of 200) for mobs to drop spell books. */
	public static int spellBookDropChance = 3;
	/** Whether or not wizardry loot should generate in dungeon chests. */
	public static boolean generateLoot = true;
	/** Whether or not firebombs should be craftable. */
	public static boolean firebombIsCraftable = true;
	/** Whether or not poison bombs should be craftable. */
	public static boolean poisonBombIsCraftable = true;
	/** Whether or not poison bombs should be craftable. */
	public static boolean smokeBombIsCraftable = true;
	/** Whether to require a magic crystal in the blank scroll crafting recipe (in case it conflicts with another mod). */
	public static boolean useAlternateScrollRecipe = false;
	/** Whether or not players can teleport through unbreakable blocks (e.g. bedrock) using the phase step spell. */
	public static boolean teleportThroughUnbreakableBlocks = false;
	/** Whether to show summoned creatures' names and owners above their heads. */
	public static boolean showSummonedCreatureNames = true;
	/** Whether to allow players to damage their designated allies using magic. */
	public static boolean friendlyFire = true;
	/** Whether to allow players to disarm other players using the telekinesis spell. */
	public static boolean telekineticDisarmament = true;
	/** When set to true, spells a player hasn't cast yet will be unreadable until they are cast (on a per-world basis). Has no effect when in creative mode. Spells of
	 * identification will be unobtainable in survival mode if this is false. */
	public static boolean discoveryMode = true;
	/** Whether the player can switch between spells on a wand by scrolling with the mouse wheel while sneaking. Note that different players connected to the same server each have their own setting for this.  */
	public static boolean enableShiftScrolling = true;
	/** Whether summoned creatures can revenge attack their caster if their caster attacks them.  */
	public static boolean minionRevengeTargeting = true;
	/** Global damage scaling factor for all player magic damage. */
	public static double playerDamageScale = 1.0f;
	/** Global damage scaling factor for all npc magic damage. */
	public static double npcDamageScale = 1.0f;
	/** The maximum allowed multiplier for the /cast command. This limit is here to stop people from accidentally
	 * breaking their worlds! */
	public static double maxSpellCommandMultiplier = 20d;
	/** List of names of entities which summoned creatures are allowed to attack, in addition to the defaults. */
	public static String[] summonedCreatureTargetsWhitelist = {};
	/** List of names of entities which summoned creatures are specifically not allowed to attack, overriding the defaults and the whitelist. */
	public static String[] summonedCreatureTargetsBlacklist = {"creeper"};
	/** The position of the spell HUD. */
	public static String spellHUDPosition = "Bottom left";
	/** The name of the /cast command. */
	public static String castCommandName = "cast";
	/** The name of the /discoverspell command. */
	public static String discoverspellCommandName = "discoverspell";
	/** The name of the /ally command. */
	public static String allyCommandName = "ally";
	/** The name of the /allies command. */
	public static String alliesCommandName = "allies";

	// Config ids
	/** The id of the frost potion effect. */
	private static int frostPotionID = 31;
	/** The id of the fireskin potion effect. */
	private static int transiencePotionID = 32;
	/** The id of the fireskin potion effect. */
	private static int fireskinPotionID = 33;
	/** The id of the ice shroud potion effect. */
	private static int iceShroudPotionID = 34;
	/** The id of the static aura potion effect. */
	private static int staticAuraPotionID = 35;
	/** The id of the decay potion effect. */
	private static int decayPotionID = 36;
	/** The id of the sixth sense potion effect. */
	private static int sixthSensePotionID = 37;
	/** The id of the arcane jammer potion effect. */
	private static int arcaneJammerPotionID = 38;
	/** The id of the mind trick potion effect. */
	private static int mindTrickPotionID = 39;
	/** The id of the mind control potion effect. */
	private static int mindControlPotionID = 40;
	/** The id of the font of mana potion effect. */
	private static int fontOfManaPotionID = 41;
	/** The id of the fear potion effect. */
	private static int fearPotionID = 42;
	/** The id of the magic sword enchantment. */
	private static int magicSwordEnchantmentID = 100;
	/** The id of the magic bow enchantment. */
	private static int magicBowEnchantmentID = 101;
	/** The id of the flaming weapon enchantment. */
	private static int flamingWeaponEnchantmentID = 102;
	/** The id of the flaming weapon enchantment. */
	private static int freezingWeaponEnchantmentID = 103;

	private static Comparator<ItemStack> itemSorter;
	private static Comparator<ItemStack> spellItemSorter;
	//private static Pattern entityNamePattern;

	// Creative Tabs
	public static CreativeTabs tabWizardry = new CreativeTabs("wizardry"){
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return wizardHandbook;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllReleventItems(List items) {
			// items is a list of itemstacks, not items!
			super.displayAllReleventItems(items);
			Collections.sort(items, itemSorter);
		}
	};

	public static CreativeTabs tabSpells = new CreativeTabs("wizardryspells"){
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return spellBook;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllReleventItems(List items) {
			// items is a list of itemstacks, not items!
			super.displayAllReleventItems(items);
			Collections.sort(items, spellItemSorter);
		}
	};

	/** The wizardry config file */
	public static Configuration config;

	// Anything set to use the material 'air' will not render, even with a TESR!

	// Blocks
	public final static Block arcaneWorkbench = new BlockArcaneWorkbench().setHardness(1.0F).setBlockName("arcaneWorkbench").setCreativeTab(tabWizardry);
	public final static Block crystalOre = new BlockCrystalOre(Material.rock).setHardness(3.0F).setBlockName("crystalOre").setCreativeTab(tabWizardry).setBlockTextureName("wizardry:crystal_ore");
	public final static Block petrifiedStone = new BlockStatue(Material.rock).setHardness(1.5F).setResistance(10.0F).setBlockName("petrifiedStone").setBlockTextureName("minecraft:stone");
	public final static Block iceStatue = new BlockStatue(Material.ice).setHardness(0.5F).setLightOpacity(3).setStepSound(Block.soundTypeGlass).setBlockName("iceStatue").setBlockTextureName("minecraft:ice");
	public final static Block magicLight = new BlockMagicLight(Material.circuits).setBlockName("magicLight").setBlockTextureName("minecraft:beacon");
	public final static Block crystalFlower = new BlockCrystalFlower(Material.plants).setHardness(0.0F).setBlockName("crystalFlower").setStepSound(Block.soundTypeGrass).setBlockTextureName("wizardry:crystal_flower").setCreativeTab(tabWizardry);
	public final static Block snare = new BlockSnare(Material.plants).setHardness(0.0F).setBlockName("snare").setStepSound(Block.soundTypeGrass).setBlockTextureName("wizardry:snare");
	public final static Block transportationStone = new BlockTransportationStone(Material.rock).setHardness(0.0F).setLightLevel(0.5f).setLightOpacity(0).setBlockName("transportationStone").setCreativeTab(tabWizardry).setBlockTextureName("wizardry:transportation_stone");
	public final static Block spectralBlock = new BlockSpectral(Material.glass).setBlockName("spectralBlock").setStepSound(Block.soundTypeGlass).setBlockTextureName("wizardry:spectral_block").setLightLevel(0.7f).setLightOpacity(0).setBlockUnbreakable().setResistance(6000000.0F);
	public final static Block crystalBlock = new BlockCrystal(Material.iron).setHardness(5.0F).setResistance(10.0F).setBlockName("crystalBlock").setBlockTextureName("wizardry:crystal_block").setCreativeTab(tabWizardry);
    public final static Block meteor = new Block(Material.rock){}.setBlockTextureName("wizardry:meteor").setLightLevel(1);
	public final static Block vanishingCobweb = new BlockVanishingCobweb(Material.web).setBlockName("vanishingCobweb").setBlockTextureName("minecraft:web").setLightOpacity(1).setHardness(4.0F);

	// Items
	public final static Item magicCrystal = new Item().setTextureName("wizardry:magic_crystal").setUnlocalizedName("magicCrystal").setCreativeTab(tabWizardry);

	public final static Item magicWand = new ItemWand(EnumTier.BASIC, null).setTextureName("wizardry:wand_basic").setUnlocalizedName("magicWand");
	public final static Item apprenticeWand = new ItemWand(EnumTier.APPRENTICE, null).setTextureName("wizardry:wand_apprentice").setUnlocalizedName("apprenticeWand");
	public final static Item advancedWand = new ItemWand(EnumTier.ADVANCED, null).setTextureName("wizardry:wand_advanced").setUnlocalizedName("advancedWand");
	public final static Item masterWand = new ItemWand(EnumTier.MASTER, null).setTextureName("wizardry:wand_master").setUnlocalizedName("masterWand");

	public final static Item arcaneTome = new ItemArcaneTome();
	public final static Item wizardHandbook = new ItemWizardHandbook();
	public final static Item spellBook = new ItemSpellBook();

	public final static Item basicFireWand = new ItemWand(EnumTier.BASIC, EnumElement.FIRE).setTextureName("wizardry:wand_basic_fire").setUnlocalizedName("basicFireWand");
	public final static Item basicIceWand = new ItemWand(EnumTier.BASIC, EnumElement.ICE).setTextureName("wizardry:wand_basic_ice").setUnlocalizedName("basicIceWand");
	public final static Item basicLightningWand = new ItemWand(EnumTier.BASIC, EnumElement.LIGHTNING).setTextureName("wizardry:wand_basic_lightning").setUnlocalizedName("basicLightningWand");
	public final static Item basicNecromancyWand = new ItemWand(EnumTier.BASIC, EnumElement.NECROMANCY).setTextureName("wizardry:wand_basic_necromancy").setUnlocalizedName("basicNecromancyWand");
	public final static Item basicEarthWand = new ItemWand(EnumTier.BASIC, EnumElement.EARTH).setTextureName("wizardry:wand_basic_earth").setUnlocalizedName("basicEarthWand");
	public final static Item basicSorceryWand = new ItemWand(EnumTier.BASIC, EnumElement.SORCERY).setTextureName("wizardry:wand_basic_sorcery").setUnlocalizedName("basicSorceryWand");
	public final static Item basicHealingWand = new ItemWand(EnumTier.BASIC, EnumElement.HEALING).setTextureName("wizardry:wand_basic_healing").setUnlocalizedName("basicHealingWand");

	public final static Item apprenticeFireWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.FIRE).setTextureName("wizardry:wand_apprentice_fire").setUnlocalizedName("apprenticeFireWand");
	public final static Item apprenticeIceWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.ICE).setTextureName("wizardry:wand_apprentice_ice").setUnlocalizedName("apprenticeIceWand");
	public final static Item apprenticeLightningWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.LIGHTNING).setTextureName("wizardry:wand_apprentice_lightning").setUnlocalizedName("apprenticeLightningWand");
	public final static Item apprenticeNecromancyWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.NECROMANCY).setTextureName("wizardry:wand_apprentice_necromancy").setUnlocalizedName("apprenticeNecromancyWand");
	public final static Item apprenticeEarthWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.EARTH).setTextureName("wizardry:wand_apprentice_earth").setUnlocalizedName("apprenticeEarthWand");
	public final static Item apprenticeSorceryWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.SORCERY).setTextureName("wizardry:wand_apprentice_sorcery").setUnlocalizedName("apprenticeSorceryWand");
	public final static Item apprenticeHealingWand = new ItemWand(EnumTier.APPRENTICE, EnumElement.HEALING).setTextureName("wizardry:wand_apprentice_healing").setUnlocalizedName("apprenticeHealingWand");

	public final static Item advancedFireWand = new ItemWand(EnumTier.ADVANCED, EnumElement.FIRE).setTextureName("wizardry:wand_advanced_fire").setUnlocalizedName("advancedFireWand");
	public final static Item advancedIceWand = new ItemWand(EnumTier.ADVANCED, EnumElement.ICE).setTextureName("wizardry:wand_advanced_ice").setUnlocalizedName("advancedIceWand");
	public final static Item advancedLightningWand = new ItemWand(EnumTier.ADVANCED, EnumElement.LIGHTNING).setTextureName("wizardry:wand_advanced_lightning").setUnlocalizedName("advancedLightningWand");
	public final static Item advancedNecromancyWand = new ItemWand(EnumTier.ADVANCED, EnumElement.NECROMANCY).setTextureName("wizardry:wand_advanced_necromancy").setUnlocalizedName("advancedNecromancyWand");
	public final static Item advancedEarthWand = new ItemWand(EnumTier.ADVANCED, EnumElement.EARTH).setTextureName("wizardry:wand_advanced_earth").setUnlocalizedName("advancedEarthWand");
	public final static Item advancedSorceryWand = new ItemWand(EnumTier.ADVANCED, EnumElement.SORCERY).setTextureName("wizardry:wand_advanced_sorcery").setUnlocalizedName("advancedSorceryWand");
	public final static Item advancedHealingWand = new ItemWand(EnumTier.ADVANCED, EnumElement.HEALING).setTextureName("wizardry:wand_advanced_healing").setUnlocalizedName("advancedHealingWand");

	public final static Item masterFireWand = new ItemWand(EnumTier.MASTER, EnumElement.FIRE).setTextureName("wizardry:wand_master_fire").setUnlocalizedName("masterFireWand");
	public final static Item masterIceWand = new ItemWand(EnumTier.MASTER, EnumElement.ICE).setTextureName("wizardry:wand_master_ice").setUnlocalizedName("masterIceWand");
	public final static Item masterLightningWand = new ItemWand(EnumTier.MASTER, EnumElement.LIGHTNING).setTextureName("wizardry:wand_master_lightning").setUnlocalizedName("masterLightningWand");
	public final static Item masterNecromancyWand = new ItemWand(EnumTier.MASTER, EnumElement.NECROMANCY).setTextureName("wizardry:wand_master_necromancy").setUnlocalizedName("masterNecromancyWand");
	public final static Item masterEarthWand = new ItemWand(EnumTier.MASTER, EnumElement.EARTH).setTextureName("wizardry:wand_master_earth").setUnlocalizedName("masterEarthWand");
	public final static Item masterSorceryWand = new ItemWand(EnumTier.MASTER, EnumElement.SORCERY).setTextureName("wizardry:wand_master_sorcery").setUnlocalizedName("masterSorceryWand");
	public final static Item masterHealingWand = new ItemWand(EnumTier.MASTER, EnumElement.HEALING).setTextureName("wizardry:wand_master_healing").setUnlocalizedName("masterHealingWand");

	public final static Item spectralSword = new ItemSpectralSword(ToolMaterial.IRON).setTextureName("wizardry:spectral_sword").setUnlocalizedName("spectralSword");
	public final static Item spectralPickaxe = new ItemSpectralPickaxe(ToolMaterial.IRON).setTextureName("wizardry:spectral_pickaxe").setUnlocalizedName("spectralPickaxe");
	public final static Item spectralBow = new ItemSpectralBow().setUnlocalizedName("spectralBow");

	public final static Item manaFlask = new Item(){
		@Override
		public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer entityplayer){
			entityplayer.triggerAchievement(Wizardry.craftFlask);
		}
	}.setTextureName("wizardry:mana_flask").setUnlocalizedName("manaFlask").setCreativeTab(tabWizardry);

	public final static Item storageUpgrade = new Item().setTextureName("wizardry:upgrade_storage").setUnlocalizedName("storageUpgrade").setCreativeTab(tabWizardry);
	public final static Item siphonUpgrade = new Item().setTextureName("wizardry:upgrade_siphon").setUnlocalizedName("siphonUpgrade").setCreativeTab(tabWizardry);
	public final static Item condenserUpgrade = new Item().setTextureName("wizardry:upgrade_condenser").setUnlocalizedName("condenserUpgrade").setCreativeTab(tabWizardry);
	public final static Item rangeUpgrade = new Item().setTextureName("wizardry:upgrade_range").setUnlocalizedName("rangeUpgrade").setCreativeTab(tabWizardry);
	public final static Item durationUpgrade = new Item().setTextureName("wizardry:upgrade_duration").setUnlocalizedName("durationUpgrade").setCreativeTab(tabWizardry);
	public final static Item cooldownUpgrade = new Item().setTextureName("wizardry:upgrade_cooldown").setUnlocalizedName("cooldownUpgrade").setCreativeTab(tabWizardry);
	public final static Item blastUpgrade = new Item().setTextureName("wizardry:upgrade_blast").setUnlocalizedName("blastUpgrade").setCreativeTab(tabWizardry);
	public final static Item attunementUpgrade = new Item().setTextureName("wizardry:upgrade_attunement").setUnlocalizedName("attunementUpgrade").setCreativeTab(tabWizardry);

	public final static Item magicSilk = new Item().setTextureName("wizardry:magic_silk").setUnlocalizedName("magicSilk").setCreativeTab(tabWizardry);

	public final static Item.ToolMaterial MAGICAL = EnumHelper.addToolMaterial("MAGICAL", 3, 1000, 8.0f, 4.0f, 0);

	public final static Item flamingAxe = new ItemFlamingAxe(Wizardry.MAGICAL).setTextureName("wizardry:flaming_axe").setUnlocalizedName("flamingAxe");
	public final static Item frostAxe = new ItemFrostAxe(Wizardry.MAGICAL).setTextureName("wizardry:frost_axe").setUnlocalizedName("frostAxe");

	public final static Item firebomb = new ItemFirebomb().setTextureName("wizardry:firebomb").setUnlocalizedName("firebomb");
	public final static Item poisonBomb = new ItemPoisonBomb().setTextureName("wizardry:poison_bomb").setUnlocalizedName("poisonBomb");

	public final static Item blankScroll = new Item().setTextureName("wizardry:scroll").setUnlocalizedName("blankScroll").setCreativeTab(tabWizardry);
	public final static Item scroll = new ItemScroll().setTextureName("wizardry:scroll").setUnlocalizedName("scroll").setCreativeTab(tabSpells);

	// The only way to get these is in dungeon chests (They are legendary, after all. Wizards don't just have them.
	// Also if they were sold you could buy four from the same wizard - and that's no fun at all!)
	public final static Item armourUpgrade = new ItemArmourUpgrade().setTextureName("wizardry:armour_upgrade").setUnlocalizedName("armourUpgrade");

	public final static ItemArmor.ArmorMaterial SILK = EnumHelper.addArmorMaterial("SILK", 15, new int[]{2, 5, 4, 2}, 0);

	// Saw a post somewhere that said you have to put these in the init methods rather than defining them as constants.
	// I *think* that's because the post was for a newer Minecraft version, where custom armour has its own renderer or
	// something, meaning it would be done a lot like the entity rendering registry in ClientProxy. This is all working
	// fine it seems, so I'm not going to fiddle with it, but it might be useful to know if I update versions again.
	public final static Item wizardHat = new ItemWizardArmour(Wizardry.SILK, 1, 0, null);
	public final static Item wizardRobe = new ItemWizardArmour(Wizardry.SILK, 1, 1, null);
	public final static Item wizardLeggings = new ItemWizardArmour(Wizardry.SILK, 1, 2, null);
	public final static Item wizardBoots = new ItemWizardArmour(Wizardry.SILK, 1, 3, null);

	public final static Item wizardHatFire = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.FIRE);
	public final static Item wizardRobeFire = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.FIRE);
	public final static Item wizardLeggingsFire = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.FIRE);
	public final static Item wizardBootsFire = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.FIRE);

	public final static Item wizardHatIce = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.ICE);
	public final static Item wizardRobeIce = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.ICE);
	public final static Item wizardLeggingsIce = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.ICE);
	public final static Item wizardBootsIce = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.ICE);

	public final static Item wizardHatLightning = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.LIGHTNING);
	public final static Item wizardRobeLightning = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.LIGHTNING);
	public final static Item wizardLeggingsLightning = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.LIGHTNING);
	public final static Item wizardBootsLightning = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.LIGHTNING);

	public final static Item wizardHatNecromancy = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.NECROMANCY);
	public final static Item wizardRobeNecromancy = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.NECROMANCY);
	public final static Item wizardLeggingsNecromancy = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.NECROMANCY);
	public final static Item wizardBootsNecromancy = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.NECROMANCY);

	public final static Item wizardHatEarth = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.EARTH);
	public final static Item wizardRobeEarth = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.EARTH);
	public final static Item wizardLeggingsEarth = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.EARTH);
	public final static Item wizardBootsEarth = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.EARTH);

	public final static Item wizardHatSorcery = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.SORCERY);
	public final static Item wizardRobeSorcery = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.SORCERY);
	public final static Item wizardLeggingsSorcery = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.SORCERY);
	public final static Item wizardBootsSorcery = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.SORCERY);

	public final static Item wizardHatHealing = new ItemWizardArmour(Wizardry.SILK, 1, 0, EnumElement.HEALING);
	public final static Item wizardRobeHealing = new ItemWizardArmour(Wizardry.SILK, 1, 1, EnumElement.HEALING);
	public final static Item wizardLeggingsHealing = new ItemWizardArmour(Wizardry.SILK, 1, 2, EnumElement.HEALING);
	public final static Item wizardBootsHealing = new ItemWizardArmour(Wizardry.SILK, 1, 3, EnumElement.HEALING);
	
	public final static Item spawnWizard = new ItemSpawnWizard().setUnlocalizedName("spawn_wizard");
	
	public final static Item spectralHelmet = new ItemSpectralArmour(ItemArmor.ArmorMaterial.IRON, 1, 0).setTextureName("wizardry:spectral_helmet").setUnlocalizedName("spectral_helmet");
	public final static Item spectralChestplate = new ItemSpectralArmour(ItemArmor.ArmorMaterial.IRON, 1, 1).setTextureName("wizardry:spectral_chestplate").setUnlocalizedName("spectral_chestplate");
	public final static Item spectralLeggings = new ItemSpectralArmour(ItemArmor.ArmorMaterial.IRON, 1, 2).setTextureName("wizardry:spectral_leggings").setUnlocalizedName("spectral_leggings");
	public final static Item spectralBoots = new ItemSpectralArmour(ItemArmor.ArmorMaterial.IRON, 1, 3).setTextureName("wizardry:spectral_boots").setUnlocalizedName("spectral_boots");

	public final static Item smokeBomb = new ItemSmokeBomb().setTextureName("wizardry:smoke_bomb").setUnlocalizedName("smoke_bomb");
	
	public final static Item identificationScroll = new ItemIdentificationScroll();
	
	// Potion Effects
	public static Potion frost;
	public static Potion transience;
	public static Potion fireskin;
	public static Potion iceShroud;
	public static Potion staticAura;
	public static Potion decay;
	public static Potion sixthSense;
	public static Potion arcaneJammer;
	public static Potion mindTrick;
	public static Potion mindControl;
	public static Potion fontOfMana;
	public static Potion fear;
	
	// Enchantments
	public static Enchantment magicSword;
	public static Enchantment magicBow;
	public static Enchantment flamingWeapon;
	public static Enchantment freezingWeapon;

	// Achievements
	public static final Achievement crystal = new Achievement("crystal", "Crystal", -1, -1, magicCrystal, null).registerStat();
	public static final Achievement arcaneInitiate = new Achievement("arcaneInitiate", "ArcaneInitiate", 2, -1, magicWand, crystal).registerStat();
	public static final Achievement apprentice = new Achievement("apprentice", "Apprentice", 2, 3, apprenticeWand, arcaneInitiate).registerStat();
	public static final Achievement master = new Achievement("master", "Master", 3, 6, masterWand, apprentice).registerStat();
	public static final Achievement allSpells = new Achievement("allSpells", "AllSpells", 5, 7, wizardHandbook, master).registerStat().setSpecial();
	public static final Achievement wizardTrade = new Achievement("wizardTrade", "WizardTrade", 2, -5, Items.emerald, arcaneInitiate).registerStat();
	public static final Achievement buyMasterSpell = new Achievement("buyMasterSpell", "BuyMasterSpell", 5, -5, spellBook, wizardTrade).registerStat().setSpecial();
	public static final Achievement freezeBlaze = new Achievement("freezeBlaze", "FreezeBlaze", -1, 3, Blocks.ice, apprentice).registerStat();
	public static final Achievement chargeCreeper = new Achievement("chargeCreeper", "ChargeCreeper", -1, 1, Items.gunpowder, arcaneInitiate).registerStat();
	public static final Achievement frankenstein = new Achievement("frankenstein", "Frankenstein", -3, 1, advancedLightningWand, chargeCreeper).registerStat().setSpecial();
	public static final Achievement specialUpgrade = new Achievement("specialUpgrade", "SpecialUpgrade", 4, 1, condenserUpgrade, arcaneInitiate).registerStat();
	public static final Achievement craftFlask = new Achievement("craftFlask", "CraftFlask", 4, -2, manaFlask, arcaneInitiate).registerStat();
	public static final Achievement elemental = new Achievement("elemental", "Elemental", 6, -1, basicFireWand, arcaneInitiate).registerStat();
	public static final Achievement armourSet = new Achievement("armourSet", "ArmourSet", 0, -4, wizardHat, arcaneInitiate).registerStat();
	public static final Achievement legendary = new Achievement("legendary", "Legendary", -2, -5, armourUpgrade, armourSet).registerStat().setSpecial();
	public static final Achievement selfDestruct = new Achievement("selfDestruct", "SelfDestruct", 1, 0, Blocks.pumpkin, arcaneInitiate).registerStat();
	public static final Achievement pigTornado = new Achievement("pigTornado", "PigTornado", 6, 3, Items.saddle, apprentice).registerStat().setSpecial();
	public static final Achievement jamWizard = new Achievement("jamWizard", "JamWizard", 4, 4, Blocks.web, apprentice).registerStat();
	public static final Achievement slimeSkeleton = new Achievement("slimeSkeleton", "SlimeSkeleton", 1, 5, Items.slime_ball, apprentice).registerStat();
	public static final Achievement angerWizard = new Achievement("angerWizard", "AngerWizard", 1, -7, Items.iron_sword, wizardTrade).registerStat();
	public static final Achievement defeatEvilWizard = new Achievement("defeatEvilWizard", "DefeatEvilWizard", 4, -7, wizardBootsNecromancy, wizardTrade).registerStat();
	public static final Achievement maxOutWand = new Achievement("maxOutWand", "MaxOutWand", 7, 1, arcaneTome, specialUpgrade).registerStat().setSpecial();
	public static final Achievement elementMaster = new Achievement("elementMaster", "ElementMaster", 7, -3, masterIceWand, elemental).registerStat().setSpecial();
	public static final Achievement identifySpell = new Achievement("identifySpell", "IdentifySpell", -2, -3, identificationScroll, arcaneInitiate).registerStat();

	private static final Achievement[] achievementsList = {crystal, arcaneInitiate, apprentice, master, allSpells, wizardTrade, buyMasterSpell, freezeBlaze, chargeCreeper,
			frankenstein, specialUpgrade, craftFlask, elemental, armourSet, legendary, selfDestruct, pigTornado, jamWizard, slimeSkeleton, angerWizard, defeatEvilWizard,
			maxOutWand, elementMaster, identifySpell};
	public static final AchievementPage wizardryAchievementTab = new AchievementPage("Wizardry", achievementsList);

	// EventManager
	WizardryWorldGenerator oreManager = new WizardryWorldGenerator();

	// The instance of your mod that Forge uses.
	@Instance(value = Wizardry.MODID)
	public static Wizardry instance;

	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="electroblob.wizardry.client.ClientProxy", serverSide="electroblob.wizardry.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		
		expandPotionTypesArray();

		// Config file

		/* The first part of the config file has to be done here (as is conventional) so that the various registries
		 * can change what they do accordingly. The spell part of the config has to be done in the init method. */

		config = new Configuration(event.getSuggestedConfigurationFile());

		config.load();

		setupGeneralConfig();

		config.save();

		WizardryRegistry.registerBlocks();

		WizardryRegistry.registerItems();

		WizardryRegistry.registerTileEntities();

		WizardryRegistry.registerEntities(this);

		WizardryRegistry.registerSpells();
		// The check for the generateLoot setting is now done within this method.
		WizardryRegistry.registerLoot();

		// Potion effects
		frost = new PotionFrost(frostPotionID, true, 0x38ddec);
		transience = new PotionMagicEffect(transiencePotionID, false, 0xffe89b, 0).setPotionName("potion.transience");
		fireskin = new PotionMagicEffect(fireskinPotionID, false, 0xff2f02, 1).setPotionName("potion.fireskin");
		iceShroud = new PotionMagicEffect(iceShroudPotionID, false, 0x52f1ff, 2).setPotionName("potion.ice_shroud");
		staticAura = new PotionMagicEffect(staticAuraPotionID, false, 0x0070ff, 3).setPotionName("potion.static_aura");
		decay = new PotionDecay(decayPotionID, true, 0x3c006c);
		sixthSense = new PotionMagicEffect(sixthSensePotionID, false, 0xc6ff01, 4).setPotionName("potion.sixth_sense");
		arcaneJammer = new PotionMagicEffect(arcaneJammerPotionID, false, 0xcf4aa2, 5).setPotionName("potion.arcane_jammer");
		mindTrick = new PotionMagicEffect(mindTrickPotionID, true, 0x601683, 6).setPotionName("potion.mind_trick");
		mindControl = new PotionMagicEffect(mindControlPotionID, true, 0x320b44, 7).setPotionName("potion.mind_control");
		fontOfMana = new PotionMagicEffect(fontOfManaPotionID, false, 0xffe5bb, 8).setPotionName("potion.font_of_mana");
		fear = new PotionMagicEffect(fearPotionID, true, 0xbd0100, 9).setPotionName("potion.fear");

		// Enchantments
		magicSword = new EnchantmentMagicSword(magicSwordEnchantmentID);
		magicBow = new EnchantmentTimed(magicBowEnchantmentID).setName("magic_bow");
		flamingWeapon = new EnchantmentTimed(flamingWeaponEnchantmentID).setName("flaming_weapon");
		freezingWeapon = new EnchantmentTimed(freezingWeaponEnchantmentID).setName("freezing_weapon");
		
		// Creative tab sorter

		List<Item> orderedItemList = Arrays.asList(

				Item.getItemFromBlock(arcaneWorkbench),
				Item.getItemFromBlock(crystalOre),
				Item.getItemFromBlock(crystalBlock),
				Item.getItemFromBlock(crystalFlower),
				Item.getItemFromBlock(transportationStone),
				magicCrystal, magicWand, apprenticeWand, advancedWand, masterWand, arcaneTome, wizardHandbook, spawnWizard,
				basicFireWand, basicIceWand, basicLightningWand, basicNecromancyWand, basicEarthWand, basicSorceryWand, basicHealingWand,
				smokeBomb, firebomb, poisonBomb, blankScroll, identificationScroll, manaFlask, storageUpgrade, siphonUpgrade,
				condenserUpgrade, rangeUpgrade, durationUpgrade, cooldownUpgrade, blastUpgrade, attunementUpgrade, magicSilk, armourUpgrade,
				wizardHat, wizardRobe, wizardLeggings, wizardBoots,
				wizardHatFire, wizardRobeFire, wizardLeggingsFire, wizardBootsFire,
				wizardHatIce, wizardRobeIce, wizardLeggingsIce, wizardBootsIce,
				wizardHatLightning, wizardRobeLightning, wizardLeggingsLightning, wizardBootsLightning,
				wizardHatNecromancy, wizardRobeNecromancy, wizardLeggingsNecromancy, wizardBootsNecromancy,
				wizardHatEarth, wizardRobeEarth, wizardLeggingsEarth, wizardBootsEarth,
				wizardHatSorcery, wizardRobeSorcery, wizardLeggingsSorcery, wizardBootsSorcery,
				wizardHatHealing, wizardRobeHealing, wizardLeggingsHealing, wizardBootsHealing

				);

		itemSorter = Ordering.explicit(orderedItemList).onResultOf(new Function<ItemStack, Item>() {
			@Override
			public Item apply(ItemStack input) {
				return input.getItem();
			}
		});
		
		spellItemSorter = new Comparator<ItemStack>(){
			@Override
			public int compare(ItemStack stack1, ItemStack stack2) {
				
				if((stack1.getItem() instanceof ItemSpellBook && stack2.getItem() instanceof ItemSpellBook)
						|| (stack1.getItem() instanceof ItemScroll && stack2.getItem() instanceof ItemScroll)){
					
					Spell spell1 = Spell.get(stack1.getItemDamage());
					Spell spell2 = Spell.get(stack2.getItemDamage());
					
					return spell1.compareTo(spell2);
					
				}else if(stack1.getItem() instanceof ItemScroll){
					return 1;
				}else if(stack2.getItem() instanceof ItemScroll){
					return -1;
				}
				return 0;
			}
		};

	}

	@EventHandler
	public void init(FMLInitializationEvent event){

		// Spell config (this has to be here and not in preInit because all spells must be registered before it is
		// added, including those in other mods.)

		//config.load();

		setupSpellsConfig();

		config.save();

		proxy.registerKeyBindings();

		// Event Handlers
		GameRegistry.registerWorldGenerator(oreManager, 0);
		FMLCommonHandler.instance().bus().register(new WizardryKeyHandler());
		FMLCommonHandler.instance().bus().register(instance);
		proxy.registerEventHandlers();
		proxy.registerSpellHUD();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new WizardryGuiHandler());
		WizardryPacketHandler.initPackets();

		// Achievements
		AchievementPage.registerAchievementPage(wizardryAchievementTab);

		WizardryRegistry.registerRecipes();

		proxy.registerRenderers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		// To ensure backwards compatibility, the spell books and scrolls themselves are now sorted in the creative tab,
		// and the spells are left in whatever order they were registered.
		//Spell.sortSpells();
		// This needs to be here or it won't necessarily include all the mods' entities.
		/* Doesn't seem to be doing anything...
		String entityNames = "";
		for(Object name : EntityList.classToStringMapping.values()){
			if(name instanceof String){
				entityNames = entityNames + name + '|';
			}
		}
		// Cuts off the last '|'
		entityNames = entityNames.substring(0, entityNames.length()-1);
		entityNamePattern = Pattern.compile(entityNames);
		*/
	}
	
	@EventHandler
    public void serverStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new CommandCastSpell());
        event.registerServerCommand(new CommandSetAlly());
        event.registerServerCommand(new CommandViewAllies());
        event.registerServerCommand(new CommandDiscoverSpell());
    }

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs){
		if(eventArgs.modID.equals(Wizardry.MODID)) Wizardry.syncConfig();
	}

	/** Call to sync the config file after it has been edited in game from the menus. */
	public static void syncConfig(){

		setupGeneralConfig();

		setupSpellsConfig();

		config.save();

	}
	
	// As of wizardry 1.1, all keys used in the config file itself are now hardcoded, not localised. The localisations
	// are done in the config GUI. The config file has to be written in one language or it will end up with multiple
	// options for different languages.

	private static void setupSpellsConfig(){

		config.addCustomCategoryComment(Wizardry.SPELLS_CATEGORY, "Set a spell to false to disable it. Disabled spells will still have their associated spell book (mainly so the spell books don't all change) and can still be bound to wands, but cannot be cast in game, will not appear in any subsequently generated chests or wizard trades and will not drop from mobs. Disable a spell if it is causing problems, conflicts with another mod or creates an unintended exploit.");

		Property property;
		
		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			property = config.get(Wizardry.SPELLS_CATEGORY, spell.getUnlocalisedName(), true, spell.getDescription());
			property.setLanguageKey("spell." + spell.getUnlocalisedName());
			spell.setEnabled(property.getBoolean());
		}

	}

	private static void setupGeneralConfig(){
		
		// This trick is borrowed from forge; it sorts the config options into the order you want them.
		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(config.CATEGORY_GENERAL, "Please note that changing some of these settings may make the mod very difficult to play.");

		property = config.get(config.CATEGORY_GENERAL, "towerRarity", 8, "Rarity of wizard towers. Higher numbers are rarer. Set to 0 to disable wizard towers completely.", 0, 50);
		property.setLanguageKey("config.tower_rarity");
		property.setRequiresWorldRestart(true);
		Wizardry.proxy.setToNumberSliderEntry(property);
		towerRarity = property.getInt();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "spellBookDropChance", 3, "The chance for mobs to drop a spell book when killed. The greater this number, the more often they will drop. Set to 0 to disable spell book drops. Set to 200 for guaranteed drops.", 0, 200);
		property.setLanguageKey("config.spell_book_drop_chance");
		Wizardry.proxy.setToNumberSliderEntry(property);
		spellBookDropChance = property.getInt();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "oreDimensions", oreDimensions, "List of dimension ids in which crystal ore will generate. Note that removing the overworld (id 0) from this list will make the mod VERY difficult to play!");
		property.setLanguageKey("config.ore_dimensions");
		property.setRequiresWorldRestart(true);
		oreDimensions = property.getIntList();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "flowerDimensions", flowerDimensions, "List of dimension ids in which crystal flowers will generate.");
		property.setLanguageKey("config.flower_dimensions");
		property.setRequiresWorldRestart(true);
		flowerDimensions = property.getIntList();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "towerDimensions", towerDimensions, "List of dimension ids in which wizard towers will generate.");
		property.setLanguageKey("config.tower_dimensions");
		property.setRequiresWorldRestart(true);
		towerDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "generateLoot", true, "Whether to generate wizardry loot in dungeon chests.");
		property.setLanguageKey("config.generate_loot");
		property.setRequiresWorldRestart(true);
		generateLoot = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "firebombIsCraftable", true, "Whether firebombs can be crafted or not.");
		property.setLanguageKey("config.firebomb_is_craftable");
		property.setRequiresMcRestart(true);
		firebombIsCraftable = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "poisonBombIsCraftable", true, "Whether poison bombs can be crafted or not.");
		property.setLanguageKey("config.poison_bomb_is_craftable");
		property.setRequiresMcRestart(true);
		poisonBombIsCraftable = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "smokeBombIsCraftable", true, "Whether smoke bombs can be crafted or not.");
		property.setLanguageKey("config.smoke_bomb_is_craftable");
		property.setRequiresMcRestart(true);
		smokeBombIsCraftable = property.getBoolean();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "useAlternateScrollRecipe", false, "Whether to require a magic crystal in the shapeless crafting recipe for blank scrolls. Set to true if another mod adds a conflicting recipe.");
		property.setLanguageKey("config.use_alternate_scroll_recipe");
		property.setRequiresMcRestart(true);
		useAlternateScrollRecipe = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "teleportThroughUnbreakableBlocks", false, "Whether players are allowed to teleport through unbreakable blocks (e.g. bedrock) using the phase step spell.");
		property.setLanguageKey("config.teleport_through_unbreakable_blocks");
		teleportThroughUnbreakableBlocks = property.getBoolean();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "showSummonedCreatureNames", true, "Whether to show summoned creatures' names and owners above their heads.");
		property.setLanguageKey("config.show_summoned_creature_names");
		showSummonedCreatureNames = property.getBoolean();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "friendlyFire", true, "Whether to allow players to damage their designated allies using magic.");
		property.setLanguageKey("config.friendly_fire");
		friendlyFire = property.getBoolean();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "telekineticDisarmament", true, "Whether to allow players to disarm other players using the telekinesis spell. Set to false to prevent stealing of items.");
		property.setLanguageKey("config.telekinetic_disarmament");
		telekineticDisarmament = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "discoveryMode", true, "For those who like a sense of mystery! When set to true, spells you haven't cast yet will be unreadable until you cast them (on a per-world basis). Has no effect when in creative mode. Spells of identification will be unobtainable in survival mode if this is false.");
		property.setLanguageKey("config.discovery_mode");
		property.setRequiresWorldRestart(true);
		discoveryMode = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "enableShiftScrolling", true, "Whether you can switch between spells on a wand by scrolling with the mouse wheel while sneaking. Note that this will only affect you; other players connected to the same server obey their own settings.");
		property.setLanguageKey("config.enable_shift_scrolling");
		property.setRequiresWorldRestart(false);
		enableShiftScrolling = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "minionRevengeTargeting", true, "Whether summoned creatures can revenge attack their owner if their owner attacks them.");
		property.setLanguageKey("config.minion_revenge_targeting");
		property.setRequiresWorldRestart(false);
		minionRevengeTargeting = property.getBoolean();
		propOrder.add(property.getName());
		
		// These two aren't sliders because using a slider makes it difficult to fine-tune the numbers; the nature of a
		// scaling factor means that 0.5 is as big a change as 2.0, so whilst a slider is fine for increasing the damage,
		// it doesn't give fine enough control for values less than 1.
		property = config.get(config.CATEGORY_GENERAL, "playerDamageScaling", 1.0, "Global damage scaling factor for the damage dealt by players casting spells, relative to 1.", 0, 20);
		property.setLanguageKey("config.player_damage_scaling");
		playerDamageScale = property.getDouble();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "npcDamageScaling", 1.0, "Global damage scaling factor for the damage dealt by NPCs casting spells, relative to 1.", 0, 20);
		property.setLanguageKey("config.npc_damage_scaling");
		npcDamageScale = property.getDouble();
		propOrder.add(property.getName());
		
		// This one isn't a slider either because people are likely to want exact values ("it must be at most 50.3" is a bit strange!).
		property = config.get(config.CATEGORY_GENERAL, "castCommandMultiplierLimit", 20.0, "Upper limit for the multipliers passed into the /cast command. This is here to stop players from accidentally breaking a world/server. Large blast mutipliers can cause extreme lag - you have been warned!", 1, 255);
		property.setLanguageKey("config.cast_command_multiplier_limit");
		maxSpellCommandMultiplier = property.getDouble();
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "summonedCreatureTargetsWhitelist", summonedCreatureTargetsWhitelist, "List of names of entities which summoned creatures and wizards are allowed to attack, in addition to the defaults. Add mod creatures to this list if you want summoned creatures to attack them and they aren't already doing so. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.summoned_creature_targets_whitelist");
		property.setRequiresWorldRestart(true);
		summonedCreatureTargetsWhitelist = property.getStringList();
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<summonedCreatureTargetsWhitelist.length; i++){
			summonedCreatureTargetsWhitelist[i] = summonedCreatureTargetsWhitelist[i].toLowerCase(Locale.ROOT).trim();
		}
		propOrder.add(property.getName());
		
		property = config.get(config.CATEGORY_GENERAL, "summonedCreatureTargetsBlacklist", summonedCreatureTargetsBlacklist, "List of names of entities which summoned creatures and wizards are specifically not allowed to attack, overriding the defaults and the whitelist. Add creatures to this list if allowing them to be attacked causes problems or is too destructive (removing creepers from this list is done at your own risk!). Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.summoned_creature_targets_blacklist");
		property.setRequiresWorldRestart(true);
		summonedCreatureTargetsBlacklist = property.getStringList();
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<summonedCreatureTargetsBlacklist.length; i++){
			summonedCreatureTargetsBlacklist[i] = summonedCreatureTargetsBlacklist[i].toLowerCase(Locale.ROOT).trim();
		}
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "spellHUDPosition", spellHUDPosition, "The position of the spell HUD.", new String[]{"Bottom left", "Top left", "Top right", "Bottom right"});
		property.setLanguageKey("config.spell_hud_position");
		spellHUDPosition = property.getString();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "castCommandName", castCommandName, "The name of the /cast command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /cast you would type /magic instead.");
		property.setLanguageKey("config.cast_command_name");
		castCommandName = property.getString();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "discoverspellCommandName", discoverspellCommandName, "The name of the /discoverspell command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /discoverspell you would type /magic instead.");
		property.setLanguageKey("config.discoverspell_command_name");
		discoverspellCommandName = property.getString();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "allyCommandName", allyCommandName, "The name of the /ally command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /ally you would type /magic instead.");
		property.setLanguageKey("config.ally_command_name");
		allyCommandName = property.getString();
		propOrder.add(property.getName());

		property = config.get(config.CATEGORY_GENERAL, "alliesCommandName", alliesCommandName, "The name of the /allies command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /allies you would type /magic instead.");
		property.setLanguageKey("config.allies_command_name");
		alliesCommandName = property.getString();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(config.CATEGORY_GENERAL, propOrder);
		
		// Resistances
		
		List<String> propOrder1 = new ArrayList<String>();
		
		config.addCustomCategoryComment(Wizardry.RESISTANCES_CATEGORY, "These options allow entities to be made immune to certain types of magic.");

		property = config.get(Wizardry.RESISTANCES_CATEGORY, "mobsImmuneToFire", new String[]{}, "List of names of entities that are immune to fire, in addition to the defaults. Add mod creatures to this list if you want them to be immune to fire magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.mobs_immune_to_fire");
		property.setRequiresMcRestart(true);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity((Class<? extends Entity>)EntityList.stringToClassMapping.get(property.getStringList()[i]), DamageType.FIRE);
		}
		propOrder1.add(property.getName());
		
		property = config.get(Wizardry.RESISTANCES_CATEGORY, "mobsImmuneToIce", new String[]{}, "List of names of entities that are immune to ice, in addition to the defaults. Add mod creatures to this list if you want them to be immune to ice magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.mobs_immune_to_ice");
		property.setRequiresMcRestart(true);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity((Class<? extends Entity>)EntityList.stringToClassMapping.get(property.getStringList()[i]), DamageType.FROST);
		}
		propOrder1.add(property.getName());
		
		property = config.get(Wizardry.RESISTANCES_CATEGORY, "mobsImmuneToLightning", new String[]{}, "List of names of entities that are immune to lightning, in addition to the defaults. Add mod creatures to this list if you want them to be immune to lightning magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.mobs_immune_to_lightning");
		property.setRequiresMcRestart(true);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity((Class<? extends Entity>)EntityList.stringToClassMapping.get(property.getStringList()[i]), DamageType.SHOCK);
		}
		propOrder1.add(property.getName());
		
		property = config.get(Wizardry.RESISTANCES_CATEGORY, "mobsImmuneToWither", new String[]{}, "List of names of entities that are immune to wither effects, in addition to the defaults. Add mod creatures to this list if you want them to be immune to withering magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.mobs_immune_to_wither");
		property.setRequiresMcRestart(true);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity((Class<? extends Entity>)EntityList.stringToClassMapping.get(property.getStringList()[i]), DamageType.WITHER);
		}
		propOrder1.add(property.getName());
		
		property = config.get(Wizardry.RESISTANCES_CATEGORY, "mobsImmuneToPoison", new String[]{}, "List of names of entities that are immune to poison, in addition to the defaults. Add mod creatures to this list if you want them to be immune to poison magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. wizardry:Wizard).");
		property.setLanguageKey("config.mobs_immune_to_poison");
		property.setRequiresMcRestart(true);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i=0; i<property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity((Class<? extends Entity>)EntityList.stringToClassMapping.get(property.getStringList()[i]), DamageType.POISON);
		}
		propOrder1.add(property.getName());
		
		config.setCategoryPropertyOrder(Wizardry.RESISTANCES_CATEGORY, propOrder1);
		
		// IDs
		
		List<String> propOrder2 = new ArrayList<String>();
		
		config.addCustomCategoryComment(Wizardry.IDS_CATEGORY, "Change these IDs if they conflict with another mod.");

		property = config.get(Wizardry.IDS_CATEGORY, "frostPotionID", 31, "The ID of the frost potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.frost_potion_id");
		property.setRequiresMcRestart(true);
		frostPotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "transiencePotionID", 32, "The ID of the transience potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.transience_potion_id");
		property.setRequiresMcRestart(true);
		transiencePotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "fireskinPotionID", 33, "The ID of the fireskin potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.fireskin_potion_id");
		property.setRequiresMcRestart(true);
		fireskinPotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "iceShroudPotionID", 34, "The ID of the ice shroud potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.ice_shroud_potion_id");
		property.setRequiresMcRestart(true);
		iceShroudPotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "staticAuraPotionID", 35, "The ID of the static aura potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.static_aura_potion_id");
		property.setRequiresMcRestart(true);
		staticAuraPotionID = property.getInt();
		propOrder2.add(property.getName());

		property = config.get(Wizardry.IDS_CATEGORY, "decayPotionID", 36, "The ID of the decay potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.decay_potion_id");
		property.setRequiresMcRestart(true);
		decayPotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "sixthSensePotionID", 37, "The ID of the sixth sense potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.sixth_sense_potion_id");
		property.setRequiresMcRestart(true);
		sixthSensePotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "arcaneJammerPotionID", 38, "The ID of the arcane jammer potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.arcane_jammer_potion_id");
		property.setRequiresMcRestart(true);
		arcaneJammerPotionID = property.getInt();
		propOrder2.add(property.getName());

		property = config.get(Wizardry.IDS_CATEGORY, "mindTrickPotionID", 39, "The ID of the mind trick potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.mind_trick_potion_id");
		property.setRequiresMcRestart(true);
		mindTrickPotionID = property.getInt();
		propOrder2.add(property.getName());

		property = config.get(Wizardry.IDS_CATEGORY, "mindControlPotionID", 40, "The ID of the mind control potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.mind_control_potion_id");
		property.setRequiresMcRestart(true);
		mindControlPotionID = property.getInt();
		propOrder2.add(property.getName());

		property = config.get(Wizardry.IDS_CATEGORY, "fontOfManaPotionID", 41, "The ID of the font of mana potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.font_of_mana_potion_id");
		property.setRequiresMcRestart(true);
		fontOfManaPotionID = property.getInt();
		propOrder2.add(property.getName());

		property = config.get(Wizardry.IDS_CATEGORY, "fearPotionID", 42, "The ID of the fear potion effect. Change if this conflicts with another mod.", 24, 255);
		property.setLanguageKey("config.fear_potion_id");
		property.setRequiresMcRestart(true);
		fearPotionID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "magicSwordEnchantmentID", 100, "The ID of the magic sword enchantment. Change if this conflicts with another mod.", 63, 255);
		property.setLanguageKey("config.magic_sword_enchantment_id");
		property.setRequiresMcRestart(true);
		magicSwordEnchantmentID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "magicBowEnchantmentID", 101, "The ID of the magic bow enchantment. Change if this conflicts with another mod.", 63, 255);
		property.setLanguageKey("config.magic_bow_enchantment_id");
		property.setRequiresMcRestart(true);
		magicBowEnchantmentID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "flamingWeaponEnchantmentID", 102, "The ID of the flaming weapon enchantment. Change if this conflicts with another mod.", 63, 255);
		property.setLanguageKey("config.flaming_weapon_enchantment_id");
		property.setRequiresMcRestart(true);
		flamingWeaponEnchantmentID = property.getInt();
		propOrder2.add(property.getName());
		
		property = config.get(Wizardry.IDS_CATEGORY, "freezingWeaponEnchantmentID", 103, "The ID of the freezing weapon enchantment. Change if this conflicts with another mod.", 63, 255);
		property.setLanguageKey("config.freezing_weapon_enchantment_id");
		property.setRequiresMcRestart(true);
		freezingWeaponEnchantmentID = property.getInt();
		propOrder2.add(property.getName());
		
		config.setCategoryPropertyOrder(Wizardry.IDS_CATEGORY, propOrder2);

	}

	private static void expandPotionTypesArray(){
		
		Potion[] potionTypes = null;

		for(Field f : Potion.class.getDeclaredFields()){
			f.setAccessible(true);
			// Notice that this already takes the obfuscation into account.
			try{
				if(f.getName().equals("potionTypes") || f.getName().equals("field_76425_a")) {
					Field modfield = Field.class.getDeclaredField("modifiers");
					modfield.setAccessible(true);
					modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);

					potionTypes = (Potion[])f.get(null);
					final Potion[] newPotionTypes = new Potion[256];
					System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
					f.set(null, newPotionTypes);
				}
			}
			catch (Exception e) {
				System.err.println("Something went very wrong! Error while expanding potion types array:");
				e.printStackTrace();
			}
		}
	}
	
}