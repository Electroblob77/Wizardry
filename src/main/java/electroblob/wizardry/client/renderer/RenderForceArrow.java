package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityForceArrow;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderForceArrow extends Render<EntityForceArrow> {

	private static final ResourceLocation arrowTextures = new ResourceLocation(Wizardry.MODID,
			"textures/entity/force_arrow.png");

	public RenderForceArrow(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityForceArrow arrow, double par2, double par4, double par6, float par8, float par9){

		this.bindEntityTexture(arrow);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlStateManager.translate((float)par2, (float)par4, (float)par6);
		GlStateManager.rotate(arrow.prevRotationYaw + (arrow.rotationYaw - arrow.prevRotationYaw) * par9 - 90.0F, 0.0F,
				1.0F, 0.0F);
		GlStateManager.rotate(arrow.prevRotationPitch + (arrow.rotationPitch - arrow.prevRotationPitch) * par9, 0.0F,
				0.0F, 1.0F);
		GlStateManager.rotate(180, 0, 1, 0);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		float pixel = 1.0f / 32.0f;
		float u1 = 0.0f;
		float u2 = pixel * 14;
		float v1 = 0.0f;
		float v2 = pixel * 7;
		float u3 = pixel * 16;
		float u4 = 1.0f;
		float v3 = 0.0f;
		float v4 = pixel * 16;
		float u5 = 0.0f;
		float u6 = pixel * 7;
		float v5 = pixel * 25;
		float v6 = 1.0f;
		float scale = 0.05625F;
		float f11 = 0.0f;
		GlStateManager.enableRescaleNormal();
		// f11 = (float)par1EntityArrow.arrowShake - par9;
		if(f11 > 0.0F){
			float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
			GlStateManager.rotate(f12, 0.0F, 0.0F, 1.0F);
		}

		scale *= 0.8f;

		GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(-4.0F, 0.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(-5, 3.5, -3.5).tex((double)u5, (double)v5).endVertex();
		buffer.pos(-5, 3.5, 3.5).tex((double)u6, (double)v5).endVertex();
		buffer.pos(-5, -3.5, 3.5).tex((double)u6, (double)v6).endVertex();
		buffer.pos(-5, -3.5, -3.5).tex((double)u5, (double)v6);
		tessellator.draw();

		for(int i = 0; i < 5; i++){
			GlStateManager.color(1, 1, 1, 1 - i * 0.2f);
			double j = i + ((double)arrow.ticksExisted % 3) / 3;
			double width = 2.0d + (Math.sqrt(j * 2) - 0.6) * 2;
			GL11.glNormal3f(scale, 0.0F, 0.0F);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-10 + j * 4, -width, -width).tex((double)u3, (double)v3).endVertex();
			buffer.pos(-10 + j * 4, -width, width).tex((double)u4, (double)v3).endVertex();
			buffer.pos(-10 + j * 4, width, width).tex((double)u4, (double)v4).endVertex();
			buffer.pos(-10 + j * 4, width, -width).tex((double)u3, (double)v4).endVertex();
			tessellator.draw();
			GL11.glNormal3f(-scale, 0.0F, 0.0F);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-10 + j * 4, width, -width).tex((double)u3, (double)v3).endVertex();
			buffer.pos(-10 + j * 4, width, width).tex((double)u4, (double)v3).endVertex();
			buffer.pos(-10 + j * 4, -width, width).tex((double)u4, (double)v4).endVertex();
			buffer.pos(-10 + j * 4, -width, -width).tex((double)u3, (double)v4).endVertex();
			tessellator.draw();
		}

		GlStateManager.color(1, 1, 1, 1);

		for(int i = 0; i < 4; ++i){
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GL11.glNormal3f(0.0F, 0.0F, scale);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-10.0D, -4.0D, 0.0D).tex((double)u1, (double)v1).endVertex();
			buffer.pos(10.0D, -4.0D, 0.0D).tex((double)u2, (double)v1).endVertex();
			buffer.pos(10.0D, 4.0D, 0.0D).tex((double)u2, (double)v2).endVertex();
			buffer.pos(-10.0D, 4.0D, 0.0D).tex((double)u1, (double)v2).endVertex();
			tessellator.draw();
		}

		GlStateManager.disableBlend();

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityForceArrow par1Entity){
		return arrowTextures;
	}
}
