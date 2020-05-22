package electroblob.wizardry;

import electroblob.wizardry.packet.PacketSyncSettings;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.AllyDesignationSystem.FriendlyFire;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Singleton class which deals with everything related to wizardry's config file. To access individual settings, use
 * {@link Wizardry#settings}. Also stores a few string constants for easy access.
 * <p></p>
 * As part of the 2.1 update and code overhaul, the way the config settings work in multiplayer has been tightened up.
 * Importantly, there are <b>three</b> different types of config options:
 * <p></p>
 * <li>Server-only settings. These only affect server-side code and hence are not synced. Changing these locally only
 * has an effect if the local game is the host, i.e. a dedicated server, a LAN host or a singleplayer world. Examples
 * include worldgen, mob drops and commands.
 * <li>Synchronised settings. These settings affect both client-side AND server-side code, and are synchronised with
 * each client on login via {@link Settings#sync(EntityPlayerMP)}. Changing these locally only has an effect if the
 * local game is the host, i.e. a dedicated server, a LAN host or a singleplayer world. Examples include discovery mode
 * and crafting recipes. <i> Note that as far as users are concerned, there is no difference in behaviour between
 * server-only and synchronised settings.</i>
 * <li>Client-only settings. These settings only affect client-side code and hence are not synced. Each client obeys its
 * own values for these, and changing them on a dedicated server will have no effect. These are usually only display and
 * controls settings.</li>
 * <p></p>
 * Each of the settings fields in this class is marked with one of the above categories to indicate which it belongs to.
 * This in turn dictates which logical side it should be called from: client, server or both. <b> Do not access a config
 * setting from the wrong side, because it may cause unexpected or strange behaviour.</b>
 * 
 * @since Wizardry 1.2
 * @author Electroblob
 */
// For the time being, I'm sticking with the old config system because @Config doesn't support custom config entry classes
// @Config(modid = Wizardry.MODID)
public final class Settings {

	// Category names
	/** The unlocalised name of the gameplay config category. */
	public static final String GAMEPLAY_CATEGORY = "gameplay";
	/** The unlocalised name of the difficulty config category. */
	public static final String DIFFICULTY_CATEGORY = "difficulty";
	/** The unlocalised name of the tweaks config category. */
	public static final String TWEAKS_CATEGORY = "tweaks";
	/** The unlocalised name of the spells config category. */
	public static final String SPELLS_CATEGORY = "spells";
	/** The unlocalised name of the resistances config category. */
	public static final String RESISTANCES_CATEGORY = "resistances";
	/** The unlocalised name of the client config category. */
	public static final String CLIENT_CATEGORY = "client";
	/** The unlocalised name of the commands config category. */
	public static final String COMMANDS_CATEGORY = "commands";
	/** The unlocalised name of the worldgen config category. */
	public static final String WORLDGEN_CATEGORY = "worldgen";
	/** The unlocalised name of the compatibility config category. */
	public static final String COMPATIBILITY_CATEGORY = "compatibility";

	private static final String[] DEFAULT_TREE_BLOCKS = {"dynamictrees:oakbranch", "dynamictrees:sprucebranch",
			"dynamictrees:birchbranch", "dynamictrees:junglebranch", "dynamictrees:darkoakbranch",
			"dynamictrees:acaciabranch", "dynamictrees:cactusbranch", "dynamictrees:leaves0", "dynamictrees:leaves1"};

	private static final String[] DEFAULT_LOOT_INJECTION_LOCATIONS = {"minecraft:chests/simple_dungeon",
			"minecraft:chests/abandoned_mineshaft", "minecraft:chests/desert_pyramid", "minecraft:chests/jungle_temple",
			"minecraft:chests/stronghold_corridor", "minecraft:chests/stronghold_crossing",
			"minecraft:chests/stronghold_library", "minecraft:chests/igloo_chest", "minecraft:chests/woodland_mansion",
			"minecraft:chests/end_city_treasure"};

	/** The wizardry config file. */
	private Configuration config;

	// Server-only settings. These only affect server-side code and hence are not synced. Changing these locally only
	// has an effect if the local game is the host, i.e. a dedicated server, a LAN host or a singleplayer world.

	// Worldgen
	/** <b>[Server-only]</b> Whether to use faster worldgen at the cost of 'seamlessness'. */
	public boolean fastWorldgen = false;
	/** <b>[Server-only]</b> List of dimension ids in which to generate wizard towers. */
	public int[] towerDimensions = {0};
	/** <b>[Server-only]</b> The rarity of wizard towers, used by the world generator. Larger numbers are rarer. */
	public int towerRarity = 700;
	/** <b>[Server-only]</b> List of structure file locations for wizard towers without loot chests. */
	public ResourceLocation[] towerFiles = {new ResourceLocation(Wizardry.MODID, "wizard_tower_0"),
			new ResourceLocation(Wizardry.MODID, "wizard_tower_1"),
			new ResourceLocation(Wizardry.MODID, "wizard_tower_2"),
			new ResourceLocation(Wizardry.MODID, "wizard_tower_3")};
	/** <b>[Server-only]</b> List of structure file locations for wizard towers with loot chests. */
	public ResourceLocation[] towerWithChestFiles = {new ResourceLocation(Wizardry.MODID, "wizard_tower_chest_0"),
			new ResourceLocation(Wizardry.MODID, "wizard_tower_chest_1"),
			new ResourceLocation(Wizardry.MODID, "wizard_tower_chest_2"),
			new ResourceLocation(Wizardry.MODID, "wizard_tower_chest_3")};
	/** <b>[Server-only]</b> List of dimension ids in which to generate obelisks. */
	public int[] obeliskDimensions = {0, -1};
	/** <b>[Server-only]</b> The rarity of obelisks, used by the world generator. Larger numbers are rarer. */
	public int obeliskRarity = 600;
	/** <b>[Server-only]</b> List of structure file locations for obelisks. */
	public ResourceLocation[] obeliskFiles = {new ResourceLocation(Wizardry.MODID, "obelisk_0"),
			new ResourceLocation(Wizardry.MODID, "obelisk_1"),
			new ResourceLocation(Wizardry.MODID, "obelisk_2"),
			new ResourceLocation(Wizardry.MODID, "obelisk_3"),
			new ResourceLocation(Wizardry.MODID, "obelisk_4")};
	/** <b>[Server-only]</b> List of dimension ids in which to generate shrines. */
	public int[] shrineDimensions = {0, -1};
	/** <b>[Server-only]</b> The rarity of shrines, used by the world generator. Larger numbers are rarer. */
	public int shrineRarity = 1100;
	/** <b>[Server-only]</b> List of structure file locations for shrines. */
	public ResourceLocation[] shrineFiles = {new ResourceLocation(Wizardry.MODID, "shrine_0"),
			new ResourceLocation(Wizardry.MODID, "shrine_1"),
			new ResourceLocation(Wizardry.MODID, "shrine_2"),
			new ResourceLocation(Wizardry.MODID, "shrine_3"),
			new ResourceLocation(Wizardry.MODID, "shrine_4"),
			new ResourceLocation(Wizardry.MODID, "shrine_5"),
			new ResourceLocation(Wizardry.MODID, "shrine_6"),
			new ResourceLocation(Wizardry.MODID, "shrine_7")};
	/** <b>[Server-only]</b> List of solid blocks (usually trees) which are ignored by the structure generators. */
	public Pair<ResourceLocation, Short>[] treeBlocks = parseItemMetaStrings(DEFAULT_TREE_BLOCKS);
	/** <b>[Server-only]</b> The chance for wizard towers to generate with an evil wizard and chest inside. */
	public double evilWizardChance = 0.2;
	/** <b>[Server-only]</b> List of dimension ids in which to generate crystal ore. */
	public int[] oreDimensions = {0};
	/** <b>[Server-only]</b> List of dimension ids in which to generate crystal flowers. */
	public int[] flowerDimensions = {0};
	/**
	 * <b>[Server-only]</b> List of resource location strings for loot tables to inject wizardry loot into. Note that
	 * this does not affect the generation of loot in wizard towers.
	 */
	public ResourceLocation[] lootInjectionLocations = toResourceLocations(DEFAULT_LOOT_INJECTION_LOCATIONS);

	// Entities' drops, targeting, damage, etc.
	/** <b>[Server-only]</b> Whitelist for loot tables to inject additional mob drops into. */
	public ResourceLocation[] mobLootTableWhitelist = {};
	/** <b>[Server-only]</b> Blacklist for loot tables to inject additional mob drops into. */
	public ResourceLocation[] mobLootTableBlacklist = toResourceLocations("entities/vex", "entities/ender_dragon",
			"entities/wither", "entities/silverfish", "entities/endermite",
			Wizardry.MODID + "entities/evil_wizard");
	/**
	 * <b>[Server-only]</b> Whether or not players can teleport through unbreakable blocks (e.g. bedrock) using the
	 * phase step spell.
	 */
	public boolean teleportThroughUnbreakableBlocks = false;
	/** <b>[Server-only]</b> Whether to allow players to damage their designated allies using magic. */
	public FriendlyFire friendlyFire = FriendlyFire.ALL;
	/** <b>[Server-only]</b> Whether to allow players to disarm other players using the telekinesis spell. */
	public boolean telekineticDisarmament = true;
	/** <b>[Server-only]</b> Whether summoned creatures can revenge attack their caster if their caster attacks them. */
	public boolean minionRevengeTargeting = true;
	/** <b>[Server-only]</b> Whether to allow players to change the world time using the speed time spell. */
	public boolean worldTimeManipulation = true;
	/** <b>[Server-only]</b> Whether to allow players to move other players around using magic. */
	public boolean playersMoveEachOther = true;
	/** <b>[Server-only]</b> Whether spells cast by players can destroy blocks in the world. */
	public boolean playerBlockDamage = true;
	/** <b>[Server-only]</b> Whether to revert to the old wand upgrade system, which only requires tomes of arcana. */
	public boolean legacyWandLevelling = false;
	/**
	 * <b>[Server-only]</b> Whether to replace Minecraft's distance-based fall damage calculation with an equivalent,
	 * velocity-based one.
	 */
	public boolean replaceVanillaFallDamage = true; // TODO: This should be synced
	/** <b>[Server-only]</b> Whether using bonemeal on grass blocks has a chance to grow crystal flowers. */
	public boolean bonemealGrowsCrystalFlowers = true;
	/**
	 * <b>[Server-only]</b> List of registry names of entities which summoned creatures are allowed to attack, in addition
	 * to the defaults.
	 */
	public ResourceLocation[] summonedCreatureTargetsWhitelist = {};
	/**
	 * <b>[Server-only]</b> List of registry names of entities which summoned creatures are specifically not allowed to
	 * attack, overriding the defaults and the whitelist.
	 */
	public ResourceLocation[] summonedCreatureTargetsBlacklist = toResourceLocations("creeper");
	/**
	 * <b>[Server-only]</b> List of registry names of entities which are immune to the mind control spell, in addition to
	 * the defaults.
	 */
	public ResourceLocation[] mindControlTargetsBlacklist = {};
	/**
	 * <b>[Server-only]</b> List of registry names of items which cannot be smelted by the pocket furnace spell, in
	 * addition to armour, tools and weapons.
	 */
	public Pair<ResourceLocation, Short>[] pocketFurnaceItemBlacklist = parseItemMetaStrings("cobblestone", "netherrack");
	/** <b>[Server-only]</b> List of registry names of blocks which can be detected by the divination spell. */
	public Pair<ResourceLocation, Short>[] divinationOreWhitelist = parseItemMetaStrings(); // That works I guess
	/** <b>[Server-only]</b> List of registry names of items which count as swords for imbuement spells. */
	public Pair<ResourceLocation, Short>[] swordItemWhitelist = parseItemMetaStrings();
	/** <b>[Server-only]</b> List of registry names of items which count as bows for imbuement spells. */
	public Pair<ResourceLocation, Short>[] bowItemWhitelist = parseItemMetaStrings();
	/** <b>[Server-only]</b> Map of items to values which wizard trades may use as currency. */
	public Map<Pair<ResourceLocation, Short>, Integer> currencyItems = new HashMap<>();
	/** <b>[Server-only]</b> Global damage scaling factor for all player magic damage. */
	public double playerDamageScale = 1.0;
	/** <b>[Server-only]</b> Global damage scaling factor for all npc magic damage. */
	public double npcDamageScale = 1.0;
	/** <b>[Server-only]</b> List of dimension ids in which wizardry's hostile mobs can spawn. */
	public int[] mobSpawnDimensions = {0};
	/** <b>[Server-only]</b> Spawn rate for naturally-spawned evil wizards; higher numbers mean more evil wizards will spawn. */
	public int evilWizardSpawnRate = 3;
	/** <b>[Server-only]</b> Spawn rate for naturally-spawned evil wizards; higher numbers mean more evil wizards will spawn. */
	public int iceWraithSpawnRate = 3;
	/** <b>[Server-only]</b> Spawn rate for naturally-spawned evil wizards; higher numbers mean more evil wizards will spawn. */
	public int lightningWraithSpawnRate = 1;
	/** <b>[Server-only]</b> List of registry names of biomes in which wizardry's hostile mobs cannot spawn. */
	public ResourceLocation[] mobSpawnBiomeBlacklist = toResourceLocations("mushroom_island", "mushroom_island_shore");

	// Commands (these don't need synchronising since typing a command always queries the server).
	/**
	 * <b>[Server-only]</b> The maximum allowed multiplier for the /cast command. This limit is here to stop people from
	 * accidentally breaking their worlds!
	 */
	public double maxSpellCommandMultiplier = 20d;
	/** <b>[Server-only]</b> The name of the /cast command. */
	public String castCommandName = "cast";
	/** <b>[Server-only]</b> The name of the /discoverspell command. */
	public String discoverspellCommandName = "discoverspell";
	/** <b>[Server-only]</b> The name of the /ally command. */
	public String allyCommandName = "ally";
	/** <b>[Server-only]</b> The name of the /allies command. */
	public String alliesCommandName = "allies";

	/**
	 * <b>[Server-only]</b> List of damage source string identifiers to be ignored when re-applying damage.
	 */
	public String[] damageSourceBlacklist = {};
	/** <b>[Server-only]</b> Whether to print compatibility warnings to the console. */
	public boolean compatibilityWarnings = true;
	// TODO: Should these really be server-only?
	/** <b>[Server-only]</b> Whether Baubles integration features are enabled. */
	public boolean baublesIntegration = true;
	/** <b>[Server-only]</b> Whether JEI integration features are enabled. */
	public boolean jeiIntegration = true;
	/** <b>[Server-only]</b> Whether Antique Atlas integration features are enabled. */
	public boolean antiqueAtlasIntegration = true;
	/** <b>[Server-only]</b> Whether global markers for wizard towers are added to antique atlases. */
	public boolean autoTowerMarkers = true;
	/** <b>[Server-only]</b> Whether global markers for obelisks are added to antique atlases. */
	public boolean autoObeliskMarkers = true;
	/** <b>[Server-only]</b> Whether global markers for shrines are added to antique atlases. */
	public boolean autoShrineMarkers = true;

	// Synchronised settings. These settings affect both client-side AND server-side code. Changing these locally
	// only has an effect if the local game is the host, i.e. a dedicated server, a LAN host or a singleplayer world.

	// Gamemodes
	/**
	 * <b>[Synchronised]</b> When set to true, spells a player hasn't cast yet will be unreadable until they are cast
	 * (on a per-world basis). Has no effect when in creative mode. Scrolls of identification will be unobtainable in
	 * survival mode if this is false.
	 */
	public boolean discoveryMode = true;
	/**
	 * <b>[Synchronised]</b> When set to true, players in creative mode can bypass arcane locks regardless of whether
	 * they are an op or not.
	 */
	public boolean creativeBypassesArcaneLock = true;
	/**
	 * <b>[Synchronised]</b> When set to true, players will be slowed when a nearby player or entity has the slow time
	 * effect.
	 */
	public boolean slowTimeAffectsPlayers = true;
	/** <b>[Synchronised]</b> Whether passive mobs should count as allies, i.e. they should not be damaged indirectly by spells */
	public boolean passiveMobsAreAllies = false;
	/** <b>[Synchronised]</b> Whether to replace Minecraft's own fireballs with wizardry fireballs. */
	public boolean replaceVanillaFireballs = true;
	/** <b>[Synchronised]</b> Chance of 'misreading' an undiscovered spell and triggering a forfeit instead. */
	public double forfeitChance = 0.2;
	/**
	 * <b>[Synchronised]</b> The maximum number of blocks a bookshelf can be from an arcane workbench or lectern to be
	 * able to link to it.
	 */
	public int bookshelfSearchRadius = 4;
	/**
	 * <b>[Synchronised]</b> List of registry names of blocks that count as bookshelves for the arcane workbench and
	 * lectern.
	 */
	public Pair<ResourceLocation, Short>[] bookshelfBlocks = parseItemMetaStrings(
			Wizardry.MODID + ":oak_bookshelf",
			Wizardry.MODID + ":spruce_bookshelf",
			Wizardry.MODID + ":birch_bookshelf",
			Wizardry.MODID + ":jungle_bookshelf",
			Wizardry.MODID + ":acacia_bookshelf",
			Wizardry.MODID + ":dark_oak_bookshelf"
	);
	/** <b>[Synchronised]</b> List of registry names of items that can be placed in a bookshelf. */
	public Pair<ResourceLocation, Short>[] bookItems = parseItemMetaStrings();

	// Client-only settings. These settings only affect client-side code and hence are not synced. Each client obeys
	// its own values for these, and changing them on a dedicated server will have no effect.

	// Controls
	/**
	 * <b>[Client-only]</b> Whether the player can switch between spells on a wand by scrolling with the mouse wheel
	 * while sneaking.
	 */
	public boolean shiftScrolling = true;
	/** <b>[Client-only]</b> Whether to reverse the spell switching scroll direction. */
	public boolean reverseScrollDirection = false;

	// Display
	/** <b>[Client-only]</b> Whether to show summoned creatures' names and owners above their heads. */
	public boolean summonedCreatureNames = true;
	/**
	 * <b>[Client-only]</b> When set to true, sections of <i>The Wizard's Handbook</i> are unlocked when a player
	 * gains the advancement that triggers them, and are hidden otherwise. When set to false, the entire handbook is
	 * readable regardless of advancement progress.
	 */
	public boolean handbookProgression = true;
	/** <b>[Client-only]</b> Whether the various book GUIs pause the game in singleplayer worlds. */
	public boolean booksPauseGame = true;
	/** <b>[Client-only]</b> Whether to use custom shaders for certain spells. */
	public boolean useShaders = true;
	/** <b>[Client-only]</b> Whether to use the screen shake effect for certain spells. */
	public boolean screenShake = true;
	/** <b>[Client-only]</b> Whether to use the screen blink effect for teleportation spells. */
	public boolean blinkEffect = true;
	/** <b>[Client-only]</b> The position of the spell HUD. */
	public GuiPosition spellHUDPosition = GuiPosition.BOTTOM_LEFT;

	public static final String DEFAULT_HUD_SKIN_KEY = "default"; // Defined here so it's not in a client-only class.
	/** <b>[Client-only]</b> The string identifier of the skin used for the spell HUD. */
	public String spellHUDSkin = DEFAULT_HUD_SKIN_KEY;

	/** Set of constants for each of the eight positions that the spell HUD can be in. */
	public enum GuiPosition {

		BOTTOM_LEFT("Bottom left", false, false, false),
		TOP_LEFT("Top left", false, true, false),
		TOP_RIGHT("Top right", true, true, false),
		BOTTOM_RIGHT("Bottom right", true, false, false),
		FOLLOW_BOTTOM("Follow wand, bottom", false, false, true),
		FOLLOW_TOP("Follow wand, top", false, true, true),
		OPPOSITE_BOTTOM("Opposite wand, bottom", true, false, true),
		OPPOSITE_TOP("Opposite wand, top", true, true, true);

		/** Constant array storing the names of each of the constants, in the order they are declared. */
		public static final String[] names;

		static{
			names = new String[values().length];
			for(GuiPosition position : values()){
				names[position.ordinal()] = position.name;
			}
		}

		/** The readable name for this GUI position that will be displayed on the button in the config GUI. */
		public final String name;
		public final boolean flipX;
		public final boolean flipY;
		public final boolean dynamic;

		GuiPosition(String name, boolean flipX, boolean flipY, boolean dynamic){
			this.name = name;
			this.flipX = flipX;
			this.flipY = flipY;
			this.dynamic = dynamic;
		}

		/**
		 * Gets a GUI position from its string name (ignoring case), or BOTTOM_LEFT if the given name is not a valid
		 * position.
		 */
		public static GuiPosition fromName(String name){

			for(GuiPosition position : values()){
				if(position.name.equalsIgnoreCase(name)) return position;
			}

			Wizardry.logger.info("Invalid string for the spell HUD position. Using default (bottom left) instead.");
			return BOTTOM_LEFT;
		}

	}

	// As of Wizardry 1.1, all keys used in the config file itself are now hardcoded, not localised. The localisations
	// are done in the config GUI. The config file has to be written in one language or it will end up with multiple
	// options for different languages.

	// These methods are package-protected to stop anyone else from calling them.

	/**
	 * Called from preInit to initialise the config file. The first part of the config file has to be done here (as is
	 * conventional) so that the various registries can change what they do accordingly. The spell part of the config
	 * and the resistance part of the config have to be done in the init method since they depend on the registry events.
	 */
	void initConfig(FMLPreInitializationEvent event){

		config = new Configuration(new File(Wizardry.configDirectory, Wizardry.MODID + ".cfg"));
		config.load();

		Wizardry.logger.info("Setting up main config");

		setupGeneralConfig();
		setupDifficultyConfig();
		setupWorldgenConfig();
		setupTweaksConfig();
		if(event.getSide() == Side.CLIENT) setupClientConfig(); // Server has no spell HUD skins so this would crash it
		setupCommandsConfig();
		setupCompatibilityConfig();

		config.save();
	}

	/**
	 * Called from init to initialise the parts of the config file that have to be done after registry events are fired.
	 * For example, the spell config is done here and not in preInit because all spells must be registered before it is
	 * added, including those in other mods.
	 */
	void initConfigExtras(){

		Wizardry.logger.info("Setting up spells config for " + Spell.getTotalSpellCount() + " spells");

		setupSpellsConfig();

		Wizardry.logger.info("Setting up resistances config");

		setupResistancesConfig();

		config.save();
	}

	/** Called to save changes to the config file after it has been edited in game from the menus. */
	void saveConfigChanges(){

		Wizardry.logger.info("Saving in-game config changes");

		setupGeneralConfig();
		setupWorldgenConfig();
		setupClientConfig();
		setupCommandsConfig();
		setupCompatibilityConfig();
		setupSpellsConfig();
		setupResistancesConfig();

		config.save();
	}

	/** Sends a packet to the specified player's client containing all the <b>synchronised</b> settings. */
	public void sync(EntityPlayerMP player){
		Wizardry.logger.info("Synchronising config settings for " + player.getName());
		IMessage message = new PacketSyncSettings.Message(this);
		WizardryPacketHandler.net.sendTo(message, player);
	}

	public ConfigCategory getConfigCategory(String name){
		return config.getCategory(name);
	}

	private void setupSpellsConfig(){

		config.addCustomCategoryComment(SPELLS_CATEGORY,
				"Set a spell to false to disable it. Disabled spells will still have their associated spell book (mainly so the spell books don't all change) and can still be bound to wands, but cannot be cast in game, will not appear in any subsequently generated chests or wizard trades and will not drop from mobs. Disable a spell if it is causing problems, conflicts with another mod or creates an unintended exploit.");

		Property property;

		for(Spell spell : Spell.getAllSpells()){
			property = config.get(SPELLS_CATEGORY, spell.getRegistryName().toString(), true,
					"Set to false to disable this spell");
			// Uses the same config key as the spell name, because - well, that's what it's called!
			property.setLanguageKey("spell." + spell.getUnlocalisedName());
			Wizardry.proxy.setToNamedBooleanEntry(property);
			spell.setEnabled(property.getBoolean());
		}

	}

	private void setupGeneralConfig(){

		// This trick is borrowed from forge; it sorts the config options into the order you want them.
		List<String> propOrder = new ArrayList<>();

		Property property;

		config.addCustomCategoryComment(GAMEPLAY_CATEGORY, "Global settings that affect game mechanics. In multiplayer, the server/LAN host settings will apply. Please note that changing some of these settings may make the mod very difficult to play.");

		property = config.get(GAMEPLAY_CATEGORY, "playerBlockDamage", true,
				"Whether spells cast by players can destroy blocks in the world. Set to false to prevent griefing. To prevent non-players from destroying blocks with magic, use the mobGriefing gamerule.");
		property.setLanguageKey("config." + Wizardry.MODID + ".player_block_damage");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		playerBlockDamage = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "playersMoveEachOther", true,
				"Whether to allow players to move other players around using magic.");
		property.setLanguageKey("config." + Wizardry.MODID + ".players_move_each_other");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		playersMoveEachOther = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "telekineticDisarmament", true,
				"Whether to allow players to disarm other players using the telekinesis spell. Set to false to prevent stealing of items.");
		property.setLanguageKey("config." + Wizardry.MODID + ".telekinetic_disarmament");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		telekineticDisarmament = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "teleportThroughUnbreakableBlocks", false,
				"Whether players are allowed to teleport through unbreakable blocks (e.g. bedrock) using the phase step spell.");
		property.setLanguageKey("config." + Wizardry.MODID + ".teleport_through_unbreakable_blocks");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		teleportThroughUnbreakableBlocks = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "worldTimeManipulation", true,
				"Whether players are allowed to change the world time with the speed time spell. If this is false, the speed time spell will not change the world time but will still speed up nearby block, entity and tile entity ticks.");
		property.setLanguageKey("config." + Wizardry.MODID + ".world_time_manipulation");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		worldTimeManipulation = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "slowTimeAffectsPlayers", true,
				"Whether players are slowed when another nearby player uses the slow time spell. If this is false, mobs and projectiles will still be affected but players will move at normal speed.");
		property.setLanguageKey("config." + Wizardry.MODID + ".slow_time_affects_players");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		property.setRequiresWorldRestart(true);
		slowTimeAffectsPlayers = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "creativeBypassesArcaneLock", true,
				"Whether any player in creative mode can bypass arcane-locked blocks. If this is false, players must also be op in order to do so.");
		property.setLanguageKey("config." + Wizardry.MODID + ".creative_bypasses_arcane_lock");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		property.setRequiresWorldRestart(true);
		creativeBypassesArcaneLock = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "bonemealGrowsCrystalFlowers", true,
				"Whether using bonemeal on grass blocks has a chance to grow crystal flowers.");
		property.setLanguageKey("config." + Wizardry.MODID + ".bonemeal_grows_crystal_flowers");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		bonemealGrowsCrystalFlowers = property.getBoolean();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(GAMEPLAY_CATEGORY, propOrder);

	}

	private void setupDifficultyConfig(){

		List<String> propOrder = new ArrayList<>();

		Property property;

		config.addCustomCategoryComment(DIFFICULTY_CATEGORY, "Settings that affect the mod's difficulty. In multiplayer, the server/LAN host settings will apply.");

		property = config.get(DIFFICULTY_CATEGORY, "discoveryMode", true,
				"For those who like a sense of mystery! When set to true, spells you haven't cast yet will be unreadable until you cast them (on a per-world basis). Has no effect when in creative mode. Spells of identification will be unobtainable in survival mode if this is false.");
		property.setLanguageKey("config." + Wizardry.MODID + ".discovery_mode");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		property.setRequiresWorldRestart(true);
		discoveryMode = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "legacyWandLevelling", false,
				"Controls whether wands are required to gain progression before they can be upgraded to the next tier. Enable this option to revert to the pre-4.2 system, which only requires tomes of arcana. Wands will still gain progression even when this is enabled, so if you go back to the new system you won't lose any progress.");
		property.setLanguageKey("config." + Wizardry.MODID + ".legacy_wand_levelling");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		legacyWandLevelling = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "friendlyFire", FriendlyFire.ALL.name,
				"Controls which creatures may be damaged by your magic when allied to you. Your spells will not target your allies or creatures summoned/owned by them regardless of this setting, but this setting prevents all magic damage to allies.", FriendlyFire.names);
		property.setLanguageKey("config." + Wizardry.MODID + ".friendly_fire");
		friendlyFire = FriendlyFire.fromName(property.getString());
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "passiveMobsAreAllies", false,
				"Whether passive mobs should count as allies, i.e. they should not be damaged indirectly by spells.");
		property.setLanguageKey("config." + Wizardry.MODID + ".passive_mobs_are_allies");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		passiveMobsAreAllies = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "minionRevengeTargeting", true,
				"Whether summoned creatures can revenge attack their owner if their owner attacks them.");
		property.setLanguageKey("config." + Wizardry.MODID + ".minion_revenge_targeting");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		property.setRequiresWorldRestart(false);
		minionRevengeTargeting = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "forfeitChance", 0.2,
				"The chance to 'misread' an undiscovered spell and trigger a forfeit instead. Setting this to 0 effectively disables the forfeit mechanic. Has no effect if discovery mode is disabled.",
				0, 1);
		property.setLanguageKey("config." + Wizardry.MODID + ".forfeit_chance");
		Wizardry.proxy.setToNumberSliderEntry(property);
		property.setRequiresWorldRestart(true);
		forfeitChance = property.getDouble();
		propOrder.add(property.getName());

		// These two aren't sliders because using a slider makes it difficult to fine-tune the numbers; the nature of a
		// scaling factor means that 0.5 is as big a change as 2.0, so whilst a slider is fine for increasing the
		// damage, it doesn't give fine enough control for values less than 1.
		property = config.get(DIFFICULTY_CATEGORY, "playerDamageScaling", 1.0,
				"Global damage scaling factor for the damage dealt by players casting spells, relative to 1.", 0, 255);
		property.setLanguageKey("config." + Wizardry.MODID + ".player_damage_scaling");
		playerDamageScale = property.getDouble();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "npcDamageScaling", 1.0,
				"Global damage scaling factor for the damage dealt by NPCs casting spells, relative to 1.", 0, 255);
		property.setLanguageKey("config." + Wizardry.MODID + ".npc_damage_scaling");
		npcDamageScale = property.getDouble();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "evilWizardSpawnRate", 3,
				"Spawn rate for naturally-spawned evil wizards; higher numbers mean more evil wizards will spawn. 5 is equivalent to witches, 100 is equivalent to zombies, skeletons and creepers. Set to 0 to disable evil wizard spawning entirely.",
				0, 100);
		property.setLanguageKey("config." + Wizardry.MODID + ".evil_wizard_spawn_rate");
		Wizardry.proxy.setToNumberSliderEntry(property);
		property.setRequiresMcRestart(true);
		evilWizardSpawnRate = property.getInt();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "iceWraithSpawnRate", 3,
				"Spawn rate for naturally-spawned ice wraiths; higher numbers mean more ice wraiths will spawn. 5 is equivalent to witches, 100 is equivalent to zombies, skeletons and creepers. Set to 0 to disable ice wraith spawning entirely.",
				0, 100);
		property.setLanguageKey("config." + Wizardry.MODID + ".ice_wraith_spawn_rate");
		Wizardry.proxy.setToNumberSliderEntry(property);
		property.setRequiresMcRestart(true);
		iceWraithSpawnRate = property.getInt();
		propOrder.add(property.getName());

		property = config.get(DIFFICULTY_CATEGORY, "lightningWraithSpawnRate", 1,
				"Spawn rate for naturally-spawned lightning wraiths; higher numbers mean more lightning wraiths will spawn. 5 is equivalent to witches, 100 is equivalent to zombies, skeletons and creepers. Set to 0 to disable lightning wraith spawning entirely.",
				0, 100);
		property.setLanguageKey("config." + Wizardry.MODID + ".lightning_wraith_spawn_rate");
		Wizardry.proxy.setToNumberSliderEntry(property);
		property.setRequiresMcRestart(true);
		lightningWraithSpawnRate = property.getInt();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(DIFFICULTY_CATEGORY, propOrder);

	}

	private void setupTweaksConfig(){

		List<String> propOrder = new ArrayList<>();

		Property property;

		config.addCustomCategoryComment(TWEAKS_CATEGORY, "Assorted settings for tweaking the mod's behaviour. In multiplayer, the server/LAN host settings will apply.");

		property = config.get(TWEAKS_CATEGORY, "replaceVanillaFireballs", true,
				"Whether to replace Minecraft's own fireballs with wizardry fireballs. If this is disabled, only wizardry spells will use the custom fireballs.");
		property.setLanguageKey("config." + Wizardry.MODID + ".replace_vanilla_fireballs");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		property.setRequiresWorldRestart(true);
		replaceVanillaFireballs = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "replaceVanillaFallDamage", true,
				"Whether to replace Minecraft's distance-based fall damage calculation with an equivalent, velocity-based one. This is done such that mobs in freefall will take exactly the same damage as normal, so it will not break falling-based mob farms. Disable this if you experience falling-related weirdness! If this is disabled, some spells will use a more simplistic method of resetting the player's fall damage in certain cases.");
		property.setLanguageKey("config." + Wizardry.MODID + ".replace_vanilla_fall_damage");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		replaceVanillaFallDamage = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "mobLootTableWhitelist", new String[0], "Whitelist for loot tables to inject additional mob drops (as specified in loot_tables/entities/mob_additions.json) into. Wizardry makes a best guess as to which loot tables belong to hostile mobs, but this may not always be correct or appropriate; add loot table locations (not entity IDs) to this list to manually include them.");
		property.setLanguageKey("config." + Wizardry.MODID + ".mob_loot_table_whitelist");
		property.setRequiresMcRestart(true);
		mobLootTableWhitelist = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "mobLootTableBlacklist", new String[]{"entities/vex", "entities/ender_dragon", "entities/wither", Wizardry.MODID + ":entities/evil_wizard"}, "Blacklist for loot tables to inject additional mob drops (as specified in loot_tables/entities/mob_additions.json) into. Wizardry makes a best guess as to which loot tables belong to hostile mobs, but this may not always be correct or appropriate; add loot table locations (not entity IDs) to this list to manually exclude them.");
		property.setLanguageKey("config." + Wizardry.MODID + ".mob_loot_table_blacklist");
		property.setRequiresMcRestart(true);
		mobLootTableBlacklist = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "mobSpawnDimensions", new int[]{0},
				"List of dimension ids in which wizardry's hostile mobs can spawn.");
		property.setLanguageKey("config." + Wizardry.MODID + ".mob_spawn_dimensions");
		property.setRequiresMcRestart(true);
		mobSpawnDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "mobSpawnBiomeBlacklist", new String[]{"mushroom_island", "mushroom_island_shore"},
				"List of names of biomes in which wizardry's hostile mobs cannot spawn. Biome names are not case-sensitive. For mod biomes, prefix with the mod ID (e.g. biomesoplenty:mystic_grove).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mob_spawn_biome_blacklist");
		property.setRequiresMcRestart(true);
		mobSpawnBiomeBlacklist = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "summonedCreatureTargetsWhitelist", new String[0],
				"List of names of entities which summoned creatures and wizards are allowed to attack, in addition to the defaults. Add mod creatures to this list if you want summoned creatures to attack them and they aren't already doing so. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".summoned_creature_targets_whitelist");
		property.setRequiresWorldRestart(true);
		summonedCreatureTargetsWhitelist = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "summonedCreatureTargetsBlacklist", new String[]{"creeper"},
				"List of names of entities which summoned creatures and wizards are specifically not allowed to attack, overriding the defaults and the whitelist. Add creatures to this list if allowing them to be attacked causes problems or is too destructive (removing creepers from this list is done at your own risk!). SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".summoned_creature_targets_blacklist");
		property.setRequiresWorldRestart(true);
		summonedCreatureTargetsBlacklist = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "mindControlTargetsBlacklist", new String[]{},
				"List of names of entities which cannot be mind controlled, in addition to the defaults. Add creatures to this list if allowing them to be mind-controlled causes problems or could be exploited. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mind_control_targets_blacklist");
		mindControlTargetsBlacklist = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "pocketFurnaceItemBlacklist", new String[]{"cobblestone", "netherrack"},
				"List of registry names of blocks or items which cannot be smelted by the pocket furnace spell, in addition to armour, tools and weapons. Block/item names are not case sensitive. For mod items, prefix with the mod ID (e.g. " + Wizardry.MODID + ":crystal_ore).");
		property.setLanguageKey("config." + Wizardry.MODID + ".pocket_furnace_item_blacklist");
		pocketFurnaceItemBlacklist = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "divinationOreWhitelist", new String[0], "List of registry names of ore blocks which can be detected by the divination spell. Block names are not case sensitive. For mod blocks, prefix with the mod ID (e.g. " + Wizardry.MODID + ":crystal_ore).");
		property.setLanguageKey("config." + Wizardry.MODID + ".divination_ore_whitelist");
		divinationOreWhitelist = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "swordItemWhitelist", new String[0], "List of registry names of items which should count as swords for imbuement spells. Most swords should work automatically, but those that don't can be added manually here. Item names are not case sensitive. For mod items, prefix with the mod ID (e.g. tconstruct:broadsword).");
		property.setLanguageKey("config." + Wizardry.MODID + ".sword_item_whitelist");
		swordItemWhitelist = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "bowItemWhitelist", new String[0], "List of registry names of items which should count as bows for imbuement spells. Most bows should work automatically, but those that don't can be added manually here. Item names are not case sensitive. For mod items, prefix with the mod ID (e.g. tconstruct:shortbow).");
		property.setLanguageKey("config." + Wizardry.MODID + ".bow_item_whitelist");
		bowItemWhitelist = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "bookshelfBlocks", new String[]{Wizardry.MODID + ":oak_bookshelf", Wizardry.MODID + ":spruce_bookshelf", Wizardry.MODID + ":birch_bookshelf", Wizardry.MODID + ":jungle_bookshelf",  Wizardry.MODID + ":acacia_bookshelf",  Wizardry.MODID + ":dark_oak_bookshelf"}, "List of registry names of blocks that count as bookshelves for the arcane workbench and lectern. Block names are not case sensitive. For mod blocks, prefix with the mod ID (e.g. " + Wizardry.MODID + ":oak_bookshelf).");
		property.setLanguageKey("config." + Wizardry.MODID + ".bookshelf_blocks");
		property.setRequiresWorldRestart(true);
		bookshelfBlocks = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "bookItems", new String[0], "List of registry names of items which can be placed in a bookshelf, in addition to the defaults. Item names are not case sensitive. For mod items, prefix with the mod ID (e.g. thaumcraft:thaumonomicon).");
		property.setLanguageKey("config." + Wizardry.MODID + ".book_items");
		property.setRequiresWorldRestart(true);
		bookItems = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "bookshelfSearchRadius", 4,
				"The maximum number of blocks a bookshelf can be from an arcane workbench or lectern to be able to link to it.",
				1, 10);
		property.setLanguageKey("config." + Wizardry.MODID + ".bookshelf_search_radius");
		Wizardry.proxy.setToNumberSliderEntry(property);
		property.setRequiresWorldRestart(true);
		bookshelfSearchRadius = property.getInt();
		propOrder.add(property.getName());

		property = config.get(TWEAKS_CATEGORY, "currencyItems", new String[]{"gold_ingot 3", "emerald 6"}, "List of registry names of items which wizard trades can use as currency (in the first slot; the second slot is unaffected). Each entry in this list should consist of an item registry name, followed by a single space, then an integer which defines the 'value' of the item. Higher values mean fewer of that currency item are required for a given trade.",
				Pattern.compile("[A-Za-z0-9:_]+ [0-9]+"));
		property.setLanguageKey("config." + Wizardry.MODID + ".currency_items");
		propOrder.add(property.getName());
		currencyItems = new HashMap<>();
		for(String string : property.getStringList()){
			string = string.toLowerCase(Locale.ROOT).trim();
			String[] args = string.split(" ");
			if(args.length != 2){
				Wizardry.logger.warn("Invalid entry in currency items: {}", string);
				continue; // Ignore invalid entries, the pattern above should ensure this never happens
			}
			try{
				currencyItems.put(parseItemMetaString(args[0]), Integer.parseInt(args[1]));
			}catch(NumberFormatException e){
				Wizardry.logger.warn("Invalid integer in currency items: {}", args[1]);
			}
		}

		config.setCategoryPropertyOrder(TWEAKS_CATEGORY, propOrder);

	}

	private void setupWorldgenConfig(){

		List<String> propOrder = new ArrayList<>();

		Property property;

		config.addCustomCategoryComment(WORLDGEN_CATEGORY, "Settings that affect world generation. In multiplayer, the server/LAN host settings will apply.");

		property = config.get(WORLDGEN_CATEGORY, "fastWorldgen", false, "Whether to use faster worldgen at the cost of 'seamlessness'. Enabling this option removes the checks for steep slopes and cleanup of floating trees that improve the look of worldgen. Performance improvement will vary depending on your setup. This option will affect randomisation; for any given seed, structures will not be the same as when it is turned off.");
		property.setLanguageKey("config." + Wizardry.MODID + ".fast_worldgen");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		fastWorldgen = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "towerDimensions", new int[]{0}, "List of dimension ids in which wizard towers will generate. Remove all dimensions to disable wizard towers completely.");
		property.setLanguageKey("config." + Wizardry.MODID + ".tower_dimensions");
		property.setRequiresWorldRestart(true);
		towerDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "towerRarity", 900, "Rarity of wizard towers. 1 in this many chunks will contain a wizard tower, meaning higher numbers are rarer.", 20, 5000);
		property.setLanguageKey("config." + Wizardry.MODID + ".tower_rarity");
		property.setRequiresWorldRestart(true);
		Wizardry.proxy.setToNumberSliderEntry(property);
		towerRarity = property.getInt();
		propOrder.add(property.getName());

		// A bit of backwards-compatibility: predictably, people don't read my instructions and update without deleting
		// the config, resulting in wizard towers generating everywhere - so let's update it for them!
		if(towerRarity < 20) towerRarity = 600; // The old default was 8, which is outside the new allowed range

		property = config.get(WORLDGEN_CATEGORY, "evilWizardChance", 0.2, "The chance for wizard towers to generate with an evil wizard and chest inside, instead of a friendly wizard.", 0, 1);
		property.setLanguageKey("config." + Wizardry.MODID + ".evil_wizard_chance");
		property.setRequiresWorldRestart(true);
		Wizardry.proxy.setToNumberSliderEntry(property);
		evilWizardChance = property.getDouble();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "towerFiles", new String[]{Wizardry.MODID + ":wizard_tower_0", Wizardry.MODID + ":wizard_tower_1", Wizardry.MODID + ":wizard_tower_2", Wizardry.MODID + ":wizard_tower_3"},
				"List of structure file locations for wizard towers without loot chests. One of these files will be randomly selected each time a wizard tower is generated. File locations are of the format [mod id]:[filename], which refers to the file assets/[mod id]/structures/[filename].nbt. Duplicate entries are permitted, allowing for simple weighting without duplicating the structure files themselves. This list should not be empty; to disable wizard towers, use the tower dimensions setting.");
		property.setLanguageKey("config." + Wizardry.MODID + ".tower_files");
		property.setRequiresWorldRestart(true);
		towerFiles = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "towerWithChestFiles", new String[]{Wizardry.MODID + ":wizard_tower_chest_0", Wizardry.MODID + ":wizard_tower_chest_1", Wizardry.MODID + ":wizard_tower_chest_2", Wizardry.MODID + ":wizard_tower_chest_3"},
				"List of structure file locations for wizard towers with loot chests. One of these files will be randomly selected each time a wizard tower is generated. File locations are of the format [mod id]:[filename], which refers to the file assets/[mod id]/structures/[filename].nbt. Duplicate entries are permitted, allowing for simple weighting without duplicating the structure files themselves. This list should not be empty; to disable wizard towers, use the tower dimensions setting.");
		property.setLanguageKey("config." + Wizardry.MODID + ".tower_with_chest_files");
		property.setRequiresWorldRestart(true);
		towerWithChestFiles = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "obeliskDimensions", new int[]{0, -1}, "List of dimension ids in which obelisks will generate. Remove all dimensions to disable obelisks completely.");
		property.setLanguageKey("config." + Wizardry.MODID + ".obelisk_dimensions");
		property.setRequiresWorldRestart(true);
		obeliskDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "obeliskRarity", 800, "Rarity of obelisks. 1 in this many chunks will contain an obelisk, meaning higher numbers are rarer.", 20, 5000);
		property.setLanguageKey("config." + Wizardry.MODID + ".obelisk_rarity");
		property.setRequiresWorldRestart(true);
		Wizardry.proxy.setToNumberSliderEntry(property);
		obeliskRarity = property.getInt();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "obeliskFiles", new String[]{Wizardry.MODID + ":obelisk_0", Wizardry.MODID + ":obelisk_1", Wizardry.MODID + ":obelisk_2", Wizardry.MODID + ":obelisk_3", Wizardry.MODID + ":obelisk_4"},
				"List of structure file locations for obelisks. One of these files will be randomly selected each time an obelisk is generated. File locations are of the format [mod id]:[filename], which refers to the file assets/[mod id]/structures/[filename].nbt. Duplicate entries are permitted, allowing for simple weighting without duplicating the structure files themselves. This list should not be empty; to disable obelisks, use the obelisk dimensions setting.");
		property.setLanguageKey("config." + Wizardry.MODID + ".obelisk_files");
		property.setRequiresWorldRestart(true);
		obeliskFiles = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "shrineDimensions", new int[]{0, -1}, "List of dimension ids in which shrines will generate. Remove all dimensions to disable shrines completely.");
		property.setLanguageKey("config." + Wizardry.MODID + ".shrine_dimensions");
		property.setRequiresWorldRestart(true);
		shrineDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "shrineRarity", 1500, "Rarity of shrines. 1 in this many chunks will contain a shrine, meaning higher numbers are rarer.", 20, 5000);
		property.setLanguageKey("config." + Wizardry.MODID + ".shrine_rarity");
		property.setRequiresWorldRestart(true);
		Wizardry.proxy.setToNumberSliderEntry(property);
		shrineRarity = property.getInt();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "shrineFiles", new String[]{Wizardry.MODID + ":shrine_0", Wizardry.MODID + ":shrine_1", Wizardry.MODID + ":shrine_2", Wizardry.MODID + ":shrine_3", Wizardry.MODID + ":shrine_4", Wizardry.MODID + ":shrine_5", Wizardry.MODID + ":shrine_6", Wizardry.MODID + ":shrine_7"},
				"List of structure file locations for shrines. One of these files will be randomly selected each time a shrine is generated. File locations are of the format [mod id]:[filename], which refers to the file assets/[mod id]/structures/[filename].nbt. Duplicate entries are permitted, allowing for simple weighting without duplicating the structure files themselves. This list should not be empty; to disable shrines, use the shrine dimensions setting.");
		property.setLanguageKey("config." + Wizardry.MODID + ".shrine_files");
		property.setRequiresWorldRestart(true);
		shrineFiles = getResourceLocationList(property);
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "treeBlocks", DEFAULT_TREE_BLOCKS, "List of registry names of blocks which can be overwritten by wizardry's structure generators, affecting both fast and fancy structure generation. Most tree blocks and other foliage should work automatically, but those that don't can be added manually here. Block names are not case sensitive. For mod blocks, prefix with the mod ID (e.g. dynamictrees:oakbranch).");
		property.setLanguageKey("config." + Wizardry.MODID + ".tree_blocks");
		property.setRequiresWorldRestart(true);
		treeBlocks = parseItemMetaStrings(property.getStringList());
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "oreDimensions", new int[]{0}, "List of dimension ids in which crystal ore will generate. Note that removing the overworld (id 0) from this list will make the mod VERY difficult to play!");
		property.setLanguageKey("config." + Wizardry.MODID + ".ore_dimensions");
		property.setRequiresWorldRestart(true);
		oreDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "flowerDimensions", new int[]{0}, "List of dimension ids in which crystal flowers will generate.");
		property.setLanguageKey("config." + Wizardry.MODID + ".flower_dimensions");
		property.setRequiresWorldRestart(true);
		flowerDimensions = property.getIntList();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "lootInjectionLocations", DEFAULT_LOOT_INJECTION_LOCATIONS, "List of loot tables to inject wizardry loot (as specified in loot_tables/chests/dungeon_additions.json) into.");
		property.setLanguageKey("config." + Wizardry.MODID + ".loot_injection_locations");
		property.setRequiresMcRestart(true);
		lootInjectionLocations = getResourceLocationList(property);
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(WORLDGEN_CATEGORY, propOrder);
	}

	private void setupClientConfig(){

		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(CLIENT_CATEGORY, "Client-side settings that only affect the local minecraft game. If this file is on a dedicated server, these settings will have no effect; in multiplayer, each player obeys their own settings.");

		property = config.get(CLIENT_CATEGORY, "shiftScrolling", true,
				"Whether you can switch between spells on a wand by scrolling with the mouse wheel while sneaking. Note that this will only affect you; other players connected to the same server obey their own settings.");
		property.setLanguageKey("config." + Wizardry.MODID + ".shift_scrolling");
		property.setRequiresWorldRestart(false);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		shiftScrolling = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "reverseScrollDirection", false, "The scroll direction used to switch between spells on a wand while sneaking.");
		property.setLanguageKey("config." + Wizardry.MODID + ".reverse_scroll_direction");
		property.setRequiresWorldRestart(false);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		reverseScrollDirection = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "spellHUDPosition", GuiPosition.BOTTOM_LEFT.name, "The position of the spell HUD.", GuiPosition.names);
		property.setLanguageKey("config." + Wizardry.MODID + ".spell_hud_position");
		spellHUDPosition = GuiPosition.fromName(property.getString());
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "spellHUDSkin", DEFAULT_HUD_SKIN_KEY, "The skin used for the spell HUD.", Wizardry.proxy.getSpellHUDSkins().toArray(new String[0]));
		property.setLanguageKey("config." + Wizardry.MODID + ".spell_hud_skin");
		Wizardry.proxy.setToHUDChooserEntry(property);
		spellHUDSkin = property.getString();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "handbookProgression", true, "When set to true, sections of The Wizard's Handbook are unlocked when a player gains the advancement that triggers them, and are hidden otherwise. When set to false, the entire handbook is readable regardless of advancement progress.");
		property.setLanguageKey("config." + Wizardry.MODID + ".handbook_progression");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		handbookProgression = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "booksPauseGame", true, "Whether opening any of wizardry's books pauses the game in singleplayer. Has no effect on servers or LAN worlds.");
		property.setLanguageKey("config." + Wizardry.MODID + ".books_pause_game");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		booksPauseGame = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "summonedCreatureNames", true, "Whether to show summoned creatures' names and owners above their heads.");
		property.setLanguageKey("config." + Wizardry.MODID + ".summoned_creature_names");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		summonedCreatureNames = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "useShaders", true, "Whether to use custom shaders for certain spells. These use the vanilla shader system (like mob spectating shaders) and shouldn't have much of an effect on performance in most cases, but they may conflict with other shaders.");
		property.setLanguageKey("config." + Wizardry.MODID + ".use_shaders");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		useShaders = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "screenShake", true, "Whether to use the screen shake effect for certain spells.");
		property.setLanguageKey("config." + Wizardry.MODID + ".screen_shake");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		screenShake = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "blinkEffect", true, "Whether to use the screen blink effect for teleportation spells.");
		property.setLanguageKey("config." + Wizardry.MODID + ".blink_effect");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		blinkEffect = property.getBoolean();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(CLIENT_CATEGORY, propOrder);
	}

	private void setupCommandsConfig(){

		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(COMMANDS_CATEGORY, "Settings for the commands added by Wizardry. In multiplayer, the server/LAN host settings will apply.");

		property = config.get(COMMANDS_CATEGORY, "castCommandMultiplierLimit", 20.0,
				"Upper limit for the multipliers passed into the /cast command. This is here to stop players from accidentally breaking a world/server. Large blast mutipliers can cause extreme lag - you have been warned!",
				1, Integer.MAX_VALUE); // Sure, I mean you COULD set it to 2^31-1... what could possibly go wrong?
		property.setLanguageKey("config." + Wizardry.MODID + ".cast_command_multiplier_limit");
		maxSpellCommandMultiplier = property.getDouble();
		propOrder.add(property.getName());

		property = config.get(COMMANDS_CATEGORY, "castCommandName", "cast",
				"The name of the /cast command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /cast you would type /magic instead.");
		property.setLanguageKey("config." + Wizardry.MODID + ".cast_command_name");
		property.setRequiresWorldRestart(true);
		castCommandName = property.getString();
		propOrder.add(property.getName());

		property = config.get(COMMANDS_CATEGORY, "discoverspellCommandName", "discoverspell",
				"The name of the /discoverspell command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /discoverspell you would type /magic instead.");
		property.setLanguageKey("config." + Wizardry.MODID + ".discoverspell_command_name");
		property.setRequiresWorldRestart(true);
		discoverspellCommandName = property.getString();
		propOrder.add(property.getName());

		property = config.get(COMMANDS_CATEGORY, "allyCommandName", "ally",
				"The name of the /ally command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /ally you would type /magic instead.");
		property.setLanguageKey("config." + Wizardry.MODID + ".ally_command_name");
		property.setRequiresWorldRestart(true);
		allyCommandName = property.getString();
		propOrder.add(property.getName());

		property = config.get(COMMANDS_CATEGORY, "alliesCommandName", "allies",
				"The name of the /allies command. This is what you type directly after the /; for example if this was set to 'magic' then instead of typing /allies you would type /magic instead.");
		property.setLanguageKey("config." + Wizardry.MODID + ".allies_command_name");
		property.setRequiresWorldRestart(true);
		alliesCommandName = property.getString();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(COMMANDS_CATEGORY, propOrder);
	}

	private void setupResistancesConfig(){

		Property property;

		List<String> propOrder = new ArrayList<String>();

		config.addCustomCategoryComment(RESISTANCES_CATEGORY,
				"Settings which allow entities to be made immune to certain types of magic. In multiplayer, the server/LAN host settings will apply.");

		property = config.get(RESISTANCES_CATEGORY, "mobsImmuneToFire", new String[]{},
				"List of names of entities that are immune to fire, in addition to the defaults. Add mod creatures to this list if you want them to be immune to fire magic and they aren't already. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mobs_immune_to_fire");
		property.setRequiresMcRestart(true);
		// Wizardry.proxy.setToEntityNameEntry(property);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i = 0; i < property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity(EntityList.getClass(new ResourceLocation(property.getStringList()[i])),
					DamageType.FIRE);
		}
		propOrder.add(property.getName());

		property = config.get(RESISTANCES_CATEGORY, "mobsImmuneToIce", new String[]{},
				"List of names of entities that are immune to ice, in addition to the defaults. Add mod creatures to this list if you want them to be immune to ice magic and they aren't already. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mobs_immune_to_ice");
		property.setRequiresMcRestart(true);
		// Wizardry.proxy.setToEntityNameEntry(property);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i = 0; i < property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity(EntityList.getClass(new ResourceLocation(property.getStringList()[i])),
					DamageType.FROST);
		}
		propOrder.add(property.getName());

		property = config.get(RESISTANCES_CATEGORY, "mobsImmuneToLightning", new String[]{},
				"List of names of entities that are immune to lightning, in addition to the defaults. Add mod creatures to this list if you want them to be immune to lightning magic and they aren't already. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mobs_immune_to_lightning");
		property.setRequiresMcRestart(true);
		// Wizardry.proxy.setToEntityNameEntry(property);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i = 0; i < property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity(EntityList.getClass(new ResourceLocation(property.getStringList()[i])),
					DamageType.SHOCK);
		}
		propOrder.add(property.getName());

		property = config.get(RESISTANCES_CATEGORY, "mobsImmuneToWither", new String[]{},
				"List of names of entities that are immune to wither effects, in addition to the defaults. Add mod creatures to this list if you want them to be immune to withering magic and they aren't already. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mobs_immune_to_wither");
		property.setRequiresMcRestart(true);
		// Wizardry.proxy.setToEntityNameEntry(property);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i = 0; i < property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity(EntityList.getClass(new ResourceLocation(property.getStringList()[i])),
					DamageType.WITHER);
		}
		propOrder.add(property.getName());

		property = config.get(RESISTANCES_CATEGORY, "mobsImmuneToPoison", new String[]{},
				"List of names of entities that are immune to poison, in addition to the defaults. Add mod creatures to this list if you want them to be immune to poison magic and they aren't already. SoundLoopSpellEntity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mobs_immune_to_poison");
		property.setRequiresMcRestart(true);
		// Wizardry.proxy.setToEntityNameEntry(property);
		// Converts all strings in the list to lower case, to ignore case sensitivity, and trims them.
		for(int i = 0; i < property.getStringList().length; i++){
			property.getStringList()[i] = property.getStringList()[i].toLowerCase(Locale.ROOT).trim();
			MagicDamage.addEntityImmunity(EntityList.getClass(new ResourceLocation(property.getStringList()[i])),
					DamageType.POISON);
		}
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(RESISTANCES_CATEGORY, propOrder);
	}

	private void setupCompatibilityConfig(){

		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(COMPATIBILITY_CATEGORY, "Settings that affect how wizardry interacts with other mods. In multiplayer, the server/LAN host settings will apply.");

		property = config.get(COMPATIBILITY_CATEGORY, "damageSourceBlacklist", new String[]{},
				"List of damage source string identifiers to be ignored when re-applying damage. Case-sensitive. A message will be logged if wizardry detects a damage source that should be added to this list. Otherwise, don't change unless instructed to do so.");
		property.setLanguageKey("config." + Wizardry.MODID + ".damage_source_blacklist");
		property.setRequiresWorldRestart(true);
		damageSourceBlacklist = property.getStringList();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "compatibilityWarnings", true,
				"Whether to print compatibility warnings to the console. Set to false if excessive messages are being printed.");
		property.setLanguageKey("config." + Wizardry.MODID + ".compatibility_warnings");
		Wizardry.proxy.setToNamedBooleanEntry(property);
		compatibilityWarnings = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "baublesIntegration", true,
				"If Baubles is installed, controls whether Baubles integration features are enabled. If this is disabled, wizardry will always behave as if Baubles is not installed.");
		property.setLanguageKey("config." + Wizardry.MODID + ".baubles_integration");
		property.setRequiresMcRestart(true);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		baublesIntegration = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "jeiIntegration", true,
				"If JEI (Just Enough Items) is installed, controls whether JEI integration features are enabled. If this is disabled, wizardry will always behave as if JEI is not installed.");
		property.setLanguageKey("config." + Wizardry.MODID + ".jei_integration");
		property.setRequiresMcRestart(true);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		jeiIntegration = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "antiqueAtlasIntegration", true,
				"If Antique Atlas is installed, controls whether Antique Atlas integration features are enabled. If this is disabled, wizardry will always behave as if Antique Atlas is not installed.");
		property.setLanguageKey("config." + Wizardry.MODID + ".antique_atlas_integration");
		property.setRequiresMcRestart(true);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		antiqueAtlasIntegration = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "autoPlaceTowerMarkers", true,
				"Controls whether wizardry automatically places antique atlas markers at the locations of wizard towers.");
		property.setLanguageKey("config." + Wizardry.MODID + ".auto_place_tower_markers");
		property.setRequiresMcRestart(true);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		autoTowerMarkers = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "autoPlaceObeliskMarkers", true,
				"Controls whether wizardry automatically places antique atlas markers at the locations of obelisks.");
		property.setLanguageKey("config." + Wizardry.MODID + ".auto_place_obelisk_markers");
		property.setRequiresMcRestart(true);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		autoObeliskMarkers = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(COMPATIBILITY_CATEGORY, "autoPlaceShrineMarkers", true,
				"Controls whether wizardry automatically places antique atlas markers at the locations of shrines.");
		property.setLanguageKey("config." + Wizardry.MODID + ".auto_place_shrine_markers");
		property.setRequiresMcRestart(true);
		Wizardry.proxy.setToNamedBooleanEntry(property);
		autoShrineMarkers = property.getBoolean();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(COMPATIBILITY_CATEGORY, propOrder);

	}

	/** Retrieves a string list from the given property and converts it to an array of {@link ResourceLocation}s. */
	public static ResourceLocation[] getResourceLocationList(Property property){
		return toResourceLocations(property.getStringList());
	}

	/** Converts the given strings to an array of {@link ResourceLocation}s */
	public static ResourceLocation[] toResourceLocations(String... strings){
		return Arrays.stream(strings).map(s -> new ResourceLocation(s.toLowerCase(Locale.ROOT).trim())).toArray(ResourceLocation[]::new);
	}

	/** Applies {@link Settings#parseItemMetaString(String)} to each input string and returns an array of the resulting
	 * {@link Pair}s. */
	@SuppressWarnings("unchecked") // Shut up java
	public static Pair<ResourceLocation, Short>[] parseItemMetaStrings(String... strings){
		return Arrays.stream(strings).map(Settings::parseItemMetaString).toArray(Pair[]::new);
	}

	/** Parses the given input string as an item of the form {@code id:metadata} and returns the resulting
	 * {@link ResourceLocation} ID and metadata value as a {@link Pair} object. */
	public static Pair<ResourceLocation, Short> parseItemMetaString(String string){

		string = string.toLowerCase(Locale.ROOT).trim();

		String[] itemArgs = string.split(":");
		String item;
		short meta;

		try {
			meta = Short.parseShort(itemArgs[itemArgs.length-1]);
			item = String.join(":", Arrays.copyOfRange(itemArgs, 0, itemArgs.length-1));
		}catch(NumberFormatException e){ // If no metadata is specified
			meta = OreDictionary.WILDCARD_VALUE;
			item = string;
		}

		return Pair.of(new ResourceLocation(item), meta);
	}

	/**
	 * Checks a metadata-sensitive list option (see {@link Settings#parseItemMetaStrings(String...)} for the given block.
	 * @param array The config option to check
	 * @param block The block state to search for
	 * @return True if the given array contains an entry that matches the given block, false if not.
	 */
	public static boolean containsMetaBlock(Pair<ResourceLocation, Short>[] array, IBlockState block){
		return containsMetaThing(array, block.getBlock().getRegistryName(), (short)block.getBlock().getMetaFromState(block));
	}

	/**
	 * Checks a metadata-sensitive list option (see {@link Settings#parseItemMetaStrings(String...)} for the given item.
	 * @param array The config option to check
	 * @param stack An item stack with the item and metadata to search for
	 * @return True if the given array contains an entry that matches the given stack, false if not.
	 */
	public static boolean containsMetaItem(Pair<ResourceLocation, Short>[] array, ItemStack stack){
		return containsMetaThing(array, stack.getItem().getRegistryName(), (short)stack.getMetadata());
	}

	/**
	 * Checks a metadata-sensitive list option (see {@link Settings#parseItemMetaStrings(String...)} for the given
	 * id/metadata pair.
	 * @param array The config option to check
	 * @param id The id to search for
	 * @param metadata The metadata value to search for
	 * @return True if the given array contains the given id/metadata pair, or the given id paired with the wildcard value.
	 */
	public static boolean containsMetaThing(Pair<ResourceLocation, Short>[] array, ResourceLocation id, short metadata){
		return Arrays.asList(array).contains(Pair.of(id, metadata)) || Arrays.asList(array).contains(Pair.of(id, (short)OreDictionary.WILDCARD_VALUE));
	}

}
