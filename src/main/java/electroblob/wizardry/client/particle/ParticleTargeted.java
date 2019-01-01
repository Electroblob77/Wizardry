package electroblob.wizardry.client.particle;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/** Superclass for particles with a second target entity or target position. */
public abstract class ParticleTargeted extends ParticleWizardry {
	
	protected double targetX;
	protected double targetY;
	protected double targetZ;

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
	public void setTargetEntity(Entity target){
		this.target = target;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ){
		
		if(this.target != null){
			this.targetX = this.target.posX;
			this.targetY = this.target.getEntityBoundingBox().minY + target.height/2;
			this.targetZ = this.target.posZ;
		}
		
		if(Double.isNaN(targetX) || Double.isNaN(targetY) || Double.isNaN(targetZ)){
			Wizardry.logger.warn("Attempted to render a targeted particle, but neither its target entity nor target"
					+ "position was set!");
			return;
		}
		
		// I'm pretty sure these were always static.
		interpPosX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * (double)partialTicks;
		interpPosY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * (double)partialTicks;
		interpPosZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * (double)partialTicks;

		float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		double dx = this.targetX - this.posX;
		double dy = this.targetY - this.posY;
		double dz = this.targetZ - this.posZ;

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
	
	/** Called from {@link ParticleTargeted#renderParticle(BufferBuilder, Entity, float, float, float, float, float, float)},
	 * once the appropriate calculations and transformations have been applied, to actually render the particle. Subclasses
	 * override this <i>instead</i> of overriding {@code renderParticle} directly, and inside render the particle <b>along
	 * the z-axis, starting at (0, 0, 0)</b> - it will be translated and rotated automatically.
	 * <p>
	 * <i>N.B. Other than transformations, no GL state changes are applied; these should be done within this method.</i>
	 * 
	 * @param tessellator A reference to the tessellator, for convenience.
	 * @param length The distance from the origin to the endpoint for the particle being rendered; the particle should
	 * therefore be rendered between (0, 0, 0) and (0, 0, length) within this method.
	 * @param partialTicks The partial tick time.
	 */
	protected abstract void draw(Tessellator tessellator, double length, float partialTicks);

}
