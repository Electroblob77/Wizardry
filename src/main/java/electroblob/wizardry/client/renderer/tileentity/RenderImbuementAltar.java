package electroblob.wizardry.client.renderer.tileentity;

import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderImbuementAltar extends TileEntitySpecialRenderer<TileEntityImbuementAltar> {

	public RenderImbuementAltar(){}

	@Override
	public void render(TileEntityImbuementAltar tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha){

		GlStateManager.pushMatrix();

		GlStateManager.translate((float)x + 0.5F, (float)y + 1.4F, (float)z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);

		float t = Minecraft.getMinecraft().player.ticksExisted + partialTicks;
		GlStateManager.translate(0, 0.05f * MathHelper.sin(t/15), 0);

		this.renderItem(tileentity, t);
		this.renderRays(tileentity, partialTicks);

		GlStateManager.popMatrix();
	}

	private void renderItem(TileEntityImbuementAltar tileentity, float t){

		ItemStack stack = tileentity.getStack();

		if(!stack.isEmpty()){

			GlStateManager.pushMatrix();

			GlStateManager.rotate(180, 1, 0, 0);
			GlStateManager.rotate(t, 0, 1, 0);
			GlStateManager.scale(0.85F, 0.85F, 0.85F);

			Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.FIXED);

			GlStateManager.popMatrix();
		}
	}

	private void renderRays(TileEntityImbuementAltar tileentity, float partialTicks){

		float t = Minecraft.getMinecraft().player.ticksExisted + partialTicks;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Random random = new Random(tileentity.getPos().toLong()); // Use position to get a constant seed

		int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(tileentity.getDisplayElement());

		if(colours == null) return; // Shouldn't happen

		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		GlStateManager.depthMask(false);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		RenderHelper.disableStandardItemLighting();

		int r1 = colours[1] >> 16 & 255;
		int g1 = colours[1] >> 8 & 255;
		int b1 = colours[1] & 255;

		int r2 = colours[2] >> 16 & 255;
		int g2 = colours[2] >> 8 & 255;
		int b2 = colours[2] & 255;

		for(int j = 0; j < 30; j++){

			int m = random.nextInt(10);
			int n = random.nextInt(10);

			int sliceAngle = 20 + m;
			float scale = 0.5f;

			GlStateManager.pushMatrix();

			float progress = Math.min(tileentity.getImbuementProgress() + partialTicks/141, 1);
			float s = 1 - progress;
			s = 1 - s*s;
			GlStateManager.scale(s, s, s);

			// TODO: This needs optimising! We should easily be able to do this with a single draw() call
			// Same for magic light and black hole, which don't need ray textures either!
			GlStateManager.rotate(31 * m, 1, 0, 0);
			GlStateManager.rotate(31 * n, 0, 0, 1);

			buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);

			float fade = (Math.min(1, 1.9f - progress) - 0.9f) * 10;

			buffer.pos(0, 0, 0).color(r1, g1, b1, (int)(255 * fade)).endVertex();
			buffer.pos(0, 0, 0).color(r1, g1, b1, (int)(255 * fade)).endVertex();

			double x1 = scale * MathHelper.sin((t + 40 * j) * ((float)Math.PI / 180));
			double z1 = scale * MathHelper.cos((t + 40 * j) * ((float)Math.PI / 180));

			double x2 = scale * MathHelper.sin((t + 40 * j - sliceAngle) * ((float)Math.PI / 180));
			double z2 = scale * MathHelper.cos((t + 40 * j - sliceAngle) * ((float)Math.PI / 180));

			buffer.pos(x1, 0, z1).color(r2, g2, b2, 0).endVertex();
			buffer.pos(x2, 0, z2).color(r2, g2, b2, 0).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableLighting();
		RenderHelper.enableStandardItemLighting();
	}

}
