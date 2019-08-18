package electroblob.wizardry.worldgen;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockRunestone;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.apache.commons.lang3.ArrayUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class WorldGenObelisk extends WorldGenSurfaceStructure {

	private static final String SPAWNER_DATA_BLOCK_TAG = "spawner";

	private static final EnumMap<Element, ResourceLocation> MOB_TYPES = new EnumMap<>(Element.class);

	static {
		MOB_TYPES.put(Element.FIRE, new ResourceLocation(Wizardry.MODID, "blaze_minion"));
		MOB_TYPES.put(Element.ICE, new ResourceLocation(Wizardry.MODID, "ice_wraith"));
		MOB_TYPES.put(Element.LIGHTNING, new ResourceLocation(Wizardry.MODID, "lightning_wraith"));
		MOB_TYPES.put(Element.NECROMANCY, new ResourceLocation(Wizardry.MODID, "wither_skeleton_minion"));
		MOB_TYPES.put(Element.EARTH, new ResourceLocation(Wizardry.MODID, "spider_minion"));
		MOB_TYPES.put(Element.SORCERY, new ResourceLocation(Wizardry.MODID, "vex_minion"));
		MOB_TYPES.put(Element.HEALING, new ResourceLocation(Wizardry.MODID, "husk_minion"));
	}

	@Override
	public String getStructureName(){
		return "obelisk";
	}

	@Override
	public long getRandomSeedModifier(){
		return 19348242L;
	}

	@Override
	public Mirror[] getValidMirrors(){
		return new Mirror[]{Mirror.NONE}; // It's symmetrical so there's no point mirroring it
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ){
		return ArrayUtils.contains(Wizardry.settings.obeliskDimensions, world.provider.getDimension())
				&& Wizardry.settings.obeliskRarity > 0 && random.nextInt(Wizardry.settings.obeliskRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return Wizardry.settings.obeliskFiles[random.nextInt(Wizardry.settings.obeliskFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final Element element = Element.values()[1 + random.nextInt(Element.values().length-1)];

		ITemplateProcessor processor = (w, p, i) -> i.blockState.getBlock() instanceof BlockRunestone ? new Template.BlockInfo(
				i.pos, i.blockState.withProperty(BlockRunestone.ELEMENT, element), i.tileentityData) : i;

		template.addBlocksToWorld(world, origin, processor, settings, 2);

		WizardryAntiqueAtlasIntegration.markObelisk(world, origin.getX(), origin.getZ());

		// Mob spawner
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for(Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()){

			if(entry.getValue().equals(SPAWNER_DATA_BLOCK_TAG)){

				world.setBlockState(entry.getKey(), Blocks.MOB_SPAWNER.getDefaultState());

				if(world.getTileEntity(entry.getKey()) instanceof TileEntityMobSpawner){

					MobSpawnerBaseLogic spawnerLogic = ((TileEntityMobSpawner)world.getTileEntity(entry.getKey())).getSpawnerBaseLogic();
					spawnerLogic.setEntityId(MOB_TYPES.get(element));

				}else{
					Wizardry.logger.info("Tried to set the mob spawned by an obelisk, but the expected TileEntityMobSpawner was not present");
				}

			}else{
				// This probably shouldn't happen...
				Wizardry.logger.info("Unrecognised data block value {} in structure {}", entry.getValue(), structureFile);
			}
		}
	}
}
