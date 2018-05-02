package electroblob.wizardry.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract superclass for all of wizardry's particles. This replaces {@code ParticleCustomTexture} (the functionality of
 * which is no longer necessary since wizardry now uses {@code TextureAtlasSprite}s to do the rendering), and fits into
 * {@code ParticleBuilder} by exposing all the necessary variables through getters, allowing them to be set on the fly
 * rather than needing to be passed into the constructor.
 * <p>
 * The new system is as follows:
 * <p>
 * - All particle classes have a single constructor which takes a world and a position only.<br>
 * - Each particle class defines any relevant default values in its constructor, including velocity.<br>
 * - The particle builder then overwrites any other values that were set during building.
 * <p>
 * This beauty of this system is that there are never any redundant parameters when spawning particles. For example,
 * snow particles nearly always fall at the same speed, which can now be defined in the particle class and no longer
 * needs to be defined when spawning the particle - but importantly, it can still be overridden if desired.
 * 
 * @author Electroblob
 * @since Wizardry 4.2.0
 * @see electroblob.wizardry.util.ParticleBuilder ParticleBuilder
 */
@SideOnly(Side.CLIENT)
public abstract class ParticleWizardry extends Particle {

	/** True if the particle is shaded, false if the particle always renders at full brightness. Defaults to false. */
	protected boolean shaded = false;
	
	protected float fadeRed = 1;
	protected float fadeGreen = 1;
	protected float fadeBlue = 0;

	public ParticleWizardry(World world, double x, double y, double z){
		super(world, x, y, z);
	}
	
	/** Sets whether the particle should render at full brightness or not. True if the particle is shaded, false if
	 * the particle always renders at full brightness. Defaults to false.*/
	public void setShaded(boolean shaded){
		this.shaded = shaded;
	}
	
	/** Sets this particle's gravity. True to enable gravity, false to disable. Defaults to false.*/
	public void setGravity(boolean gravity){
		this.particleGravity = gravity ? 1 : 0;
	}
	
	/** Sets this particle's lifetime in ticks.*/
	public void setLifetime(int lifetime){
		this.particleMaxAge = lifetime;
	}
	
	/**
	 * Sets the velocity of the particle.
	 * @param vx The x velocity
	 * @param vy The y velocity
	 * @param vz The z velocity
	 */
	public void setVelocity(double vx, double vy, double vz){
		this.motionX = vx;
		this.motionY = vy;
		this.motionZ = vz;
	}
	
	/**
	 * Sets the fade colour of the particle.
	 * @param r The red colour component
	 * @param g The green colour component
	 * @param g The blue colour component
	 */
	public void setFadeColour(float r, float g, float b){
		this.fadeRed = r;
		this.fadeGreen = g;
		this.fadeBlue = b;
	}

	@Override
	public int getBrightnessForRender(float partialTick){
		return shaded ? super.getBrightnessForRender(partialTick) : 15728880;
	}

	/** Simple particle factory interface which takes a world and a position and returns a particle. Used (via lambda
	 * expressions) in the client proxy to link particle enum types to actual particle classes. */
	@SideOnly(Side.CLIENT)
	@FunctionalInterface
	public interface IWizardryParticleFactory {
	    ParticleWizardry createParticle(World world, double x, double y, double z);
	}
}
