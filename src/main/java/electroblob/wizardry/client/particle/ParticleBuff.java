package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBuff extends ParticleEntityLinked {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/particle/buff.png");
	private final boolean mirror;
	
	public ParticleBuff(World world, Entity entity, float r, float g, float b, boolean mirror){
		super(world, entity);
		this.setRBGColorF(r, g, b);
		this.mirror = mirror;
	}

	public ParticleBuff(World world, Entity entity, int maxAge, float r, float g, float b, boolean mirror){
		super(world, entity, maxAge);
		this.setRBGColorF(r, g, b);
		this.mirror = mirror;
	}

	@Override
	public void init(){
		this.particleGravity = 0;
		this.posY += 1;
	}
	
	@Override
	public void onUpdate(){
		super.onUpdate();
		this.setPosition(this.entity.posX, this.entity.posY + 1 + 4f * this.particleAge/this.particleMaxAge, this.entity.posZ);
		if(this.particleAge > this.particleMaxAge/2) this.particleAlpha = 2f - 2f*(float)this.particleAge/(float)this.particleMaxAge;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ){
		
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
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		
		GlStateManager.translate((this.particleAge + partialTicks)/(float)this.particleMaxAge * -2, 0, 0);
		
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		RenderHelper.disableStandardItemLighting();

		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		// I'm pretty sure these were always static.
		Particle.interpPosX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * (double)partialTicks;
		Particle.interpPosY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * (double)partialTicks;
		Particle.interpPosZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * (double)partialTicks;

		float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
		float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
		float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
		
		// Increases from 0 to 1 in steps of 0.125 evenly throughout the particle's lifetime
		float f = 0.875f - 0.125f * MathHelper.floor((float)this.particleAge/(float)this.particleMaxAge * 8 - 0.000001f);
		float g = f + 0.125f;
		float hrepeat = 1;
		float yScale = 0.7f;
		
		buffer.pos(x-1, y-yScale, z-1).tex(0, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y+yScale, z-1).tex(0, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y-yScale, z-1).tex(0.25*hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y+yScale, z-1).tex(0.25*hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y-yScale, z+1).tex(0.5*hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x+1, y+yScale, z+1).tex(0.5*hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y-yScale, z+1).tex(0.75*hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y+yScale, z+1).tex(0.75*hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y-yScale, z-1).tex(hrepeat, g).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();
		buffer.pos(x-1, y+yScale, z-1).tex(hrepeat, f).color(particleRed, particleGreen, particleBlue, particleAlpha).endVertex();

		Tessellator.getInstance().draw();
		
		// Undoes the texture transformations
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
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
