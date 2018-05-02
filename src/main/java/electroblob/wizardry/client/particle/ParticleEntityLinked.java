package electroblob.wizardry.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract superclass for particles that are linked to entities, i.e. move with a given entity. This is generally used
 * for visual effects in spell casting.
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
@SideOnly(Side.CLIENT)
public abstract class ParticleEntityLinked extends Particle {

	/** True if the particle always renders at full brightness. Defaults to false. */
	protected boolean fullBrightness = false;
	
	/** The entity this particle is linked to. The particle will move with this entity. */
	protected Entity entity;

	public ParticleEntityLinked(World world, Entity entity){
		super(world, entity.posX, entity.posY, entity.posZ, entity.motionX, entity.motionY, entity.motionZ);
		this.entity = entity;
		this.init();
	}

	public ParticleEntityLinked(World world, Entity entity, int maxAge){
		this(world, entity);
		this.particleMaxAge = maxAge;
	}

	/**
	 * Called from both constructors to set constants, avoiding duplicate code. Common fields to set here include:
	 * particleScale, particleGravity, canCollide, fullBrightness and setting the texture index.
	 */
	public abstract void init();

	/* There are 4 layers of particles, specified as 0-3 by the method below. - Layer 0 causes the normal particles.png
	 * to be bound to the render engine for normal particles. - Layer 1 causes the block textures to be bound to the
	 * render engine for digging fx and falling fx. - Layer 2 causes the item textures to be bound to the render engine
	 * for tool breaking fx, snowballpoofs, slime particles, etc. - Layer 3 is not used in vanilla minecraft and was
	 * presumably added by forge for exactly this reason. This means no texture is bound by vanilla minecraft, meaning
	 * you are free to do as you wish without possibly overwriting vanilla particles. Mod particles won't be overwritten
	 * anyway since they bind their own textures. It is of course important to bind the texture every time you render a
	 * custom particle, but I don't see how you could do it any other way, since you don't have access to
	 * EffectRenderer. */
	@Override
	public int getFXLayer(){
		// This can only be 0-3 or it will cause an ArrayIndexOutOfBoundsException in EffectRenderer.
		return 3;
	}

	@Override
	public int getBrightnessForRender(float partialTick){
		return fullBrightness ? 15728880 : super.getBrightnessForRender(partialTick);
	}
}
