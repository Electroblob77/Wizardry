package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

//@SideOnly(Side.CLIENT)
public class ParticleSummon extends ParticleWizardry {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/particle/summon.png");
	private final boolean mirror;

	public ParticleSummon(World world, double x, double y, double z){
		super(world, x, y, z);
		this.mirror = random.nextBoolean();
		this.setMaxAge(10);
		this.setGravity(false);
		this.canCollide = false;
	}
	
//	@Override
//	public void onUpdate(){
//		super.onUpdate();
//		if(this.particleAge > this.particleMaxAge/2) this.particleAlpha = 2f - 2f*(float)this.particleAge/(float)this.particleMaxAge;
//	}
	
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
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ){

		// Copied from ParticleWizardry, needs to be here since we're not calling super
		updateEntityLinking(partialTicks);

		// I don't know why, but despite not being in any superclass renderParticle these also need to be here if we're
		// not calling super
		interpPosX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * (double)partialTicks;
		interpPosY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * (double)partialTicks;
		interpPosZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * (double)partialTicks;
		
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		
		float scale = 0.6f;
		GlStateManager.scale(scale, scale, scale);
		if(mirror) GlStateManager.scale(-1, 1, 1);
		
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		// Makes the particle colour add to the colour of the texture pixels, rather than the default multiplying
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		// Does the texture translation wrapping thing (the cool stuff)
//		GlStateManager.matrixMode(GL11.GL_TEXTURE);
//		GlStateManager.loadIdentity();
//
//		GlStateManager.translate((this.particleAge + partialTicks)/(float)this.particleMaxAge * -2, 0, 0);
//
//		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		RenderHelper.disableStandardItemLighting();

		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		
		// Increases from 0 to 1 in steps of 0.125 evenly throughout the particle's lifetime
		float f = 0.125f * MathHelper.floor((float)this.particleAge/(float)this.particleMaxAge * 8 - 0.000001f);
		float g = f + 0.125f;
		float hrepeat = 1;
		float yScale = 3f;

		this.setRBGColorF(1, 1, 1);
		
		buffer.pos(x-1, y, z-1).tex(0, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y+yScale, z-1).tex(0, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y, z-1).tex(0.25*hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y+yScale, z-1).tex(0.25*hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y, z+1).tex(0.5*hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y+yScale, z+1).tex(0.5*hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y, z+1).tex(0.75*hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y+yScale, z+1).tex(0.75*hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y, z-1).tex(hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y+yScale, z-1).tex(hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();

		Tessellator.getInstance().draw();
		
		// Undoes the texture transformations
//		GlStateManager.matrixMode(GL11.GL_TEXTURE);
//		GlStateManager.loadIdentity();
//		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		// Reverses the colour addition change from before
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();

	}
}
