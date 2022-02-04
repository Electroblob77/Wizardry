package electroblob.wizardry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.command.CommandCastSpell;
import electroblob.wizardry.command.CommandDiscoverSpell;
import electroblob.wizardry.command.CommandSetAlly;
import electroblob.wizardry.command.CommandViewAllies;
import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.data.Behaviour;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.integration.baubles.WizardryBaublesIntegration;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.misc.DonationPerksHandler;
import electroblob.wizardry.misc.Forfeit;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.worldgen.*;
import net.minecraft.item.Item;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * <i>"Electroblob's Wizardry adds an RPG-like system of spells to Minecraft, with the aim of being as playable as
 * possible. No crazy constructs, no perk trees, no complex recipes - simply find spell books, cast spells, and master
 * the arcane! - But you knew that, right?"</i>
 * <p></p>
 * Main mod class for Wizardry. Contains the logger and settings instances, along with all the other stuff that's normally
 * in a main mod class.
 * @author Electroblob
 * @since Wizardry 1.0
 */

@Mod(modid = Wizardry.MODID, name = Wizardry.NAME, version = Wizardry.VERSION, acceptedMinecraftVersions = "[1.12.2]",
		guiFactory = "electroblob.wizardry.WizardryGuiFactory",
		dependencies = "required-after:forge@[14.23.5.2847,);after:jei@[4.15.0,);after:baubles@[1.5.2,);after:antiqueatlas@[4.6,)")

public class Wizardry {

	/** Wizardry's mod ID. */
	public static final String MODID = "ebwizardry";
	/** Wizardry's mod name, in readable form. */
	public static final String NAME = "Electroblob's Wizardry";
	/**
	 * The version number for this version of wizardry. The following system is used for version numbers:
	 * <p></p>
	 * <center><b>[major Minecraft version].[major mod version].[minor mod version/patch]</b>
	 * <p></p>
	 * </center> The major mod version is consistent across Minecraft versions, i.e. Wizardry 1.1 has the same
	 * features as Wizardry 2.1, but they are for different versions of Minecraft and have separate minor versioning.
	 * 1.x.x represents Minecraft 1.7.x versions, 2.x.x represents Minecraft 1.10.x versions, 3.x.x represents Minecraft
	 * 1.11.x versions, and so on.
	 */
	public static final String VERSION = "4.3.4";

	// IDEA: Triggering of inbuilt Forge events in relevant places?
	// IDEA: Abstract the vanilla particles behind the particle builder

	// TODO: Have particles obey Minecraft's particle setting where appropriate
	// (see https://github.com/RootsTeam/Embers/blob/master/src/main/java/teamroots/embers/particle/ParticleUtil.java)
	// TODO: TileEntityArcaneWorkbench needs looking at, esp. regarding inventory and markDirty

	/** Static instance of the {@link Settings} object for Wizardry. */
	public static final Settings settings = new Settings();

	/** Static instance of the {@link Logger} object for Wizardry.
	 * <p></p>
	 * Logging conventions for wizardry (only these levels are used currently):
	 * <p></p>
	 * - <b>ERROR</b>: Anything that threw an exception; may or may not crash the game.<br>
	 * - <b>WARN</b>: Anything that isn't supposed to happen during normal operation, but didn't throw an exception.<br>
	 * - <b>INFO</b>: Anything that might happen during normal mod operation that the user needs to know about. */
	public static Logger logger;

	/** A {@link File} object representing wizardry's config folder, {@code config/ebwizardry}). As of wizardry 4.2.4,
	 * this folder contains the main config file and the global spell properties folder, if used. */
	public static File configDirectory;

	public static boolean tisTheSeason;

	// The instance of wizardry that Forge uses.
	@Instance(Wizardry.MODID)
	public static Wizardry instance;

	// Location of the proxy code, used by Forge.
	@SidedProxy(clientSide = "electroblob.wizardry.client.ClientProxy", serverSide = "electroblob.wizardry.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){

		logger = event.getModLog();
		
		proxy.registerResourceReloadListeners();

		configDirectory = new File(event.getModConfigurationDirectory(), Wizardry.MODID);
		settings.initConfig(event);

		Calendar calendar = Calendar.getInstance();
		tisTheSeason = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DAY_OF_MONTH) >= 24
				&& calendar.get(Calendar.DAY_OF_MONTH) <= 26;

		// Capabilities
		WizardData.register();
		DispenserCastingData.register();

		// Register things that don't have registries
		WizardryBlocks.registerTileEntities();
		WizardryLoot.register();
		WizardryAdvancementTriggers.register();
		Forfeit.register();
		BlockBookshelf.registerStandardBookModelTextures();

		// Client-side stuff (via proxies)
		proxy.registerRenderers();
		proxy.registerKeyBindings();

		// Commented out for 4.2.3 to get rid of the sound bug, reinstate once a fix is found.
		WizardrySounds.SPELLS = SoundCategory.PLAYERS;//CustomSoundCategory.add(Wizardry.MODID + "_spells");

		WizardryBaublesIntegration.init();
		WizardryAntiqueAtlasIntegration.init();

		Behaviour.registerBehaviours();

	}

	@EventHandler
	public void init(FMLInitializationEvent event){

		settings.initConfigExtras();

		// World generators
		// Weight is a misnomer, it's actually the priority (where lower numbers get generated first)
		// Literally nothing on typical 'weight' values here, there isn't even an upper limit
		// Examples I've managed to find:
		// - Tinker's construct slime islands use 25
		GameRegistry.registerWorldGenerator(new WorldGenCrystalOre(), 0);
		GameRegistry.registerWorldGenerator(new WorldGenCrystalFlower(), 50);
		GameRegistry.registerWorldGenerator(new WorldGenWizardTower(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenObelisk(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenShrine(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenLibraryRuins(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenUndergroundLibraryRuins(), 20);

		// This is for the config change and missing mappings events
		MinecraftForge.EVENT_BUS.register(instance); // Since there's already an instance we might as well use it

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new WizardryGuiHandler());
		WizardryPacketHandler.initPackets();

		// Post-registry extras
		BlockBookshelf.compileBookModelTextures();
		ContainerBookshelf.initDefaultBookItems();
		WizardryItems.registerDispenseBehaviours();
		WizardryItems.registerBannerPatterns();
		WandHelper.populateUpgradeMap();
		Spell.registry.forEach(Spell::init);
		SpellProperties.init();

		// Client-side stuff (via proxies)
		proxy.initGuiBits();
		proxy.registerParticles();
		proxy.registerSoundEventListener();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		proxy.initialiseLayers();
		proxy.initialiseAnimations();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		event.registerServerCommand(new CommandCastSpell());
		event.registerServerCommand(new CommandSetAlly());
		event.registerServerCommand(new CommandViewAllies());
		event.registerServerCommand(new CommandDiscoverSpell());
	}

	@SubscribeEvent
	public void onConfigChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.getModID().equals(Wizardry.MODID)){
			settings.saveConfigChanges();
			// All of the synchronised settings require a world restart anyway so don't need syncing, except for the
			// donor perks which have special behaviour (since we have to update everyone when a donor logs in anyway,
			// we might as well reuse the packet and let them change the setting mid-game)
			if(event.isWorldRunning() && DonationPerksHandler.isDonor(proxy.getThePlayer())){
				DonationPerksHandler.sendToServer(proxy.getThePlayer());
			}
		}
	}

	// 2.1 changed some item ids, so this fixes them for existing worlds
	// Nobody updates minecraft versions when using mods, but I may as well leave this here just in case.
	@SubscribeEvent
	public static void onMissingItemMappingEvent(RegistryEvent.MissingMappings<Item> event){
		// Just get, not getAll, since the mod id didn't change!
		for(RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()){
			if(mapping.key.getNamespace().equals(Wizardry.MODID)){

				Item replacement;

				switch(mapping.key.getPath()){

				case "wand_basic": replacement = WizardryItems.magic_wand; break;
				case "wand_basic_fire": replacement = WizardryItems.novice_fire_wand; break;
				case "wand_basic_ice": replacement = WizardryItems.novice_ice_wand; break;
				case "wand_basic_lightning": replacement = WizardryItems.novice_lightning_wand; break;
				case "wand_basic_necromancy": replacement = WizardryItems.novice_necromancy_wand; break;
				case "wand_basic_earth": replacement = WizardryItems.novice_earth_wand; break;
				case "wand_basic_sorcery": replacement = WizardryItems.novice_sorcery_wand; break;
				case "wand_basic_healing": replacement = WizardryItems.novice_healing_wand; break;
				case "basic_fire_wand": replacement = WizardryItems.novice_fire_wand; break;
				case "basic_ice_wand": replacement = WizardryItems.novice_ice_wand; break;
				case "basic_lightning_wand": replacement = WizardryItems.novice_lightning_wand; break;
				case "basic_necromancy_wand": replacement = WizardryItems.novice_necromancy_wand; break;
				case "basic_earth_wand": replacement = WizardryItems.novice_earth_wand; break;
				case "basic_sorcery_wand": replacement = WizardryItems.novice_sorcery_wand; break;
				case "basic_healing_wand": replacement = WizardryItems.novice_healing_wand; break;
				case "wand_apprentice": replacement = WizardryItems.apprentice_wand; break;
				case "wand_apprentice_fire": replacement = WizardryItems.apprentice_fire_wand; break;
				case "wand_apprentice_ice": replacement = WizardryItems.apprentice_ice_wand; break;
				case "wand_apprentice_lightning": replacement = WizardryItems.apprentice_lightning_wand; break;
				case "wand_apprentice_necromancy": replacement = WizardryItems.apprentice_necromancy_wand; break;
				case "wand_apprentice_earth": replacement = WizardryItems.apprentice_earth_wand; break;
				case "wand_apprentice_sorcery": replacement = WizardryItems.apprentice_sorcery_wand; break;
				case "wand_apprentice_healing": replacement = WizardryItems.apprentice_healing_wand; break;
				case "wand_advanced": replacement = WizardryItems.advanced_wand; break;
				case "wand_advanced_fire": replacement = WizardryItems.advanced_fire_wand; break;
				case "wand_advanced_ice": replacement = WizardryItems.advanced_ice_wand; break;
				case "wand_advanced_lightning": replacement = WizardryItems.advanced_lightning_wand; break;
				case "wand_advanced_necromancy": replacement = WizardryItems.advanced_necromancy_wand; break;
				case "wand_advanced_earth": replacement = WizardryItems.advanced_earth_wand; break;
				case "wand_advanced_sorcery": replacement = WizardryItems.advanced_sorcery_wand; break;
				case "wand_advanced_healing": replacement = WizardryItems.advanced_healing_wand; break;
				case "wand_master": replacement = WizardryItems.master_wand; break;
				case "wand_master_fire": replacement = WizardryItems.master_fire_wand; break;
				case "wand_master_ice": replacement = WizardryItems.master_ice_wand; break;
				case "wand_master_lightning": replacement = WizardryItems.master_lightning_wand; break;
				case "wand_master_necromancy": replacement = WizardryItems.master_necromancy_wand; break;
				case "wand_master_earth": replacement = WizardryItems.master_earth_wand; break;
				case "wand_master_sorcery": replacement = WizardryItems.master_sorcery_wand; break;
				case "wand_master_healing": replacement = WizardryItems.master_healing_wand; break;
				case "upgrade_storage": replacement = WizardryItems.storage_upgrade; break;
				case "upgrade_siphon": replacement = WizardryItems.siphon_upgrade; break;
				case "upgrade_condenser": replacement = WizardryItems.condenser_upgrade; break;
				case "upgrade_range": replacement = WizardryItems.range_upgrade; break;
				case "upgrade_duration": replacement = WizardryItems.duration_upgrade; break;
				case "upgrade_cooldown": replacement = WizardryItems.cooldown_upgrade; break;
				case "upgrade_blast": replacement = WizardryItems.blast_upgrade; break;
				case "upgrade_attunement": replacement = WizardryItems.attunement_upgrade; break;
				// If it didn't match any of the ones that changed, do nothing.
				default:
					return;
				}
				// No need to log, Forge does that.
				mapping.remap(replacement);
			}
		}
	}

	@SubscribeEvent
	public static void onMissingSpellMappingEvent(RegistryEvent.MissingMappings<Spell> event){
		for(RegistryEvent.MissingMappings.Mapping<Spell> mapping : event.getAllMappings()){
			if(mapping.key.getNamespace().equals(Wizardry.MODID)){
				if(mapping.key.getPath().equals("firestorm")) mapping.remap(Spells.fire_breath);
			}
		}
	}

}