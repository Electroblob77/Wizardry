package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.entity.projectile.EntityMagicArrow;
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
public class RenderMagicArrow extends Render<EntityMagicArrow> {

	private final ResourceLocation texture;
	private boolean blend;
	private boolean renderEnds;
	private double length = 8.0, width = 2.0;
	private int pixelsLong = 16, pixelsWide = 5;

	public RenderMagicArrow(RenderManager renderManager, ResourceLocation texture, boolean blend, double length,
			double width, int pixelsLong, int pixelsWide, boolean renderEnds){
		super(renderManager);
		this.texture = texture;
		this.blend = blend;
		this.renderEnds = renderEnds;
		this.length = length;
		this.width = width;
		this.pixelsLong = pixelsLong;
		this.pixelsWide = pixelsWide;
	}

	@Override
	public void doRender(EntityMagicArrow entity, double par2, double par4, double par6, float par8, float par9){

		this.bindEntityTexture(entity);
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		if(this.blend){
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		GlStateManager.translate((float)par2, (float)par4, (float)par6);
		GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par9 - 90.0F,
				0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par9, 0.0F,
				0.0F, 1.0F);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		float f2 = 0.0F;
		float f3 = pixelsLong / 32.0F;
		float f4 = 0.0F;
		float f5 = pixelsWide / 32.0F;
		float f6 = 0.0F;
		float f7 = 0.15625F;
		float f8 = (float)5 / 32.0F;
		float f9 = (float)10 / 32.0F;
		float f10 = 0.05625F;
		float f11 = 0.0f;
		GlStateManager.enableRescaleNormal();
		// f11 = (float)par1EntityArrow.arrowShake - par9;
		if(f11 > 0.0F){
			float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
			GlStateManager.rotate(f12, 0.0F, 0.0F, 1.0F);
		}

		GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(f10, f10, f10);
		GlStateManager.translate(-4.0F, 0.0F, 0.0F);
		GL11.glNormal3f(f10, 0.0F, 0.0F);

		if(renderEnds){
			// Ends
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-7.0D, -width, -width).tex((double)f6, (double)f8).endVertex();
			buffer.pos(-7.0D, -width, width).tex((double)f7, (double)f8).endVertex();
			buffer.pos(-7.0D, width, width).tex((double)f7, (double)f9).endVertex();
			buffer.pos(-7.0D, width, -width).tex((double)f6, (double)f9).endVertex();
			tessellator.draw();
			GL11.glNormal3f(-f10, 0.0F, 0.0F);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-7.0D, width, -width).tex((double)f6, (double)f8).endVertex();
			buffer.pos(-7.0D, width, width).tex((double)f7, (double)f8).endVertex();
			buffer.pos(-7.0D, -width, width).tex((double)f7, (double)f9).endVertex();
			buffer.pos(-7.0D, -width, -width).tex((double)f6, (double)f9).endVertex();
			tessellator.draw();
		}

		for(int i = 0; i < 4; ++i){
			// Sides
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
			GL11.glNormal3f(0.0F, 0.0F, f10);
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-length, -width, 0.0D).tex((double)f2, (double)f4).endVertex();
			buffer.pos(length, -width, 0.0D).tex((double)f3, (double)f4).endVertex();
			buffer.pos(length, width, 0.0D).tex((double)f3, (double)f5).endVertex();
			buffer.pos(-length, width, 0.0D).tex((double)f2, (double)f5).endVertex();
			tessellator.draw();
		}

		if(this.blend){
			GlStateManager.disableBlend();
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMagicArrow arrow){
		return texture;
	}
}
