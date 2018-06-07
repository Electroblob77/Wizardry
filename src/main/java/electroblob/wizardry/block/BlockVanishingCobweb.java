package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

// For future reference - extend BlockContainer whenever possible because it has methods for removing tile entities on block break.
public class BlockVanishingCobweb extends BlockContainer {

	public BlockVanishingCobweb(Material material) {
		super(material);
	}
    
	@Override
    public int getRenderType()
    {
        return 1;
    }
    
	@Override
	public boolean isOpaqueCube()
    {
        return false;
    }
    
    @Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityTimer(400);
	}
    
    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
    {
        entity.setInWeb();
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
    	return null;
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
