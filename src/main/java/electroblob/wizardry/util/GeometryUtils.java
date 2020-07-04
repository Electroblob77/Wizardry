package electroblob.wizardry.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Contains useful static methods for performing geometry operations on vectors, bounding boxes, {@code BlockPos}
 * objects, etc. These methods used to be part of {@code WizardryUtilities}.
 * @see Vec3d
 * @see BlockPos
 * @see EnumFacing
 * @see RelativeFacing
 * @see Location
 * @see RayTracer
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class GeometryUtils {

	private GeometryUtils(){} // No instances!

	/** A global offset used for placing/rendering flat things so that they appear to sit flush with the face of blocks
	 * but do not cause z-fighting at a distance where it is noticeable. */
	// This value is a compromise between flushness and minimum view distance for z-fighting to occur. 0.005 seems to
	// be immune to z-fighting until over a hundred blocks away, which is pretty good, and the distance from flat
	// surfaces is still indistinguishable.
	public static final double ANTI_Z_FIGHTING_OFFSET = 0.005;

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
	 * Returns a {@link Vec3d} of the coordinates at the centre of the given entity's bounding box. This is more
	 * efficient than {@code GeometryUtils.getCentre(entity.getEntityBoundingBox())} as it can use the entity's fields.
	 */
	public static Vec3d getCentre(Entity entity){
		return new Vec3d(entity.posX, entity.posY + entity.height/2, entity.posZ);
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

}
