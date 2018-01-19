package electroblob.wizardry.potion;

import net.minecraft.world.World;

/**
 * Interface for potion effects that spawn custom particles instead of (or as well as) the vanilla 'swirly' particles.
 * @author Electroblob
 * @since Wizardry 1.2
 */
// TODO: Backport.
public interface ICustomPotionParticles {
	
	/**
	 * Called from the event handler to spawn a <b>single</b> custom potion particle. To get an instance of
	 * <code>Random</code> inside this method, use <code>world.rand</code>.
	 * @param world The world to spawn the particle in.
	 * @param x The x coordinate of the particle, already set to a random value within the entity's bounding box.
	 * @param y The y coordinate of the particle, already set to a random value within the entity's bounding box.
	 * @param z The z coordinate of the particle, already set to a random value within the entity's bounding box.
	 */
	void spawnCustomParticle(World world, double x, double y, double z);

}
