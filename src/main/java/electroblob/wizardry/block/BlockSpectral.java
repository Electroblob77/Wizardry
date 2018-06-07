package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

// For future reference - extend BlockContainer whenever possible because it has methods for removing tile entities on block break.
public class BlockSpectral extends BlockContainer {

	public BlockSpectral(Material material) {
		super(material);
	}
    
	@Override
	public int getRenderBlockPass()
    {
        return 1;
    }
    
	@Override
	public boolean isOpaqueCube()
    {
        return false;
    }
    
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        Block block = par1IBlockAccess.getBlock(par2, par3, par4);
        return block == this ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
    }
    
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random){
    	// Middle of block
		Wizardry.proxy.spawnParticle(EnumParticleType.DUST, world, x+random.nextDouble(), y+random.nextDouble(), z+random.nextDouble(), 0, 0, 0, (int)(16.0D / (Math.random() * 0.8D + 0.2D)),
    			0.4f + random.nextFloat()*0.2f, 0.6f + random.nextFloat()*0.4f, 0.6f + random.nextFloat()*0.4f);
    	// Top surface
		Wizardry.proxy.spawnParticle(EnumParticleType.DUST, world, x+random.nextDouble(), y+1, z+random.nextDouble(), 0, 0, 0, (int)(16.0D / (Math.random() * 0.8D + 0.2D)),
    			0.4f + random.nextFloat()*0.2f, 0.6f + random.nextFloat()*0.4f, 0.6f + random.nextFloat()*0.4f);
		Wizardry.proxy.spawnParticle(EnumParticleType.DUST, world, x+random.nextDouble(), y+1, z+random.nextDouble(), 0, 0, 0, (int)(16.0D / (Math.random() * 0.8D + 0.2D)),
    			0.4f + random.nextFloat()*0.2f, 0.6f + random.nextFloat()*0.4f, 0.6f + random.nextFloat()*0.4f);
    }
    
    // Overriden to make the block always look full brightness despite not emitting full light.
	@Override
	public int getMixedBrightnessForBlock(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        return 15;
    }
    
    @Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityTimer(1200);
	}
    
    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
	public int quantityDropped(Random par1Random)
    {
        return 0;
    }
	
}
