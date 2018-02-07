package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockArcaneWorkbench extends BlockContainer {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

	public BlockArcaneWorkbench(){
		super(Material.ROCK);
		this.setLightLevel(0.8f);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata){
		return new TileEntityArcaneWorkbench();
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos){
		return false;
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState block, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ){

		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity == null || player.isSneaking()){
			return false;
		}

		player.openGui(Wizardry.instance, WizardryGuiHandler.ARCANE_WORKBENCH, world, pos.getX(), pos.getY(),
				pos.getZ());
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState block){

		Random random = new Random();
		TileEntityArcaneWorkbench tileentityarcaneworkbench = (TileEntityArcaneWorkbench)world.getTileEntity(pos);

		if(tileentityarcaneworkbench != null){

			for(int j1 = 0; j1 < tileentityarcaneworkbench.getSizeInventory(); ++j1){

				ItemStack itemstack = tileentityarcaneworkbench.getStackInSlot(j1);

				if(!itemstack.isEmpty()){

					float f = random.nextFloat() * 0.8F + 0.1F;
					float f1 = random.nextFloat() * 0.8F + 0.1F;
					EntityItem entityitem;

					for(float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.getCount() > 0; world
							.spawnEntity(entityitem)){

						int k1 = random.nextInt(21) + 10;

						if(k1 > itemstack.getCount()){
							k1 = itemstack.getCount();
						}

						itemstack.shrink(k1);

						entityitem = new EntityItem(world, (double)((float)pos.getX() + f),
								(double)((float)pos.getY() + f1), (double)((float)pos.getZ() + f2),
								new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
						float f3 = 0.05F;
						entityitem.motionX = (double)((float)random.nextGaussian() * f3);
						entityitem.motionY = (double)((float)random.nextGaussian() * f3 + 0.2F);
						entityitem.motionZ = (double)((float)random.nextGaussian() * f3);

						if(itemstack.hasTagCompound()){
							entityitem.getEntityItem()
									.setTagCompound(((NBTTagCompound)itemstack.getTagCompound().copy()));
						}
					}
				}
			}

			// par1World.func_96440_m(par2, par3, par4, block);
		}

		super.breakBlock(world, pos, block);
	}

}
