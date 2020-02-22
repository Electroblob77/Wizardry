package electroblob.wizardry.util;

import electroblob.wizardry.CommonProxy;
import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * <i>"Where do you put random but useful bits and pieces? {@code WizardryUtilities} of course - the 'stuff that doesn't
 * fit anywhere else' class!"</i>
 * <p></p>
 * This class contains some useful static methods for use anywhere - items, entities, spells, events, blocks, etc.
 * Broadly speaking, these fall into the following categories:
 * <p></p>
 * - In-world utilities (position calculating, retrieving entities, etc.)<br>
 * - Raytracing<br>
 * - NBT and data storage utilities<br>
 * - Interaction with the ally designation system<br>
 * - Loot and weighting utilities
 * 
 * @see CommonProxy
 * @see WandHelper
 * @since Wizardry 1.0
 * @author Electroblob
 */
public final class WizardryUtilities {

	/** Constant which is simply an array of the four armour slots. (Could've sworn this exists somewhere in vanilla,
	 * but I can't find it anywhere...) */
	public static final EntityEquipmentSlot[] ARMOUR_SLOTS;
	/** Changed to a constant in wizardry 2.1, since this is a lot more efficient. */
	private static final DataParameter<Boolean> POWERED;
//	/** Pointing item action used in various spells. */
//	public static final EnumAction POINT;

	static {
		// The list of slots needs to be mutable.
		List<EntityEquipmentSlot> slots = new ArrayList<>(
				Arrays.asList(EntityEquipmentSlot.values()));
		slots.removeIf(slot -> slot.getSlotType() != Type.ARMOR);
		ARMOUR_SLOTS = slots.toArray(new EntityEquipmentSlot[0]);

		// Null is passed in deliberately since POWERED is a static field.
		POWERED = ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, null, "field_184714_b");

		//POINT = EnumHelper.addAction("POINT");
	}
	
	/** A global offset used for placing/rendering flat things so that they appear to sit flush with the face of blocks
	 * but do not cause z-fighting at a distance where it is noticeable. */
	// This value is a compromise between flushness and minimum view distance for z-fighting to occur. 0.005 seems to
	// be immune to z-fighting until over a hundred blocks away, which is pretty good, and the distance from flat
	// surfaces is still indistinguishable.
	public static final double ANTI_Z_FIGHTING_OFFSET = 0.005;

	// I'm fed up with remembering these...
	/** Stores constant values for attribute modifier operations (and javadoc for what they actually do!) */
	public static final class Operations {
		/** Adds the attribute modifier amount to the base value. */
		public static final int ADD = 0;
		/** Multiplies the base value by 1 plus the attribute modifier amount. Multiple modifiers are processed in
		 * parallel, i.e. the calculation is based on the base value and does not depend on previous modifiers. */
		public static final int MULTIPLY_FLAT = 1;
		/** Multiplies the base value by 1 plus the attribute modifier amount. Multiple modifiers are processed in
		 * series, i.e. the calculation is based on the value after previous modifiers are applied, in the order added. */
		public static final int MULTIPLY_CUMULATIVE = 2;
	}

	// World, Blocks and Coordinates
	// ===============================================================================================================

	/**
	 * Returns the actual light level, taking natural light (skylight) and artificial light (block light) into account.
	 * This uses the same logic as mob spawning.
	 * 
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
	 * This is a shortcut for <code>world.getBlockState(pos).getMaterial().isReplaceable()</code>.
	 *
	 * @param world The world the block is in.
	 * @param pos The position of the block.
	 * @param excludeLiquids True to treat liquids as non-replaceable, false to treat liquids as replaceable.
	 *
	 * @see WizardryUtilities#canBlockBeReplaced(World, BlockPos)
	 */
	public static boolean canBlockBeReplaced(World world, BlockPos pos, boolean excludeLiquids){
		return (world.isAirBlock(new BlockPos(pos)) || world.getBlockState(pos).getMaterial().isReplaceable())
				&& (!excludeLiquids || !world.getBlockState(pos).getMaterial().isLiquid());
	}

	/**
	 * Returns whether the block at the given position can be replaced by another one (works as if a block is being
	 * placed by a player). True for air, liquids, vines, tall grass and snow layers but not for flowers, signs etc.
	 * This is a shorthand version of {@link WizardryUtilities#canBlockBeReplaced(World, BlockPos, boolean)};
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
	@SuppressWarnings("unchecked") // Don't complain to me about Mojang's code design...
	public static IBlockState copyState(Block block, IBlockState source){

		IBlockState state = block.getDefaultState();

		for(IProperty property : source.getPropertyKeys()){
			// It ain't pretty but it works
			state = state.withProperty(property, (Comparable)source.getProperties().get(property));
		}

		return state;
	}

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
				surface = (int)component(getFaceCentre(testPos, direction), direction.getAxis());
				currentBest = Math.abs(i);
			}
		}

		return surface;
	}

	/**
	 * A {@code SurfaceCriteria} object is used to define a 'surface', a boundary between two blocks which differ in
	 * some way, for use in {@link WizardryUtilities#getNearestSurface(World, BlockPos, EnumFacing, int, boolean, SurfaceCriteria)}.
	 * This provides a more flexible replacement for the (now deprecated) {@code getNearestFloorLevel} methods.<br>
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
		 * Used for particles. */
		// Was getNearestFloorLevelC
		SurfaceCriteria NOT_AIR_TO_AIR = basedOn(World::isAirBlock).flip();

		/** Surface criterion which defines a surface as the boundary between a block that cannot be moved through, and
		 * a block that can be moved through or a tree block (log or leaves). Used for structure generation. */
		SurfaceCriteria COLLIDABLE_IGNORING_TREES = basedOn((world, pos) ->
				world.getBlockState(pos).getMaterial().blocksMovement() && !isTreeBlock(world, pos));

	}

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
	 * Finds the nearest floor level in the given direction from the given position,
	 * within the range specified. This is a shorthand for
	 * {@link WizardryUtilities#getNearestSurface(World, BlockPos, EnumFacing, int, boolean, SurfaceCriteria)};
	 * {@code doubleSided} defaults to true, {@code direction} defaults to {@code EnumFacing.UP}, and
	 * {@code criteria} defaults to {@link SurfaceCriteria#COLLIDABLE}.
	 */
	@Nullable
	public static Integer getNearestFloor(World world, BlockPos pos, int range){
		return getNearestSurface(world, pos, EnumFacing.UP, range, true, SurfaceCriteria.COLLIDABLE);
	}

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * As of Wizardry 4.2, this is now a wrapper for {@link WizardryUtilities#getNearestFloor(World, BlockPos, int)}
	 * which retains the old functionality (i.e. returning an {@code int}, with -1 as 'not found') for compatibility.
	 *
	 * @param world The world to search in
	 * @param pos The coordinates to search from
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the
	 *         floor as would be seen in the debug screen when the player is standing on it.
	 * @deprecated Use {@link WizardryUtilities#getNearestFloor(World, BlockPos, int)}; this method may be removed in
	 * future.
	 */
	// Since this is always a y-coordinate, the 'not found' value can just be any negative number.
	@Deprecated
	public static int getNearestFloorLevel(World world, BlockPos pos, int range){
		Integer floor = getNearestFloor(world, pos, range);
		return floor == null ? -1 : floor;
	}

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords. Only
	 * works if the block above the floor is actually air and the floor is solid or a liquid.
	 * 
	 * @param world The world to search in
	 * @param pos The coordinates to search from
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the
	 *         floor as would be seen in the debug screen when the player is standing on it.
	 * @deprecated Use {@link WizardryUtilities#getNearestSurface(World, BlockPos, EnumFacing, int, boolean, SurfaceCriteria)};
	 * this method may be removed in future.
	 */
	@Deprecated
	public static int getNearestFloorLevelB(World world, BlockPos pos, int range){
		Integer floor = getNearestSurface(world, pos, EnumFacing.UP, range, true, SurfaceCriteria.SOLID_LIQUID_TO_AIR);
		return floor == null ? -1 : floor;
	}

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * Everything that is not air is treated as floor, even stuff that can't be walked on.
	 *
	 * @param world The world to search in
	 * @param pos The coordinates to search from
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the
	 *         floor as would be seen in the debug screen when the player is standing on it.
	 * @deprecated Use {@link WizardryUtilities#getNearestSurface(World, BlockPos, EnumFacing, int, boolean, SurfaceCriteria)};
	 * this method may be removed in future.
	 */
	@Deprecated
	public static int getNearestFloorLevelC(World world, BlockPos pos, int range){
		Integer floor = getNearestSurface(world, pos, EnumFacing.UP, range, true, SurfaceCriteria.NOT_AIR_TO_AIR);
		return floor == null ? -1 : floor;
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
		
		List<BlockPos> possibleLocations = new ArrayList<BlockPos>();

		for(int x = -horizontalRange; x <= horizontalRange; x++){
			for(int z = -horizontalRange; z <= horizontalRange; z++){
				Integer y = WizardryUtilities.getNearestFloor(world, origin.add(x, 0, z), verticalRange);
				if(y != null) possibleLocations.add(new BlockPos(origin.getX() + x, y, origin.getZ() + z));
			}
		}

		if(possibleLocations.isEmpty()){
			return null;
		}else{
			return possibleLocations.get(world.rand.nextInt(possibleLocations.size()));
		}
	}

	/**
	 * Gets the blockstate of the block the specified entity is standing on. Uses
	 * {@link MathHelper#floor(double)} because casting to int will not return the correct coordinate when x or z
	 * is negative.
	 */
	public static IBlockState getBlockEntityIsStandingOn(Entity entity){
		BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), (int)entity.getEntityBoundingBox().minY - 1,
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

	/**
	 * Shorthand for {@link WizardryUtilities#getEntitiesWithinRadius(double, double, double, double, World, Class)}
	 * with EntityLivingBase as the entity type. This is by far the most common use for that method.
	 * 
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 */
	public static List<EntityLivingBase> getEntitiesWithinRadius(double radius, double x, double y, double z,
			World world){
		return getEntitiesWithinRadius(radius, x, y, z, world, EntityLivingBase.class);
	}

	/**
	 * Returns all entities of the specified type within the specified radius of the given coordinates. This is
	 * different to using a raw AABB because a raw AABB will search in a cube volume rather than a sphere. Note that
	 * this does not exclude any entities; if any specific entities are to be excluded this must be checked when
	 * iterating through the list.
	 * 
	 * @see WizardryUtilities#getEntitiesWithinRadius(double, double, double, double, World)
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 * @param entityType The class of entity to search for; pass in Entity.class for all entities
	 */
	public static <T extends Entity> List<T> getEntitiesWithinRadius(double radius, double x, double y, double z,
			World world, Class<T> entityType){
		AxisAlignedBB aabb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
		List<T> entityList = world.getEntitiesWithinAABB(entityType, aabb);
		for(int i = 0; i < entityList.size(); i++){
			if(entityList.get(i).getDistance(x, y, z) > radius){
				entityList.remove(i);
				break;
			}
		}
		return entityList;
	}

	// Why is there a distanceSqToCentre method in Vec3i but not a getCentre method?

	/**
	 * Returns a {@link Vec3d} of the coordinates at the centre of the given block position (i.e. the block coordinates
	 * plus 0.5 in x, y, and z).
	 */
	public static Vec3d getCentre(BlockPos pos){
		return new Vec3d(pos).add(0.5, 0.5, 0.5);
	}

	/**
	 * Returns a {@link Vec3d} of the coordinates at the centre of the given bounding box (The one in {@code AxisAlignedBB}
	 * itself is client-side only).
	 */
	public static Vec3d getCentre(AxisAlignedBB box){
		return new Vec3d(box.minX + (box.maxX - box.minX) * 0.5, box.minY + (box.maxY - box.minY) * 0.5, box.minZ + (box.maxZ - box.minZ) * 0.5);
	}

	/**
	 * Returns a {@link Vec3d} of the coordinates at the centre of the given face of the given block position (i.e. the
	 * centre of the block plus 0.5 in the given direction).
	 */
    public static Vec3d getFaceCentre(BlockPos pos, EnumFacing face){
		return getCentre(pos).add(new Vec3d(face.getDirectionVec()).scale(0.5));
	}

	/**
	 * Returns the component of the given {@link Vec3d} corresponding to the given {@link Axis Axis}.
	 */
	public static double component(Vec3d vec, Axis axis){
    	return new double[]{vec.x, vec.y, vec.z}[axis.ordinal()]; // Damn, that's compact.
	}

	/**
	 * Returns the component of the given {@link Vec3i} corresponding to the given {@link Axis Axis}.
	 */
	public static int component(Vec3i vec, Axis axis){
		return new int[]{vec.getX(), vec.getY(), vec.getZ()}[axis.ordinal()];
	}

	/**
	 * Returns a new {@link Vec3d} with the component corresponding to the given {@link Axis Axis} replaced by the
	 * given value.
	 */
	public static Vec3d replaceComponent(Vec3d vec, Axis axis, double newValue){
		double[] components = {vec.x, vec.y, vec.z};
		components[axis.ordinal()] = newValue;
		return new Vec3d(components[0], components[1], components[2]);
	}

	/**
	 * Returns a new {@link Vec3i} with the component corresponding to the given {@link Axis Axis} replaced by the
	 * given value.
	 */
	public static Vec3i replaceComponent(Vec3i vec, Axis axis, int newValue){
		int[] components = {vec.getX(), vec.getY(), vec.getZ()};
		components[axis.ordinal()] = newValue;
		return new Vec3i(components[0], components[1], components[2]);
	}

	/**
	 * Returns an array of {@code Vec3d} objects representing the vertices of the given bounding box.
	 * @param box The bounding box whose vertices are to be returned.
	 * @return The list of vertices, which will contain 8 elements. Using EnumFacing initials, the order is:
	 * DNW, DNE, DSE, DSW, UNW, UNE, USE, USW. The returned coordinates are absolute (i.e. measured from the world origin).
	 */
	public static Vec3d[] getVertices(AxisAlignedBB box){
		return new Vec3d[]{
				new Vec3d(box.minX, box.minY, box.minZ),
				new Vec3d(box.maxX, box.minY, box.minZ),
				new Vec3d(box.maxX, box.minY, box.maxZ),
				new Vec3d(box.minX, box.minY, box.maxZ),
				new Vec3d(box.minX, box.maxY, box.minZ),
				new Vec3d(box.maxX, box.maxY, box.minZ),
				new Vec3d(box.maxX, box.maxY, box.maxZ),
				new Vec3d(box.minX, box.maxY, box.maxZ)
		};
	}

	/**
	 * Returns an array of {@code Vec3d} objects representing the vertices of the block at the given position.
	 * @param pos The position of the block whose vertices are to be returned.
	 * @return The list of vertices, which will contain 8 elements. Using EnumFacing initials, the order is:
	 * DNW, DNE, DSE, DSW, UNW, UNE, USE, USW. The returned coordinates are absolute (i.e. measured from the world origin).
	 */
	public static Vec3d[] getVertices(World world, BlockPos pos){
		return getVertices(world.getBlockState(pos).getBoundingBox(world, pos).offset(pos.getX(), pos.getY(), pos.getZ()));
	}

	/**
	 * Returns the pitch angle in degrees of the given {@link EnumFacing}. For some reason {@code EnumFacing} has a get
	 * yaw method ({@link EnumFacing#getHorizontalAngle()}) but not a get pitch method.
	 */
	public static float getPitch(EnumFacing facing){
		return facing == EnumFacing.UP ? 90 : facing == EnumFacing.DOWN ? -90 : 0;
	}

	/**
	 * Gets an entity from its UUID. If the UUID is known to belong to an {@code EntityPlayer}, use the more efficient
	 * {@link World#getPlayerEntityByUUID(UUID)} instead.
	 * 
	 * @param world The world the entity is in
	 * @param id The entity's UUID
	 * @return The Entity that has the given UUID, or null if no such entity exists in the specified world.
	 */
	@Nullable
	public static Entity getEntityByUUID(World world, @Nullable UUID id){

		if(id == null) return null; // It would return null eventually but there's no point even looking

		for(Entity entity : world.loadedEntityList){
			// This is a perfect example of where you need to use .equals() and not ==. For most applications,
			// this was unnoticeable until world reload because the UUID instance or entity instance is stored.
			// Fixed now though.
			if(entity.getUniqueID().equals(id)){
				return entity;
			}
		}
		return null;
	}

	// No point allowing anything other than players for these methods since other entities can use Entity#playSound.

	/**
	 * Shortcut for
	 * {@link World#playSound(EntityPlayer, double, double, double, SoundEvent, SoundCategory, float, float)} where the
	 * player is null but the x, y and z coordinates are those of the passed in player. Use in preference to
	 * {@link EntityPlayer#playSound(SoundEvent, float, float)} if there are client-server discrepancies.
	 */
	public static void playSoundAtPlayer(EntityPlayer player, SoundEvent sound, SoundCategory category, float volume,
			float pitch){
		player.world.playSound(null, player.posX, player.posY, player.posZ, sound, category, volume, pitch);
	}

	/**
	 * See {@link WizardryUtilities#playSoundAtPlayer(EntityPlayer, SoundEvent, SoundCategory, float, float)}. Category
	 * defaults to {@link SoundCategory#PLAYERS}.
	 */
	public static void playSoundAtPlayer(EntityPlayer player, SoundEvent sound, float volume, float pitch){
		player.world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, volume, pitch);
	}

	// Players and Mobs
	// ===============================================================================================================

	/**
	 * Returns the entity riding the given entity, or null if there is none. Allows for neater code now that entities
	 * have a list of passengers, because it is necessary to check that the list is not empty first.
	 */
	@Nullable
	public static Entity getRider(Entity entity){
		return !entity.getPassengers().isEmpty() ? entity.getPassengers().get(0) : null;
	}

	/**
	 * Attacks the given entity with the given damage source and amount, but preserving the entity's original velocity
	 * instead of applying knockback, as would happen with
	 * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)} <i>(More accurately, calls that method as normal
	 * and then resets the entity's velocity to what it was before).</i> Handy for when you need to damage an entity
	 * repeatedly in a short space of time.
	 * 
	 * @param entity The entity to attack
	 * @param source The source of the damage
	 * @param amount The amount of damage to apply
	 * @return True if the attack succeeded, false if not.
	 */
	public static boolean attackEntityWithoutKnockback(Entity entity, DamageSource source, float amount){
		double vx = entity.motionX;
		double vy = entity.motionY;
		double vz = entity.motionZ;
		boolean succeeded = entity.attackEntityFrom(source, amount);
		entity.motionX = vx;
		entity.motionY = vy;
		entity.motionZ = vz;
		return succeeded;
	}

	/**
	 * Applies the standard (non-enchanted) amount of knockback to the given target, using the same calculation as
	 * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}. Use in conjunction with
	 * {@link WizardryUtilities#attackEntityWithoutKnockback(Entity, DamageSource, float)} to change the source of
	 * knockback for an attack.
	 * 
	 * @param attacker The entity that caused the knockback; the target will be pushed away from this entity.
	 * @param target The entity to be knocked back.
	 */
	public static void applyStandardKnockback(Entity attacker, EntityLivingBase target){
		double dx = attacker.posX - target.posX;
		double dz;
		for(dz = attacker.posZ - target.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random())
				* 0.01D){
			dx = (Math.random() - Math.random()) * 0.01D;
		}
		target.knockBack(attacker, 0.4f, dx, dz);
	}
	
	/**
	 * Undoes 1 tick's worth of velocity change due to gravity for the given entity. If the entity has no gravity,
	 * this method does nothing. This method is intended to be used in situations where entity gravity needs to be
	 * turned on and off and it is not practical to use {@link Entity#setNoGravity(boolean)}, usually if there is no
	 * easy way to get a reference to the entity to turn gravity back on.
	 * 
	 * @param entity The entity to undo gravity for.
	 */
	public static void undoGravity(Entity entity){
		if(!entity.hasNoGravity()){
			double gravity = 0.04;
			if(entity instanceof EntityThrowable) gravity = 0.03;
			else if(entity instanceof EntityArrow) gravity = 0.05;
			else if(entity instanceof EntityLivingBase) gravity = 0.08;
			entity.motionY += gravity;
		}
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar. Defined here for convenience and to centralise the
	 * (unfortunately unavoidable) use of hardcoded numbers to reference the inventory slots. The returned list is a
	 * modifiable copy of part of the player's inventory stack list; as such, changes to the list are <b>not</b> written
	 * through to the player's inventory. However, the ItemStack instances themselves are not copied, so changes to any
	 * of their fields (size, metadata...) will change those in the player's inventory.
	 * 
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getHotbar(EntityPlayer player){
		NonNullList<ItemStack> hotbar = NonNullList.create();
		hotbar.addAll(player.inventory.mainInventory.subList(0, 9));
		return hotbar;
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar and offhand, sorted into the following order: main
	 * hand, offhand, rest of hotbar left-to-right. The returned list is a modifiable shallow copy of part of the player's
	 * inventory stack list; as such, changes to the list are <b>not</b> written through to the player's inventory.
	 * However, the ItemStack instances themselves are not copied, so changes to any of their fields (size, metadata...)
	 * will change those in the player's inventory.
	 * 
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getPrioritisedHotbarAndOffhand(EntityPlayer player){
		List<ItemStack> hotbar = WizardryUtilities.getHotbar(player);
		// Adds the offhand item to the beginning of the list so it is processed before the hotbar
		hotbar.add(0, player.getHeldItemOffhand());
		// Moves the item in the main hand to the beginning of the list so it is processed first
		hotbar.remove(player.getHeldItemMainhand());
		hotbar.add(0, player.getHeldItemMainhand());
		return hotbar;
	}

	/**
	 * Tests whether the specified player has any of the specified item in their entire inventory, including armour
	 * slots and offhand.
	 */
	public static boolean doesPlayerHaveItem(EntityPlayer player, Item item){

		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.armorInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.offHandInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given player is opped on the given server. If the server is a singleplayer or LAN server, this
	 * means they have cheats enabled.
	 */
	public static boolean isPlayerOp(EntityPlayer player, MinecraftServer server){
		return server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()) != null;
	}

	/** Checks that the given entity is allowed to damage blocks in the given world. If the entity is a player, this
	 * checks the player block damage config setting, otherwise it posts a mob griefing event and returns the result. */
	public static boolean canDamageBlocks(EntityLivingBase entity, World world){
		if(entity instanceof EntityPlayer) return Wizardry.settings.playerBlockDamage;
		return ForgeEventFactory.getMobGriefingEvent(world, entity);
	}

	/** Returns the default aiming arror used by skeletons for the given difficulty. For reference, these are: Easy - 10,
	 * Normal - 6, Hard - 2, Peaceful - 10 (rarely used). */
	public static int getDefaultAimingError(EnumDifficulty difficulty){
		switch(difficulty){
		case EASY: return 10;
		case NORMAL: return 6;
		case HARD: return 2;
		default: return 10; // Peaceful counts as easy; the only time this is used is when a player attacks a (good) wizard.
		}
	}
	
	/**
	 * Returns true if the given entity is an EntityLivingBase and not an armour stand; makes the code a bit neater.
	 * This was added because armour stands are a subclass of EntityLivingBase, but shouldn't necessarily be treated
	 * as living entities - this depends on the situation. <i>The given entity can safely be cast to EntityLivingBase
	 * if this method returns true.</i>
	 */
	// In my opinion, it's a bad design choice to have armour stands extend EntityLivingBase directly - it would be
	// better to make a parent class which is extended by both armour stands and EntityLivingBase and contains only
	// the code required by both.
	public static boolean isLiving(Entity entity){
		return entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
	}

	/**
	 * Turns the given creeper into a charged creeper. In 1.10, this requires reflection since the DataManager keys are
	 * private. (You <i>could</i> call {@link EntityCreeper#onStruckByLightning(EntityLightningBolt)} and then heal it
	 * and extinguish it, but that's a bit awkward, and it'll trigger events and stuff...)
	 */
	// The reflection here only gets done once to initialise the POWERED field, so it's not a performance issue at all.
	public static void chargeCreeper(EntityCreeper creeper){
		creeper.getDataManager().set(POWERED, true);
	}
	
	/**
	 * Returns true if the given caster is currently casting the given spell by any means. This method is intended to
	 * eliminate the long and cumbersome wand use checking in event handlers, which often missed out spells cast by
	 * means other than wands.
	 * @param caster The potential spell caster, which may be a player or an {@link ISpellCaster}. Any other entity will
	 * cause this method to always return false.
	 * @param spell The spell to check for. The spell must be continuous or this method will always return false.
	 * @return True if the caster is currently casting the given spell through any means, false otherwise.
	 */
	// The reason this is a boolean check is that actually returning a spell presents a problem: players can cast two
	// continuous spells at once, one via commands and one via an item, so which do you choose? Since the main point was
	// to check for specific spells, it seems more useful to do it this way.
	public static boolean isCasting(EntityLivingBase caster, Spell spell){
		
		if(!spell.isContinuous) return false;
		
		if(caster instanceof EntityPlayer){
			
			WizardData data = WizardData.get((EntityPlayer)caster);
			
			if(data != null && data.currentlyCasting() == spell) return true;

			if(caster.isHandActive()){

				ItemStack stack = caster.getHeldItem(caster.getActiveHand());

				if(stack.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)stack.getItem()).getCurrentSpell(stack) == spell){
					// Don't do this, it interferes with stuff! We effectively already tested this with caster.isHandActive() anyway
//						&& ((ISpellCastingItem)stack.getItem()).canCast(stack, spell, (EntityPlayer)caster,
//						EnumHand.MAIN_HAND, 0, new SpellModifiers())){
					return true;
				}
			}

		}else if(caster instanceof ISpellCaster){
			if(((ISpellCaster)caster).getContinuousSpell() == spell) return true;
		}
		
		return false;
	}

	/**
	 * Returns whether the given {@link DamageSource} is melee damage. This method makes a best guess as to whether
	 * the damage was from a melee attack; there is no way of testing this properly.
	 * @param source The damage source to be tested.
	 * @return True if the given damage source is melee damage, false otherwise.
	 */
	public static boolean isMeleeDamage(DamageSource source){

		// With the exception of minions, melee damage always has the same entity for immediate/true source
		if(!(source instanceof MinionDamage) && source.getImmediateSource() != source.getTrueSource()) return false;
		if(source.isProjectile()) return false; // Projectile damage obviously isn't melee damage
		if(source.isUnblockable()) return false; // Melee damage should always be blockable
		if(!(source instanceof MinionDamage) && source instanceof IElementalDamage) return false;
		if(!(source.getTrueSource() instanceof EntityLivingBase)) return false; // Only living things can melee!

		if(source.getTrueSource() instanceof EntityPlayer && source.getDamageLocation() != null
				&& source.getDamageLocation().distanceTo(source.getTrueSource().getPositionVector()) > ((EntityLivingBase)source
				.getTrueSource()).getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()){
			return false; // Out of melee reach for players
		}

		// If it got through all that, chances are it's melee damage
		return true;
	}

	// Miscellaneous
	// ===============================================================================================================

	/**
	 * Verifies that the given string is a valid string representation of a UUID. More specifically, returns true if and
	 * only if the given string is not null and matches the regular expression:
	 * <p></p>
	 * <center><code>/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/<p></code></center>
	 * which is the regex equivalent of the standard string representation of a UUID as described in
	 * {@link UUID#toString()}. This method is intended to be used as a check to prevent an
	 * {@link IllegalArgumentException} from occurring when calling {@link UUID#fromString(String)}.
	 * 
	 * @param string The string to be checked
	 * @return Whether the given string is a valid string representation of a UUID
	 * @deprecated UUIDs can now be stored in NBT directly; use that in preference to storing them as strings.
	 */
	@Deprecated
	public static boolean verifyUUIDString(String string){
		return string != null
				&& string.matches("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/");
	}

	/**
	 * Flattens the given nested collection. The returned collection is an unmodifiable collection of all the elements
	 * contained within all of the sub-collections of the given nested collection.
	 * @param collection A nested collection to flatten
	 * @param <E> The type of elements in the given nested collection
	 * @return The resulting flattened collection.
	 */
	public static <E> Collection<E> flatten(Collection<? extends Collection<E>> collection){
		Collection<E> result = new ArrayList<>();
		collection.forEach(result::addAll);
		return Collections.unmodifiableCollection(result);
	}

	// Neat way of getting a random element from a set, wasn't needed in the end but kept here for future reference
//	public static <E> E randomElement(Collection<E> collection, Random random){
//		if(collection.isEmpty()) throw new IndexOutOfBoundsException("The given collection must not be empty");
//		Iterator<E> iterator = collection.iterator();
//		for(int n = random.nextInt(collection.size()); n > 0; n--) iterator.next();
//		return iterator.next();
//	}

}
