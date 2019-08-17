package electroblob.wizardry.entity;

import net.minecraft.util.math.Vec3d;

/** This interface allows implementing entity classes to define their own hitbox for wizardry's raytracing and
 * particle collision methods. Typically, entities implementing this interface will return null from the collision
 * bounding box methods in {@code Entity} (there are two for some reason) <b>but</b> return true from
 * {@link net.minecraft.entity.Entity#canBeCollidedWith()} */
public interface ICustomHitbox {

	/**
	 * Calculates the point at which the line starting at the given origin and ending at the given endpoint hits this
	 * entity, if any. Used in raytracing to allow entities to define fully custom behaviour. See
	 * {@link electroblob.wizardry.entity.construct.EntityForcefield} for an example implementation of a spherical hitbox.
	 * @param origin The origin of the line.
	 * @param endpoint The endpoint of the line.
	 * @param fuzziness Maximum distance around the line that should still count as a hit.
	 * @return A {@link Vec3d} representing the point hit, or null if there is no intercept. This should be the first
	 * point that the line hits, i.e. if there is more than one intercept this method should return the one nearest to
	 * the given origin.
	 */
	Vec3d calculateIntercept(Vec3d origin, Vec3d endpoint, float fuzziness);

	/**
	 * Returns whether the given point is inside this entity.
	 * @param point The coordinates to test.
	 * @return True if the point is inside this entity, false if not.
	 */
	boolean contains(Vec3d point);

}
