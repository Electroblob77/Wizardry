package electroblob.wizardry.worldgen;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockPedestal;
import electroblob.wizardry.block.BlockRunestone;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.integration.antiqueatlas.WizardryAntiqueAtlasIntegration;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.spell.ArcaneLock;
import electroblob.wizardry.tileentity.TileEntityShrineCore;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WorldGenShrine extends WorldGenSurfaceStructure {

	private static final String CORE_DATA_BLOCK_TAG = "core";

	@Override
	public String getStructureName(){
		return "shrine";
	}

	@Override
	public long getRandomSeedModifier(){
		return 17502749L;
	}

	@Override
	public boolean canGenerate(Random random, World world, int chunkX, int chunkZ){
		return ArrayUtils.contains(Wizardry.settings.shrineDimensions, world.provider.getDimension())
				&& Wizardry.settings.shrineRarity > 0 && random.nextInt(Wizardry.settings.shrineRarity) == 0;
	}

	@Override
	public ResourceLocation getStructureFile(Random random){
		return Wizardry.settings.shrineFiles[random.nextInt(Wizardry.settings.shrineFiles.length)];
	}

	@Override
	public void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile){

		final Element element = Element.values()[1 + random.nextInt(Element.values().length-1)];

		ITemplateProcessor processor = (w, p, i) -> i.blockState.getBlock() instanceof BlockRunestone ? new Template.BlockInfo(
				i.pos, i.blockState.withProperty(BlockRunestone.ELEMENT, element), i.tileentityData) : i;

		template.addBlocksToWorld(world, origin, processor, settings, 2);

		WizardryAntiqueAtlasIntegration.markShrine(world, origin.getX(), origin.getZ());

		// Shrine core
		Map<BlockPos, String> dataBlocks = template.getDataBlocks(origin, settings);

		for(Map.Entry<BlockPos, String> entry : dataBlocks.entrySet()){

			if(entry.getValue().equals(CORE_DATA_BLOCK_TAG)){
				// This bit could have been done with a template processor, but we also need to link the chest and lock it
				world.setBlockState(entry.getKey(), WizardryBlocks.runestone_pedestal.getDefaultState()
						.withProperty(BlockPedestal.ELEMENT, element).withProperty(BlockPedestal.NATURAL, true));

				TileEntity core = world.getTileEntity(entry.getKey());
				TileEntity container = world.getTileEntity(entry.getKey().up());

				if(container != null){

					container.getTileData().setUniqueId(ArcaneLock.NBT_KEY, new UUID(0, 0)); // Nil UUID

					if(core instanceof TileEntityShrineCore){
						((TileEntityShrineCore)core).linkContainer(container);
					}else{
						Wizardry.logger.info("What?!");
					}

				}else{
					Wizardry.logger.info("Expected chest or other container at {} in structure {}, found no tile entity", entry.getKey(), structureFile);
				}

			}else{
				// This probably shouldn't happen...
				Wizardry.logger.info("Unrecognised data block value {} in structure {}", entry.getValue(), structureFile);
			}
		}
	}

}
