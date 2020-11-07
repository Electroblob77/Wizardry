package electroblob.wizardry.util;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Contains useful static methods for interacting with blocks and block states. These methods used to be part of
 * {@code WizardryUtilities}.
 * @see GeometryUtils
 * @see EntityUtils
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class BlockUtils {

	private BlockUtils(){} // No instances!

	// General
	// ===============================================================================================================

	/**
	 * Returns a block state for the given block, with all properties set to the values from the given source state.
	 * The source block state must have <b>exactly</b> the same properties as the destination block will allow or an
	 * {@link IllegalArgumentException} will be thrown.
	 * <p></p>
	 * This method allows, for example, an oak door block to be replaced with a spruce one whilst keeping the same
	 * orientation, door half, open/closed state, etc.
	 * @param block The new block type
	 * @param source The block state to copy property values from
	 * @return The resulting block state
	 */
	@SuppressWarnings({"unchecked", "rawtypes"}) // Don't complain to me about Mojang's code design...
	public static IBlockState copyState(Block block, IBlockState source){

		IBlockState state = block.getDefaultState();

		for(IProperty property : source.getPropertyKeys()){
			// It ain't pretty but it works
			state = state.withProperty(property, (Comparable)source.getProperties().get(property));
		}

		return state;
	}

	/**
	 * Returns the actual light level at the given position, taking natural light (skylight) and artificial light
	 * (block light) into account. This uses the same logic as mob spawning.
	 * @param world The world the position is in
	 * @param pos The position to query
	 * @return The light level, from 0 (pitch darkness) to 15 (full daylight/at a torch).
	 */
	public static int getLightLevel(World world, BlockPos pos){

		int i = world.getLightFromNeighbors(pos);

        if(world.isThundering()){
            int j = world.getSkylightSubtracted();
            world.setSkylightSubtracted(10);
            i = world.getLightFromNeighbors(pos);
            world.setSkylightSubtracted(j);
        }

        return i;
	}

	/**
	 * Returns whether the block at the given position can be replaced by another one (works as if a block is being
	 * placed by a player). True for air, vines, tall grass and snow layers but not for flowers, signs etc.
	 * This is a shortcut for <code>world.getBlockState(pos).getMaterial().isReplaceable()</code>, with a few extra bits.
	 *
	 * @param world The world the block is in.
	 * @param pos The position of the block.
	 * @param excludeLiquids True to treat liquids as non-replaceable, false to treat liquids as replaceable.
	 *
	 * @see BlockUtils#canBlockBeReplaced(World, BlockPos)
	 */
	public static boolean canBlockBeReplaced(World world, BlockPos pos, boolean excludeLiquids){
		return (world.isAirBlock(new BlockPos(pos)) || world.getBlockState(pos).getMaterial().isReplaceable())
				&& (!excludeLiquids || !world.getBlockState(pos).getMaterial().isLiquid());
	}

	/**
	 * Returns whether the block at the given position can be replaced by another one (works as if a block is being
	 * placed by a player). True for air, liquids, vines, tall grass and snow layers but not for flowers, signs etc.
	 * This is a shorthand version of {@link BlockUtils#canBlockBeReplaced(World, BlockPos, boolean)};
	 * excludeLiquids defaults to false.
	 *
	 * @param world The world the block is in.
	 * @param pos The position of the block.
	 */
	public static boolean canBlockBeReplaced(World world, BlockPos pos){
		return canBlockBeReplaced(world, pos, false);
	}

	/**
	 * Returns whether the block at the given coordinates is unbreakable in survival mode. In vanilla this is true for
	 * bedrock and end portal frame, for example. This is a shortcut for:<p></p>
	 * {@code world.getBlockState(pos).getBlockHardness(world, pos) == -1.0f}
	 */
	public static boolean isBlockUnbreakable(World world, BlockPos pos){
		return !world.isAirBlock(new BlockPos(pos)) && world.getBlockState(pos).getBlockHardness(world, pos) == -1.0f;
	}

	/**
	 * Gets the blockstate of the block the specified entity is standing on. Uses
	 * {@link MathHelper#floor(double)} because casting to int will not return the correct coordinate when x or z
	 * is negative.
	 */
	public static IBlockState getBlockEntityIsStandingOn(Entity entity){
		BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), (int)entity.posY - 1,
				MathHelper.floor(entity.posZ));
		return entity.world.getBlockState(pos);
	}

	/**
	 * Generates a sphere of block positions centred on the given position, with the given radius. This is an efficient
	 * implementation - rather than simply generating a cube and cutting the rest out, it works as follows:
	 * <p></p>
	 * 1. Step through all x offsets within the specified radius<br>
	 * 2. For each x offset, check the maximum y offset within the radius using Pythagoras<br>
	 * 3. Step through the resulting valid y offsets<br>
	 * 4. For each y offset, check the maximum z offset within the radius using Pythagoras<br>
	 * 5. Step through the resulting valid z offsets
	 * @return A list of BlockPos objects in a sphere. This list will be ordered negative to positive, with axes
	 * nested in order (i.e. blocks in a line on the z axis will be consecutive in the list).
	 */
	public static List<BlockPos> getBlockSphere(BlockPos centre, double radius){

		// Extra efficiency by assigning enough capacity for a cube of side length r
		List<BlockPos> sphere = new ArrayList<>((int)Math.pow(radius, 3));

		for(int i=-(int)radius; i<=radius; i++){

			float r1 = MathHelper.sqrt(radius*radius - i*i);

			for(int j=-(int)r1; j<=r1; j++){

				float r2 = MathHelper.sqrt(radius*radius - i*i - j*j);

				for(int k=-(int)r2; k<=r2; k++){
					sphere.add(centre.add(i, j, k));
				}
			}
		}

		return sphere;
	}

	// Region protection compatibility
	// ===============================================================================================================

	// This is the best I can do to play nicely with world protection mods/plugins like WorldGuard. As before, the
	// playerBlockDamage config option is the ONLY anti-grief feature that is guaranteed to be 100% watertight.

	// For Forge mods, it's not too bad, we can just make sure we trigger the appropriate Forge events (or call other
	// methods that subsequently trigger them). For breaking, there are four events that claim mods might hook into:
	// - PlayerInteractEvent.LeftClickBlock (I actually use this one myself for arcane lock, it's completely seamless)
	// - PlayerEvent.BreakSpeed (not really what it's for, but it is possible to prevent it this way. Can show the first
	// frame of the breaking animation but otherwise seamless. Not fired in creative!)
	// - LivingDestroyBlockEvent (useful for wizards, probably not intended for players)
	// - BlockEvent.BreakEvent (the most obvious one but also the last to be fired. Shows the entire animation before
	// cancelling, looks super ugly)
	// As long as we trigger the last three, it should be fine! Also note that PlayerInteractEvent.LeftClickBlock is fired
	// every tick during breaking (only once in creative).

	// For Bukkit plugins, it's exceedingly difficult! We have to make sure we call the VANILLA methods that will then
	// trigger Bukkit's hooks for plugins to respond to. These are likely to also trigger the relevant Forge events, but
	// to be safe we should trigger everything we possibly can that won't have too many side-effects.
	// Building Gadgets' implementation is a useful resource which I'm pretty sure works with WorldGuard:
	// https://github.com/Direwolf20-MC/BuildingGadgets/blob/1.12.x/src/main/java/com/direwolf20/buildinggadgets/common/items/gadgets/GadgetBuilding.java
	// See also: https://worldguard.enginehub.org/en/latest/regions/scope/#mod-and-plugin-support

	/**
	 * Tests whether the given entity may place the given block at the given location. Does not actually place the block
	 * (this gives callers more flexibility in how they use it, for example it allows different block placement methods
	 * in {@link World} to be used depending on the situation). This should only be called server-side.
	 * <p></p>
	 * <i>Be aware that this method triggers a variety of vanilla methods and Forge events, and depending on other
	 * installed mods these may have side-effects. Use of this method just to query is therefore not advised - if it
	 * returns true, callers should actually place the block to ensure behaviour is consistent for other mods.</i>
	 * <p></p>
	 * <i>Also note that this method does <b>not</b> do blockstate-specific checks (notice it doesn't even take the
	 * block being placed as a parameter; this is in line with the block place events). It is up to the caller to
	 * determine whether the position is suitable for placing whatever kind of block is to be placed.</i>
	 * @param placer The entity doing the placing, or null if the block is not being placed by an entity
	 * @param world The world in which the block is being placed
	 * @param pos The position of the block being placed
	 * @return True if the given entity is allowed to place the block, false otherwise.
	 */
	public static boolean canPlaceBlock(@Nullable Entity placer, World world, BlockPos pos){

		if(world.isRemote){
			Wizardry.logger.warn("BlockUtils#canPlaceBlock called from the client side! Blocks should be modified server-side only");
			return true;
		}

		if(!EntityUtils.canDamageBlocks(placer, world)) return false; // General block damage prevention comes first

		if(world.isOutsideBuildHeight(pos)) return false;
		// This line *should* trigger bukkit plugin hooks
		if(placer instanceof EntityPlayer && !world.isBlockModifiable((EntityPlayer)placer, pos)) return false;

		BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, pos);
		// Despite there being a separate event for players, BOTH events seem to be fired for players during normal placement
		if(ForgeEventFactory.onBlockPlace(placer, snapshot, EnumFacing.UP).isCanceled()) return false;

		if(placer instanceof EntityPlayer && ForgeEventFactory.onPlayerBlockPlace(
				(EntityPlayer)placer, snapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()){
			return false;
		}

		return true;
	}

	/**
	 * Tests whether the given entity may break the block at the given location. Does not actually break the block (this
	 * gives callers more flexibility in how they use it, for example it allows different block break methods in
	 * {@link World} to be used depending on whether particles, sound, drops, etc. are desired). This should only be
	 * called server-side.
	 * <p></p>
	 * This method is a shorthand for {@link BlockUtils#checkBlockBreakXP(Entity, World, BlockPos)} {@code >= 0}.
	 * <p></p>
	 * <i>Be aware that this method triggers a variety of vanilla methods and Forge events, and depending on other
	 * installed mods these may have side-effects. Use of this method just to query is therefore not advised - if it
	 * returns true, callers should actually break the block to ensure behaviour is consistent for other mods.</i>
	 * @param breaker The entity doing the breaking, or null if the block is not being broken by an entity
	 * @param world The world in which the block is being broken
	 * @param pos The position of the block being broken
	 * @return True if the given entity is allowed to break the block, false otherwise.
	 */
	public static boolean canBreakBlock(@Nullable Entity breaker, World world, BlockPos pos){
		return checkBlockBreakXP(breaker, world, pos) >= 0;
	}

	/**
	 * Tests whether the given entity may break the block at the given location and returns the xp it should drop. Does
	 * not actually break the block or drop the xp (this gives callers more flexibility in how they use it, for example
	 * it allows different block break methods in {@link World} to be used depending on whether particles, sound, drops,
	 * etc. are desired). This should only be called server-side.
	 * <p></p>
	 * <i>Be aware that this method triggers a variety of vanilla methods and Forge events, and depending on other
	 * installed mods these may have side-effects. Use of this method just to query is therefore not advised - if it
	 * returns true, callers should actually break the block to ensure behaviour is consistent for other mods.</i>
	 * @param breaker The entity doing the breaking, or null if the block is not being broken by an entity
	 * @param world The world in which the block is being broken
	 * @param pos The position of the block being broken
	 * @return The xp to be dropped if the given entity is allowed to break the block, -1 if the block cannot be broken.
	 */
	public static int checkBlockBreakXP(@Nullable Entity breaker, World world, BlockPos pos){

		if(world.isRemote){
			Wizardry.logger.warn("BlockUtils#checkBlockBreakXP called from the client side! Blocks should be modified server-side only");
			return 0;
		}

		if(!EntityUtils.canDamageBlocks(breaker, world)) return -1; // General block damage prevention comes first

		if(world.isOutsideBuildHeight(pos)) return -1;

		EntityPlayer fakeplayer = FakePlayerFactory.getMinecraft((WorldServer)world);

		if(breaker instanceof EntityPlayer){
			// This line *should* trigger bukkit plugin hooks
			if(!world.isBlockModifiable((EntityPlayer)breaker, pos)) return -1;
		}else if(breaker == null){
			// Also need this for dispensers
			if(!world.isBlockModifiable(fakeplayer, pos)) return -1;
		}

		// I think this is irrelevant in forge because it's only for vanilla entities, but bukkit might use it
		IBlockState state = world.getBlockState(pos);
		if(!state.getBlock().canEntityDestroy(state, world, pos, breaker)) return -1;
		// Although the forge event only needs an EntityLivingBase, it seems it's not supposed to be for players
		if(breaker instanceof EntityLiving && ForgeEventFactory.onEntityDestroyBlock((EntityLivingBase)breaker, pos, state)) return -1;
		// Need to trigger PlayerEvent.BreakSpeed as some claim mods (e.g. LandManager) use it instead of BlockEvent.BreakEvent
		if(breaker instanceof EntityPlayer && ForgeEventFactory.getBreakSpeed((EntityPlayer)breaker, state, 1, pos) < 0) return -1;

		if(breaker == null){ // Dispensers need to trigger something, so use a fake player as per BreakEvent's comment
			if(MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, fakeplayer))){
				return -1;
			}
		}

		int xp = 0;

		if(breaker instanceof EntityPlayerMP){
			xp = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)breaker).interactionManager.getGameType(), (EntityPlayerMP)breaker, pos);
		}

		return xp;
	}

	// Specific blocks
	// ===============================================================================================================

	/**
	 * Returns true if the block at the given position is a tree block (or other 'solid' vegetation, such as cacti).
	 * Used for structure generation.
	 * @param world The world the block is in
	 * @param pos The position of the block to be tested
	 * @return True if the given block is a tree block, false if not.
	 */
	public static boolean isTreeBlock(World world, BlockPos pos){
		Block block = world.getBlockState(pos).getBlock();
		return block instanceof BlockLog || block instanceof BlockCactus
				|| block.isLeaves(world.getBlockState(pos), world, pos) || block.isFoliage(world, pos)
				|| Settings.containsMetaBlock(Wizardry.settings.treeBlocks, world.getBlockState(pos));
	}

	/**
	 * Returns the predominant wood type (see {@link net.minecraft.block.BlockPlanks.EnumType}) for the given biome, or
	 * the default {@link net.minecraft.block.BlockPlanks.EnumType#OAK} for biomes with no trees. This is accurate for
	 * most vanilla biomes
	 * @param biome The biome to query
	 * @return The wood type for the given biome
	 */
	public static BlockPlanks.EnumType getBiomeWoodVariant(Biome biome){
		// Unfortunately, I can't check all the wood types with the biome dictionary
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)) 	return BlockPlanks.EnumType.SPRUCE;
		if(biome == Biomes.BIRCH_FOREST || biome == Biomes.BIRCH_FOREST_HILLS) 	return BlockPlanks.EnumType.BIRCH;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) 		return BlockPlanks.EnumType.JUNGLE;
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SAVANNA)) 		return BlockPlanks.EnumType.ACACIA;
		// Not technically a tree type, but I think it fits quite well anyway
		if(BiomeDictionary.hasType(biome, BiomeDictionary.Type.SPOOKY)) 		return BlockPlanks.EnumType.DARK_OAK;
		// Everything else is oak
		return BlockPlanks.EnumType.OAK;
	}

	/**
	 * Given the position of one half of a double chest, returns the position of the other half of that double chest.
	 * @param world The world in which the chest is located
	 * @param pos The position of one half of the double chest
	 * @return The position of the other half of the double chest, or null if the given position is not part of a
	 * double chest.
	 */
	public static BlockPos getConnectedChest(World world, BlockPos pos){

		Block block = world.getBlockState(pos).getBlock();

		if(block instanceof BlockChest){
			for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL){
				BlockPos pos1 = pos.offset(enumfacing);
				if(world.getBlockState(pos1).getBlock() == block){
					return pos1;
				}
			}
		}

		return null;
	}

	/**
	 * Returns true if the given block is a water source block (specifically, water or flowing water with a level of 0).
	 * @param state The block state to query
	 * @return True if the given block state is a water source block, false otherwise.
	 */
	public static boolean isWaterSource(IBlockState state){
		return state.getMaterial() == Material.WATER && (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.FLOWING_WATER) && state.getValue(BlockLiquid.LEVEL) == 0;
	}

	/**
	 * Returns true if the given block is a lava source block (specifically, lava or flowing lava with a level of 0).
	 * @param state The block state to query
	 * @return True if the given block state is a lava source block, false otherwise.
	 */
	public static boolean isLavaSource(IBlockState state){
		return state.getMaterial() == Material.LAVA && (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.FLOWING_LAVA) && state.getValue(BlockLiquid.LEVEL) == 0;
	}

	/**
	 * Freezes the given block, either by turning water to ice, lava to obsidian/cobblestone or by placing snow on top
	 * of it if possible.
	 * @param world The world the block is in
	 * @param pos The position of the block to freeze
	 * @param freezeLava True to freeze lava into obsidian or cobblestone, false to leave it unchanged
	 * @return True if any blocks were changed, false if not.
	 */
	public static boolean freeze(World world, BlockPos pos, boolean freezeLava){

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();

		if(isWaterSource(state)){
			world.setBlockState(pos, Blocks.ICE.getDefaultState());
		}else if(freezeLava && isLavaSource(state)){
			world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
		}else if(freezeLava && (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)){
			world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
		}else if(block.isReplaceable(world, pos.up()) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos.up())){
			world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState());
		}else{
			return false;
		}

		return true;
	}

	// Floor finding
	// ===============================================================================================================

	/**
	 * Finds the nearest surface (according to the given criteria) in the given direction from the given position,
	 * within the range specified. This is a generalised replacement for the old {@code getNearestFloorLevel} methods;
	 * overloads and predefined surface criteria that replicate the old behaviours are available.
	 *
	 * @param world The world to search in
	 * @param pos The position to search from
	 * @param direction The direction to search in; also defines the direction the surface must face.
	 * @param range The maximum distance from the given y coordinate to search.
	 * @param doubleSided True to also search in the opposite direction, false to only search in the given direction.
	 * @param criteria A {@link SurfaceCriteria} representing the criteria defining a surface. See that class for
	 *                 available predefined criteria.
	 * @return The x, y, or z coordinate of the closest surface, or null if no surface was found. This represents the
	 * exact position of the block boundary that forms the surface (and therefore it may correspond to the coordinate
	 * of the inside or outside block of the surface depending on the direction).
	 */
	@Nullable
	public static Integer getNearestSurface(World world, BlockPos pos, EnumFacing direction, int range,
											boolean doubleSided, SurfaceCriteria criteria){

		// This is a neat trick that allows a default 'not found' return value for integers where all possible integer
		// values could, in theory, be returned. The alternative is to use a double and have NaN as the default, but
		// that would introduce extra casting, and since NaN can be calculated with, it could produce strange results
		// when unaccounted for. Using an Integer means it'll immediately throw an NPE instead.
		Integer surface = null;
		int currentBest = Integer.MAX_VALUE;

		for(int i = doubleSided ? -range : 0; i <= range && i < currentBest; i++){ // Now short-circuits for efficiency

			BlockPos testPos = pos.offset(direction, i);

			if(criteria.test(world, testPos, direction)){
				// Because the loop now short-circuits, this must be closer than the previous surface found
				surface = (int)GeometryUtils.component(GeometryUtils.getFaceCentre(testPos, direction), direction.getAxis());
				currentBest = Math.abs(i);
			}
		}

		return surface;
	}

	/**
	 * Finds the nearest floor level in the given direction from the given position,
	 * within the range specified. This is a shorthand for
	 * {@link BlockUtils#getNearestSurface(World, BlockPos, EnumFacing, int, boolean, SurfaceCriteria)};
	 * {@code doubleSided} defaults to true, {@code direction} defaults to {@code EnumFacing.UP}, and
	 * {@code criteria} defaults to {@link SurfaceCriteria#COLLIDABLE}.
	 */
	@Nullable
	public static Integer getNearestFloor(World world, BlockPos pos, int range){
		return getNearestSurface(world, pos, EnumFacing.UP, range, true, SurfaceCriteria.COLLIDABLE);
	}

	/**
	 * Gets a random position on the ground near the given entity within the specified horizontal and vertical ranges.
	 * Used to find a position to spawn entities in summoning spells.
	 *
	 * @param entity The entity around which to search.
	 * @param horizontalRange The maximum number of blocks on the x or z axis the returned position can be from the
	 *        given entity. <i>The number of operations performed by this method is proportional to the square of this
	 *        parameter, so for performance reasons it is recommended that it does not exceed around 10.</i>
	 * @param verticalRange The maximum number of blocks on the y axis the returned position can be from the given
	 *        entity.
	 * @return A BlockPos with the coordinates of the block directly above the ground at the position found, or null if
	 *         none were found within range. Importantly, since this method checks <i>all possible</i> positions within
	 *         range (i.e. randomness only occurs when deciding between the possible positions), if it returns null once
	 *         then it will always return null given the same circumstances and parameters. What this means is that you
	 *         can (and should) immediately stop trying to cast a summoning spell if this returns null.
	 */
	@Nullable
	public static BlockPos findNearbyFloorSpace(Entity entity, int horizontalRange, int verticalRange){

		World world = entity.world;
		BlockPos origin = new BlockPos(entity);
		return findNearbyFloorSpace(world, origin, horizontalRange, verticalRange);
	}

	/**
	 * Gets a random position on the ground near the given BlockPos within the specified horizontal and vertical ranges.
	 * Used to find a position to spawn entities in summoning spells.
	 *
	 * @param world The world in which to search.
	 * @param origin The BlockPos around which to search.
	 * @param horizontalRange The maximum number of blocks on the x or z axis the returned position can be from the
	 *        given position. <i>The number of operations performed by this method is proportional to the square of this
	 *        parameter, so for performance reasons it is recommended that it does not exceed around 10.</i>
	 * @param verticalRange The maximum number of blocks on the y axis the returned position can be from the given
	 *        position.
	 * @return A BlockPos with the coordinates of the block directly above the ground at the position found, or null if
	 *         none were found within range. Importantly, since this method checks <i>all possible</i> positions within
	 *         range (i.e. randomness only occurs when deciding between the possible positions), if it returns null once
	 *         then it will always return null given the same circumstances and parameters. What this means is that you
	 *         can (and should) immediately stop trying to cast a summoning spell if this returns null.
	 */
	@Nullable
	public static BlockPos findNearbyFloorSpace(World world, BlockPos origin, int horizontalRange, int verticalRange){
		return findNearbyFloorSpace(world, origin, horizontalRange, verticalRange, true);
	}

	/**
	 * Gets a random position on the ground near the given BlockPos within the specified horizontal and vertical ranges.
	 * Used to find a position to spawn entities in summoning spells.
	 *
	 * @param world The world in which to search.
	 * @param origin The BlockPos around which to search.
	 * @param horizontalRange The maximum number of blocks on the x or z axis the returned position can be from the
	 *        given position. <i>The number of operations performed by this method is proportional to the square of this
	 *        parameter, so for performance reasons it is recommended that it does not exceed around 10.</i>
	 * @param verticalRange The maximum number of blocks on the y axis the returned position can be from the given
	 *        position.
	 * @param lineOfSight Whether to require line-of-sight from the origin to the returned position.
	 * @return A BlockPos with the coordinates of the block directly above the ground at the position found, or null if
	 *         none were found within range. Importantly, since this method checks <i>all possible</i> positions within
	 *         range (i.e. randomness only occurs when deciding between the possible positions), if it returns null once
	 *         then it will always return null given the same circumstances and parameters. What this means is that you
	 *         can (and should) immediately stop trying to cast a summoning spell if this returns null.
	 */
	@Nullable
	public static BlockPos findNearbyFloorSpace(World world, BlockPos origin, int horizontalRange, int verticalRange, boolean lineOfSight){

		List<BlockPos> possibleLocations = new ArrayList<>();

		final Vec3d centre = GeometryUtils.getCentre(origin);

		for(int x = -horizontalRange; x <= horizontalRange; x++){
			for(int z = -horizontalRange; z <= horizontalRange; z++){

				Integer y = getNearestFloor(world, origin.add(x, 0, z), verticalRange);

				if(y != null){

					BlockPos location = new BlockPos(origin.getX() + x, y, origin.getZ() + z);

					if(lineOfSight){
						// Since we're only using finding collidable surfaces, it doesn't make much sense to include
						// non-collidable blocks here!
						RayTraceResult rayTrace = world.rayTraceBlocks(centre, GeometryUtils.getCentre(location),
								false, true, false);
						if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) continue;
					}

					possibleLocations.add(location);
				}
			}
		}

		if(possibleLocations.isEmpty()){
			return null;
		}else{
			return possibleLocations.get(world.rand.nextInt(possibleLocations.size()));
		}
	}

	/**
	 * A {@code SurfaceCriteria} object is used to define a 'surface', a boundary between two blocks which differ in
	 * some way, for use in {@link BlockUtils#getNearestSurface(World, BlockPos, EnumFacing, int, boolean, SurfaceCriteria)}.
	 * This provides a more flexible replacement for the old {@code getNearestFloorLevel} methods.<br>
	 * <br>
	 * <i>In the context of this class, 'outside' refers to the side of the surface that is in the supplied direction,
	 * and 'inside' refers to the side which is in the opposite direction. For example, if the direction is {@code UP},
	 * the inside of the surface is defined as below it, and the outside is defined as above it.</i>
	 */
	@FunctionalInterface
	public interface SurfaceCriteria {

		/**
		 * Tests whether the inputs define a valid surface according to this set of criteria.
		 * @param world The world in which the surface is to be tested.
		 * @param pos The block coordinates of the inside ('solid' part) of the surface.
		 * @param side The direction in which the surface must face.
		 * @return True if the side {@code side} of the block at {@code pos} in {@code world} is a valid surface
		 * according to this set of criteria, false otherwise.
		 */
		boolean test(World world, BlockPos pos, EnumFacing side);

		/** Returns a {@code SurfaceCriteria} with the opposite arrangement to this one. */
		default SurfaceCriteria flip(){
			return (world, pos, side) -> this.test(world, pos.offset(side), side.getOpposite());
		}

		/** Returns a {@code SurfaceCriteria} based on the given condition, where the inside of the surface satisfies
		 * the condition and the outside does not. */
		static SurfaceCriteria basedOn(BiPredicate<World, BlockPos> condition){
			return (world, pos, side) -> condition.test(world, pos) && !condition.test(world, pos.offset(side));
		}

		/** Returns a {@code SurfaceCriteria} based on the given condition, where the inside of the surface satisfies
		 * the condition and the outside does not. */
		static SurfaceCriteria basedOn(Predicate<IBlockState> condition){
			return (world, pos, side) -> condition.test(world.getBlockState(pos)) && !condition.test(world.getBlockState(pos.offset(side)));
		}

		/** Surface criterion which defines a surface as the boundary between a block that cannot be moved through and
		 * a block that can be moved through. This means the surface can be stood on. */
		SurfaceCriteria COLLIDABLE = basedOn(b -> b.getMaterial().blocksMovement());

		/** Surface criterion which defines a surface as the boundary between a block that is solid on the required side and
		 * a block that is replaceable. This means the surface can be built on. */
		SurfaceCriteria BUILDABLE = (world, pos, side) -> world.isSideSolid(pos, side) && world.getBlockState(pos.offset(side)).getBlock().isReplaceable(world, pos.offset(side));

		/** Surface criterion which defines a surface as the boundary between a block that is solid on the required side
		 * or a liquid, and an air block. Used for freezing water and placing snow. */
		// Was getNearestFloorLevelB
		SurfaceCriteria SOLID_LIQUID_TO_AIR = (world, pos, side) -> (world.getBlockState(pos).getMaterial().isLiquid()
				|| world.isSideSolid(pos, side) && world.isAirBlock(pos.offset(side)));

		/** Surface criterion which defines a surface as the boundary between any non-air block and an air block.
		 * Used for particles, and is also good for placing fire. */
		// Was getNearestFloorLevelC
		SurfaceCriteria NOT_AIR_TO_AIR = basedOn(World::isAirBlock).flip();

		/** Surface criterion which defines a surface as the boundary between a block that cannot be moved through, and
		 * a block that can be moved through or a tree block (log or leaves). Used for structure generation. */
		SurfaceCriteria COLLIDABLE_IGNORING_TREES = basedOn((world, pos) ->
				world.getBlockState(pos).getMaterial().blocksMovement() && !isTreeBlock(world, pos));

	}

}
