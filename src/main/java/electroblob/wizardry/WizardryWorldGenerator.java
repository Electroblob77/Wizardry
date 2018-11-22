package electroblob.wizardry;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WizardryWorldGenerator implements IWorldGenerator {

	/** The string identifier for wizard tower chests, used in ChestGenHooks. */
	public static final String WIZARD_TOWER = Wizardry.MODID + "wizardTower";

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider){

		for(int id : Wizardry.settings.oreDimensions){
			if(id == world.provider.getDimension()) this.addOreSpawn(WizardryBlocks.crystal_ore.getDefaultState(),
					world, random, chunkX * 16, chunkZ * 16, 16, 16, 5, 7, 5, 30);
		}

		for(int id : Wizardry.settings.flowerDimensions){
			if(id == world.provider.getDimension()) this.generatePlant(WizardryBlocks.crystal_flower.getDefaultState(),
					world, random, chunkX * 16, chunkZ * 16, 2, 20);
		}

		if(world.getWorldInfo().isMapFeaturesEnabled()){
			for(int id : Wizardry.settings.towerDimensions){
				if(id == world.provider.getDimension())
					this.generateWizardTower(world, random, chunkX * 16, chunkZ * 16);
			}
		}
	}

	/**
	 * Adds an Ore Spawn to Minecraft. Simply register all Ores to spawn with this method in your Generation method in
	 * your IWorldGeneration extending Class
	 *
	 * @param The Block to spawn
	 * @param The World to spawn in
	 * @param A Random object for retrieving random positions within the world to spawn the Block
	 * @param An int for passing the X-Coordinate for the Generation method
	 * @param An int for passing the Z-Coordinate for the Generation method
	 * @param An int for setting the maximum X-Coordinate values for spawning on the X-Axis on a Per-Chunk basis
	 * @param An int for setting the maximum Z-Coordinate values for spawning on the Z-Axis on a Per-Chunk basis
	 * @param An int for setting the maximum size of a vein
	 * @param An int for the Number of chances available for the Block to spawn per-chunk
	 * @param An int for the minimum Y-Coordinate height at which this block may spawn
	 * @param An int for the maximum Y-Coordinate height at which this block may spawn
	 **/
	public void addOreSpawn(IBlockState state, World world, Random random, int blockXPos, int blockZPos, int maxX,
			int maxZ, int maxVeinSize, int chancesToSpawn, int minY, int maxY){
		// int maxPossY = minY + (maxY - 1);
		assert maxY > minY : "The maximum Y must be greater than the Minimum Y";
		assert maxX > 0 && maxX <= 16 : "addOreSpawn: The Maximum X must be greater than 0 and less than 16";
		assert minY > 0 : "addOreSpawn: The Minimum Y must be greater than 0";
		assert maxY < 256 && maxY > 0 : "addOreSpawn: The Maximum Y must be less than 256 but greater than 0";
		assert maxZ > 0 && maxZ <= 16 : "addOreSpawn: The Maximum Z must be greater than 0 and less than 16";

		int diffBtwnMinMaxY = maxY - minY;
		for(int x = 0; x < chancesToSpawn; x++){
			int posX = blockXPos + random.nextInt(maxX);
			int posY = minY + random.nextInt(diffBtwnMinMaxY);
			int posZ = blockZPos + random.nextInt(maxZ);
			(new WorldGenMinable(state, maxVeinSize)).generate(world, random, new BlockPos(posX, posY, posZ));
		}
	}

	/**
	 * Generates the specified plant randomly throughout the world.
	 * 
	 * @param block The plant block
	 * @param world The world
	 * @param random A random instance
	 * @param x The x coord of the first block in the chunk
	 * @param z The y coord of the first block in the chunk
	 * @param chancesToSpawn Number of chances to spawn a flower patch
	 * @param groupSize The number of times to try generating a flower per flower patch spawn
	 */
	public void generatePlant(IBlockState state, World world, Random random, int x, int z, int chancesToSpawn,
			int groupSize){

		for(int i = 0; i < chancesToSpawn; i++){
			int randPosX = x + random.nextInt(16);
			int randPosY = random.nextInt(256);
			int randPosZ = z + random.nextInt(16);
			for(int l = 0; l < groupSize; ++l){
				int i1 = randPosX + random.nextInt(8) - random.nextInt(8);
				int j1 = randPosY + random.nextInt(4) - random.nextInt(4);
				int k1 = randPosZ + random.nextInt(8) - random.nextInt(8);

				BlockPos pos = new BlockPos(i1, j1, k1);
 
				if(world.isBlockLoaded(pos) && world.isAirBlock(pos) && (!world.provider.isNether() || j1 < 127)
						&& state.getBlock().canPlaceBlockOnSide(world, pos, EnumFacing.UP)){

					world.setBlockState(pos, state, 2);
				}
			}
		}
	}

	/**
	 * Generates wizard towers randomly throughout the world.
	 */
	public void generateWizardTower(World world, Random random, int chunkX, int chunkZ){

		// Allows the config file to set the rarity value to 0 to disable tower generation completely.
		if(Wizardry.settings.towerRarity == 0) return;

		// Compensates for the lack of space in forests. Math.max is required since treeless biomes have treesPerChunk =
		// -999
		// double treeFactor = 70 - Math.max((double)world.getBiomeGenForCoords(chunkX,
		// chunkZ).theBiomeDecorator.treesPerChunk, 0) * 1.5d;

		// Multiplied by 70 to (roughly) retain the old rarity scale
		if(random.nextInt((int)(Wizardry.settings.towerRarity * 70)) == 0){

			BlockPos origin = new BlockPos(chunkX + random.nextInt(16), 0, chunkZ + random.nextInt(16));

			// Despite what its name suggests, this method does not return the position of a liquid. It is in fact
			// exactly what is needed here since it is used for placing villages and stuff, and doesn't include leaves
			// or other foliage.
			origin = origin.up(world.getTopSolidOrLiquidBlock(origin).getY() - 1);

			int[][][] towerBlueprint = towerBlueprintSmall;

			switch(random.nextInt(4)){
			case 0:
				towerBlueprint = towerBlueprintSmall;
				break;
			case 1:
				towerBlueprint = towerBlueprintMedium;
				break;
			case 2:
				towerBlueprint = towerBlueprintTall;
				break;
			case 3:
				towerBlueprint = towerBlueprintDouble;
				break;
			}

			// 0 = West, 1 = North, 2 = East, 3 = South (The way you would face when walking out of the door)
			EnumFacing orientation = EnumFacing.byHorizontalIndex(random.nextInt(4));
			boolean flip = random.nextBoolean();

			if(checkSpaceForTower(world, origin, towerBlueprint, orientation, flip)){

				// == Setup ==

				boolean evilWizard = random.nextInt(5) == 0;

				IBlockState wallMaterial = Blocks.COBBLESTONE.getDefaultState();
				BlockPlanks.EnumType woodType = BlockPlanks.EnumType.OAK;

				Biome biome = world.getBiome(origin);

				// The order of these is somewhat important in that biomes can be many types, so the last of these
				// checks
				// that is true gets priority. Generally speaking, the later the check, the more specific it is.
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.DENSE))
					wallMaterial = Blocks.MOSSY_COBBLESTONE.getDefaultState();
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP))
					wallMaterial = Blocks.MOSSY_COBBLESTONE.getDefaultState();
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY))
					wallMaterial = Blocks.SANDSTONE.getDefaultState();
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER))
					wallMaterial = Blocks.NETHER_BRICK.getDefaultState();
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN))
					wallMaterial = Blocks.STONEBRICK.getDefaultState();
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.MESA))
					wallMaterial = Blocks.HARDENED_CLAY.getDefaultState();

				// Unfortunately, I can't check all the wood types with the biome dictionary
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS))
					woodType = BlockPlanks.EnumType.SPRUCE;
				if(biome == Biomes.BIRCH_FOREST || biome == Biomes.BIRCH_FOREST_HILLS)
					woodType = BlockPlanks.EnumType.BIRCH;
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) woodType = BlockPlanks.EnumType.JUNGLE;
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) woodType = BlockPlanks.EnumType.ACACIA;
				// Not technically a tree type, but I think it fits quite well anyway
				if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPOOKY))
					woodType = BlockPlanks.EnumType.DARK_OAK;

				IBlockState[] blockStateList = new IBlockState[]{null, Blocks.AIR.getDefaultState(),
						Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, woodType),
						Blocks.BOOKSHELF.getDefaultState(),
						Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR,
								EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)]),
						Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM),
						Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockSlab.HALF, EnumBlockHalf.TOP),
						wallMaterial, Blocks.GLASS_PANE.getDefaultState(), Blocks.OAK_DOOR.getDefaultState(),
						WizardryBlocks.arcane_workbench.getDefaultState(), Blocks.TORCH.getDefaultState(),
						evilWizard ? Blocks.CHEST.getDefaultState() : Blocks.BOOKSHELF.getDefaultState()};

				Set<BlockPos> blocksPlaced = new HashSet<BlockPos>();

				// == Foundations ==

				// Fills in foundations. This is done first so the door always has something to be placed on.
				boolean flag = true;

				// BlockPos is immutable, so I'm not sure if simply saying pos1 = pos will be sufficient.
				BlockPos layerCentre = new BlockPos(origin);

				while(flag){

					flag = false;

					for(BlockPos offset : foundationLayer){
						if(!world.isBlockNormalCube(layerCentre.add(offset), false)){
							world.setBlockState(layerCentre.add(offset), wallMaterial);
							blocksPlaced.add(layerCentre.add(offset));
							// Keeps going as long as something was filled in.
							flag = true;
						}
					}

					layerCentre = layerCentre.down();
				}

				// == Main Structure ==

				// It is assumed that the width of the blueprint is the same all the way up, and that the layers are
				// square.
				int width = towerBlueprint[0].length - 1;

				// x, y and z are the position the block is being put in.
				// x1, y and z1 are the position in the blueprint which determines which block is being placed.

				int x1 = 0, z1 = 0;

				for(int y = 0; y < towerBlueprint.length; y++){
					for(int z = 0; z < towerBlueprint[y].length; z++){
						for(int x = 0; x < towerBlueprint[y][z].length; x++){

							BlockPos pos = origin.add(x - width / 2, y, z - width / 2);

							switch(orientation){
							case WEST:
								x1 = flip ? width - x : x;
								z1 = z;
								break;
							case NORTH:
								x1 = z;
								z1 = flip ? x : width - x;
								break;
							case EAST:
								x1 = flip ? x : width - x;
								z1 = width - z;
								break;
							case SOUTH:
								x1 = width - z;
								z1 = flip ? width - x : x;
								break;
							default:
								break;
							}

							if(blockStateList[towerBlueprint[y][z1][x1]] != null
									&& blockStateList[towerBlueprint[y][z1][x1]].getBlock() != Blocks.TORCH
									&& blockStateList[towerBlueprint[y][z1][x1]].getBlock() != Blocks.CHEST){

								if(blockStateList[towerBlueprint[y][z1][x1]].getBlock() == Blocks.OAK_DOOR){
									// Rotates the door depending on whether flip is true.
									ItemDoor.placeDoor(
											world, pos, flip
													? EnumFacing.byHorizontalIndex(3 - orientation.getHorizontalIndex())
															.getOpposite()
													: orientation.rotateYCCW(),
											Blocks.OAK_DOOR, false);
								}else{
									world.setBlockState(pos, blockStateList[towerBlueprint[y][z1][x1]], 2);
								}

								blocksPlaced.add(pos);

							}
						}
					}
				}

				// == Extras ==

				// Torches are done afterwards so they don't fall off
				// Chests are also done afterwards, because for some reason the chest decides which way round to face
				// itself, after it has been placed, based on the surrounding blocks... but if it was placed during
				// the rest of the tower generation, some of those blocks wouldn't exist, hence it must be done here.
				for(int y = 0; y < towerBlueprint.length; y++){
					for(int z = 0; z < towerBlueprint[y].length; z++){
						for(int x = 0; x < towerBlueprint[y][z].length; x++){

							BlockPos pos = origin.add(x - width / 2, y, z - width / 2);

							switch(orientation){
							case WEST:
								x1 = flip ? width - x : x;
								z1 = z;
								break;
							case NORTH:
								x1 = z;
								z1 = flip ? x : width - x;
								break;
							case EAST:
								x1 = flip ? x : width - x;
								z1 = width - z;
								break;
							case SOUTH:
								x1 = width - z;
								z1 = flip ? width - x : x;
								break;
							default:
								break;
							}

							if(blockStateList[towerBlueprint[y][z1][x1]] != null){

								if(blockStateList[towerBlueprint[y][z1][x1]].getBlock() == Blocks.TORCH){
									if(placeTorch(world, pos, true)){
										blocksPlaced.add(pos);
									}else{
										Wizardry.logger.info("Attempted to generate a torch at " + pos + " in " + world
												+ ", but failed!");
									}
								}

								// World should always be a WorldServer, but it's worth checking anyway.
								if(blockStateList[towerBlueprint[y][z1][x1]].getBlock() == Blocks.CHEST
										&& world instanceof WorldServer){
									if(placeChest(world, pos)){
										blocksPlaced.add(pos);
										LootTable table = world.getLootTableManager().getLootTableFromLocation(
												new ResourceLocation(Wizardry.MODID, "chests/wizard_tower"));
										IInventory inventory = (IInventory)world.getTileEntity(pos);
										LootContext context = new LootContext.Builder((WorldServer)world).build();
										table.fillInventory(inventory, random, context);
									}else{
										Wizardry.logger.info("Attempted to generate a chest at " + pos + " in " + world
												+ ", but failed!");
									}
								}
							}
						}
					}
				}

				if(evilWizard){

					EntityEvilWizard wizard = new EntityEvilWizard(world);
					wizard.hasTower = true;
					wizard.setLocationAndAngles(origin.getX() + 1.5, origin.getY() + towerBlueprint.length - 9.5,
							origin.getZ() + 1.5, 0, 0);
					wizard.onInitialSpawn(world.getDifficultyForLocation(origin), null);

					world.spawnEntity(wizard);

				}else{

					EntityWizard wizard = new EntityWizard(world);
					wizard.setLocationAndAngles(origin.getX() + 1.5, origin.getY() + towerBlueprint.length - 9.5,
							origin.getZ() + 1.5, 0, 0);
					wizard.onInitialSpawn(world.getDifficultyForLocation(origin), null);
					wizard.setTowerBlocks(blocksPlaced);

					world.spawnEntity(wizard);
				}
			}
		}
	}

	/**
	 * Places a torch at the given position in the given world and automatically assigns an appropriate state. Order of
	 * priority is U-S-W-N-E, or S-W-N-E-U if wallPriority is true.
	 * 
	 * @param world The world to place the torch in.
	 * @param pos The position to place the torch at.
	 * @param wallPriority True to prioritise wall torches, false to prioritise floor torches.
	 * @return True if the torch was placed, false if it is not possible.
	 */
	private static boolean placeTorch(World world, BlockPos pos, boolean wallPriority){

		for(EnumFacing facing : ArrayUtils.add(EnumFacing.HORIZONTALS, wallPriority ? 4 : 0, EnumFacing.UP)){
			if(world.isSideSolid(pos.offset(facing.getOpposite()), facing)){
				world.setBlockState(pos, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, facing));
				return true;
			}
		}

		return false;
	}

	/**
	 * Places a chest at the given position in the given world and automatically assigns an appropriate state. Order of
	 * priority is S-W-N-E.
	 * 
	 * @param world The world to chest the torch in.
	 * @param pos The position to chest the torch at.
	 * @return True if the chest was placed, false if it is not possible.
	 */
	private static boolean placeChest(World world, BlockPos pos){

		for(EnumFacing facing : EnumFacing.HORIZONTALS){
			if(world.isAirBlock(pos.offset(facing))){
				world.setBlockState(pos, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, facing));
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether a tower generated at the given coordinates will intersect any solid or liquid blocks. Only tests
	 * for liquids (not solid blocks) for the first four layers to account for the floor and for sloping terrain.
	 * 
	 * @return True if none of the blocks which the tower would replace are solid or liquid. Dirt, stone etc., water and
	 *         lava count, as do logs, but leaves and plants don't.
	 */
	private static boolean checkSpaceForTower(World world, BlockPos pos, int[][][] towerBlueprint,
			EnumFacing orientation, boolean flip){

		// x, y and z are the position the block is being put in.
		// x1, y and z1 are the position in the blueprint which determines which block is being placed.

		int x1 = 0, z1 = 0;

		// It is assumed that the width of the blueprint is the same all the way up, and that the layers are square.
		int width = towerBlueprint[0].length - 1;

		for(int y = 0; y < towerBlueprint.length; y++){
			for(int z = 0; z < towerBlueprint[y].length; z++){
				for(int x = 0; x < towerBlueprint[y][z].length; x++){

					BlockPos pos1 = pos.add(x - width / 2, y, z - width / 2);

					switch(orientation){
					case WEST:
						x1 = flip ? width - x : x;
						z1 = z;
						break;
					case NORTH:
						x1 = z;
						z1 = flip ? x : width - x;
						break;
					case EAST:
						x1 = flip ? x : width - x;
						z1 = width - z;
						break;
					case SOUTH:
						x1 = width - z;
						z1 = flip ? width - x : x;
						break;
					default:
						break;
					}

					if(towerBlueprint[y][z1][x1] != 0 && !WizardryUtilities.canBlockBeReplacedB(world, pos1)
					// TODO: Is this a better replacement for the subsequent two lines?
					// && !world.getBlockState(pos1).getBlock().isFoliage(world, pos1)
							&& !(world.getBlockState(pos1).getBlock() instanceof IPlantable)
							&& !(world.getBlockState(pos1).getBlock() instanceof BlockLeaves)
							&& (y > 3 || world.getBlockState(pos1).getBlock() instanceof BlockLiquid)){
						return false;
					}
				}
			}
		}

		return true;
	}

	/** Array of relative positions of each block in a layer of foundations. */
	private static final BlockPos[] foundationLayer = new BlockPos[]{new BlockPos(-2, 0, -1), new BlockPos(-2, 0, 0),
			new BlockPos(-2, 0, 1), new BlockPos(2, 0, -1), new BlockPos(2, 0, 0), new BlockPos(2, 0, 1),
			new BlockPos(-1, 0, -2), new BlockPos(0, 0, -2), new BlockPos(1, 0, -2), new BlockPos(-1, 0, 2),
			new BlockPos(0, 0, 2), new BlockPos(1, 0, 2),};

	/**
	 * 3D matrix of integers representing the different blocks which make up the wizard tower. The blocks corresponding
	 * to each integer are as follows (note that some blocks change depending on the biome):
	 * <p>
	 * 0 Nothing (keep existing block)<br>
	 * 1 Air (remove existing block)<br>
	 * 2 Floor (planks)<br>
	 * 3 Bookshelf<br>
	 * 4 Roof (stained clay)<br>
	 * 5 Floor slab (lower half)<br>
	 * 6 Floor slab (upper half)<br>
	 * 7 Wall (cobblestone by default)<br>
	 * 8 Glass pane<br>
	 * 9 Door (metadata is handled by the built-in vanilla method)<br>
	 * 10 Arcane workbench<br>
	 * 11 Torch (metadata is handled separately depending on adjacent blocks)<br>
	 * 12 Chest (only generates if the wizard is evil, otherwise places a bookshelf instead)
	 */
	private static final int[][][] towerBlueprintSmall = {
			// x is horizontal, z is vertical, y is layers
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 2, 2, 2, 0, 0, 0}, {0, 0, 0, 2, 2, 2, 0, 0, 0}, {0, 0, 0, 2, 2, 2, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 9, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 6, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 0, 7, 0, 0, 0}, {0, 0, 0, 11, 1, 11, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 11, 7, 0, 0}, {0, 0, 7, 5, 6, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 6, 7, 0, 0}, {0, 0, 7, 1, 1, 5, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 11, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 7, 2, 2, 2, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 2, 7, 0}, {0, 7, 5, 2, 2, 6, 2, 7, 0}, {0, 7, 2, 2, 2, 2, 2, 7, 0},
					{0, 0, 7, 2, 2, 2, 7, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 7, 3, 3, 3, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 10, 1, 3, 7, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 7, 11, 1, 11, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 8, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 1, 1, 3, 7, 0},
					{0, 0, 7, 11, 1, 11, 7, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 4, 7, 7, 7, 4, 0, 0}, {0, 4, 7, 1, 1, 1, 7, 4, 0},
					{4, 7, 1, 1, 1, 1, 3, 7, 4}, {4, 7, 1, 1, 1, 1, 3, 7, 4}, {4, 7, 1, 1, 1, 1, 12, 7, 4},
					{0, 4, 7, 1, 1, 1, 7, 4, 0}, {0, 0, 4, 7, 7, 7, 4, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0},
					{0, 4, 1, 1, 1, 1, 1, 4, 0}, {0, 4, 1, 1, 1, 1, 1, 4, 0}, {0, 4, 1, 1, 1, 1, 1, 4, 0},
					{0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},
					{0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0},
					{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0},
					{0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0},
					{0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},}};

	/**
	 * 3D matrix of integers representing the different blocks which make up the wizard tower. The blocks corresponding
	 * to each integer are as follows (note that some blocks change depending on the biome):
	 * <p>
	 * 0 Nothing (keep existing block)<br>
	 * 1 Air (remove existing block)<br>
	 * 2 Floor (planks)<br>
	 * 3 Bookshelf<br>
	 * 4 Roof (stained clay)<br>
	 * 5 Floor slab (lower half)<br>
	 * 6 Floor slab (upper half)<br>
	 * 7 Wall (cobblestone by default)<br>
	 * 8 Glass pane<br>
	 * 9 Door (metadata is handled by the built-in vanilla method)<br>
	 * 10 Arcane workbench<br>
	 * 11 Torch (metadata is handled separately depending on adjacent blocks)<br>
	 * 12 Chest (only generates if the wizard is evil, otherwise places a bookshelf instead)
	 */
	private static final int[][][] towerBlueprintMedium = {
			// x is horizontal, z is vertical, y is layers
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 2, 2, 2, 0, 0, 0}, {0, 0, 0, 2, 2, 2, 0, 0, 0}, {0, 0, 0, 2, 2, 2, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 9, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 6, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 0, 7, 0, 0, 0}, {0, 0, 0, 11, 1, 11, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 11, 7, 0, 0}, {0, 0, 7, 5, 6, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 6, 7, 0, 0}, {0, 0, 7, 1, 1, 5, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 11, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 6, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 11, 7, 0, 0}, {0, 0, 7, 5, 6, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 6, 7, 0, 0}, {0, 0, 7, 1, 1, 5, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 11, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 7, 2, 2, 2, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 2, 7, 0}, {0, 7, 5, 2, 2, 6, 2, 7, 0}, {0, 7, 2, 2, 2, 2, 2, 7, 0},
					{0, 0, 7, 2, 2, 2, 7, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 7, 3, 3, 3, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 10, 1, 3, 7, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 7, 11, 1, 11, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 8, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 1, 1, 3, 7, 0},
					{0, 0, 7, 11, 1, 11, 7, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 4, 7, 7, 7, 4, 0, 0}, {0, 4, 7, 1, 1, 1, 7, 4, 0},
					{4, 7, 1, 1, 1, 1, 3, 7, 4}, {4, 7, 1, 1, 1, 1, 3, 7, 4}, {4, 7, 1, 1, 1, 1, 12, 7, 4},
					{0, 4, 7, 1, 1, 1, 7, 4, 0}, {0, 0, 4, 7, 7, 7, 4, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0},
					{0, 4, 1, 1, 1, 1, 1, 4, 0}, {0, 4, 1, 1, 1, 1, 1, 4, 0}, {0, 4, 1, 1, 1, 1, 1, 4, 0},
					{0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},
					{0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0},
					{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0},
					{0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0},
					{0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},}};

	/**
	 * 3D matrix of integers representing the different blocks which make up the wizard tower. The blocks corresponding
	 * to each integer are as follows (note that some blocks change depending on the biome):
	 * <p>
	 * 0 Nothing (keep existing block)<br>
	 * 1 Air (remove existing block)<br>
	 * 2 Floor (planks)<br>
	 * 3 Bookshelf<br>
	 * 4 Roof (stained clay)<br>
	 * 5 Floor slab (lower half)<br>
	 * 6 Floor slab (upper half)<br>
	 * 7 Wall (cobblestone by default)<br>
	 * 8 Glass pane<br>
	 * 9 Door (metadata is handled by the built-in vanilla method)<br>
	 * 10 Arcane workbench<br>
	 * 11 Torch (metadata is handled separately depending on adjacent blocks)<br>
	 * 12 Chest (only generates if the wizard is evil, otherwise places a bookshelf instead)
	 */
	private static final int[][][] towerBlueprintTall = {
			// x is horizontal, z is vertical, y is layers
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 2, 2, 2, 0, 0, 0}, {0, 0, 0, 2, 2, 2, 0, 0, 0}, {0, 0, 0, 2, 2, 2, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 9, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 6, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 0, 7, 0, 0, 0}, {0, 0, 0, 11, 1, 11, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 11, 7, 0, 0}, {0, 0, 7, 5, 6, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 6, 7, 0, 0}, {0, 0, 7, 1, 1, 5, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0}, {0, 0, 0, 1, 1, 1, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 11, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 6, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 11, 7, 0, 0}, {0, 0, 7, 5, 6, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 6, 7, 0, 0}, {0, 0, 7, 1, 1, 5, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 11, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 6, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 11, 7, 0, 0}, {0, 0, 7, 5, 6, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 6, 7, 0, 0}, {0, 0, 7, 1, 1, 5, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 1, 6, 5, 7, 0, 0}, {0, 0, 7, 11, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0},
					{0, 0, 7, 5, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 7, 1, 1, 1, 7, 0, 0},
					{0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 7, 2, 2, 2, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 2, 7, 0}, {0, 7, 5, 2, 2, 6, 2, 7, 0}, {0, 7, 2, 2, 2, 2, 2, 7, 0},
					{0, 0, 7, 2, 2, 2, 7, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 7, 3, 3, 3, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 10, 1, 3, 7, 0},
					{0, 0, 7, 1, 1, 1, 7, 0, 0}, {0, 0, 0, 7, 7, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 7, 11, 1, 11, 7, 0, 0},
					{0, 7, 1, 1, 1, 1, 3, 7, 0}, {0, 8, 1, 1, 1, 1, 3, 7, 0}, {0, 7, 1, 1, 1, 1, 3, 7, 0},
					{0, 0, 7, 11, 1, 11, 7, 0, 0}, {0, 0, 0, 7, 8, 7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 4, 7, 7, 7, 4, 0, 0}, {0, 4, 7, 1, 1, 1, 7, 4, 0},
					{4, 7, 1, 1, 1, 1, 3, 7, 4}, {4, 7, 1, 1, 1, 1, 3, 7, 4}, {4, 7, 1, 1, 1, 1, 12, 7, 4},
					{0, 4, 7, 1, 1, 1, 7, 4, 0}, {0, 0, 4, 7, 7, 7, 4, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0},
					{0, 4, 1, 1, 1, 1, 1, 4, 0}, {0, 4, 1, 1, 1, 1, 1, 4, 0}, {0, 4, 1, 1, 1, 1, 1, 4, 0},
					{0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},
					{0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0},
					{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0},
					{0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 4, 1, 1, 1, 4, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0},
					{0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 4, 4, 4, 0, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 0, 4, 4, 4, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 4, 1, 4, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},}};

	/**
	 * 3D matrix of integers representing the different blocks which make up the wizard tower. The blocks corresponding
	 * to each integer are as follows (note that some blocks change depending on the biome):
	 * <p>
	 * 0 Nothing (keep existing block)<br>
	 * 1 Air (remove existing block)<br>
	 * 2 Floor (planks)<br>
	 * 3 Bookshelf<br>
	 * 4 Roof (stained clay)<br>
	 * 5 Floor slab (lower half)<br>
	 * 6 Floor slab (upper half)<br>
	 * 7 Wall (cobblestone by default)<br>
	 * 8 Glass pane<br>
	 * 9 Door (metadata is handled by the built-in vanilla method)<br>
	 * 10 Arcane workbench<br>
	 * 11 Torch (metadata is handled separately depending on adjacent blocks)<br>
	 * 12 Chest (only generates if the wizard is evil, otherwise places a bookshelf instead)
	 */
	private static final int[][][] towerBlueprintDouble = {
			// x is horizontal, z is vertical, y is layers
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 6, 5, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 9, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 5, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 6, 1, 1, 7, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 11, 1, 11, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 1, 1, 11, 7, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 5, 6, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 1, 1, 6, 7, 7, 7, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 5, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 6, 5, 7, 7, 7, 0, 0},
					{0, 0, 0, 0, 7, 11, 1, 1, 1, 1, 1, 7, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 7, 7, 0, 0},
					{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 5, 1, 1, 7, 7, 8, 7, 0},
					{0, 0, 0, 0, 7, 6, 1, 1, 1, 1, 1, 8, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 7, 8, 7, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 8, 7, 0, 4, 4, 4, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 7, 7, 7, 4},
					{0, 0, 0, 0, 7, 1, 1, 11, 7, 1, 1, 7, 4}, {0, 0, 0, 0, 7, 5, 6, 1, 7, 7, 7, 7, 4},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 4, 4, 4, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 4, 4, 4, 0},
					{0, 0, 0, 0, 7, 1, 1, 6, 7, 4, 1, 4, 0}, {0, 0, 0, 0, 7, 1, 1, 5, 7, 4, 4, 4, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 1, 6, 5, 7, 0, 4, 0, 0},
					{0, 0, 0, 0, 7, 11, 1, 1, 7, 4, 1, 4, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 4, 0, 0},
					{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 7, 5, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 4, 0, 0}, {0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 2, 2, 2, 7, 0, 0, 0, 0}, {0, 0, 0, 7, 1, 1, 1, 1, 2, 7, 0, 0, 0},
					{0, 0, 0, 7, 5, 2, 2, 6, 2, 7, 4, 0, 0}, {0, 0, 0, 7, 2, 2, 2, 2, 2, 7, 0, 0, 0},
					{0, 0, 0, 0, 7, 2, 2, 2, 7, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 3, 3, 3, 7, 0, 0, 0, 0}, {0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
					{0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0}, {0, 0, 0, 7, 1, 1, 10, 1, 3, 7, 0, 0, 0},
					{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 7, 11, 1, 11, 7, 0, 0, 0, 0}, {0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
					{0, 0, 0, 8, 1, 1, 1, 1, 3, 7, 0, 0, 0}, {0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
					{0, 0, 0, 0, 7, 11, 1, 11, 7, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 7, 7, 7, 4, 0, 0, 0, 0},
					{0, 0, 0, 4, 7, 1, 1, 1, 7, 4, 0, 0, 0}, {0, 0, 4, 7, 1, 1, 1, 1, 3, 7, 4, 0, 0},
					{0, 0, 4, 7, 1, 1, 1, 1, 3, 7, 4, 0, 0}, {0, 0, 4, 7, 1, 1, 1, 1, 12, 7, 4, 0, 0},
					{0, 0, 0, 4, 7, 1, 1, 1, 7, 4, 0, 0, 0}, {0, 0, 0, 0, 4, 7, 7, 7, 4, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0}, {0, 0, 0, 4, 1, 1, 1, 1, 1, 4, 0, 0, 0},
					{0, 0, 0, 4, 1, 1, 1, 1, 1, 4, 0, 0, 0}, {0, 0, 0, 4, 1, 1, 1, 1, 1, 4, 0, 0, 0},
					{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
					{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},},
			{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},}};

}
