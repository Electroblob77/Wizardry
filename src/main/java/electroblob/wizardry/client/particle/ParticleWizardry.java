package electroblob.wizardry.client.particle;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract superclass for all of wizardry's particles. This replaces {@code ParticleCustomTexture} (the functionality of
 * which is no longer necessary since wizardry now uses {@code TextureAtlasSprite}s to do the rendering), and fits into
 * {@code ParticleBuilder} by exposing all the necessary variables through getters, allowing them to be set on the fly
 * rather than needing to be passed into the  constructor.
 * <p>
 * The new system is as follows:
 * <p>
 * - All particle classes have a single constructor which takes a world and a position only.<br>
 * - Each particle class defines any relevant default values in its constructor, including velocity.<br>
 * - The particle builder then overwrites any other values that were set during building.
 * <p>
 * This beauty of this system is that there are never any redundant parameters when spawning particles, since you can set
 * as many or as few parameters as necessary - and in addition, common defaults don't need setting at all. For example,
 * snow particles nearly always fall at the same speed, which can now be defined in the particle class and no longer
 * needs to be defined when spawning the particle - but importantly, it can still be overridden if desired.
 * 
 * @author Electroblob
 * @since Wizardry 4.2.0 
 * @see electroblob.wizardry.util.ParticleBuilder ParticleBuilder
 */
@SideOnly(Side.CLIENT)
public abstract class ParticleWizardry extends Particle {

	/** Implementation of animated particles using the TextureAtlasSprite system. Why vanilla doesn't support this I
	 * don't know, considering it too has animated particles. */
	protected final TextureAtlasSprite[] sprites;
	
	/** True if the particle is shaded, false if the particle always renders at full brightness. Defaults to false. */
	protected boolean shaded = false;

	protected float initialRed;
	protected float initialGreen;
	protected float initialBlue;
	
	protected float fadeRed = 0;
	protected float fadeGreen = 0;
	protected float fadeBlue = 0;
	
	protected float angle;
	protected double radius = 0;
	protected double speed = 0;
	
	/** The entity this particle is linked to. The particle will move with this entity. */
	@Nullable
	protected Entity entity = null;
	/** Coordinates of this particle relative to the linked entity. If the linked entity is null, these are not used. */
	protected double relativeX, relativeY, relativeZ;
	/** Velocity of this particle relative to the linked entity. The relative x and z velocities are also used when
	 * the particle has spin to move the centre of rotation. If the linked entity is null and the particle is not
	 * spinning, these are not used and will be {@code NaN}. */
	protected double relativeMotionX = Double.NaN, relativeMotionY = Double.NaN, relativeMotionZ = Double.NaN;
	// Note that roll (equivalent to rotating the texture) is effectively handled by particleAngle - although that is
	// actually the rotation speed and not the angle itself.
	/** The yaw angle this particle is facing, or {@code NaN} if this particle always faces the viewer (default behaviour). */
	protected float yaw = Float.NaN;
	/** The pitch angle this particle is facing, or {@code NaN} if this particle always faces the viewer (default behaviour). */
	protected float pitch = Float.NaN;

	/**
	 * Creates a new particle in the given world at the given position. All other parameters are set via the various
	 * setter methods ({@link electroblob.wizardry.util.ParticleBuilder ParticleBuilder} deals with all of that anyway). 
	 * @param world The world in which to create the particle.
	 * @param x The x-coordinate at which to create the particle.
	 * @param y The y-coordinate at which to create the particle.
	 * @param z The z-coordinate at which to create the particle.
	 * @param textures One or more {@code ResourceLocation}s representing the texture(s) used by this particle. These
	 * <b>must</b> be registered as {@link TextureAtlasSprite}s using {@link TextureStitchEvent} or the textures will be
	 * missing. If more than one {@code ResourceLocation} is specified, the particle will be animated with each texture
	 * shown in order for an equal proportion of the particle's lifetime. If this argument is omitted (or a zero-length
	 * array is given), the particle will use the vanilla system instead (based on the X/Y texture indices).
	 */
	public ParticleWizardry(World world, double x, double y, double z, ResourceLocation... textures){
		
		super(world, x, y, z);
		
		// Sets the relative coordinates in case they are needed
		this.relativeX = x;
		this.relativeY = y;
		this.relativeZ = z;
		
		// Deals with the textures
		if(textures.length > 0){
			
			sprites = Arrays.stream(textures).map(t -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
					t.toString())).collect(Collectors.toList()).toArray(new TextureAtlasSprite[0]);
			
			this.setParticleTexture(sprites[0]);
					
		}else{
			
			sprites = new TextureAtlasSprite[0];
		}
	}
	
	// ============================================== Parameter Setters ==============================================
	
	// Setters for parameters that affect all particles - these are implemented in this class (although they may be
	// reimplemented in subclasses)
	
	/** Sets whether the particle should render at full brightness or not. True if the particle is shaded, false if
	 * the particle always renders at full brightness. Defaults to false.*/
	public void setShaded(boolean shaded){
		this.shaded = shaded;
	}
	
	/** Sets this particle's gravity. True to enable gravity, false to disable. Defaults to false.*/
	public void setGravity(boolean gravity){
		this.particleGravity = gravity ? 1 : 0;
	}
	
	/** Sets this particle's collisions. True to enable block collisions, false to disable. Defaults to false.*/
	public void setCollisions(boolean canCollide){
		this.canCollide = canCollide;
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
	 * Sets the spin parameters of the particle.
	 * @param radius The spin radius
	 * @param speed The spin speed in rotations per tick
	 */
	public void setSpin(double radius, double speed){
		this.radius = radius;
		this.speed = speed * 2 * Math.PI; // Converts rotations per tick into radians per tick for the trig functions
		this.angle = this.rand.nextFloat() * (float)Math.PI * 2; // Random start angle TODO: Perhaps this should be specified?
		// Need to set the start position or the circle won't be centred on the correct position
		this.relativeX = radius * -MathHelper.cos(angle);
		this.relativeZ = radius * MathHelper.sin(angle);
		this.setPosition(posX + relativeX, posY, posZ + relativeZ);
		this.prevPosX = posX;
		this.prevPosZ = posZ;
		// Set these to the correct values
		this.relativeMotionX = motionX;
		this.relativeMotionY = motionY;
		this.relativeMotionZ = motionZ;
	}
	
	/**
	 * Links this particle to the given entity. This will cause its position and velocity to be relative to the entity.
	 * @param entity The entity to link to.
	 */
	public void setEntity(Entity entity){
		this.entity = entity;
		// Set these to the correct values
		if(entity != null){
			this.setPosition(this.entity.posX + relativeX, this.entity.getEntityBoundingBox().minY
					+ relativeY, this.entity.posZ + relativeZ);
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			this.relativeMotionX = motionX;
			this.relativeMotionY = motionY;
			this.relativeMotionZ = motionZ;
		}
	}
	
	// Overridden to set the initial colour values
	/**
	 * Sets the base colour of the particle. <i>Note that this also sets the fade colour so that particles without a
	 * fade colour do not change colour at all; as such fade colour must be set <b>after</b> calling this method.</i>
	 * @param r The red colour component
	 * @param g The green colour component
	 * @param b The blue colour component
	 */
	@Override
	public void setRBGColorF(float r, float g, float b){
		super.setRBGColorF(r, g, b);
		initialRed = r;
		initialGreen = g;
		initialBlue = b;
		// If fade colour is not specified, it defaults to the main colour - this method is always called first
		setFadeColour(r, g, b);
	}
	
	/**
	 * Sets the fade colour of the particle.
	 * @param r The red colour component
	 * @param g The green colour component
	 * @param b The blue colour component
	 */
	public void setFadeColour(float r, float g, float b){
		this.fadeRed = r;
		this.fadeGreen = g;
		this.fadeBlue = b;
	}
	
	/**
	 * Sets the direction this particle faces. This will cause the particle to render facing the given direction.
	 * @param yaw The yaw angle of this particle in degrees, where 0 is [TODO south?].
	 * @param pitch The pitch angle of this particle in degrees, where 0 is horizontal.
	 */
	public void setFacing(float yaw, float pitch){
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	// Setters for parameters that only affect some particles - these are unimplemented in this class because they
	// doesn't make sense for most particles
	
	/**
	 * Sets the target position for this particle. This will cause it to stretch to touch the given position,
	 * if supported.
	 * @param x The x-coordinate of the target position.
	 * @param y The y-coordinate of the target position.
	 * @param z The z-coordinate of the target position.
	 */
	public void setTargetPosition(double x, double y, double z){
		// Does nothing for normal particles since normal particles always render at a single point
	}
	
	/**
	 * Links this particle to the given target. This will cause it to stretch to touch the target, if supported.
	 * @param target The target to link to.
	 */
	public void setTargetEntity(Entity target){
		// Does nothing for normal particles since normal particles always render at a single point
	}
	
	// ============================================== Method Overrides ==============================================
	
	@Override
	public int getFXLayer(){
		return sprites.length == 0 ? super.getFXLayer() : 1; // This has to be 1 for the TextureAtlasSprites to work
	}

	@Override
	public int getBrightnessForRender(float partialTick){
		return shaded ? super.getBrightnessForRender(partialTick) : 15728880;
	}
	
	/**
	 * Renders the particle. The mapping names given to the parameters in this method are very misleading; see below for
	 * details of what they actually do. (They're also in a strange order...)
	 * @param buffer The {@code BufferBuilder} object.
	 * @param viewer The entity whose viewpoint the particle is being rendered from; this should always be the
	 * client-side player.
	 * @param partialTicks The partial tick time.
	 * @param lookZ Equal to the cosine of {@code viewer.rotationYaw}. Will be -1 when facing north (negative Z), 0 when 
	 * east/west, and +1 when facing south (positive Z). Independent of pitch.
	 * @param lookY Equal to the cosine of {@code viewer.rotationPitch}. Will be 1 when facing directly up or down, and 0
	 * when facing directly horizontally.
	 * @param lookX Equal to the sine of {@code viewer.rotationYaw}.  Will be -1 when facing east (positive X), 0 when
	 * facing north/south, and +1 when facing west (negative X). Independent of pitch.
	 * @param lookXY Equal to {@code lookX} times the sine of {@code viewer.rotationPitch}. Will be 0 when facing directly horizontal.
	 * When facing directly up, will be equal to {@code -lookX}. When facing directly down, will be equal to {@code lookX}. 
	 * @param lookYZ Equal to {@code -lookZ} times the sine of {@code viewer.rotationPitch}. Will be 0 when facing directly horizontal.
	 * When facing directly up, will be equal to {@code -lookZ}. When facing directly down, will be equal to {@code lookZ}. 
	 */
	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float lookZ, float lookY,
			float lookX, float lookXY, float lookYZ){
		
		if(Float.isNaN(this.yaw) || Float.isNaN(this.pitch)){
			
			// Normal behaviour (rotates to face the viewer)
			super.renderParticle(buffer, viewer, partialTicks, lookZ, lookY, lookX, lookXY, lookYZ);
			
		}else{
			
			// Specific rotation
			
			// Copied from ActiveRenderInfo; converts yaw and pitch into the weird parameters used by renderParticle.
			// The 1st/3rd person distinction has been removed since this has nothing to do with the view angle.
			
			float degToRadFactor = 0.017453292f; // Conversion from degrees to radians
			
	        float rotationX = MathHelper.cos(yaw * degToRadFactor);
	        float rotationZ = MathHelper.sin(yaw * degToRadFactor);
	        float rotationY = MathHelper.cos(pitch * degToRadFactor);
	        float rotationYZ = -rotationZ * MathHelper.sin(pitch * degToRadFactor);
	        float rotationXY = rotationX * MathHelper.sin(pitch * degToRadFactor);
	        
			super.renderParticle(buffer, viewer, partialTicks, rotationX, rotationY, rotationZ, rotationYZ, rotationXY);
		}
	}
	
	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// If any of these values is NaN, the particle has neither an entity nor a spin
		if(!Double.isNaN(relativeMotionX) && !Double.isNaN(relativeMotionY) && !Double.isNaN(relativeMotionZ)){
		
			// This allows velocity changes from entity linking and spin to stack
			double vx = relativeMotionX;
			double vy = relativeMotionY;
			double vz = relativeMotionZ;
			
			// Entity linking
			if(this.entity != null){
				
				if(this.entity.isDead) this.setExpired();
				
				this.setPosition(this.entity.posX + relativeX, this.entity.getEntityBoundingBox().minY + relativeY,
						this.entity.posZ + relativeZ);
				// Velocity is set so that the renderer will interpolate correctly between ticks
				vx += this.entity.motionX;
				vy += this.entity.motionY;
				vz += this.entity.motionZ;
			}
			
			// Spin
			if(radius > 0){
				angle += speed;
				vx += radius * speed * MathHelper.sin(angle);
				vz += radius * speed * MathHelper.cos(angle);
			}
			
			this.relativeX += vx;
			this.relativeY += vy;
			this.relativeZ += vz;
		}
		
		// Colour fading
		float ageFraction = (float)this.particleAge / (float)this.particleMaxAge;
		// No longer uses setRBGColorF because that method now also sets the initial values
		this.particleRed   = this.initialRed   + (this.fadeRed   - this.initialRed)   * ageFraction;
		this.particleGreen = this.initialGreen + (this.fadeGreen - this.initialGreen) * ageFraction;
		this.particleBlue  = this.initialBlue  + (this.fadeBlue  - this.initialBlue)  * ageFraction;
		
		// Animation
		if(sprites.length > 1){
			// Math.min included for safety so the index cannot possibly exceed the length - 1 an cause an AIOOBE
			// (which would probably otherwise happen if particleAge == particleMaxAge)
			this.setParticleTexture(sprites[Math.min((int)(ageFraction * sprites.length), sprites.length - 1)]);
		}
	}
	
	// =============================================== Helper Methods ===============================================
	
	/** Static helper method that generates an array of n ResourceLocations using the particle file naming convention,
	 * which is the given stem plus an underscore plus the integer index. */
	public static ResourceLocation[] generateTextures(String stem, int n){

		ResourceLocation[] textures = new ResourceLocation[n];
		
		for(int i=0; i<n; i++){
			textures[i] = new ResourceLocation(Wizardry.MODID, "particle/" + stem + "_" + i);
		}
		
		return textures;
	}
	
	/** Static helper method that generates a 2D m x n array of ResourceLocations using the particle file naming
	 * convention, which is the given stem plus an underscore plus the first index, plus an underscore plus the second
	 * index. Useful for animated particles that also pick a random animation strip. */
	public static ResourceLocation[][] generateTextures(String stem, int m, int n){

		ResourceLocation[][] textures = new ResourceLocation[m][n];
		
		for(int i=0; i<m; i++){
			for(int j=0; j<n; j++){
				textures[i][j] = new ResourceLocation(Wizardry.MODID, "particle/" + stem + "_" + i + "_" + j);
			}
		}
		
		return textures;
	}

	/** Simple particle factory interface which takes a world and a position and returns a particle. Used (via method
	 * references) in the client proxy to link particle enum types to actual particle classes. */
	@SideOnly(Side.CLIENT)
	@FunctionalInterface
	public interface IWizardryParticleFactory {
	    ParticleWizardry createParticle(World world, double x, double y, double z);
	}
}
