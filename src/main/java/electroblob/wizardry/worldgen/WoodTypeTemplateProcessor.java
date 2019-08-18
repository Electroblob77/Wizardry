package electroblob.wizardry.worldgen;

import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWoodSlab;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nullable;
import java.util.EnumMap;

/** Structure template processor that switches all wood in the structure to a certain given wood type. */
public class WoodTypeTemplateProcessor implements ITemplateProcessor {

	private final BlockPlanks.EnumType woodType;

	private final EnumMap<BlockPlanks.EnumType, Block> DOORS;
	private final EnumMap<BlockPlanks.EnumType, Block> STAIRS;
	private final EnumMap<BlockPlanks.EnumType, Block> FENCES;
	private final EnumMap<BlockPlanks.EnumType, Block> FENCE_GATES;

	/**
	 * Creates a new {@code WoodTypeTemplateProcessor} of the given type.
	 * @param woodType The wood type to be used.
	 */
	public WoodTypeTemplateProcessor(BlockPlanks.EnumType woodType){

		this.woodType = woodType;

		DOORS = new EnumMap<>(BlockPlanks.EnumType.class);
		DOORS.put(BlockPlanks.EnumType.OAK, Blocks.OAK_DOOR);
		DOORS.put(BlockPlanks.EnumType.SPRUCE, Blocks.SPRUCE_DOOR);
		DOORS.put(BlockPlanks.EnumType.BIRCH, Blocks.BIRCH_DOOR);
		DOORS.put(BlockPlanks.EnumType.JUNGLE, Blocks.JUNGLE_DOOR);
		DOORS.put(BlockPlanks.EnumType.ACACIA, Blocks.ACACIA_DOOR);
		DOORS.put(BlockPlanks.EnumType.DARK_OAK, Blocks.DARK_OAK_DOOR);

		STAIRS = new EnumMap<>(BlockPlanks.EnumType.class);
		STAIRS.put(BlockPlanks.EnumType.OAK, Blocks.OAK_STAIRS);
		STAIRS.put(BlockPlanks.EnumType.SPRUCE, Blocks.SPRUCE_STAIRS);
		STAIRS.put(BlockPlanks.EnumType.BIRCH, Blocks.BIRCH_STAIRS);
		STAIRS.put(BlockPlanks.EnumType.JUNGLE, Blocks.JUNGLE_STAIRS);
		STAIRS.put(BlockPlanks.EnumType.ACACIA, Blocks.ACACIA_STAIRS);
		STAIRS.put(BlockPlanks.EnumType.DARK_OAK, Blocks.DARK_OAK_STAIRS);

		FENCES = new EnumMap<>(BlockPlanks.EnumType.class);
		FENCES.put(BlockPlanks.EnumType.OAK, Blocks.OAK_FENCE);
		FENCES.put(BlockPlanks.EnumType.SPRUCE, Blocks.SPRUCE_FENCE);
		FENCES.put(BlockPlanks.EnumType.BIRCH, Blocks.BIRCH_FENCE);
		FENCES.put(BlockPlanks.EnumType.JUNGLE, Blocks.JUNGLE_FENCE);
		FENCES.put(BlockPlanks.EnumType.ACACIA, Blocks.ACACIA_FENCE);
		FENCES.put(BlockPlanks.EnumType.DARK_OAK, Blocks.DARK_OAK_FENCE);

		FENCE_GATES = new EnumMap<>(BlockPlanks.EnumType.class);
		FENCE_GATES.put(BlockPlanks.EnumType.OAK, Blocks.OAK_FENCE_GATE);
		FENCE_GATES.put(BlockPlanks.EnumType.SPRUCE, Blocks.SPRUCE_FENCE_GATE);
		FENCE_GATES.put(BlockPlanks.EnumType.BIRCH, Blocks.BIRCH_FENCE_GATE);
		FENCE_GATES.put(BlockPlanks.EnumType.JUNGLE, Blocks.JUNGLE_FENCE_GATE);
		FENCE_GATES.put(BlockPlanks.EnumType.ACACIA, Blocks.ACACIA_FENCE_GATE);
		FENCE_GATES.put(BlockPlanks.EnumType.DARK_OAK, Blocks.DARK_OAK_FENCE_GATE);
	}

	@Nullable
	@Override
	public Template.BlockInfo processBlock(World world, BlockPos pos, Template.BlockInfo info){

		// Why do these each have their own property key?
		if(info.blockState.getBlock() instanceof BlockPlanks){
			return new Template.BlockInfo(info.pos, info.blockState.withProperty(BlockPlanks.VARIANT, woodType), info.tileentityData);
		}else if(info.blockState.getBlock() instanceof BlockWoodSlab){
			return new Template.BlockInfo(info.pos, info.blockState.withProperty(BlockWoodSlab.VARIANT, woodType), info.tileentityData);
		// This is a mess, no wonder the flattening happened
		}else if(DOORS.containsValue(info.blockState.getBlock())){
			return new Template.BlockInfo(info.pos, WizardryUtilities.copyState(DOORS.get(woodType), info.blockState), info.tileentityData);
		}else if(STAIRS.containsValue(info.blockState.getBlock())){
			return new Template.BlockInfo(info.pos, WizardryUtilities.copyState(STAIRS.get(woodType), info.blockState), info.tileentityData);
		}else if(FENCES.containsValue(info.blockState.getBlock())){
			return new Template.BlockInfo(info.pos, WizardryUtilities.copyState(FENCES.get(woodType), info.blockState), info.tileentityData);
		}else if(FENCE_GATES.containsValue(info.blockState.getBlock())){
			return new Template.BlockInfo(info.pos, WizardryUtilities.copyState(FENCE_GATES.get(woodType), info.blockState), info.tileentityData);
		}

		return info;
	}

}
