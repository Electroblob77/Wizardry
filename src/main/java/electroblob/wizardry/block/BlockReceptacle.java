package electroblob.wizardry.block;

import com.google.common.collect.Maps;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemSpectralDust;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityReceptacle;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class BlockReceptacle extends BlockTorch implements ITileEntityProvider {

	protected static final AxisAlignedBB STANDING_AABB = 	new AxisAlignedBB(4/16d, 0/16d, 4/16d, 12/16d,  8/16d, 12/16d);
	protected static final AxisAlignedBB NORTH_WALL_AABB = 	new AxisAlignedBB(4/16d, 2/16d, 7/16d, 12/16d, 10/16d, 16/16d);
	protected static final AxisAlignedBB SOUTH_WALL_AABB = 	new AxisAlignedBB(4/16d, 2/16d, 0/16d, 12/16d, 10/16d,  9/16d);
	protected static final AxisAlignedBB WEST_WALL_AABB = 	new AxisAlignedBB(7/16d, 2/16d, 4/16d, 16/16d, 10/16d, 12/16d);
	protected static final AxisAlignedBB EAST_WALL_AABB = 	new AxisAlignedBB(0/16d, 2/16d, 4/16d,  9/16d, 10/16d, 12/16d);

	private static final double WALL_PARTICLE_OFFSET = 3/16d;

	public static final Map<Element, int[]> PARTICLE_COLOURS;

	static {

		Map<Element, int[]> map = Maps.newEnumMap(Element.class);

		map.put(Element.MAGIC, 		new int[]{0xe4c7cd, 0xfeffbe, 0x9d2cf3});
		map.put(Element.FIRE, 		new int[]{0xff9600, 0xfffe67, 0xd02700});
		map.put(Element.ICE, 		new int[]{0xa3e8f4, 0xe9f9fc, 0x138397});
		map.put(Element.LIGHTNING, 	new int[]{0x409ee1, 0xf5f0ff, 0x225474});
		map.put(Element.NECROMANCY, new int[]{0xa811ce, 0xf575f5, 0x382366});
		map.put(Element.EARTH, 		new int[]{0xa8f408, 0xc8ffb2, 0x795c28});
		map.put(Element.SORCERY, 	new int[]{0x56e8e3, 0xe8fcfc, 0x16a64d});
		map.put(Element.HEALING, 	new int[]{0xfff69e, 0xfffff6, 0xa18200});

		PARTICLE_COLOURS = Maps.immutableEnumMap(map);
	}

	public BlockReceptacle(){
		super();
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(0);
		this.setLightLevel(0.5f);
		this.setSoundType(SoundType.STONE);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		switch(state.getValue(FACING)){
			case EAST: return EAST_WALL_AABB;
			case WEST: return WEST_WALL_AABB;
			case SOUTH: return SOUTH_WALL_AABB;
			case NORTH: return NORTH_WALL_AABB;
			default: return STANDING_AABB;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		return state.getBoundingBox(world, pos);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos){

		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle && ((TileEntityReceptacle)tileEntity).getElement() != null){
			return super.getLightValue(state, world, pos); // Return super to use float value from constructor
		}

		return 0;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){

		super.getDrops(drops, world, pos, state, fortune);

		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle){
			Element element = ((TileEntityReceptacle)tileEntity).getElement();
			if(element != null) drops.add(new ItemStack(WizardryItems.spectral_dust, 1, element.ordinal()));
		}
	}

	// See BlockFlowerPot for these two (this class is essentially based on a flower pot)

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest){
		if(willHarvest) return true; // If it will harvest, delay deletion of the block until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool){
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){

		TileEntity tileEntity = world.getTileEntity(pos);

		ItemStack stack = player.getHeldItem(hand);

		if(tileEntity instanceof TileEntityReceptacle){

			Element currentElement = ((TileEntityReceptacle)tileEntity).getElement();

			if(currentElement == null){

				if(stack.getItem() instanceof ItemSpectralDust && stack.getMetadata() >= 0
						&& stack.getMetadata() < Element.values().length){

					((TileEntityReceptacle)tileEntity).setElement(Element.values()[stack.getMetadata()]);
					if(!player.capabilities.isCreativeMode) stack.shrink(1);
					world.playSound(pos.getX(), pos.getY(), pos.getZ(), WizardrySounds.BLOCK_RECEPTACLE_IGNITE,
							SoundCategory.BLOCKS, 0.7f, 0.7f, false);
					return true;
				}

			}else{

				((TileEntityReceptacle)tileEntity).setElement(null);

				ItemStack dust = new ItemStack(WizardryItems.spectral_dust, 1, currentElement.ordinal());

				if(stack.isEmpty()){
					player.setHeldItem(hand, dust);
				}else if(!player.addItemStackToInventory(dust)){
					player.dropItem(dust, false);
				}

				return true;
			}
		}

		return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand){

		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle){

			Element element = ((TileEntityReceptacle)tileEntity).getElement();

			if(element != null){

				EnumFacing facing = state.getValue(FACING).getOpposite();

				Vec3d centre = GeometryUtils.getCentre(pos);
				if(facing.getAxis().isHorizontal()){
					centre = centre.add(new Vec3d(facing.getDirectionVec()).scale(WALL_PARTICLE_OFFSET)).add(0, 0.125, 0);
				}

				int[] colours = PARTICLE_COLOURS.get(element);

				ParticleBuilder.create(ParticleBuilder.Type.FLASH).pos(centre).scale(0.35f).time(48).clr(colours[0]).spawn(world);

				double r = 0.12;

				for(int i = 0; i < 3; i++){

					double x = r * (rand.nextDouble() * 2 - 1);
					double y = r * (rand.nextDouble() * 2 - 1);
					double z = r * (rand.nextDouble() * 2 - 1);

					ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(centre.x + x, centre.y + y, centre.z + z)
							.vel(x * -0.03, 0.02, z * -0.03).time(24 + rand.nextInt(8)).clr(colours[1]).fade(colours[2]).spawn(world);
				}
			}
		}
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta){
		return new TileEntityReceptacle();
	}

}
