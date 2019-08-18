package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityDecay;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderDecay extends Render<EntityDecay> {

	private static final ResourceLocation[] textures = new ResourceLocation[10];

	public RenderDecay(RenderManager renderManager){
		super(renderManager);
		for(int i = 0; i < 10; i++){
			textures[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/decay_" + i + ".png");
		}
	}

	@Override
	public void doRender(EntityDecay entity, double par2, double par4, double par6, float par8, float par9){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		GlStateManager.translate((float)par2, (float)par4 + yOffset, (float)par6);

		this.bindTexture(textures[((EntityDecay)entity).textureIndex]);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		GlStateManager.rotate(-90, 1, 0, 0);

		float scale = 2 * Math.min(1, (float)(entity.lifetime - entity.ticksExisted) / 50f);

		GlStateManager.scale(scale, scale, scale);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		// tessellator.setColorRGBA_I(k1, 128);
		// buffer.normal(0.0F, 1.0F, 0.0F);
		buffer.pos((double)(0.0F - f7), (double)(0.0F - f8), 0.01).tex(0, 1).endVertex();
		buffer.pos((double)(f6 - f7), (double)(0.0F - f8), 0.01).tex(1, 1).endVertex();
		buffer.pos((double)(f6 - f7), (double)(1.0F - f8), 0.01).tex(1, 0).endVertex();
		buffer.pos((double)(0.0F - f7), (double)(1.0F - f8), 0.01).tex(0, 0).endVertex();

		tessellator.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDecay entity){
		return null;
	}

}
