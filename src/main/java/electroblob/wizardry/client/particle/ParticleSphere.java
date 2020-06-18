package electroblob.wizardry.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleSphere extends ParticleWizardry {

	public ParticleSphere(World world, double x, double y, double z){
		super(world, x, y, z);
		this.setRBGColorF(1, 1, 1);
		this.particleMaxAge = 5;
		this.particleAlpha = 0.8f;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}

	@Override
	public int getFXLayer(){
		return 3;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

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

		GlStateManager.pushMatrix();
		GlStateManager.translate(x - interpPosX, y - interpPosY, z - interpPosZ);

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableCull();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		float latStep = (float)Math.PI/20;
		float longStep = (float)Math.PI/20;

		float sphereRadius = this.particleScale * (this.particleAge + partialTicks - 1) / this.particleMaxAge;
		float alpha = this.particleAlpha * (1 - (this.particleAge + partialTicks - 1) / this.particleMaxAge);

		drawSphere(Tessellator.getInstance(), buffer, sphereRadius, latStep, longStep, true, particleRed, particleGreen, particleBlue, alpha);
		drawSphere(Tessellator.getInstance(), buffer, sphereRadius, latStep, longStep, false, particleRed, particleGreen, particleBlue, alpha);

		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();

		GlStateManager.popMatrix();

	}
	
	@Override
	public int getBrightnessForRender(float partialTicks){
		return 15728880;
	}

	/**
	 * Draws a sphere (using lat/long triangles) with the given parameters.
	 * @param radius The radius of the sphere.
	 * @param latStep The latitude step; smaller is smoother but increases performance cost.
	 * @param longStep The longitude step; smaller is smoother but increases performance cost.
	 * @param inside Whether to draw the outside or the inside of the sphere.
	 * @param r The red component of the sphere colour.
	 * @param g The green component of the sphere colour.
	 * @param b The blue component of the sphere colour.
	 * @param a The alpha component of the sphere colour.
	 */
	private static void drawSphere(Tessellator tessellator, BufferBuilder buffer, float radius, float latStep, float longStep, boolean inside, float r, float g, float b, float a){

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		boolean goingUp = inside;

		buffer.pos(0, goingUp ? -radius : radius, 0).color(r, g, b, a).endVertex(); // Start at the north pole

		for(float longitude = -(float)Math.PI; longitude <= (float)Math.PI; longitude += longStep){

			// Leave the poles out since they only have a single point per stack instead of two
			for(float theta = (float)Math.PI/2 - latStep; theta >= -(float)Math.PI/2 + latStep; theta -= latStep){

				float latitude = goingUp ? -theta : theta;

				float hRadius = radius * MathHelper.cos(latitude);
				float vy = radius * MathHelper.sin(latitude);
				float vx = hRadius * MathHelper.sin(longitude);
				float vz = hRadius * MathHelper.cos(longitude);

				buffer.pos(vx, vy, vz).color(r, g, b, a).endVertex();

				vx = hRadius * MathHelper.sin(longitude + longStep);
				vz = hRadius * MathHelper.cos(longitude + longStep);

				buffer.pos(vx, vy, vz).color(r, g, b, a).endVertex();
			}

			// The next pole
			buffer.pos(0, goingUp ? radius : -radius, 0).color(r, g, b, a).endVertex();

			goingUp = !goingUp;
		}

		tessellator.draw();
	}

}
