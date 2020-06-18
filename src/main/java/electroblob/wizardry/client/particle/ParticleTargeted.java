package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/** Superclass for particles with a second target entity or target position. */
public abstract class ParticleTargeted extends ParticleWizardry {

	private static final double THIRD_PERSON_AXIAL_OFFSET = 1.2;

	protected double targetX;
	protected double targetY;
	protected double targetZ;
	protected double targetVelX;
	protected double targetVelY;
	protected double targetVelZ;

	protected double length;

	/** The target this particle is linked to. The particle will stretch to touch this entity. */
	@Nullable
	protected Entity target = null;
	
	public ParticleTargeted(World world, double x, double y, double z, ResourceLocation... textures){
		super(world, x, y, z, textures);
	}
	
	@Override
	public void setTargetPosition(double x, double y, double z){
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
	}

	@Override
	public void setTargetVelocity(double vx, double vy, double vz){
		this.targetVelX = vx;
		this.targetVelY = vy;
		this.targetVelZ = vz;
	}
	
	@Override
	public void setTargetEntity(Entity target){
		this.target = target;
	}

	@Override
	public void setLength(double length){
		this.length = length;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!Double.isNaN(targetVelX) && !Double.isNaN(targetVelY) && !Double.isNaN(targetVelZ)){
			this.targetX += this.targetVelX;
			this.targetY += this.targetVelY;
			this.targetZ += this.targetVelZ;
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ){

		// Copied from ParticleWizardry, needs to be here since we're not calling super
		updateEntityLinking(partialTicks);

		// I don't know why, but despite not being in any superclass renderParticle these also need to be here if we're
		// not calling super
		interpPosX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * (double)partialTicks;
		interpPosY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * (double)partialTicks;
		interpPosZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * (double)partialTicks;

		float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks);
		float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks);
		float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks);

		// Translates the particle a short distance in front of the entity
		if(this.entity != null && this.shouldApplyOriginOffset()){
			if(this.entity != viewer || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0){
				Vec3d look = entity.getLook(partialTicks).scale(THIRD_PERSON_AXIAL_OFFSET);
				x += look.x;
				y += look.y;
				z += look.z;
			}
		}

		if(this.target != null){

			this.targetX = this.target.prevPosX + (this.target.posX - this.target.prevPosX) * partialTicks;
			double correction = this.target.getEntityBoundingBox().minY - this.target.posY;
			this.targetY = this.target.prevPosY + (this.target.posY - this.target.prevPosY) * partialTicks
					+ target.height/2 + correction;
			this.targetZ = this.target.prevPosZ + (this.target.posZ - this.target.prevPosZ) * partialTicks;

		}else if(this.entity != null && this.length > 0){

			Vec3d look = entity.getLook(partialTicks).scale(length);
			this.targetX = x + look.x;
			this.targetY = y + look.y;
			this.targetZ = z + look.z;
		}
		
		if(Double.isNaN(targetX) || Double.isNaN(targetY) || Double.isNaN(targetZ)){
			Wizardry.logger.warn("Attempted to render a targeted particle, but neither its target entity nor target"
					+ "position was set, and it either had no length assigned or was not linked to an entity!");
			return;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x - interpPosX, y - interpPosY, z - interpPosZ);

		double dx = this.targetX - x;
		double dy = this.targetY - y;
		double dz = this.targetZ - z;

		// No need for previous tick target positions and all that stuff since this is the only place they're used
		// and interpolating like this works just as well
		if(!Double.isNaN(targetVelX) && !Double.isNaN(targetVelY) && !Double.isNaN(targetVelZ)){
			dx += partialTicks * this.targetVelX;
			dy += partialTicks * this.targetVelY;
			dz += partialTicks * this.targetVelZ;
		}

		// The distance from origin to endpoint
		double length = Math.sqrt(dx*dx+dy*dy+dz*dz);

		// Math.atan2 computes within -180 to +180, rather than -90 to +90.
		float yaw = (float)(180d/Math.PI * Math.atan2(dx, dz));
		float pitch = (float)(180f/(float)Math.PI * Math.atan(-dy/Math.sqrt(dz*dz+dx*dx)));

		GL11.glRotatef(yaw, 0, 1, 0);
		GL11.glRotatef(pitch, 1, 0, 0);

		Tessellator tessellator = Tessellator.getInstance();
		
		this.draw(tessellator, length, partialTicks);
		
		GlStateManager.popMatrix();
	}

	/** Returns whether the origin of this particle should be moved a short distance in front of the entity it is
	 * linked to, if any. */
	protected boolean shouldApplyOriginOffset(){
		return true;
	}

	/** Called from {@link ParticleTargeted#renderParticle(BufferBuilder, Entity, float, float, float, float, float, float)},
	 * once the appropriate calculations and transformations have been applied, to actually render the particle. Subclasses
	 * override this <i>instead</i> of overriding {@code renderParticle} directly, and inside render the particle <b>along
	 * the z-axis, starting at (0, 0, 0)</b> - it will be translated and rotated automatically.
	 * <p></p>
	 * <i>N.B. Other than transformations, no GL state changes are applied; these should be done within this method.</i>
	 * 
	 * @param tessellator A reference to the tessellator, for convenience.
	 * @param length The distance from the origin to the endpoint for the particle being rendered; the particle should
	 * therefore be rendered between (0, 0, 0) and (0, 0, length) within this method.
	 * @param partialTicks The partial tick time.
	 */
	protected abstract void draw(Tessellator tessellator, double length, float partialTicks);

}
