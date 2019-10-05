package electroblob.wizardry.worldgen;

import com.google.common.math.Quantiles;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.util.WizardryUtilities;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

/** Base structure generation class which handles code common to all wizardry's above-ground structures, such as
 * calculating the median ground level. */
@Mod.EventBusSubscriber
public abstract class WorldGenSurfaceStructure implements IWorldGenerator {

	/** The maximum fraction of the area where a structure is to be spawned that may be covered by liquid. */
	private static final float MAX_LIQUID_FRACTION = 0.4f;

	/** Static map used to store all structure generators for the purpose of advancements. */
	private static final Map<String, WorldGenSurfaceStructure> generators = new HashMap<>();

	/** A random instance used solely for the purpose of emulating the world generation to predict locations. */
	private final Random random;

	private World world;

	private MapGenStructureData structureData;

	/** Stores the bounding boxes of all structures of this type that have been generated so far. */
	protected final Long2ObjectMap<StructureBoundingBox> structureMap = new Long2ObjectOpenHashMap<>(1024);

	public WorldGenSurfaceStructure(){
		random = new Random(); // Seed will be set later
		generators.put(this.getStructureName(), this);
	}

	/** Returns a constant (but unique) long value used to change the random seed so that each generator produces a
	 * different sequence of numbers. Without this, all wizardry's generators attempt to generate in the same chunks
	 * when set to the same rarity. */
	public abstract long getRandomSeedModifier();

	/** Pre-check for whether the structure can generate. Usually this is just used for randomisation so that
	 * calculations are only performed for chunks that will generate a structure; most placement-specific stuff
	 * can just be done using a check inside {@link WorldGenSurfaceStructure#spawnStructure(Random, World, BlockPos, Template, PlacementSettings, ResourceLocation)} */
	public abstract boolean canGenerate(Random random, World world, int chunkX, int chunkZ);

	/** Called each time the structure is generated to get a structure file to use. */
	public abstract ResourceLocation getStructureFile(Random random);

	/** Returns the name of this structure type, which is used as an identifier in the world save file and for
	 * advancement JSON files. */
	public abstract String getStructureName();

	/**
	 * Spawns the structure at the given origin with the given placement settings.
	 * @param random A {@code Random} instance to use for any further parameters that need randomising.
	 * @param world The world to spawn the structure in.
	 * @param origin The origin coordinates of the structure in the world, pre-adjusted for floor height and rotation
	 *               to avoid floating structures and minimise cascading worldgen lag.
	 * @param template The template to be generated.
	 * @param settings The placement settings for the structure.
	 * @param structureFile The location of the chosen structure file, for logging purposes.
	 */
	public abstract void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile);

	/** Specifies valid rotation values for the structure. By default this returns all rotations. */
	public Rotation[] getValidRotations(){
		return Rotation.values();
	}

	/** Specifies valid rotation values for the structure. By default this returns an array of {@code Mirror.LEFT_RIGHT}
	 * and {@code Mirror.NONE}. */
	public Mirror[] getValidMirrors(){
		return new Mirror[]{Mirror.NONE, Mirror.LEFT_RIGHT};
	}

	/**
	 * Finds a random position within the given chunk at which the given template may be generated.
	 * In an effort to make structure rarity more uniform, they now get a number of tries to spawn in each
	 * randomly-selected chunk so they have a better chance of avoiding stuff that might be in the way (cliffs,
	 * villages, lakes, etc.).
	 * <p></p>
	 * This method calculates the median floor height to ensure that sudden changes in level are ignored and the
	 * structure is always spawned at the same level as the majority of the underlying floor. Trees are also ignored
	 * when determining floor level, so that forests don't impede structure spawning.
	 *
	 * @param template The template to be generated
	 * @param settings The placement settings for the structure template
	 * @param random A random instance to use. This should have had its seed set according to the world seed and chunk
	 *               coordinates.
	 * @param world The world in which to spawn the structure
	 * @param chunkX The x-coordinate of the chunk being populated
	 * @param chunkZ The z-coordinate of the chunk being populated
	 * @return The coordinates of the position found, or null if no suitable position was found. The returned
	 * {@code BlockPos} is <b>always</b> the northwest corner of the structure, and the y-coordinate is that of the
	 * uppermost block at those (x, z) coordinates. If the structure is being rotated this needs to be altered using
	 * {@link Template#getZeroPositionWithTransform(BlockPos, Mirror, Rotation)} before it can be fed into the template
	 * spawning methods.
	 */
	@Nullable
	protected BlockPos findValidPosition(Template template, PlacementSettings settings, Random random, World world,
										 int chunkX, int chunkZ){

		// Offset by (8, 8) to minimise cascading worldgen lag
		// See https://www.reddit.com/r/feedthebeast/cowmments/5x0twz/investigating_extreme_worldgen_lag/?ref=share&ref_source=embed&utm_content=title&utm_medium=post_embed&utm_name=c07cbb545f74487793783012794733d8&utm_source=embedly&utm_term=5x0twz
		// Multiplying and left-shifting are identical but it's good practice to bitshift here I guess
		BlockPos origin = new BlockPos(8 + (chunkX << 4) + random.nextInt(16), 0, 8 + (chunkZ << 4) + random.nextInt(16));

		BlockPos size = template.transformedSize(settings.getRotation());
		// Estimate a starting height for searching for the floor
		BlockPos centre = world.getTopSolidOrLiquidBlock(new BlockPos(origin.add(size.getX()/2, 0, size.getZ()/2)));
		Integer startingHeight = WizardryUtilities.getNearestSurface(world, centre, EnumFacing.UP, 32, true,
				WizardryUtilities.SurfaceCriteria.COLLIDABLE_IGNORING_TREES);

		if(startingHeight == null) return null;

		if(Wizardry.settings.fastWorldgen){
			BlockPos result = origin.up(startingHeight);
			// Fast worldgen only checks the central position for water, which isn't perfect but it is faster
			return world.getBlockState(new BlockPos(centre.getX(), startingHeight + 1, centre.getZ())).getMaterial().isLiquid() ? null : result;
		}

		int[] floorHeights = new int[size.getX() * size.getZ()];

		int liquidCount = 0;

		for(int i = 0; i < floorHeights.length; i++){
			// Despite what its name suggests, this method does not return the position of a liquid. It is in fact
			// exactly what is needed here since it is used for placing villages and stuff, and doesn't include leaves
			// or other foliage.
			BlockPos pos = origin.add(i / size.getZ(), 0, i % size.getZ());
			Integer floor = WizardryUtilities.getNearestSurface(world, pos.up(startingHeight), EnumFacing.UP, 32, true,
					WizardryUtilities.SurfaceCriteria.COLLIDABLE_IGNORING_TREES);
			floorHeights[i] = floor == null ? 0 : floor; // Very unlikely that floor is null
			// ^ That method gets the top solid block. Most non-solid blocks are ok to have around the structure,
			// with the exception of liquids, so if there are too many the position is deemed unsuitable.
			if(world.getBlockState(pos.up(floorHeights[i])).getMaterial().isLiquid()) liquidCount++;
			if(liquidCount > floorHeights.length * MAX_LIQUID_FRACTION) return null;
		}

		// Get the median floor height (rather than the mean, that way cliffs should have no effect)
		int medianFloorHeight = MathHelper.floor(Quantiles.median().compute(floorHeights));

		// Now we know the y level of the base of the structure, we can check for stuff in the way
		// A structure is deemed to have stuff in the way if the floor level at any of the (x, z) positions it
		// occupies differs from the base y level by more than its distance from the centre plus a constant.
		// In practical terms, this means structures can't spawn on steep slopes or inside cave mouths or buildings.

		for(int i = 0; i < floorHeights.length; i++){
			int orthogonalDist = Math.max(Math.abs(i / size.getZ() - size.getX()/2), Math.abs(i % size.getZ() - size.getZ()/2));
			if(Math.abs(floorHeights[i] - medianFloorHeight) > Math.max(2, orthogonalDist)) return null; // Something is in the way
		}

		return origin.up(medianFloorHeight - 1);
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider){

		if(!world.getWorldInfo().isMapFeaturesEnabled()) return;

		// Don't need to worry about overflows because they'll just wrap around, which is fine for this purpose
		random.setSeed(random.nextLong() + getRandomSeedModifier());

		initializeStructureData(world); // Load the data from the save file if it isn't already loaded

		if(canGenerate(random, world, chunkX, chunkZ)){

			ResourceLocation structureFile = getStructureFile(random);

			Template template = world.getSaveHandler().getStructureTemplateManager().getTemplate(
					world.getMinecraftServer(), structureFile);

			Rotation[] rotations = getValidRotations();
			Mirror[] mirrors = getValidMirrors();

			PlacementSettings settings = new PlacementSettings()
					.setRotation(rotations[random.nextInt(rotations.length)])
					.setMirror(mirrors[random.nextInt(mirrors.length)]);

			int triesLeft = 10;

			BlockPos origin;

			do {
				origin = findValidPosition(template, settings, random, world, chunkX, chunkZ);
				triesLeft--;
			}while(triesLeft > 0 && origin != null);

			if(origin == null) return;

			// Need to subtract 1 from each coordinate since both corners are inclusive
			StructureBoundingBox box = new StructureBoundingBox(origin, origin.add(template.transformedSize(settings.getRotation())).add(-1, -1, -1));

			// DEBUG
//			world.setBlockState(new BlockPos(box.minX, box.minY, box.minZ), Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA));
//			world.setBlockState(new BlockPos(box.maxX, box.maxY, box.maxZ), Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA));

			if(!Wizardry.settings.fastWorldgen){
				for(WorldGenSurfaceStructure generator : generators.values()){
					StructureBoundingBox otherbox = generator.structureMap.get(ChunkPos.asLong(origin.getX() >> 4, origin.getZ() >> 4));
					if(otherbox != null && otherbox.intersectsWith(box)) return;
				}
			}

			settings.setBoundingBox(box);

			// PlacementSettings rotates and mirrors the structure around the origin, keeping the origin in the same
			// place in the world. This means the structure can be rotated/mirrored into the 8 block border, undoing all
			// our hard work to try and prevent cascading worldgen lag!

			// To properly minimise cascading worldgen lag, the method below returns the position where the corner needs
			// to be such that the original structure's NW (-X, -Z) corner is at the origin.
			origin = template.getZeroPositionWithTransform(origin, settings.getMirror(), settings.getRotation());

			spawnStructure(random, world, origin, template, settings, structureFile);

			if(!Wizardry.settings.fastWorldgen) removeFloatingTrees(world, box, random);

			structureMap.put(ChunkPos.asLong(origin.getX() >> 4, origin.getZ() >> 4), settings.getBoundingBox());

			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("ChunkX", chunkX);
			tag.setInteger("ChunkZ", chunkZ);
			tag.setTag("BB", settings.getBoundingBox().toNBTTagIntArray());
			structureData.writeInstance(tag, chunkX, chunkZ);
			structureData.markDirty();
		}
	}

	/** Copied from MapGenStructure. Unlike most NBT loading, this is lazy - it only gets read from NBT when requested. */
	protected void initializeStructureData(World world){

		// If the world that was last generated is not this world, load the data for the new world
		// This is a bit of a dirty hack, it would be better if we had separate instances per-world but... effort...
		// For now it works, maybe one day I'll improve it
		if(world != this.world){

			this.world = world;

			this.structureData = (MapGenStructureData)world.getPerWorldStorage().getOrLoadData(MapGenStructureData.class, this.getStructureName());

			// This has to be cleared or worlds will interfere with each other!
			// Vanilla doesn't have to do this because each world has a separate ChunkGenerator which stores MapGenBase
			// instances
			this.structureMap.clear();

			if(this.structureData == null){

				this.structureData = new MapGenStructureData(this.getStructureName());
				world.getPerWorldStorage().setData(this.getStructureName(), this.structureData);

			}else{

				NBTTagCompound nbt = this.structureData.getTagCompound();

				for(String s : nbt.getKeySet()){

					NBTBase nbtbase = nbt.getTag(s);

					if(nbtbase.getId() == Constants.NBT.TAG_COMPOUND){

						NBTTagCompound entry = (NBTTagCompound)nbtbase;

						if(entry.hasKey("ChunkX") && entry.hasKey("ChunkZ") && entry.hasKey("BB")){

							int i = entry.getInteger("ChunkX");
							int j = entry.getInteger("ChunkZ");
							int[] coords = entry.getIntArray("BB");

							this.structureMap.put(ChunkPos.asLong(i, j), new StructureBoundingBox(coords));
						}
					}
				}
			}
		}
	}

	/** Finds and removes any floating bits of tree in and above the given structure bounding box. */
	protected static void removeFloatingTrees(World world, StructureBoundingBox boundingBox, Random random){

		boolean changed = true;
		int y = boundingBox.minY;

		// Remove all the logs

		while(changed && y < world.getHeight()){ // I do hope the trees don't reach the world height...

			// Always checks at least the first layer above the bounding box in case the structure cut the rest off
			if(y > boundingBox.maxY + 1) changed = false;

			for(int x = boundingBox.minX; x <= boundingBox.maxX; x++){
				for(int z = boundingBox.minZ; z <= boundingBox.maxZ; z++){

					BlockPos pos = new BlockPos(x, y, z);

					Block block = world.getBlockState(pos).getBlock();
					Block below = world.getBlockState(pos.down()).getBlock();

					if(block instanceof BlockLog){
						if(below != Blocks.GRASS && below != Blocks.DIRT && !WizardryUtilities.isTreeBlock(world, pos.down())){
							world.setBlockToAir(pos);
							changed = true;
						}
					}
				}
			}

			y++;
		}

		// Now update all leaves in the area 16 times to make them decay

		int border = 8;

		List<BlockPos> leaves = new ArrayList<>();

		for(int x = boundingBox.minX - border; x <= boundingBox.maxX + border; x++){
			for(int y1 = boundingBox.minY - border; y1 <= y + border; y1++){
				for(int z = boundingBox.minZ - border; z <= boundingBox.maxZ + border; z++){
					BlockPos pos = new BlockPos(x, y1, z);
					if(world.getBlockState(pos).getBlock() instanceof BlockLeaves) leaves.add(pos);
				}
			}
		}

		for(int i=0; i<16; i++){
			leaves.forEach(p -> world.getBlockState(p).getBlock().updateTick(world, p, world.getBlockState(p), random));
		}

		// Finally, remove all the items that were dropped as a result of leaf decay

		AxisAlignedBB box = new AxisAlignedBB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, y, boundingBox.maxZ).grow(border);

		world.getEntitiesWithinAABB(EntityItem.class, box).forEach(Entity::setDead);

	}

	/** Returns true if the given position is within a structure of this type in the given world, false
	 * otherwise. This will not work on chunks that are yet to be generated; attempting to do so will print a
	 * warning to the console. */
	public boolean isInsideStructure(World world, double x, double y, double z){

		initializeStructureData(world); // Load the data from the save file if it isn't already loaded

		int chunkX = (int)x >> 4;
		int chunkZ = (int)z >> 4;

		if(!world.isChunkGeneratedAt(chunkX, chunkZ)){
			Wizardry.logger.warn("Testing whether position ({}, {}, {}) is inside a structure, but that chunk hasn't been generated yet", x, y, z);
			return false;
		}

		// Vanilla just iterates through the entire structure map, but we can be a little more intelligent
		// about it by only testing the chunks near the player (since all the structures are smaller than 32x32)
		long[] chunks = {ChunkPos.asLong(chunkX - 1, chunkZ - 1), ChunkPos.asLong(chunkX - 1, chunkZ), ChunkPos.asLong(chunkX - 1, chunkZ + 1),
				ChunkPos.asLong(chunkX, chunkZ - 1), ChunkPos.asLong(chunkX, chunkZ), ChunkPos.asLong(chunkX, chunkZ + 1),
				ChunkPos.asLong(chunkX + 1, chunkZ - 1), ChunkPos.asLong(chunkX + 1, chunkZ), ChunkPos.asLong(chunkX + 1, chunkZ + 1)};

		for(long chunkPos : chunks){
			if(structureMap.containsKey(chunkPos) && structureMap.get(chunkPos).isVecInside(new Vec3i(x, y, z)))
				return true;
		}

		return false;
	}

	/** Copied from MapGenMineshaft. The general idea (it seems) is to emulate the world generator's randomisation
	 * without actually placing any blocks. Presumably mineshafts don't need sub-chunk randomisation?  */
	public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored){

		// TODO: We have a problem here, in that the 'pragmatic' placement algorithm (good as it is) requires
		// the chunk to have already been generated, so we can't be sure if a structure actually exists until
		// the chunk is actually generated.

		int j = pos.getX() >> 4;
		int k = pos.getZ() >> 4;

		for (int l = 0; l <= 1000; ++l)
		{
			for (int i1 = -l; i1 <= l; ++i1)
			{
				boolean flag = i1 == -l || i1 == l;

				for (int j1 = -l; j1 <= l; ++j1)
				{
					boolean flag1 = j1 == -l || j1 == l;

					if (flag || flag1)
					{
						// TESTME: Is this the same as Forge's per-chunk seeds? (see caller of generate())
						int k1 = j + i1;
						int l1 = k + j1;
						this.random.setSeed((long)(k1 ^ l1) ^ world.getSeed());
						this.random.nextInt();

						if(this.canGenerate(this.random, world, k1, l1) && (!findUnexplored || !world.isChunkGeneratedAt(k1, l1))){
							return new BlockPos((k1 << 4) + 8, 64, (l1 << 4) + 8);
						}
					}
				}
			}
		}

		return null;
	}

	/** Returns the world generator with the given name. */
	public static WorldGenSurfaceStructure byName(String name){
		return generators.get(name);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.player instanceof EntityPlayerMP && event.player.ticksExisted % 20 == 0){
			WizardryAdvancementTriggers.visit_structure.trigger((EntityPlayerMP)event.player);
		}
	}

}
