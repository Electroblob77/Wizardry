package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract superclass for all particles that use custom textures. This is intended to centralise as much code as
 * possible; all subclasses need to do is to define the texture to use, how the frames are arranged (and which to
 * choose), and any properties like gravity and collisions.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
@SideOnly(Side.CLIENT)
public abstract class ParticleCustomTexture extends Particle {

	/** True if the particle always renders at full brightness. Defaults to false. */
	protected boolean fullBrightness = false;

	public ParticleCustomTexture(World world, double x, double y, double z, double vx, double vy, double vz){
		super(world, x, y, z, vx, vy, vz);
		this.motionX = vx;
		this.motionY = vy;
		this.motionZ = vz;
		this.init();
	}

	public ParticleCustomTexture(World world, double x, double y, double z, double vx, double vy, double vz,
			int maxAge){
		this(world, x, y, z, vx, vy, vz);
		this.particleMaxAge = maxAge;
	}

	/**
	 * Called from both constructors to set constants, avoiding duplicate code. Common fields to set here include:
	 * particleScale, particleGravity, canCollide, fullBrightness and setting the texture index.
	 */
	public abstract void init();

	/**
	 * Returns a ResourceLocation for the particle's texture sheet. Do not create a new ResourceLocation in this method,
	 * only return a constant.
	 */
	public abstract ResourceLocation getTexture();

	/** Returns how many 'frames' there are in the x direction on the texture. */
	protected abstract int getXFrames();

	/** Returns how many 'frames' there are in the y direction on the texture. */
	protected abstract int getYFrames();

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
	public void setParticleTextureIndex(int index){
		this.particleTextureIndexX = index % getXFrames();
		this.particleTextureIndexY = index / getYFrames();
	}

	// Overridden to fix the bug with vanilla that makes particles frictionless. (y != y... seriously, Mojang?)
	// TESTME: Probably no longer necessary.
	// @Override
	// public void move(double x, double y, double z){
	//
	// double d0 = y;
	//
	// if (this.canCollide)
	// {
	// List<AxisAlignedBB> list = this.world.getCollisionBoxes((Entity)null, this.getBoundingBox().addCoord(x, y, z));
	//
	// for (AxisAlignedBB axisalignedbb : list)
	// {
	// y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
	// }
	//
	// this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));
	//
	// for (AxisAlignedBB axisalignedbb1 : list)
	// {
	// x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
	// }
	//
	// this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));
	//
	// for (AxisAlignedBB axisalignedbb2 : list)
	// {
	// z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
	// }
	//
	// this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));
	// }
	// else
	// {
	// this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
	// }
	//
	// this.resetPositionToBB();
	// this.onGround = d0 != y && d0 < 0.0D;
	//
	// /* Can never be true! - But this doesn't seem to make any difference anyway.
	// if (x != x)
	// {
	// this.motionX = 0.0D;
	// }
	//
	// if (z != z)
	// {
	// this.motionZ = 0.0D;
	// }
	// */
	// }

	// Overridden to bind the new texture. I think this can be done with TextureAtlasSprite, but this works as it is
	// so I'm not changing it for the time being.
	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ){
		
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		this.applyGLStateChanges();

		// This stuff does the shading. It vanilla does this later on for each point, but this also seems to work.
		int brightness = this.getBrightnessForRender(partialTicks);
		int lightmapX = brightness % 65536;
		int lightmapY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)lightmapX / 1.0F,
				(float)lightmapY / 1.0F);

		RenderHelper.disableStandardItemLighting();

		Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture());

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		float u1 = (float)this.particleTextureIndexX / (float)getXFrames();
		float u2 = u1 + 1.0f / getXFrames();
		float v1 = (float)this.particleTextureIndexY / (float)getYFrames();
		float v2 = v1 + 1.0f / getYFrames();
		float scale = 0.1F * this.particleScale;

		// I'm pretty sure these were always static.
		Particle.interpPosX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * (double)partialTicks;
		Particle.interpPosY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * (double)partialTicks;
		Particle.interpPosZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * (double)partialTicks;

		float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);

		buffer.pos((double)(x - rotationX * scale - rotationXY * scale), (double)(y - rotationZ * scale),
				(double)(z - rotationYZ * scale - rotationXZ * scale)).tex(u2, v2)
				.color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos((double)(x - rotationX * scale + rotationXY * scale), (double)(y + rotationZ * scale),
				(double)(z - rotationYZ * scale + rotationXZ * scale)).tex(u2, v1)
				.color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos((double)(x + rotationX * scale + rotationXY * scale), (double)(y + rotationZ * scale),
				(double)(z + rotationYZ * scale + rotationXZ * scale)).tex(u1, v1)
				.color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos((double)(x + rotationX * scale - rotationXY * scale), (double)(y - rotationZ * scale),
				(double)(z + rotationYZ * scale - rotationXZ * scale)).tex(u1, v2)
				.color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		;

		Tessellator.getInstance().draw();

		this.undoGLStateChanges();

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();

	}

	/**
	 * Override to add any GL state changes, like blending. Does nothing by default. <b>State changes should be done
	 * using GLStateManager, not using GL11 directly</b> (as is the case with all rendering code now).
	 */
	public void applyGLStateChanges(){
	}

	/**
	 * Override to undo any GL state changes, like blending. Does nothing by default. <b>State changes should be done
	 * using GLStateManager, not using GL11 directly</b> (as is the case with all rendering code now).
	 */
	public void undoGLStateChanges(){
	}

	@Override
	public int getBrightnessForRender(float partialTick){
		return fullBrightness ? 15728880 : super.getBrightnessForRender(partialTick);
	}
}
