package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockSnare extends BlockContainer {

	public BlockSnare(Material par2Material){
		super(par2Material);
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.0625f, 1.0f);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4){
	    return null;
	}
	
	@Override
	public boolean hasTileEntity(int metadata){
		return true;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity){
		if(!world.isRemote && entity instanceof EntityLivingBase){
			if(world.getTileEntity(x, y, z) instanceof TileEntityPlayerSave){
				TileEntityPlayerSave tileentity = (TileEntityPlayerSave)world.getTileEntity(x, y, z);
				if(WizardryUtilities.isValidTarget(tileentity.getCaster(), entity)){
					((EntityLivingBase)entity).attackEntityFrom(MagicDamage.causeDirectMagicDamage(tileentity.getCaster(), DamageType.MAGIC), 6);
					((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 2));
		        	world.func_147480_a(x, y, z, false);
				}
			}
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        if(!world.isSideSolid(x, y-1, z, ForgeDirection.UP)){
        	world.setBlockToAir(x, y, z);
        }
    }
	
	@Override
	public boolean renderAsNormalBlock()
    {
        return false;
    }
	
    @Override
	public boolean isOpaqueCube()
    {
        return false;
    }
	
	@Override
	public int getRenderType(){
		return 23;
	}
	
	@Override
	public Item getItemDropped(int a, Random random, int b){
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityPlayerSave();
	}

}
