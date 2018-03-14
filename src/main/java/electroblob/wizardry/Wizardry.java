package electroblob.wizardry;

import electroblob.wizardry.command.CommandCastSpell;
import electroblob.wizardry.command.CommandDiscoverSpell;
import electroblob.wizardry.command.CommandSetAlly;
import electroblob.wizardry.command.CommandViewAllies;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryRegistry;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
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

@Mod(modid = Wizardry.MODID, name = Wizardry.NAME, version = Wizardry.VERSION, guiFactory = "electroblob." + Wizardry.MODID + ".WizardryGuiFactory")
public class Wizardry {

	/** Wizardry's mod ID. */
	// This is going to have to change for 1.12 or it'll conflict with the other wizardry mod.
	// They were there first, it's only fair... although I wonder if that will have unintended side-effects?
	public static final String MODID = "ebwizardry";
	/** Wizardry's mod name, in readable form. */
	public static final String NAME = "Electroblob's Wizardry";
	/**
	 * The version number for this version of wizardry. The following system is used for version numbers:
	 * <p>
	 * <center><b>[major Minecraft version].[major mod version].[minor mod version/patch]
	 * <p>
	 * </center></b> The major mod version is consistent across Minecraft versions, i.e. Wizardry 1.1 has the same
	 * features as Wizardry 2.1, but they are for different versions of Minecraft and have separate minor versioning.
	 * 1.x.x represents Minecraft 1.7.x versions, 2.x.x represents Minecraft 1.10.x versions, 3.x.x represents Minecraft
	 * 1.11.x versions, and so on.
	 */
	public static final String VERSION = "4.0.0";

	// IDEA: Improve the algorithm that finds a place to summon creatures to take walls into account.
	// IDEA: Replace all uses of Math.cos and Math.sin with MathHelper versions
	// IDEA: Triggering of inbuilt Forge events in relevant places?

	/* Minor bugs that need fixing at some point:
	 * - Player skin hat layer shows through wizard hats - Wizard armour breaks rather than just running out of mana (I
	 *   can't seem to replicate this bug, but I have had it happen to me before...)
	 * - Shift-clicking a stack of special upgrades when in the arcane workbench causes the whole stack to be
	 *   transferred when it should be just one (this is a bug with vanilla as well - try putting a stack of bottles into
	 *   a brewing stand). I have at least made it so only one gets used now, so it has no impact on the game.
	 * - When a spell is on cooldown, you can't break blocks when holding a wand. */

	// TODO: So somehow I have managed to overlook the fact that health is actually a float. What this means is that
	// I can make the healing spells use damage multipliers - hurrah!

	// TODO: Switch from IInventory to IItemHandler (Or don't. It's only useful for automation really.)
	// TODO: Rearrange the config gui and file

	// Updating:
	// TODO: Make sure all obf reflection names are still correct
	// TODO: Go through each of the summoned creatures and check there are no extra vanilla methods to override
	// TODO: Check whether Minecraft has added some methods that do the same job as my utilities, particularly in
	// relation to itemstacks, hotbar, armour slots and inventory slots.
	// TODO: Add wizard towers to the /locate command
	// TODO: Fix cascading worldgen lag and backport (offset area into loaded chunks, possibly use WorldGenMinable)
	// TODO: Change evil wizard sounds again to use illager sounds?
	// TODO: Chnage all instanceof checks on split entity variant classes (zombie, skeleton, etc.) to their abstract
	// base classes where appropriate

	// TODO: Have particles obey Minecraft's particle setting where appropriate
	// (see https://github.com/RootsTeam/Embers/blob/master/src/main/java/teamroots/embers/particle/ParticleUtil.java)

	// NOTE: Add melee upgrades to loot tables when they are added.

	/** Static instance of the {@link Settings} object for Wizardry. */
	public static final Settings settings = new Settings();

	/** Static instance of the {@link Logger} object for Wizardry. */
	public static Logger logger;

	// private static Pattern entityNamePattern;

	// EventManager
	WizardryWorldGenerator generator = new WizardryWorldGenerator();

	// The instance of wizardry that Forge uses.
	@Instance(Wizardry.MODID)
	public static Wizardry instance;

	// Location of the proxy code, used by Forge.
	@SidedProxy(clientSide = "electroblob.wizardry.client.ClientProxy", serverSide = "electroblob.wizardry.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){

		logger = event.getModLog();

		// The array in question no longer exists, so I'm pretty sure this isn't necessary any more.
		// expandPotionTypesArray();

		settings.initConfig(event);

		// Yes - by the looks of it, having an interface is completely unnecessary in this case.
		CapabilityManager.INSTANCE.register(WizardData.class, new IStorage<WizardData>(){
			// These methods are only called by Capability.writeNBT() or Capability.readNBT(), which in turn are
			// NEVER CALLED. Unless I'm missing some reflective invocation, that means this entire class serves only
			// to allow capabilities to be saved and loaded manually. What that would be useful for I don't know.
			// (If an API forces most users to write redundant code for no reason, it's not user friendly, is it?)
			// ... well, that's my rant for today!
			@Override
			public NBTBase writeNBT(Capability<WizardData> capability, WizardData instance, EnumFacing side){
				return null;
			}

			@Override
			public void readNBT(Capability<WizardData> capability, WizardData instance, EnumFacing side, NBTBase nbt){
			}
		}, WizardData.class);

		WizardryRegistry.registerTileEntities();

		WizardryRegistry.registerEntities();
		// The check for the generateLoot setting is now done within this method.
		WizardryRegistry.registerLoot();

		WizardryRegistry.registerAdvancementTriggers();

		// Moved to preInit, because apparently it has to be here now.
		proxy.registerRenderers();
		// It seems this also has to be here
		proxy.registerKeyBindings();

	}

	@EventHandler
	public void init(FMLInitializationEvent event){

		settings.initConfigExtras();

		// Event Handlers
		GameRegistry.registerWorldGenerator(generator, 0);
		MinecraftForge.EVENT_BUS.register(new WizardryKeyHandler());
		MinecraftForge.EVENT_BUS.register(instance);
		proxy.registerSpellHUD(); // This can't easily be converted to use the new @Mod.EventBusSubscriber system
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new WizardryGuiHandler());
		WizardryPacketHandler.initPackets();

		// Recipes
		WizardryRegistry.registerRecipes();

		WizardryTabs.sort();

		proxy.initGuiBits();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		// This needs to be here or it won't necessarily include all the mods' entities.
		// TODO: Re-implement this when the Forge bug is fixed.
		/* Doesn't seem to be doing anything... String entityNames = ""; for(Object name :
		 * EntityList.classToStringMapping.values()){ if(name instanceof String){ entityNames = entityNames + name +
		 * '|'; } } // Cuts off the last '|' entityNames = entityNames.substring(0, entityNames.length()-1);
		 * entityNamePattern = Pattern.compile(entityNames); */
		proxy.initialiseLayers();
		WizardryTabs.sort();
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
			// All of the synchronised settings require a world restart anyway so this doesn't need syncing.
		}
	}

	// 2.1 changed some item ids, so this fixes them for existing worlds
	// NOTE: Needs changing to RegistryEvent.MissingMapping in 1.12, or just removing since nobody updates minecraft
	// versions when using mods.
	@EventHandler
	public static void onMissingMappingEvent(RegistryEvent.MissingMappings<Item> event){
		// Just get, not getAll, since the mod id didn't change!
		for(MissingMappings.Mapping<Item> mapping : event.getAllMappings()){
			if(mapping.key.getResourceDomain().equals(Wizardry.MODID)){

				Item replacement = null;

				switch(mapping.key.getResourcePath()){

				case "wand_basic": replacement = WizardryItems.magic_wand; break;
				case "wand_basic_fire": replacement = WizardryItems.basic_fire_wand; break;
				case "wand_basic_ice": replacement = WizardryItems.basic_ice_wand; break;
				case "wand_basic_lightning": replacement = WizardryItems.basic_lightning_wand; break;
				case "wand_basic_necromancy": replacement = WizardryItems.basic_necromancy_wand; break;
				case "wand_basic_earth": replacement = WizardryItems.basic_earth_wand; break;
				case "wand_basic_sorcery": replacement = WizardryItems.basic_sorcery_wand; break;
				case "wand_basic_healing": replacement = WizardryItems.basic_healing_wand; break;
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

}