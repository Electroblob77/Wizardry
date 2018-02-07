package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTransportationStone extends Block {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625f * 5, 0, 0.0625f * 5, 0.0625f * 11, 0.0625f * 6,
			0.0625f * 11);

	public BlockTransportationStone(Material material){
		super(material);
		this.setTickRandomly(true);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	// The number of these methods is quite simply ridiculous. This one seems to be for placement logic and block
	// connections (fences, glass panes, etc.)...
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	// ...this one isn't used much but has something to do with redstone...
	@Override
	public boolean isBlockNormalCube(IBlockState state){
		return false;
	}

	// ... this one is for most other game logic...
	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	}

	// Forge version of the above method. I still need to override both though because vanilla uses the other one.
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

	// ... and this one is for rendering.
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor){

		super.onNeighborChange(world, pos, neighbor);

		if(!world.isSideSolid(pos.down(), EnumFacing.UP, false) && world instanceof World){
			this.dropBlockAsItem((World)world, pos, world.getBlockState(pos), 0);
			((World)world).setBlockToAir(pos);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random){

		if(!world.isSideSolid(pos.down(), EnumFacing.UP)){
			this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		return super.canPlaceBlockAt(world, pos) && world.isSideSolid(pos.down(), EnumFacing.UP);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ){

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getItem() instanceof ItemWand){
			if(WizardData.get(player) != null){

				WizardData data = WizardData.get(player);

				for(int x = -1; x <= 1; x++){
					for(int z = -1; z <= 1; z++){
						BlockPos pos1 = pos.add(x, 0, z);
						if(testForCircle(world, pos1)){
							data.setStoneCircleLocation(pos1, world.provider.getDimension());
							if(!world.isRemote) player.sendMessage(
									new TextComponentTranslation("tile.wizardry:transportation_stone.confirm",
											Spells.transportation.getNameForTranslationFormatted()));
							return true;
						}
					}
				}

				if(!world.isRemote)
					player.sendMessage(new TextComponentTranslation("tile.wizardry:transportation_stone.invalid"));
				return true;
			}
		}
		return false;
	}

	/** Returns whether the specified location is surrounded by a complete cicle of 8 transportation stones. */
	public static boolean testForCircle(World world, BlockPos pos){

		if(world.getBlockState(pos).getMaterial().blocksMovement()) return false;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(world.getBlockState(pos.add(x, 0, z)).getBlock() != WizardryBlocks.transportation_stone){
					if(x != 0 || z != 0) return false;
				}
			}
		}

		return true;
	}
}
