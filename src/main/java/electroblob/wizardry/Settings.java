package electroblob.wizardry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import electroblob.wizardry.packet.PacketSyncSettings;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Singleton class which deals with everything related to wizardry's config file. To access individual settings, use
 * {@link Wizardry#settings}. Also stores a few string constants for easy access.
 * <p>
 * As part of the 1.2 update and code overhaul, the way the config settings work in multiplayer has been tightened up.
 * Importantly, there are <b>three</b> different types of config options:
 * <p>
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
 * <p>
 * Each of the settings fields in this class is marked with one of the above categories to indicate which it belongs to.
 * This in turn dictates which logical side it should be called from: client, server or both. <b> Do not access a config
 * setting from the wrong side, because it may cause unexpected or strange behaviour.</b>
 * 
 * @since Wizardry 1.2
 * @author Electroblob
 */
// TODO: New plan: convert to the @Config system BUT separate the spells config out so it doesn't break stuff. (Maybe even
// make it a JSON instead? Either way, there's scope for then adding more options for adjusting the base attributes etc.)
// @Config(modid = Wizardry.MODID)
@SuppressWarnings("deprecation") // Used server I18n deliberately; we want to write the comments in english.
public final class Settings {

	// Category names
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
	/** The unlocalised name of the gameplay config category. */
	public static final String GAMEPLAY_CATEGORY = "gameplay";

	/** The wizardry config file. */
	private Configuration config;

	// Server-only settings. These only affect server-side code and hence are not synced. Changing these locally only
	// has an effect if the local game is the host, i.e. a dedicated server, a LAN host or a singleplayer world.

	// Worldgen
	/** <b>[Server-only]</b> The rarity of wizard towers, used by the world generator. Larger numbers are rarer. */
	public int towerRarity = 8;
	/** <b>[Server-only]</b> List of dimension ids in which to generate crystal ore. */
	public int[] oreDimensions = {0};
	/** <b>[Server-only]</b> List of dimension ids in which to generate crystal ore. */
	public int[] flowerDimensions = {0};
	/** <b>[Server-only]</b> List of dimension ids in which to generate crystal ore. */
	public int[] towerDimensions = {0};
	/**
	 * <b>[Server-only]</b> Whether or not wizardry loot should generate in dungeon chests. Note that this does not
	 * affect the generation of loot in wizard towers.
	 */
	public boolean generateLoot = true;

	// Entities' drops, targeting, damage, etc.
	/** <b>[Server-only]</b> Chance (out of 200) for mobs to drop spell books. */
	public int spellBookDropChance = 3;
	/**
	 * <b>[Server-only]</b> Whether or not players can teleport through unbreakable blocks (e.g. bedrock) using the
	 * phase step spell.
	 */
	public boolean teleportThroughUnbreakableBlocks = false;
	/** <b>[Server-only]</b> Whether to allow players to damage their designated allies using magic. */
	public boolean friendlyFire = true;
	/** <b>[Server-only]</b> Whether to allow players to disarm other players using the telekinesis spell. */
	public boolean telekineticDisarmament = true;
	/** <b>[Server-only]</b> Whether summoned creatures can revenge attack their caster if their caster attacks them. */
	public boolean minionRevengeTargeting = true;
	/**
	 * <b>[Server-only]</b> List of registry names of entities which summoned creatures are allowed to attack, in addition
	 * to the defaults.
	 */
	public ResourceLocation[] summonedCreatureTargetsWhitelist = {};
	/**
	 * <b>[Server-only]</b> List of registry names of entities which summoned creatures are specifically not allowed to
	 * attack, overriding the defaults and the whitelist.
	 */
	public ResourceLocation[] summonedCreatureTargetsBlacklist = {new ResourceLocation("creeper")};
	/**
	 * <b>[Server-only]</b> List of registry names of entities which are immune to the mind control spell, in addition to
	 * the defaults.
	 */
	public ResourceLocation[] mindControlTargetsBlacklist = {};
	/** <b>[Server-only]</b> Global damage scaling factor for all player magic damage. */
	public double playerDamageScale = 1.0f;
	/** <b>[Server-only]</b> Global damage scaling factor for all npc magic damage. */
	public double npcDamageScale = 1.0f;
	/** <b>[Server-only]</b> List of dimension ids in which evil wizards can spawn. */
	public int[] evilWizardDimensions = {0};

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

	// Synchronised settings. These settings affect both client-side AND server-side code. Changing these locally
	// only has an effect if the local game is the host, i.e. a dedicated server, a LAN host or a singleplayer world.

	// Gamemodes
	/**
	 * <b>[Synchronised]</b> When set to true, spells a player hasn't cast yet will be unreadable until they are cast
	 * (on a per-world basis). Has no effect when in creative mode. Spells of identification will be unobtainable in
	 * survival mode if this is false.
	 */
	public boolean discoveryMode = true;

	// Client-only settings. These settings only affect client-side code and hence are not synced. Each client obeys
	// its own values for these, and changing them on a dedicated server will have no effect.

	// Controls
	/**
	 * <b>[Client-only]</b> Whether the player can switch between spells on a wand by scrolling with the mouse wheel
	 * while sneaking.
	 */
	public boolean enableShiftScrolling = true;

	// Display
	/** <b>[Client-only]</b> Whether to show summoned creatures' names and owners above their heads. */
	public boolean showSummonedCreatureNames = true;
	/** <b>[Client-only]</b> The position of the spell HUD. */
	public GuiPosition spellHUDPosition = GuiPosition.BOTTOM_LEFT;

	/** Set of constants for each of the four positions that the spell HUD can be in. */
	public enum GuiPosition {

		BOTTOM_LEFT("Bottom left", false, false),
		TOP_LEFT("Top left", false, true),
		TOP_RIGHT("Top right", true, true),
		BOTTOM_RIGHT("Bottom right", true, false);

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
		public boolean flipX;
		public boolean flipY;

		GuiPosition(String name, boolean flipX, boolean flipY){
			this.name = name;
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

		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		Wizardry.logger.info("Setting up main config");

		setupGeneralConfig();
		setupWorldgenConfig();
		setupClientConfig();
		setupCommandsConfig();

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

		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			property = config.get(SPELLS_CATEGORY, spell.getRegistryName().toString(), true,
					I18n.translateToLocal("spell." + spell.getUnlocalisedName() + ".desc"));
			// Uses the same config key as the spell name, because - well, that's what it's called!
			property.setLanguageKey("spell." + spell.getUnlocalisedName());
			spell.setEnabled(property.getBoolean());
		}

	}

	private void setupGeneralConfig(){

		// This trick is borrowed from forge; it sorts the config options into the order you want them.
		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(GAMEPLAY_CATEGORY, "Global settings that affect game mechanics. In multiplayer, the server/LAN host settings will apply. Please note that changing some of these settings may make the mod very difficult to play.");

		property = config.get(GAMEPLAY_CATEGORY, "discoveryMode", true,
				"For those who like a sense of mystery! When set to true, spells you haven't cast yet will be unreadable until you cast them (on a per-world basis). Has no effect when in creative mode. Spells of identification will be unobtainable in survival mode if this is false.");
		property.setLanguageKey("config." + Wizardry.MODID + ".discovery_mode");
		property.setRequiresWorldRestart(true);
		discoveryMode = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "friendlyFire", true,
				"Whether to allow players to damage their designated allies using magic.");
		property.setLanguageKey("config." + Wizardry.MODID + ".friendly_fire");
		friendlyFire = property.getBoolean();
		propOrder.add(property.getName());
		
		property = config.get(GAMEPLAY_CATEGORY, "spellBookDropChance", 3,
				"The chance for mobs to drop a spell book when killed. The greater this number, the more often they will drop. Set to 0 to disable spell book drops. Set to 200 for guaranteed drops.",
				0, 200);
		property.setLanguageKey("config." + Wizardry.MODID + ".spell_book_drop_chance");
		Wizardry.proxy.setToNumberSliderEntry(property);
		spellBookDropChance = property.getInt();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "evilWizardDimensions", new int[]{0},
				"List of dimension ids in which evil wizards can spawn.");
		property.setLanguageKey("config." + Wizardry.MODID + ".evil_wizard_dimensions");
		property.setRequiresMcRestart(true);
		evilWizardDimensions = property.getIntList();
		propOrder.add(property.getName());

		// These two aren't sliders because using a slider makes it difficult to fine-tune the numbers; the nature of a
		// scaling factor means that 0.5 is as big a change as 2.0, so whilst a slider is fine for increasing the
		// damage, it doesn't give fine enough control for values less than 1.
		property = config.get(GAMEPLAY_CATEGORY, "playerDamageScaling", 1.0,
				"Global damage scaling factor for the damage dealt by players casting spells, relative to 1.", 0, 20);
		property.setLanguageKey("config." + Wizardry.MODID + ".player_damage_scaling");
		playerDamageScale = property.getDouble();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "npcDamageScaling", 1.0,
				"Global damage scaling factor for the damage dealt by NPCs casting spells, relative to 1.", 0, 20);
		property.setLanguageKey("config." + Wizardry.MODID + ".npc_damage_scaling");
		npcDamageScale = property.getDouble();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "minionRevengeTargeting", true,
				"Whether summoned creatures can revenge attack their owner if their owner attacks them.");
		property.setLanguageKey("config." + Wizardry.MODID + ".minion_revenge_targeting");
		property.setRequiresWorldRestart(false);
		minionRevengeTargeting = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "summonedCreatureTargetsWhitelist", new String[0],
				"List of names of entities which summoned creatures and wizards are allowed to attack, in addition to the defaults. Add mod creatures to this list if you want summoned creatures to attack them and they aren't already doing so. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".summoned_creature_targets_whitelist");
		property.setRequiresWorldRestart(true);
		// Converts all strings in the list to a ResourceLocation.
		summonedCreatureTargetsWhitelist = Arrays.stream(property.getStringList()).map(s -> new ResourceLocation(s.toLowerCase(Locale.ROOT).trim())).toArray(ResourceLocation[]::new);
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "summonedCreatureTargetsBlacklist",
				new String[]{"creeper"},
				"List of names of entities which summoned creatures and wizards are specifically not allowed to attack, overriding the defaults and the whitelist. Add creatures to this list if allowing them to be attacked causes problems or is too destructive (removing creepers from this list is done at your own risk!). Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".summoned_creature_targets_blacklist");
		property.setRequiresWorldRestart(true);
		// Converts all strings in the list to a ResourceLocation.
		summonedCreatureTargetsBlacklist = Arrays.stream(property.getStringList()).map(s -> new ResourceLocation(s.toLowerCase(Locale.ROOT).trim())).toArray(ResourceLocation[]::new);
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "telekineticDisarmament", true,
				"Whether to allow players to disarm other players using the telekinesis spell. Set to false to prevent stealing of items.");
		property.setLanguageKey("config." + Wizardry.MODID + ".telekinetic_disarmament");
		telekineticDisarmament = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "teleportThroughUnbreakableBlocks", false,
				"Whether players are allowed to teleport through unbreakable blocks (e.g. bedrock) using the phase step spell.");
		property.setLanguageKey("config." + Wizardry.MODID + ".teleport_through_unbreakable_blocks");
		teleportThroughUnbreakableBlocks = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(GAMEPLAY_CATEGORY, "mindControlTargetsBlacklist", new String[]{},
				"List of names of entities which cannot be mind controlled, in addition to the defaults. Add creatures to this list if allowing them to be mind-controlled causes problems or could be exploited. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
		property.setLanguageKey("config." + Wizardry.MODID + ".mind_control_targets_blacklist");
		property.setRequiresWorldRestart(true);
		// Converts all strings in the list to a ResourceLocation.
		mindControlTargetsBlacklist = Arrays.stream(property.getStringList()).map(s -> new ResourceLocation(s.toLowerCase(Locale.ROOT).trim())).toArray(ResourceLocation[]::new);
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(GAMEPLAY_CATEGORY, propOrder);

	}
	
	private void setupWorldgenConfig(){

		// This trick is borrowed from forge; it sorts the config options into the order you want them.
		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(WORLDGEN_CATEGORY, "Settings that affect world generation. In multiplayer, the server/LAN host settings will apply.");
	
		property = config.get(WORLDGEN_CATEGORY, "towerRarity", 8, "Rarity of wizard towers. Higher numbers are rarer. Set to 0 to disable wizard towers completely.", 0, 50);
		property.setLanguageKey("config." + Wizardry.MODID + ".tower_rarity");
		property.setRequiresWorldRestart(true);
		Wizardry.proxy.setToNumberSliderEntry(property);
		towerRarity = property.getInt();
		propOrder.add(property.getName());

		property = config.get(WORLDGEN_CATEGORY, "towerDimensions", new int[]{0}, "List of dimension ids in which wizard towers will generate.");
		property.setLanguageKey("config." + Wizardry.MODID + ".tower_dimensions");
		property.setRequiresWorldRestart(true);
		towerDimensions = property.getIntList();
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

		property = config.get(WORLDGEN_CATEGORY, "generateLoot", true, "Whether to inject wizardry loot (as specified in loot_tables/chests/dungeon_additions.json) into the loot tables for vanilla dungeon chests.");
		property.setLanguageKey("config." + Wizardry.MODID + ".generate_loot");
		property.setRequiresWorldRestart(true);
		generateLoot = property.getBoolean();
		propOrder.add(property.getName());
		
		config.setCategoryPropertyOrder(WORLDGEN_CATEGORY, propOrder);
	}
	
	private void setupClientConfig(){

		// This trick is borrowed from forge; it sorts the config options into the order you want them.
		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(CLIENT_CATEGORY, "Client-side settings that only affect the local minecraft game. If this file is on a dedicated server, these settings will have no effect; in multiplayer, each player obeys their own settings.");

		property = config.get(CLIENT_CATEGORY, "enableShiftScrolling", true,
				"Whether you can switch between spells on a wand by scrolling with the mouse wheel while sneaking. Note that this will only affect you; other players connected to the same server obey their own settings.");
		property.setLanguageKey("config." + Wizardry.MODID + ".enable_shift_scrolling");
		property.setRequiresWorldRestart(false);
		enableShiftScrolling = property.getBoolean();
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "spellHUDPosition", GuiPosition.BOTTOM_LEFT.name, "The position of the spell HUD.", GuiPosition.names);
		property.setLanguageKey("config." + Wizardry.MODID + ".spell_hud_position");
		spellHUDPosition = GuiPosition.fromName(property.getString());
		propOrder.add(property.getName());

		property = config.get(CLIENT_CATEGORY, "showSummonedCreatureNames", true, "Whether to show summoned creatures' names and owners above their heads.");
		property.setLanguageKey("config." + Wizardry.MODID + ".show_summoned_creature_names");
		showSummonedCreatureNames = property.getBoolean();
		propOrder.add(property.getName());

		config.setCategoryPropertyOrder(CLIENT_CATEGORY, propOrder);
	}
	
	private void setupCommandsConfig(){

		// This trick is borrowed from forge; it sorts the config options into the order you want them.
		List<String> propOrder = new ArrayList<String>();

		Property property;

		config.addCustomCategoryComment(COMMANDS_CATEGORY, "Settings for the commands added by Wizardry. In multiplayer, the server/LAN host settings will apply.");

		// This one isn't a slider either because people are likely to want exact values ("it must be at most 50.3" is a
		// bit strange!).
		property = config.get(COMMANDS_CATEGORY, "castCommandMultiplierLimit", 20.0,
				"Upper limit for the multipliers passed into the /cast command. This is here to stop players from accidentally breaking a world/server. Large blast mutipliers can cause extreme lag - you have been warned!",
				1, 255);
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
				"List of names of entities that are immune to fire, in addition to the defaults. Add mod creatures to this list if you want them to be immune to fire magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
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
				"List of names of entities that are immune to ice, in addition to the defaults. Add mod creatures to this list if you want them to be immune to ice magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
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
				"List of names of entities that are immune to lightning, in addition to the defaults. Add mod creatures to this list if you want them to be immune to lightning magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
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
				"List of names of entities that are immune to wither effects, in addition to the defaults. Add mod creatures to this list if you want them to be immune to withering magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
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
				"List of names of entities that are immune to poison, in addition to the defaults. Add mod creatures to this list if you want them to be immune to poison magic and they aren't already. Entity names are not case sensitive. For mod entities, prefix with the mod ID (e.g. " + Wizardry.MODID + ":wizard).");
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

}
