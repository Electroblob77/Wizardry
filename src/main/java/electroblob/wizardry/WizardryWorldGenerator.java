package electroblob.wizardry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.IWorldGenerator;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.IPlantable;

public class WizardryWorldGenerator implements IWorldGenerator {
	
	/** The string identifier for wizard tower chests, used in ChestGenHooks. */
	public static final String WIZARD_TOWER = Wizardry.MODID + "wizardTower";

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

		for(int id : Wizardry.oreDimensions){
			if(id == world.provider.dimensionId) this.addOreSpawn(Wizardry.crystalOre, world, random, chunkX * 16, chunkZ * 16, 16, 16, 5, 7, 5, 30);
		}

		for(int id : Wizardry.flowerDimensions){
			if(id == world.provider.dimensionId) this.generatePlant(Wizardry.crystalFlower, world, random, chunkX * 16, chunkZ * 16, 2, 20);
		}

		if(world.getWorldInfo().isMapFeaturesEnabled()){
			for(int id : Wizardry.towerDimensions){
				if(id == world.provider.dimensionId) this.generateWizardTower(world, random, chunkX * 16, chunkZ * 16);
			}
		}
	}

	/**
	 * Adds an Ore Spawn to Minecraft. Simply register all Ores to spawn with this method in your Generation method in your IWorldGeneration extending Class
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
	public void addOreSpawn(Block block, World world, Random random, int blockXPos, int blockZPos, int maxX, int maxZ, int maxVeinSize, int chancesToSpawn, int minY, int maxY)
	{
		int maxPossY = minY + (maxY - 1);
		assert maxY > minY: "The maximum Y must be greater than the Minimum Y";
		assert maxX > 0 && maxX <= 16: "addOreSpawn: The Maximum X must be greater than 0 and less than 16";
		assert minY > 0: "addOreSpawn: The Minimum Y must be greater than 0";
		assert maxY < 256 && maxY > 0: "addOreSpawn: The Maximum Y must be less than 256 but greater than 0";
		assert maxZ > 0 && maxZ <= 16: "addOreSpawn: The Maximum Z must be greater than 0 and less than 16";

		int diffBtwnMinMaxY = maxY - minY;
		for(int x = 0; x < chancesToSpawn; x++)
		{
			int posX = blockXPos + random.nextInt(maxX);
			int posY = minY + random.nextInt(diffBtwnMinMaxY);
			int posZ = blockZPos + random.nextInt(maxZ);
			(new WorldGenMinable(block, maxVeinSize)).generate(world, random, posX, posY, posZ);
		}
	}

	/**
	 * Generates the specified plant randomly throughout the world.
	 * @param block The plant block
	 * @param world The world
	 * @param random A random instance
	 * @param x The x coord of the first block in the chunk
	 * @param z The y coord of the first block in the chunk
	 * @param chancesToSpawn Number of chances to spawn a flower patch
	 * @param groupSize The number of times to try generating a flower per flower patch spawn
	 */
	public void generatePlant(Block block, World world, Random random, int x, int z, int chancesToSpawn, int groupSize){
		for(int i = 0; i < chancesToSpawn; i++){
			int randPosX = x + random.nextInt(16);
			int randPosY = random.nextInt(256);
			int randPosZ = z + random.nextInt(16);
			for (int l = 0; l < groupSize; ++l)
			{
				int i1 = randPosX + random.nextInt(8) - random.nextInt(8);
				int j1 = randPosY + random.nextInt(4) - random.nextInt(4);
				int k1 = randPosZ + random.nextInt(8) - random.nextInt(8);

				if(world.blockExists(i1, j1, k1) && world.isAirBlock(i1, j1, k1) && (!world.provider.hasNoSky || j1 < 127) && block.canBlockStay(world, i1, j1, k1)){
					
					world.setBlock(i1, j1, k1, block, 0, 2);
				}
			}
		}
	}

	/**
	 * Generates wizard towers randomly throughout the world.
	 */
	public void generateWizardTower(World world, Random random, int chunkX, int chunkZ){

		// Allows the config file to set the rarity value to 0 to disable tower generation completely.
		if(Wizardry.towerRarity == 0) return;
		
		// Compensates for the lack of space in forests. Math.max is required since treeless biomes have treesPerChunk = -999
		//double treeFactor = 70 - Math.max((double)world.getBiomeGenForCoords(chunkX, chunkZ).theBiomeDecorator.treesPerChunk, 0) * 1.5d;

		// Multiplied by 70 to (roughly) retain the old rarity scale
		if(random.nextInt((int)(Wizardry.towerRarity * 70)) == 0){

			int posX = chunkX + random.nextInt(16);
			int posZ = chunkZ + random.nextInt(16);
			
			// Despite what its name suggests, this method does not return the position of a liquid. It is in fact
			// exactly what is needed here since it is used for placing villages and stuff, and doesn't include leaves
			// or other foliage.
			int posY = world.getTopSolidOrLiquidBlock(posX, posZ) - 1;
			
			int[][][] towerBlueprint = towerBlueprintSmall;
			
			switch(random.nextInt(4)){
			case 0: towerBlueprint = towerBlueprintSmall; break;
			case 1: towerBlueprint = towerBlueprintMedium; break;
			case 2: towerBlueprint = towerBlueprintTall; break;
			case 3: towerBlueprint = towerBlueprintDouble; break;
			}
		
			// 0 = West, 1 = North, 2 = East, 3 = South (The way you would face when walking out of the door)
			int orientation = random.nextInt(4);
			boolean flip = random.nextBoolean();

			/* It seems there is something specific about sand which makes it very unlikely (but NOT impossible) for
			 * the towers to generate on it... 
			 * Edit: The culprit was canBlockSeeTheSky, so I've taken that out and replaced it with getTopSolidOrLiquidBlock
			 * (which also greatly increases the efficiency of the generator since it doesn't even try underground blocks).
			 * In addition, I have added a check for solid blocks in the way of the tower; see checkForSpace. */

			// Debugging
			//System.out.println("Tried to generate wizard tower at (" + posX + ", " + posY + ", " + posZ + "); Biome: "
			//		+ world.getBiomeGenForCoords(posX, posZ).biomeName + ", Can block see sky: "
			//		+ world.canBlockSeeTheSky(posX, posY, posZ) + ", Is block normal cube: "
			//		+ world.isBlockNormalCubeDefault(posX, posY, posZ, false) + ", Block: "
			//		+ world.getBlock(posX, posY, posZ).getLocalizedName());

			// These conditions are no longer necessary thanks to world.getTopSolidOrLiquidBlock
			//if((world.canBlockSeeTheSky(posX, posY, posZ) || world.getBlock(posX, posY, posZ) == Blocks.grass) &&
			//if(!world.isAirBlock(posX, posY, posZ) && world.isBlockNormalCubeDefault(posX, posY, posZ, false) &&
			
			if(checkSpaceForTower(world, posX, posY, posZ, towerBlueprint, orientation, flip)){

				boolean evilWizard = random.nextInt(5) == 0;
				
				Block wallMaterial = Blocks.cobblestone;
				int woodType = 0;
				BiomeGenBase biome = world.getBiomeGenForCoords(posX, posZ);

				// The order of these is somewhat important in that biomes can be many types, so the last of these checks
				// that is true gets priority
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.DENSE)) wallMaterial = Blocks.mossy_cobblestone;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SWAMP)) wallMaterial = Blocks.mossy_cobblestone;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SANDY)) wallMaterial = Blocks.sandstone;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.NETHER)) wallMaterial = Blocks.nether_brick;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.MOUNTAIN)) wallMaterial = Blocks.stonebrick;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.MESA)) wallMaterial = Blocks.hardened_clay;

				// Unfortunately, I can't check all the wood types with the biome dictionary
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.CONIFEROUS)) woodType = 1;
				if(biome == BiomeGenBase.birchForest || biome == BiomeGenBase.birchForestHills) woodType = 2;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.JUNGLE)) woodType = 3;
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SAVANNA)) woodType = 4;
				// Not technically a tree type, but I think it fits quite well anyway
				if(BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SPOOKY)) woodType = 5;

				Block[] blockList = new Block[]{
						null,
						Blocks.air,
						Blocks.planks,
						Blocks.bookshelf,
						Blocks.stained_hardened_clay,
						Blocks.wooden_slab,
						Blocks.wooden_slab,
						wallMaterial,
						Blocks.glass_pane,
						Blocks.wooden_door,
						Wizardry.arcaneWorkbench,
						Blocks.torch,
						evilWizard ? Blocks.chest : Blocks.bookshelf
				};
				
				int[] metadataList = new int[]{0, 0, woodType, 0, random.nextInt(15), woodType, woodType + 8, 0, 0, 0, 0, 0, 0};

				List<int[]> blocksPlaced = new ArrayList<int[]>();
				
				// Fills in foundations. This is done first so the door always has something to be placed on.
				boolean flag = true;
				int y1 = 0;

				while(flag){
					flag = false;
					if(!world.isBlockNormalCubeDefault(posX - 2, posY + y1, posZ - 1, false)){
						world.setBlock(posX - 2, posY + y1, posZ - 1, wallMaterial);
						blocksPlaced.add(new int[]{posX - 2, posY + y1, posZ - 1});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX - 2, posY + y1, posZ, false)){
						world.setBlock(posX - 2, posY + y1, posZ, wallMaterial);
						blocksPlaced.add(new int[]{posX - 2, posY + y1, posZ});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX - 2, posY + y1, posZ + 1, false)){
						world.setBlock(posX - 2, posY + y1, posZ + 1, wallMaterial);
						blocksPlaced.add(new int[]{posX - 2, posY + y1, posZ + 1});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX + 2, posY + y1, posZ - 1, false)){
						world.setBlock(posX + 2, posY + y1, posZ - 1, wallMaterial);
						blocksPlaced.add(new int[]{posX + 2, posY + y1, posZ - 1});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX + 2, posY + y1, posZ, false)){
						world.setBlock(posX + 2, posY + y1, posZ, wallMaterial);
						blocksPlaced.add(new int[]{posX + 2, posY + y1, posZ});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX + 2, posY + y1, posZ + 1, false)){
						world.setBlock(posX + 2, posY + y1, posZ + 1, wallMaterial);
						blocksPlaced.add(new int[]{posX + 2, posY + y1, posZ + 1});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX - 1, posY + y1, posZ - 2, false)){
						world.setBlock(posX - 1, posY + y1, posZ - 2, wallMaterial);
						blocksPlaced.add(new int[]{posX - 1, posY + y1, posZ - 2});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX, posY + y1, posZ - 2, false)){
						world.setBlock(posX, posY + y1, posZ - 2, wallMaterial);
						blocksPlaced.add(new int[]{posX, posY + y1, posZ - 2});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX + 1, posY + y1, posZ - 2, false)){
						world.setBlock(posX + 1, posY + y1, posZ - 2, wallMaterial);
						blocksPlaced.add(new int[]{posX + 1, posY + y1, posZ - 2});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX - 1, posY + y1, posZ + 2, false)){
						world.setBlock(posX - 1, posY + y1, posZ + 2, wallMaterial);
						blocksPlaced.add(new int[]{posX - 1, posY + y1, posZ + 2});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX, posY + y1, posZ + 2, false)){
						world.setBlock(posX, posY + y1, posZ + 2, wallMaterial);
						blocksPlaced.add(new int[]{posX, posY + y1, posZ + 2});
						flag = true;
					}
					if(!world.isBlockNormalCubeDefault(posX + 1, posY + y1, posZ + 2, false)){
						world.setBlock(posX + 1, posY + y1, posZ + 2, wallMaterial);
						blocksPlaced.add(new int[]{posX + 1, posY + y1, posZ + 2});
						flag = true;
					}
					y1--;
				}

				// It is assumed that the width of the blueprint is the same all the way up, and that the layers are square.
				int width = towerBlueprint[0].length-1;

				// x, y and z are the position the block is being put in.
				// x1, y and z1 are the position in the blueprint which determines which block is being placed.
				
				int x1 = 0, z1 = 0;
				
				// Main structure
				for(int y=0; y<towerBlueprint.length; y++){
					for(int z=0; z<towerBlueprint[y].length; z++){
						for(int x=0; x<towerBlueprint[y][z].length; x++){

							switch(orientation){
							case 0:
								x1 = flip ? width-x : x;
								z1 = z;
								break;
							case 1:
								x1 = z;
								z1 = flip ? x : width-x;
								break;
							case 2:
								x1 = flip ? x : width-x;
								z1 = width-z;
								break;
							case 3:
								x1 = width-z;
								z1 = flip ? width-x : x;
								break;
							}

							if(blockList[towerBlueprint[y][z1][x1]] != null && blockList[towerBlueprint[y][z1][x1]] != Blocks.torch
									&& blockList[towerBlueprint[y][z1][x1]] != Blocks.chest){

								if(blockList[towerBlueprint[y][z1][x1]] == Blocks.wooden_door){
									// The 5th argument here rotates the door by 90 degrees, clockwise or anticlockwise depending on whether flip is true.
									ItemDoor.placeDoorBlock(world, posX + x - width/2, posY + y, posZ + z - width/2, flip ? (5 - orientation) % 4 : (orientation + 1) % 4, Blocks.wooden_door);
								}else{
									world.setBlock(posX + x - width/2, posY + y, posZ + z - width/2, blockList[towerBlueprint[y][z1][x1]], metadataList[towerBlueprint[y][z1][x1]], 2);
								}
								blocksPlaced.add(new int[]{posX + x - width/2, posY + y, posZ + z - width/2});

							}
						}
					}
				}

				// Torches are done afterwards so they don't fall off
				// Chests are also done afterwards, because for some reason the chest decides which way round to face
				// itself, after it has been placed, based on the surrounding blocks... but if it was placed during
				// the rest of the tower generation, some of those blocks wouldn't exist, hence it must be done here.
				for(int y=0; y<towerBlueprint.length; y++){
					for(int z=0; z<towerBlueprint[y].length; z++){
						for(int x=0; x<towerBlueprint[y][z].length; x++){

							switch(orientation){
							case 0:
								x1 = flip ? width-x : x;
								z1 = z;
								break;
							case 1:
								x1 = z;
								z1 = flip ? x : width-x;
								break;
							case 2:
								x1 = flip ? x : width-x;
								z1 = width-z;
								break;
							case 3:
								x1 = width-z;
								z1 = flip ? width-x : x;
								break;
							}

							if(blockList[towerBlueprint[y][z1][x1]] == Blocks.torch || blockList[towerBlueprint[y][z1][x1]] == Blocks.chest){
								world.setBlock(posX + x - width/2, posY + y, posZ + z - width/2, blockList[towerBlueprint[y][z1][x1]]);
								blocksPlaced.add(new int[]{posX + x - width/2, posY + y, posZ + z - width/2});
								
								if(blockList[towerBlueprint[y][z1][x1]] == Blocks.chest){
									WeightedRandomChestContent.generateChestContents(random, ChestGenHooks.getItems(WIZARD_TOWER, random), (IInventory) world.getTileEntity(posX + x - width/2, posY + y, posZ + z - width/2), ChestGenHooks.getCount(WIZARD_TOWER, random));
								}
							}
						}
					}
				}

				if(evilWizard){

					EntityEvilWizard wizard = new EntityEvilWizard(world);
					wizard.hasTower = true;
					wizard.setLocationAndAngles(posX + 1.5, posY + towerBlueprint.length - 9.5, posZ + 1.5, 0, 0);
					wizard.onSpawnWithEgg(null);

					world.spawnEntityInWorld(wizard);
					
				}else{
					
					EntityWizard wizard = new EntityWizard(world);
					wizard.setLocationAndAngles(posX + 1.5, posY + towerBlueprint.length - 9.5, posZ + 1.5, 0, 0);
					wizard.onSpawnWithEgg(null);

					int[][] blockArray = new int[blocksPlaced.size()][3];
					for(int i=0; i<blockArray.length; i++){
						blockArray[i] = blocksPlaced.get(i);
					}
					wizard.setTowerBlocks(blockArray);

					world.spawnEntityInWorld(wizard);
				}
			}
		}
	}
	
	/**
	 * Checks whether a tower generated at the given coordinates will intersect any solid or liquid blocks. Only tests for
	 * liquids (not solid blocks) for the first four layers to account for the floor and for sloping terrain.
	 * @return True if none of the blocks which the tower would replace are solid or liquid. Dirt, stone etc., water and
	 * lava count, as do logs, but leaves and plants don't.
	 */
	private static boolean checkSpaceForTower(World world, int posX, int posY, int posZ, int[][][] towerBlueprint, int orientation, boolean flip){
		
		// x, y and z are the position the block is being put in.
		// x1, y and z1 are the position in the blueprint which determines which block is being placed.
		
		int x1 = 0, z1 = 0;
		
		// It is assumed that the width of the blueprint is the same all the way up, and that the layers are square.
		int width = towerBlueprint[0].length-1;
		
		for(int y=0; y<towerBlueprint.length; y++){
			for(int z=0; z<towerBlueprint[y].length; z++){
				for(int x=0; x<towerBlueprint[y][z].length; x++){

					switch(orientation){
					case 0:
						x1 = flip ? width-x : x;
						z1 = z;
						break;
					case 1:
						x1 = z;
						z1 = flip ? x : width-x;
						break;
					case 2:
						x1 = flip ? x : width-x;
						z1 = width-z;
						break;
					case 3:
						x1 = width-z;
						z1 = flip ? width-x : x;
						break;
					}

					if(towerBlueprint[y][z1][x1] != 0 && !WizardryUtilities.canBlockBeReplacedB(world, posX + x - width/2, posY + y, posZ + z - width/2)
							&& !(world.getBlock(posX + x - width/2, posY + y, posZ + z - width/2) instanceof IPlantable)
							&& !(world.getBlock(posX + x - width/2, posY + y, posZ + z - width/2) instanceof BlockLeavesBase)
							&& (y > 3 || world.getBlock(posX + x - width/2, posY + y, posZ + z - width/2) instanceof BlockLiquid)){
						return false;
					}
				}
			}
		}
		
		return true;
	}

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
			{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 9, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1 ,1, 7, 0, 0},
				{0, 0, 7, 6, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 0, 7, 0, 0, 0},
				{0, 0, 0, 11,1, 11,0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 11,7, 0, 0},
				{0, 0, 7, 5, 6, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 6, 7, 0, 0},
				{0, 0, 7, 1, 1, 5, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 11,1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 2, 2, 2, 7, 0, 0},
				{0, 7, 1, 1, 1, 1, 2, 7, 0},
				{0, 7, 5, 2, 2, 6, 2, 7, 0},
				{0, 7, 2, 2, 2, 2, 2, 7, 0},
				{0, 0, 7, 2, 2, 2, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 3, 3, 3, 7, 0, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 10,1, 3, 7, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 11,1, 11,7, 0, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 8, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 0, 7, 11,1, 11,7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 7, 7, 7, 4, 0, 0},
				{0, 4, 7, 1, 1, 1, 7, 4, 0},
				{4, 7, 1, 1, 1, 1, 3, 7, 4},
				{4, 7, 1, 1, 1, 1, 3, 7, 4},
				{4, 7, 1, 1, 1, 1, 12,7, 4},
				{0, 4, 7, 1, 1, 1, 7, 4, 0},
				{0, 0, 4, 7, 7, 7, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			}
	};
	
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
			{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 9, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1 ,1, 7, 0, 0},
				{0, 0, 7, 6, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 0, 7, 0, 0, 0},
				{0, 0, 0, 11,1, 11,0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 11,7, 0, 0},
				{0, 0, 7, 5, 6, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 6, 7, 0, 0},
				{0, 0, 7, 1, 1, 5, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 11,1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1 ,1, 7, 0, 0},
				{0, 0, 7, 6, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 11,7, 0, 0},
				{0, 0, 7, 5, 6, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 6, 7, 0, 0},
				{0, 0, 7, 1, 1, 5, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 11,1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 2, 2, 2, 7, 0, 0},
				{0, 7, 1, 1, 1, 1, 2, 7, 0},
				{0, 7, 5, 2, 2, 6, 2, 7, 0},
				{0, 7, 2, 2, 2, 2, 2, 7, 0},
				{0, 0, 7, 2, 2, 2, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 3, 3, 3, 7, 0, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 10,1, 3, 7, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 11,1, 11,7, 0, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 8, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 0, 7, 11,1, 11,7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 7, 7, 7, 4, 0, 0},
				{0, 4, 7, 1, 1, 1, 7, 4, 0},
				{4, 7, 1, 1, 1, 1, 3, 7, 4},
				{4, 7, 1, 1, 1, 1, 3, 7, 4},
				{4, 7, 1, 1, 1, 1, 12,7, 4},
				{0, 4, 7, 1, 1, 1, 7, 4, 0},
				{0, 0, 4, 7, 7, 7, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			}
	};
	
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
			{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 2, 2, 2, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 9, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1 ,1, 7, 0, 0},
				{0, 0, 7, 6, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 0, 7, 0, 0, 0},
				{0, 0, 0, 11,1, 11,0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 11,7, 0, 0},
				{0, 0, 7, 5, 6, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 6, 7, 0, 0},
				{0, 0, 7, 1, 1, 5, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
				{0, 0, 0, 1, 1, 1, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 11,1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1 ,1, 7, 0, 0},
				{0, 0, 7, 6, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 11,7, 0, 0},
				{0, 0, 7, 5, 6, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 6, 7, 0, 0},
				{0, 0, 7, 1, 1, 5, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 11,1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1 ,1, 7, 0, 0},
				{0, 0, 7, 6, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 11,7, 0, 0},
				{0, 0, 7, 5, 6, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 6, 7, 0, 0},
				{0, 0, 7, 1, 1, 5, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 1, 6, 5, 7, 0, 0},
				{0, 0, 7, 11,1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 5, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 2, 2, 2, 7, 0, 0},
				{0, 7, 1, 1, 1, 1, 2, 7, 0},
				{0, 7, 5, 2, 2, 6, 2, 7, 0},
				{0, 7, 2, 2, 2, 2, 2, 7, 0},
				{0, 0, 7, 2, 2, 2, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 7, 3, 3, 3, 7, 0, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 10,1, 3, 7, 0},
				{0, 0, 7, 1, 1, 1, 7, 0, 0},
				{0, 0, 0, 7, 7, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 7, 11,1, 11,7, 0, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 8, 1, 1, 1, 1, 3, 7, 0},
				{0, 7, 1, 1, 1, 1, 3, 7, 0},
				{0, 0, 7, 11,1, 11,7, 0, 0},
				{0, 0, 0, 7, 8, 7, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 7, 7, 7, 4, 0, 0},
				{0, 4, 7, 1, 1, 1, 7, 4, 0},
				{4, 7, 1, 1, 1, 1, 3, 7, 4},
				{4, 7, 1, 1, 1, 1, 3, 7, 4},
				{4, 7, 1, 1, 1, 1, 12,7, 4},
				{0, 4, 7, 1, 1, 1, 7, 4, 0},
				{0, 0, 4, 7, 7, 7, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 4, 1, 1, 1, 1, 1, 4, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 4, 1, 1, 1, 4, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 4, 4, 4, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0},
			}
	};

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
			{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 2, 2, 2, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 6, 5, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 9, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 5, 1 ,1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 6, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 11,1, 11,0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 11,7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 5, 6, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 6, 7, 7, 7, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 5, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 6, 5, 7, 7, 7, 0, 0},
				{0, 0, 0, 0, 7, 11,1, 1, 1, 1, 1, 7, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 7, 7, 0, 0},
				{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 5, 1 ,1, 7, 7, 8, 7, 0},
				{0, 0, 0, 0, 7, 6, 1, 1, 1, 1, 1, 8, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 7, 8, 7, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 8, 7, 0, 4, 4, 4, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 7, 7, 7, 4},
				{0, 0, 0, 0, 7, 1, 1, 11,7, 1, 1, 7, 4},
				{0, 0, 0, 0, 7, 5, 6, 1, 7, 7, 7, 7, 4},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 4, 4, 4, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 4, 4, 4, 0},
				{0, 0, 0, 0, 7, 1, 1, 6, 7, 4, 1, 4, 0},
				{0, 0, 0, 0, 7, 1, 1, 5, 7, 4, 4, 4, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 6, 5, 7, 0, 4, 0, 0},
				{0, 0, 0, 0, 7, 11,1, 1, 7, 4, 1, 4, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 4, 0, 0},
				{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 5, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 4, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 2, 2, 2, 7, 0, 0, 0, 0},
				{0, 0, 0, 7, 1, 1, 1, 1, 2, 7, 0, 0, 0},
				{0, 0, 0, 7, 5, 2, 2, 6, 2, 7, 4, 0, 0},
				{0, 0, 0, 7, 2, 2, 2, 2, 2, 7, 0, 0, 0},
				{0, 0, 0, 0, 7, 2, 2, 2, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 3, 3, 3, 7, 0, 0, 0, 0},
				{0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
				{0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
				{0, 0, 0, 7, 1, 1, 10,1, 3, 7, 0, 0, 0},
				{0, 0, 0, 0, 7, 1, 1, 1, 7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 7, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 7, 11,1, 11,7, 0, 0, 0, 0},
				{0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
				{0, 0, 0, 8, 1, 1, 1, 1, 3, 7, 0, 0, 0},
				{0, 0, 0, 7, 1, 1, 1, 1, 3, 7, 0, 0, 0},
				{0, 0, 0, 0, 7, 11,1, 11,7, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 7, 8, 7, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 7, 7, 7, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 7, 1, 1, 1, 7, 4, 0, 0, 0},
				{0, 0, 4, 7, 1, 1, 1, 1, 3, 7, 4, 0, 0},
				{0, 0, 4, 7, 1, 1, 1, 1, 3, 7, 4, 0, 0},
				{0, 0, 4, 7, 1, 1, 1, 1, 12,7, 4, 0, 0},
				{0, 0, 0, 4, 7, 1, 1, 1, 7, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 7, 7, 7, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
				{0, 0, 0, 4, 1, 1, 1, 1, 1, 4, 0, 0, 0},
				{0, 0, 0, 4, 1, 1, 1, 1, 1, 4, 0, 0, 0},
				{0, 0, 0, 4, 1, 1, 1, 1, 1, 4, 0, 0, 0},
				{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 4, 1, 1, 1, 4, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 4, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 4, 1, 4, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			},{
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			}
	};
	
	
}
