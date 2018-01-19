package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
// TODO: Apparently you shouldn't extend BlockContainer. I feel like BlockArcaneWorkbench should, but what about the rest?
public class BlockSnare extends BlockContainer {

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0f, 0.0f, 0.0f, 1.0f, 0.0625f, 1.0f);

	public BlockSnare(Material par2Material){
		super(par2Material);
		this.setSoundType(SoundType.PLANT);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        return AABB;
    }
	
	@Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos){
        return NULL_AABB;
    }
	
	@Override
	public boolean hasTileEntity(IBlockState state){
		return true;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		
		if(!world.isRemote && entity instanceof EntityLivingBase){
			if(world.getTileEntity(pos) instanceof TileEntityPlayerSave){
				
				TileEntityPlayerSave tileentity = (TileEntityPlayerSave)world.getTileEntity(pos);
				
				if(WizardryUtilities.isValidTarget(tileentity.getCaster(), entity)){
					((EntityLivingBase)entity).attackEntityFrom(MagicDamage.causeDirectMagicDamage(tileentity.getCaster(), DamageType.MAGIC), 6);
					((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
		        	
					world.destroyBlock(pos, false);
				}
			}
		}
	}
	
	// The similarly named onNeighborChange method does NOT do the same thing.
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn){
        super.neighborChanged(state, world, pos, blockIn);
        if(!world.isSideSolid(pos.down(), EnumFacing.UP, false)){
        	world.setBlockToAir(pos);
        }
    }
	
	@Override
	public BlockRenderLayer getBlockLayer(){
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
	
    @Override
	public boolean isOpaqueCube(IBlockState state){
        return false;
    }
    
    @Override
    public boolean isFullCube(IBlockState state){
    	return false;
    }
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityPlayerSave();
	}

}
