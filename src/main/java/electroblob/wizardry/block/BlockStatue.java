package electroblob.wizardry.block;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStatue extends BlockContainer {

	private boolean isIce;

	public BlockStatue(Material par2Material) {
		super(par2Material);
		this.isIce = par2Material == Material.ice;
		if(this.isIce){
			this.slipperiness = 0.98F;
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		// Not a good idea to call getBlockBoundsMinX() or whatever from in here, since this method changes those!
		if(!this.isIce){

			if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){

				TileEntityStatue statue = (TileEntityStatue)world.getTileEntity(x, y, z);

				if(statue.creature != null){
					// Block bounds are set to match the width and height of the entity, clamped to within 1 block.
					this.setBlockBounds((float)Math.max(0.5 - statue.creature.width/2, 0), 0,
							(float)Math.max(0.5 - statue.creature.width/2, 0),
							(float)Math.min(0.5 + statue.creature.width/2, 1),
							// This checks if the block is the top one and if so reduces its height so the top lines up with
							// the top of the entity model.
							statue.position == statue.parts ? (float)Math.min(statue.creature.height - statue.parts + 1, 1) : 1,
									(float)Math.min(0.5 + statue.creature.width/2, 1));
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		
		if(!this.isIce){
			
			if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){

				TileEntityStatue statue = (TileEntityStatue)world.getTileEntity(x, y, z);

				if(statue.creature != null){
					// Block bounds are set to match the width and height of the entity, clamped to within 1 block.
					return AxisAlignedBB.getBoundingBox(x + Math.max(0.5 - statue.creature.width/2, 0),
							y, z + Math.max(0.5 - statue.creature.width/2, 0),
							x + Math.min(0.5 + statue.creature.width/2, 1),
							// This checks if the block is the top one and if so reduces its height so the top lines up with
							// the top of the entity model
							statue.position == statue.parts ? y + (float)Math.min(statue.creature.height - statue.parts + 1, 1) : y + 1,
									z + Math.min(0.5 + statue.creature.width/2, 1));
				}
			}
		}

		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean renderAsNormalBlock(){
		return false;
	}

	@Override
	public boolean canRenderInPass(int pass){
		return this.isIce ? super.canRenderInPass(pass) : false;
	}

	@Override
	public boolean isOpaqueCube(){
		return false;// !this.isIce;
	}

	@Override
	public int getRenderBlockPass()
	{
		return this.isIce? 1 : 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityStatue(this.isIce);
	}
	/*
	@Override
    public void randomDisplayTick(World world, int x, int y, int z, Random random){
    	if(this.isIce){
    		float brightness = 0.5f + (random.nextFloat()/2);
    		Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, x + random.nextDouble(), y + random.nextDouble(), z + random.nextDouble(), 0, 0, 0, null, brightness, brightness + 0.1f, 1.0f, true));
    	}
    }
	 */


	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	@Override
	public int quantityDropped(Random par1Random)
	{
		return 0;
	}

	/**
	 * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
	 * coordinates.  Args: blockAccess, x, y, z, side
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_)
	{
		Block block = p_149646_1_.getBlock(p_149646_2_, p_149646_3_, p_149646_4_);

		return block == this ? false : super.shouldSideBeRendered(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_, p_149646_5_);
	}

	@Override
	public void breakBlock(World par1World, int x, int y, int z, Block block, int par6)
	{
		if(!par1World.isRemote){
			TileEntityStatue tileentity = (TileEntityStatue)par1World.getTileEntity(x, y, z);
			if(tileentity != null){
				if(tileentity.parts == 2){
					if(tileentity.position == 2){
						// func_147480_a is the method that causes the block to smash as if broken by a player.
						// Used to be called destroyBlock. The boolean is whether the block should drop items.
						par1World.func_147480_a(x, y-1, z, false);
					}else{
						par1World.func_147480_a(x, y+1, z, false);
					}
				}else if(tileentity.parts == 3){
					if(tileentity.position == 3){
						par1World.func_147480_a(x, y-1, z, false);
						par1World.func_147480_a(x, y-2, z, false);
					}else if(tileentity.position == 2){
						par1World.func_147480_a(x, y-1, z, false);
						par1World.func_147480_a(x, y+1, z, false);
					}else{
						par1World.func_147480_a(x, y+1, z, false);
						par1World.func_147480_a(x, y+2, z, false);
					}
				}
			}

			// This is only when position == 1 because world.destroyBlock calls this function for the other blocks.
			if(tileentity != null && tileentity.position == 1 && tileentity.creature != null){
				tileentity.creature.isDead = false;
				par1World.spawnEntityInWorld(tileentity.creature);
			}
		}
		super.breakBlock(par1World, x, y, z, block, par6);
	}
}
