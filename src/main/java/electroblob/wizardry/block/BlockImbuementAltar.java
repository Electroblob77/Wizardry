package electroblob.wizardry.block;

import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;

public class BlockImbuementAltar extends Block implements ITileEntityProvider {

	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);

	public BlockImbuementAltar(){
		super(Material.ROCK);
		this.setBlockUnbreakable();
		this.setResistance(6000000);
		this.setLightLevel(0.4f);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE, false));
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, ACTIVE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(ACTIVE, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(ACTIVE) ? 1 : 0;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata){
		return new TileEntityImbuementAltar();
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
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT; // Required to shade parts of the block faces differently to others
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
		// TODO: Change this back to how it should be and give receptacles a special case for attaching to the altar
		return face == EnumFacing.UP ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos){
		return state.getValue(ACTIVE) ? super.getLightValue(state, world, pos) : 0;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighbour){

		boolean shouldBeActive = Arrays.stream(EnumFacing.HORIZONTALS)
				.allMatch(s -> world.getBlockState(pos.offset(s)).getBlock() == WizardryBlocks.receptacle
							&& world.getBlockState(pos.offset(s)).getValue(BlockReceptacle.FACING) == s);

		if(world.getBlockState(pos).getValue(ACTIVE) != shouldBeActive){ // Only set when it actually needs changing
			world.setBlockState(pos, world.getBlockState(pos).withProperty(ACTIVE, shouldBeActive));
			world.checkLight(pos);
		}

		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityImbuementAltar){
			((TileEntityImbuementAltar)tileEntity).checkRecipe();
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState block, EntityPlayer player, EnumHand hand,
									EnumFacing side, float hitX, float hitY, float hitZ){

		TileEntity tileEntity = world.getTileEntity(pos);

		if(!(tileEntity instanceof TileEntityImbuementAltar) || player.isSneaking()){
			return false;
		}

		ItemStack currentStack = ((TileEntityImbuementAltar)tileEntity).getStack();
		ItemStack toInsert = player.getHeldItem(hand);

		if(currentStack.isEmpty()){
			ItemStack stack = toInsert.copy();
			stack.setCount(1);
			((TileEntityImbuementAltar)tileEntity).setStack(stack);
			((TileEntityImbuementAltar)tileEntity).setLastUser(player);
			if(!player.isCreative()) toInsert.shrink(1);

		}else{

			if(toInsert.isEmpty()){
				player.setHeldItem(hand, currentStack);
			}else if(!player.addItemStackToInventory(currentStack)){
				player.dropItem(currentStack, false);
			}

			((TileEntityImbuementAltar)tileEntity).setStack(ItemStack.EMPTY);
			((TileEntityImbuementAltar)tileEntity).setLastUser(null);

			if(currentStack.getItem() instanceof ItemWizardArmour && ((ItemWizardArmour)currentStack.getItem()).element != null){
				// Not perfect since we don't know if the player actually imbued the armour, but it's good enough for now
				WizardryAdvancementTriggers.create_elemental_armour.triggerFor(player);
			}
		}

		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState block){

        TileEntity tileentity = world.getTileEntity(pos);

        if(tileentity instanceof TileEntityArcaneWorkbench){
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityArcaneWorkbench)tileentity);
        }

        super.breakBlock(world, pos, block);
	}

}
