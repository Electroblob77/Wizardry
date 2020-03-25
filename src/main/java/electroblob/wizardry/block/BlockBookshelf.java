package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nullable;

public class BlockBookshelf extends BlockHorizontal implements ITileEntityProvider {

	public static final int SLOT_COUNT = 12;

	public static final UnlistedPropertyBool[] BOOKS = new UnlistedPropertyBool[SLOT_COUNT];

	static {
		for(int i=0; i<SLOT_COUNT; i++){
			BOOKS[i] = new UnlistedPropertyBool("book" + i);
		}
	}

	public BlockBookshelf(){
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	protected BlockStateContainer createBlockState(){
//		IProperty<?>[] properties = { FACING };
//		return new BlockStateContainer(this, ArrayUtils.addAll(properties, BOOKS));
		return new BlockStateContainer.Builder(this).add(FACING).add(BOOKS).build();
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face){
		return state.getValue(FACING).getAxis() == face.getAxis() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState block){

		TileEntity tileentity = world.getTileEntity(pos);

		if(tileentity instanceof TileEntityBookshelf){
			InventoryHelper.dropInventoryItems(world, pos, (TileEntityBookshelf)tileentity);
		}

		super.breakBlock(world, pos, block); // For blocks that don't extend BlockContainer, this removes the TE
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		EnumFacing enumfacing = EnumFacing.byIndex(meta);
		if(enumfacing.getAxis() == EnumFacing.Axis.Y) enumfacing = EnumFacing.NORTH;
		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(FACING).getIndex();
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos){

		IExtendedBlockState s = (IExtendedBlockState)super.getExtendedState(state, world, pos);

		if(world.getTileEntity(pos) instanceof TileEntityBookshelf){
			TileEntityBookshelf tileentity = ((TileEntityBookshelf)world.getTileEntity(pos));
			for(int i = 0; i < tileentity.getSizeInventory(); i++){
				s = s.withProperty(BOOKS[i], !tileentity.getStackInSlot(i).isEmpty());
			}
		}

		return s;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta){
		return new TileEntityBookshelf();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState block, EntityPlayer player, EnumHand hand,
									EnumFacing side, float hitX, float hitY, float hitZ){

		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity == null || player.isSneaking()){
			return false;
		}

		player.openGui(Wizardry.instance, WizardryGuiHandler.BOOKSHELF, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param){
		super.eventReceived(state, world, pos, id, param);
		TileEntity tileentity = world.getTileEntity(pos);
		return tileentity != null && tileentity.receiveClientEvent(id, param);
	}

	// Copied from BlockFluidBase, only reason it exists is because of java's weird restrictions on generics
	private static final class UnlistedPropertyBool extends Properties.PropertyAdapter<Boolean> {

		public UnlistedPropertyBool(String name){
			super(PropertyBool.create(name));
		}
	}

}
