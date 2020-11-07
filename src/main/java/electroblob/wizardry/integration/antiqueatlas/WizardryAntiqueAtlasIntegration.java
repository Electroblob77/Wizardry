package electroblob.wizardry.integration.antiqueatlas;

import electroblob.wizardry.Wizardry;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.api.AtlasAPI;
import hunternif.mc.atlas.marker.GlobalMarkersData;
import hunternif.mc.atlas.marker.Marker;
import hunternif.mc.atlas.registry.MarkerType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * This class handles all of wizardry's integration with the <i>Antique Atlas</i> mod. This class contains only the code
 * that requires Antique Atlas to be loaded in order to run. Conversely, all code that requires Antique Atlas to be
 * loaded is located within this class or another class in the package {@code electroblob.wizardry.integration.antiqueatlas}.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
@Mod.EventBusSubscriber
public class WizardryAntiqueAtlasIntegration {

	public static final String ANTIQUE_ATLAS_MOD_ID = "antiqueatlas";

	private static final ResourceLocation TOWER_MARKER = new ResourceLocation(Wizardry.MODID, "wizard_tower");
	private static final ResourceLocation SHRINE_MARKER = new ResourceLocation(Wizardry.MODID, "shrine");
	private static final ResourceLocation OBELISK_MARKER = new ResourceLocation(Wizardry.MODID, "obelisk");
	private static final ResourceLocation LIBRARY_MARKER = new ResourceLocation(Wizardry.MODID, "library_ruins");

	private static boolean antiqueAtlasLoaded;

	public static void init(){
		antiqueAtlasLoaded = Loader.isModLoaded(ANTIQUE_ATLAS_MOD_ID);
		Wizardry.proxy.registerAtlasMarkers(); // Needs routing through the proxies to make sure it's only client-side
	}

	public static boolean enabled(){
		return Wizardry.settings.antiqueAtlasIntegration && antiqueAtlasLoaded;
	}

	/** Places a global wizard tower marker in all antique atlases at the given coordinates in the given world if
	 * {@link electroblob.wizardry.Settings#autoTowerMarkers} is enabled. Server side only! */
	public static void markTower(World world, int x, int z){
		if(enabled() && Wizardry.settings.autoTowerMarkers){
			AtlasAPI.getMarkerAPI().putGlobalMarker(world, false, TOWER_MARKER.toString(), "integration.antiqueatlas.marker." + TOWER_MARKER.toString().replace(':', '.'), x, z);
		}
	}

	/** Places a global obelisk marker in all antique atlases at the given coordinates in the given world if
	 * {@link electroblob.wizardry.Settings#autoObeliskMarkers} is enabled. Server side only! */
	public static void markObelisk(World world, int x, int z){
		if(enabled() && Wizardry.settings.autoObeliskMarkers){
			AtlasAPI.getMarkerAPI().putGlobalMarker(world, false, OBELISK_MARKER.toString(), "integration.antiqueatlas.marker." + OBELISK_MARKER.toString().replace(':', '.'), x, z);
		}
	}

	/** Places a global shrine marker in all antique atlases at the given coordinates in the given world if
	 * {@link electroblob.wizardry.Settings#autoShrineMarkers} is enabled. Server side only! */
	public static void markShrine(World world, int x, int z){
		if(enabled() && Wizardry.settings.autoShrineMarkers){
			AtlasAPI.getMarkerAPI().putGlobalMarker(world, false, SHRINE_MARKER.toString(), "integration.antiqueatlas.marker." + SHRINE_MARKER.toString().replace(':', '.'), x, z);
		}
	}

	/** Places a global library ruins marker in all antique atlases at the given coordinates in the given world if
	 * {@link electroblob.wizardry.Settings#autoLibraryMarkers} is enabled. Server side only! */
	public static void markLibrary(World world, int x, int z, boolean underground){
		if(enabled() && (underground ? Wizardry.settings.autoUndergroundLibraryMarkers : Wizardry.settings.autoLibraryMarkers)){
			AtlasAPI.getMarkerAPI().putGlobalMarker(world, false, LIBRARY_MARKER.toString(), "integration.antiqueatlas.marker." + LIBRARY_MARKER.toString().replace(':', '.'), x, z);
		}
	}

	/** Registers the marker icons with Antique Atlas. Client side only! */
	public static void registerMarkers(){

		if(!enabled()) return;

		AtlasAPI.getMarkerAPI().registerMarker(new MarkerType(TOWER_MARKER, new ResourceLocation(Wizardry.MODID, "textures/integration/antiqueatlas/wizard_tower.png")));
		AtlasAPI.getMarkerAPI().registerMarker(new MarkerType(SHRINE_MARKER, new ResourceLocation(Wizardry.MODID, "textures/integration/antiqueatlas/shrine.png")));
		AtlasAPI.getMarkerAPI().registerMarker(new MarkerType(OBELISK_MARKER, new ResourceLocation(Wizardry.MODID, "textures/integration/antiqueatlas/obelisk.png")));
		AtlasAPI.getMarkerAPI().registerMarker(new MarkerType(LIBRARY_MARKER, new ResourceLocation(Wizardry.MODID, "textures/integration/antiqueatlas/library_ruins.png")));
	}

	@SubscribeEvent
	public static void onWorldLoadEvent(WorldEvent.Load event){

		if(!enabled()) return;

		// Backwards compatibility for existing markers using the old translation key format (with colons)
		GlobalMarkersData data = AntiqueAtlasMod.globalMarkersData.getData();
		for(Marker marker : data.getMarkersInDimension(event.getWorld().provider.getDimension())){
			if(marker.getLabel().contains(":")){
				// Remove old-format markers and replace them with new ones
				data.removeMarker(marker.getId());
				AtlasAPI.getMarkerAPI().putGlobalMarker(event.getWorld(), marker.isVisibleAhead(), marker.getType(),
						marker.getLabel().replace(':', '.'), marker.getX(), marker.getZ());
			}
		}
	}

}
