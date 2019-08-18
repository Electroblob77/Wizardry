package electroblob.wizardry.block;

import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMagicLight extends Block implements ITileEntityProvider {

	//private static final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

	public BlockMagicLight(Material material){
		super(material);
		this.setLightLevel(1.0f);
		this.setBlockUnbreakable();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos){
		// The other two bounding box methods in Block aren't nullable, so this is the
		// only one that can return NULL_AABB.
		return NULL_AABB;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		// Let the player dispel any lights if they have the lantern charm, not just the permanent ones because that would be annoying!
		if(player.getHeldItem(hand).getItem() instanceof ISpellCastingItem && ItemArtefact.isArtefactActive(player, WizardryItems.charm_light)){

			world.setBlockToAir(pos);
			return true;

		}else{
			return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
		}
	}

//	@Override
//	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
//		return AABB;
//	}

	@Override
	public boolean isCollidable(){
		// This method has nothing to do with entity movement, it's just for raytracing
		return true;
	}

	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.ParticleManager manager){
		if(world.getBlockState(pos).getBlock() == this) return true; // No break particles!
		else return super.addDestroyEffects(world, pos, manager);
	}

	@Override
	public boolean hasTileEntity(IBlockState state){
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata){
		return new TileEntityMagicLight(600);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
