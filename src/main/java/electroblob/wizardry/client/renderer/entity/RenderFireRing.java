package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.entity.construct.EntityFireRing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderFireRing extends Render<EntityFireRing> {

	private final ResourceLocation texture;
	private float scale = 1.0f;

	public RenderFireRing(RenderManager renderManager, ResourceLocation texture, float scale){
		super(renderManager);
		this.texture = texture;
		this.scale = scale;
	}

	@Override
	public void doRender(EntityFireRing entity, double par2, double par4, double par6, float par8, float par9){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		GlStateManager.translate((float)par2, (float)par4 + yOffset, (float)par6);

		this.bindTexture(texture);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		GlStateManager.rotate(-90, 1, 0, 0);

		GlStateManager.scale(scale, scale, scale);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos((double)(0.0F - f7), (double)(0.0F - f8), 0.01).tex(0, 1).endVertex();
		buffer.pos((double)(f6 - f7), (double)(0.0F - f8), 0.01).tex(1, 1).endVertex();
		buffer.pos((double)(f6 - f7), (double)(1.0F - f8), 0.01).tex(1, 0).endVertex();
		buffer.pos((double)(0.0F - f7), (double)(1.0F - f8), 0.01).tex(0, 0).endVertex();

		tessellator.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();

		// Fire

		GlStateManager.disableLighting();
		TextureAtlasSprite icon = Minecraft.getMinecraft().getBlockRendererDispatcher()
				.getModelForState(Blocks.FIRE.getDefaultState()).getParticleTexture();
		int sides = 16;
		float height = 1.0f;

		for(int k = 0; k < sides; k++){

			GlStateManager.pushMatrix();
			GlStateManager.translate((float)par2, (float)par4 + 0.05f, (float)par6);
			float f1 = 1.0f;
			GlStateManager.scale(f1, f1, f1);
			float f2 = 0.5F;
			float f3 = 0.0F;
			float f4 = 0.2f;
			float f5 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			float f61 = 0.0F;
			int i = 0;

			GlStateManager.rotate((360f / (float)sides) * k, 0, 1, 0);
			GlStateManager.translate(0, 0, -2.3f);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			while(f4 > 0.0F){

				this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				float f71 = icon.getMinU();
				float f81 = icon.getMinV();
				float f9 = icon.getMaxU();
				float f10 = icon.getMaxV();

				if(i / 2 % 2 == 0){
					float f11 = f9;
					f9 = f71;
					f71 = f11;
				}

				buffer.pos((double)(f2 - f3), (double)(0.0F - f5), (double)f61).tex((double)f9, (double)f10)
						.endVertex();
				buffer.pos((double)(-f2 - f3), (double)(0.0F - f5), (double)f61).tex((double)f71, (double)f10)
						.endVertex();
				buffer.pos((double)(-f2 - f3), (double)(height - f5), (double)f61).tex((double)f71, (double)f81)
						.endVertex();
				buffer.pos((double)(f2 - f3), (double)(height - f5), (double)f61).tex((double)f9, (double)f81)
						.endVertex();
				f4 -= 0.45F;
				f5 -= 0.45F;
				f2 *= 0.9F;
				f61 += 0.03F;
				++i;
			}

			tessellator.draw();
			GlStateManager.popMatrix();
		}

		for(int k = 0; k < sides; k++){

			GlStateManager.pushMatrix();
			GlStateManager.translate((float)par2, (float)par4 + 0.05f, (float)par6);
			float f1 = 1.0f;
			GlStateManager.scale(f1, f1, f1);
			float f2 = 0.5F;
			float f3 = 0.0F;
			float f4 = 0.2f;
			float f5 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			float f61 = 0.0F;
			int i = 0;

			GlStateManager.rotate((360f / (float)sides) * k, 0, 1, 0);
			GlStateManager.translate(0, 0, 2.3f);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			while(f4 > 0.0F){

				this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				float f71 = icon.getMinU();
				float f81 = icon.getMinV();
				float f9 = icon.getMaxU();
				float f10 = icon.getMaxV();

				if(i / 2 % 2 == 0){
					float f11 = f9;
					f9 = f71;
					f71 = f11;
				}

				buffer.pos((double)(f2 - f3), (double)(0.0F - f5), (double)f61).tex((double)f9, (double)f10)
						.endVertex();
				buffer.pos((double)(-f2 - f3), (double)(0.0F - f5), (double)f61).tex((double)f71, (double)f10)
						.endVertex();
				buffer.pos((double)(-f2 - f3), (double)(height - f5), (double)f61).tex((double)f71, (double)f81)
						.endVertex();
				buffer.pos((double)(f2 - f3), (double)(height - f5), (double)f61).tex((double)f9, (double)f81)
						.endVertex();
				f4 -= 0.45F;
				f5 -= 0.45F;
				f2 *= 0.9F;
				f61 += 0.03F;
				++i;
			}

			tessellator.draw();
			GlStateManager.popMatrix();
		}

		GlStateManager.enableLighting();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFireRing entity){
		return null;
	}

}
