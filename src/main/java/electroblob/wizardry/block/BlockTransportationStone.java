package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Transportation;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.Location;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockTransportationStone extends Block {

	private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625f * 5, 0, 0.0625f * 5, 0.0625f * 11, 0.0625f * 6,
			0.0625f * 11);

	public BlockTransportationStone(Material material){
		super(material);
		this.setTickRandomly(true);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	// The number of these methods is quite simply ridiculous. This one seems to be for placement logic and block
	// connections (fences, glass panes, etc.)...
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	// ...this one isn't used much but has something to do with redstone...
	@Override
	public boolean isBlockNormalCube(IBlockState state){
		return false;
	}

	// ... this one is for most other game logic...
	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	}

	// ... and this one is for rendering.
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side){
		return side == EnumFacing.DOWN;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos){

		super.neighborChanged(state, world, pos, block, fromPos);

		if(!world.isSideSolid(pos.down(), EnumFacing.UP, false)){
			this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random){

		if(!world.isSideSolid(pos.down(), EnumFacing.UP)){
			this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos){
		return super.canPlaceBlockAt(world, pos) && world.isSideSolid(pos.down(), EnumFacing.UP);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ){

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getItem() instanceof ISpellCastingItem){
			if(WizardData.get(player) != null){

				WizardData data = WizardData.get(player);

				for(int x = -1; x <= 1; x++){
					for(int z = -1; z <= 1; z++){
						BlockPos pos1 = pos.add(x, 0, z);
						if(testForCircle(world, pos1)){

							Location here = new Location(pos1, player.dimension);

							List<Location> locations = data.getVariable(Transportation.LOCATIONS_KEY);
							if(locations == null) data.setVariable(Transportation.LOCATIONS_KEY, locations = new ArrayList<>(Transportation.MAX_REMEMBERED_LOCATIONS));

							if(ItemArtefact.isArtefactActive(player, WizardryItems.charm_transportation)){

								if(locations.contains(here)){
									locations.remove(here);
									if(!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("tile." + Wizardry.MODID + ":transportation_stone.forget", here.pos.getX(), here.pos.getY(), here.pos.getZ(), here.dimension), true);

								}else{

									locations.add(here);
									if(!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("tile." + Wizardry.MODID + ":transportation_stone.remember", here.pos.getX(), here.pos.getY(), here.pos.getZ(), here.dimension), true);

									if(locations.size() > Transportation.MAX_REMEMBERED_LOCATIONS){
										Location removed = locations.remove(0);
										if(!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("tile." + Wizardry.MODID + ":transportation_stone.forget", removed.pos.getX(), removed.pos.getY(), removed.pos.getZ(), removed.dimension), true);
									}
								}

							}else{
								if(locations.isEmpty()) locations.add(here);
								else{
									locations.remove(here); // Prevents duplicates
									if(locations.isEmpty()) locations.add(here);
									else locations.set(Math.max(locations.size() - 1, 0), here);
								}
								if(!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("tile." + Wizardry.MODID + ":transportation_stone.confirm", Spells.transportation.getNameForTranslationFormatted()), true);
							}

							return true;
						}
					}
				}

				if(!world.isRemote){
					player.sendStatusMessage(new TextComponentTranslation("tile." + Wizardry.MODID + ":transportation_stone.invalid"), true);
				}else{

					BlockPos centre = findMostLikelyCircle(world, pos);
					// Displays particles in the required shape
					for(int x = -1; x <= 1; x++){
						for(int z = -1; z <= 1; z++){
							if(x == 0 && z == 0) continue;
							ParticleBuilder.create(ParticleBuilder.Type.PATH)
									.pos(GeometryUtils.getCentre(centre).add(x, -0.3125, z)).clr(0x86ff65)
									.time(200).scale(2).spawn(world);
						}
					}
				}

				return true;
			}
		}
		return false;
	}

	/** Returns whether the specified location is surrounded by a complete circle of 8 transportation stones. */
	public static boolean testForCircle(World world, BlockPos pos){

		if(world.getBlockState(pos).getMaterial().blocksMovement() || world.getBlockState(pos.up()).getMaterial()
				.blocksMovement()) return false;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				if(world.getBlockState(pos.add(x, 0, z)).getBlock() != WizardryBlocks.transportation_stone){
					return false;
				}
			}
		}

		return true;
	}

	private static BlockPos findMostLikelyCircle(World world, BlockPos pos){

		int bestSoFar = 0;
		BlockPos result = null;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				BlockPos pos1 = pos.add(x, 0, z);
				int n = getCircleCompleteness(world, pos1);
				if(n > bestSoFar){
					bestSoFar = n;
					result = pos1;
				}
			}
		}

		return result;
	}

	private static int getCircleCompleteness(World world, BlockPos pos){

		int n = 0;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				if(world.getBlockState(pos.add(x, 0, z)).getBlock() == WizardryBlocks.transportation_stone) n++;
			}
		}

		return n;
	}
}
