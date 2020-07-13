package electroblob.wizardry.worldgen;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.util.NBTExtras;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base structure generation class which handles code common to all wizardry's structures. This class was generalised
 * from {@link WorldGenSurfaceStructure} in Wizardry 4.3 to allow underground structures to share much of the code.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
@Mod.EventBusSubscriber
public abstract class WorldGenWizardryStructure implements IWorldGenerator {

	/** Static map used to store all structure generators for the purpose of advancements. */
	private static final Map<String, WorldGenWizardryStructure> generators = new HashMap<>();

	/** A random instance used solely for the purpose of emulating the world generation to predict locations. */
	private final Random random;

	private World world;

	private MapGenStructureData structureData;

	/** Stores the bounding boxes of all structures of this type that have been generated so far. */
	protected final Long2ObjectMap<StructureBoundingBox> structureMap = new Long2ObjectOpenHashMap<>(1024);

	public WorldGenWizardryStructure(){
		random = new Random(); // Seed will be set later
		generators.put(this.getStructureName(), this);
	}

	/** Returns a constant (but unique) long value used to change the random seed so that each generator produces a
	 * different sequence of numbers. Without this, all wizardry's generators attempt to generate in the same chunks
	 * when set to the same rarity. */
	public abstract long getRandomSeedModifier();

	/** Called each time the structure is generated to get a structure file to use. */
	public abstract ResourceLocation getStructureFile(Random random);

	/** Returns the name of this structure type, which is used as an identifier in the world save file and for
	 * advancement JSON files. */
	public abstract String getStructureName();

	/** Specifies valid rotation values for the structure. By default this returns all rotations. */
	public Rotation[] getValidRotations(){
		return Rotation.values();
	}

	/** Specifies valid rotation values for the structure. By default this returns an array of {@code Mirror.LEFT_RIGHT}
	 * and {@code Mirror.NONE}. */
	public Mirror[] getValidMirrors(){
		return new Mirror[]{Mirror.NONE, Mirror.LEFT_RIGHT};
	}

	/** Pre-check for whether the structure can generate. Usually this is just used for randomisation so that
	 * calculations are only performed for chunks that will generate a structure; most placement-specific stuff
	 * can just be done using a check inside {@link WorldGenWizardryStructure#spawnStructure(Random, World, BlockPos, Template, PlacementSettings, ResourceLocation)} */
	public abstract boolean canGenerate(Random random, World world, int chunkX, int chunkZ);

	/**
	 * Picks a random position within the given chunk at which to generate the given template, returning null if that
	 * position is found to be unsuitable. In an effort to make structure rarity more uniform, they now get a number of
	 * attempts to spawn in each randomly-selected chunk so they have a better chance of avoiding stuff that might be in
	 * the way (cliffs, villages, lakes, etc.), or otherwise satisfying spawning conditions. This method performs a
	 * single attempt at finding a suitable position.
	 *
	 * @param template The template to be generated
	 * @param settings The placement settings for the structure template
	 * @param random A random instance to use. This should have had its seed set according to the world seed and chunk
	 *               coordinates.
	 * @param world The world in which to spawn the structure
	 * @param chunkX The x-coordinate of the chunk being populated
	 * @param chunkZ The z-coordinate of the chunk being populated
	 * @param structureFile The name of the structure file, used for error messages.
	 * @return The coordinates of the position found, or null if no suitable position was found. The returned
	 * {@code BlockPos} is <b>always</b> the northwest corner of the structure, and the y-coordinate is that of the
	 * uppermost block at those (x, z) coordinates. If the structure is being rotated this needs to be altered using
	 * {@link Template#getZeroPositionWithTransform(BlockPos, Mirror, Rotation)} before it can be fed into the template
	 * spawning methods.
	 */
	// It may seem more logical for this class to generate a position and then have subclasses only check if it is
	// valid, but different types of structure pick positions differently so that has to be left to subclasses
	@Nullable
	protected abstract BlockPos attemptPosition(Template template, PlacementSettings settings, Random random, World world,
												int chunkX, int chunkZ, String structureFile);

	/**
	 * Spawns the structure at the given origin with the given placement settings. This method should handle template
	 * processors and the actual placing of the blocks in the world.
	 * @param random A {@code Random} instance to use for any further parameters that need randomising
	 * @param world The world to spawn the structure in
	 * @param origin The origin coordinates of the structure in the world, already set to a valid position and adjusted
	 *               so that it is the NW corner (-X, -Z) regardless of rotation/mirror settings
	 * @param template The template to be generated
	 * @param settings The placement settings for the structure
	 * @param structureFile The location of the chosen structure file, for logging purposes
	 */
	public abstract void spawnStructure(Random random, World world, BlockPos origin, Template template, PlacementSettings settings, ResourceLocation structureFile);

	/**
	 * Called after the structure is generated to perform any post-generation extras (optional). For example,
	 * {@link WorldGenSurfaceStructure} uses this to clean up floating trees.
	 * @param random The random number generator that was used to generate the structure
	 * @param world The world in which the structure was generated
	 * @param settings The placement settings that the structure was generated with (including its bounding box)
	 */
	protected void postGenerate(Random random, World world, PlacementSettings settings){}

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

			BlockPos size = template.getSize();

			if(size.getX() == 0 || size.getY() == 0 || size.getZ() == 0){
				Wizardry.logger.warn("Structure template file {} is missing or empty! If you're trying to disable structure spawning, pointing to a non-existent location is NOT the correct way to do so; use the structure dimension lists instead.", structureFile);
			}

			Rotation[] rotations = getValidRotations();
			Mirror[] mirrors = getValidMirrors();

			PlacementSettings settings = new PlacementSettings()
					.setRotation(rotations[random.nextInt(rotations.length)])
					.setMirror(mirrors[random.nextInt(mirrors.length)]);

			int triesLeft = 10;

			BlockPos origin;

			do {
				origin = attemptPosition(template, settings, random, world, chunkX, chunkZ, structureFile.toString());
				triesLeft--;
			}while(triesLeft > 0 && origin == null);

			if(origin == null) return;

			// Need to subtract 1 from each coordinate since both corners are inclusive
			StructureBoundingBox box = new StructureBoundingBox(origin, origin.add(template.transformedSize(settings.getRotation())).add(-1, -1, -1));

			// DEBUG
//			world.setBlockState(new BlockPos(box.minX, box.minY, box.minZ), Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA));
//			world.setBlockState(new BlockPos(box.maxX, box.maxY, box.maxZ), Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA));

			if(!Wizardry.settings.fastWorldgen){
				for(WorldGenWizardryStructure generator : generators.values()){
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
			// TODO: Actually this may not truly minimise cascading, hmmm
			origin = template.getZeroPositionWithTransform(origin, settings.getMirror(), settings.getRotation());

			spawnStructure(random, world, origin, template, settings, structureFile);

			postGenerate(random, world, settings);

			structureMap.put(ChunkPos.asLong(origin.getX() >> 4, origin.getZ() >> 4), settings.getBoundingBox());

			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("ChunkX", chunkX);
			tag.setInteger("ChunkZ", chunkZ);
			NBTExtras.storeTagSafely(tag, "BB", settings.getBoundingBox().toNBTTagIntArray());
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
	public static WorldGenWizardryStructure byName(String name){
		return generators.get(name);
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.player instanceof EntityPlayerMP && event.player.ticksExisted % 20 == 0){
			WizardryAdvancementTriggers.visit_structure.trigger((EntityPlayerMP)event.player);
		}
	}

}
