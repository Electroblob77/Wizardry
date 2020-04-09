package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderMagicLight extends TileEntitySpecialRenderer<TileEntityMagicLight> {

	private static final ResourceLocation RAY_TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/entity/light/ray.png");
	private static final ResourceLocation FLARE_TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/entity/light/flare.png");

	@Override
	public void render(TileEntityMagicLight tileentity, double x, double y, double z, float f,
			int destroyStage, float alpha){

		GlStateManager.pushMatrix();

		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		RenderHelper.disableStandardItemLighting();

		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

		if(tileentity.timer < 10){
			GlStateManager.scale((float)tileentity.timer / 10, (float)tileentity.timer / 10,
					(float)tileentity.timer / 10);
		}
		if(tileentity.maxTimer > 0 && tileentity.timer > tileentity.maxTimer - 10){
			float scale = Math.max(0, (float)(tileentity.maxTimer - tileentity.timer) / 10);
			GlStateManager.scale(scale, scale, scale);
		}

		// Renders the aura effect

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// This counteracts the reverse rotation behaviour when in front f5 view.
		// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
		float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
				? Minecraft.getMinecraft().getRenderManager().playerViewX
				: -Minecraft.getMinecraft().getRenderManager().playerViewX;
		GlStateManager.rotate(180.0F - Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		this.bindTexture(FLARE_TEXTURE);

		buffer.pos(-0.6, 0.6, 0).tex(0, 0).endVertex();
		buffer.pos(0.6, 0.6, 0).tex(1, 0).endVertex();
		buffer.pos(0.6, -0.6, 0).tex(1, 1).endVertex();
		buffer.pos(-0.6, -0.6, 0).tex(0, 1).endVertex();

		tessellator.draw();

		GlStateManager.popMatrix();

		// Renders the rays

		// For some reason, the old blend function (GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA) caused the innermost
		// ends of the rays to appear black, so I have changed it to this, which looks very slightly different.
		GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);

		this.bindTexture(RAY_TEXTURE);

		if(tileentity.randomiser.length >= 30){
			for(int j = 0; j < 30; j++){

				int sliceAngle = 20 + tileentity.randomiser[j];
				float scale = 0.5f;

				GlStateManager.pushMatrix();

				GlStateManager.rotate(31 * tileentity.randomiser[j], 1, 0, 0);
				GlStateManager.rotate(31 * tileentity.randomiser2[j], 0, 0, 1);

				/* OK, so here are the changes to rendering as far as I know: Vertex formats specify how the methods are
				 * arranged Color has to be called for every vertex, I think. The new methods thing is a bit weird,
				 * because other than the number of arguments there is essentially no difference between pos, tex,
				 * color, normal and lightmap. At least they make the code more readable. */

				buffer.begin(5, DefaultVertexFormats.POSITION_TEX_COLOR);

				buffer.pos(0, 0, 0).tex(0, 0).color(255, 255, 255, 0).endVertex();
				buffer.pos(0, 0, 0).tex(0, 1).color(255, 255, 255, 0).endVertex();

				double x1 = scale * MathHelper.sin((tileentity.timer + 40 * j) * ((float)Math.PI / 180));
				// double y1 = 0.7*MathHelper.cos((timerentity.timer - 40*j)*(Math.PI/180))*j/10;
				double z1 = scale * MathHelper.cos((tileentity.timer + 40 * j) * ((float)Math.PI / 180));

				double x2 = scale * MathHelper.sin((tileentity.timer + 40 * j - sliceAngle) * ((float)Math.PI / 180));
				// double y2 = 0.7*MathHelper.sin((timerentity.timer - 40*j)*(Math.PI/180))*j/10;
				double z2 = scale * MathHelper.cos((tileentity.timer + 40 * j - sliceAngle) * ((float)Math.PI / 180));

				buffer.pos(x1, 0, z1).tex(1, 0).color(0, 0, 0, 255).endVertex();
				buffer.pos(x2, 0, z2).tex(1, 1).color(0, 0, 0, 255).endVertex();

				tessellator.draw();

				GlStateManager.popMatrix();
			}
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}

}
