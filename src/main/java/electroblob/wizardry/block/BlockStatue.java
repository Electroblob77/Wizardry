package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.spell.Petrify;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStatue extends BlockContainer {

	private boolean isIce;

	public BlockStatue(Material material){
		super(material);
		this.isIce = material == Material.ICE;
		if(this.isIce){
			this.setDefaultSlipperiness(0.98f);
			this.setSoundType(SoundType.GLASS);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		// Not a good idea to call getBlockBoundsMinX() or whatever from in here, since this method changes those!
		if(!this.isIce){

			if(world.getTileEntity(pos) instanceof TileEntityStatue){

				TileEntityStatue statue = (TileEntityStatue)world.getTileEntity(pos);

				if(statue.creature != null){

					// Block bounds are set to match the width and height of the entity, clamped to within 1 block.
					return new AxisAlignedBB((float)Math.max(0.5 - statue.creature.width / 2, 0), 0,
							(float)Math.max(0.5 - statue.creature.width / 2, 0),
							(float)Math.min(0.5 + statue.creature.width / 2, 1),
							// This checks if the block is the top one and if so reduces its height so the top lines up
							// with
							// the top of the entity model.
							statue.position == statue.parts
									? (float)Math.min(statue.creature.height - statue.parts + 1, 1)
									: 1,
							(float)Math.min(0.5 + statue.creature.width / 2, 1));
				}
			}
		}

		return FULL_BLOCK_AABB;
	}

	// getCollisionBoundingBox eventually calls getBoundingBox anyway, and since I want the collision box and the block
	// outline to be the same here, I've removed that getCollisionBoundingBox entirely.

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
	public BlockRenderLayer getBlockLayer(){
		return this.isIce ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return this.isIce ? EnumBlockRenderType.MODEL : EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata){
		return new TileEntityStatue(this.isIce);
	}

	@Override
	public int quantityDropped(Random random){
		return 0;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){

		if(!world.isRemote){

			TileEntityStatue tileentity = (TileEntityStatue)world.getTileEntity(pos);

			if(tileentity != null){
				if(tileentity.parts == 2){
					if(tileentity.position == 2){
						world.destroyBlock(pos.down(), false);
					}else{
						world.destroyBlock(pos.up(), false);
					}
				}else if(tileentity.parts == 3){
					if(tileentity.position == 3){
						world.destroyBlock(pos.down(), false);
						world.destroyBlock(pos.down(2), false);
					}else if(tileentity.position == 2){
						world.destroyBlock(pos.down(), false);
						world.destroyBlock(pos.up(), false);
					}else{
						world.destroyBlock(pos.up(), false);
						world.destroyBlock(pos.up(2), false);
					}
				}
			}

			// This is only when position == 1 because world.destroyBlock calls this function for the other blocks.
			if(tileentity != null && tileentity.position == 1 && tileentity.creature != null){
				tileentity.creature.getEntityData().removeTag(Petrify.NBT_KEY);
				tileentity.creature.isDead = false;
				world.spawnEntity(tileentity.creature);
			}
		}

		super.breakBlock(world, pos, state);
	}

	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
			EnumFacing side){

		IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
		Block block = iblockstate.getBlock();

		return this.isIce && block == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}
}
