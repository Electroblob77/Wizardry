package electroblob.wizardry.client.renderer.effect;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Shield;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RenderShield {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/shield.png");

	// First person
	@SubscribeEvent
	public static void onRenderWorldLastEvent(RenderWorldLastEvent event){
		// Only render in first person
		if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0){

			EntityPlayer player = Minecraft.getMinecraft().player;

			if(WizardData.get(player).getVariable(Shield.SHIELD_KEY) != null && EntityUtils.isCasting(player, Spells.shield)){

				GlStateManager.pushMatrix();

				GlStateManager.disableCull();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
				GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

				GlStateManager.translate(0, 1.4, 0);

				GlStateManager.rotate(-player.rotationYaw, 0, 1, 0);
				GlStateManager.rotate(player.rotationPitch, 1, 0, 0);

				GlStateManager.translate(0, 0, 1);

				GlStateManager.pushMatrix();

				// Changes the scale at which the texture is applied to the model. See LayerCreeper for a similar example,
				// but with translation instead of scaling.
				// You can do all sorts of fun stuff with this, just by applying transformations in the 2D texture space.
				GlStateManager.matrixMode(GL11.GL_TEXTURE); // Switch to the 2D texture space
				GlStateManager.loadIdentity();

				GlStateManager.translate(0.5, 0.5, 0);

				float s = 1 - ((player.ticksExisted + event.getPartialTicks()) % 5) / 5 * 0.52f;
				s = s*s;
				GlStateManager.scale(s, s, 1);

				GlStateManager.translate(-0.5, -0.5, 0);

				GlStateManager.matrixMode(GL11.GL_MODELVIEW); // Switch back to the 3D model space

				Tessellator tessellator = Tessellator.getInstance();

				Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

				render(tessellator);

				// Undo the texture scaling
				GlStateManager.matrixMode(GL11.GL_TEXTURE);
				GlStateManager.loadIdentity();
				GlStateManager.matrixMode(GL11.GL_MODELVIEW);

				GlStateManager.popMatrix();

				GlStateManager.enableLighting();

				GlStateManager.shadeModel(GL11.GL_FLAT);
				GlStateManager.enableCull();
				GlStateManager.disableBlend();
				// RenderHelper.enableStandardItemLighting();

				GlStateManager.popMatrix();
			}
		}
	}

	// Third person
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Post event){

		EntityPlayer player = event.getEntityPlayer();

		if(WizardData.get(player).getVariable(Shield.SHIELD_KEY) != null && EntityUtils.isCasting(player, Spells.shield)){

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			// For some reason, the old blend function (GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA) caused the inner
			// edges to appear black, so I have changed it to this, which looks very slightly different.
			GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.translate(event.getX(), event.getY(), event.getZ());

			GlStateManager.translate(0, 1.3, 0);

			// GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(-player.renderYawOffset, 0, 1, 0);
			// GlStateManager.rotate(-player.rotationPitch, 1, 0, 0);

			GlStateManager.translate(0, 0, 1);

			GlStateManager.pushMatrix();

			// Changes the scale at which the texture is applied to the model. See LayerCreeper for a similar example,
			// but with translation instead of scaling.
			// You can do all sorts of fun stuff with this, just by applying transformations in the 2D texture space.
			GlStateManager.matrixMode(GL11.GL_TEXTURE); // Switch to the 2D texture space
			GlStateManager.loadIdentity();

			GlStateManager.translate(0.5, 0.5, 0);

			float s = 1 - ((player.ticksExisted + event.getPartialRenderTick()) % 5) / 5 * 0.52f;
			s = s*s;
			GlStateManager.scale(s, s, 1);

			GlStateManager.translate(-0.5, -0.5, 0);

			GlStateManager.matrixMode(GL11.GL_MODELVIEW); // Switch back to the 3D model space

			Tessellator tessellator = Tessellator.getInstance();

			Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

			render(tessellator);

			// Undo the texture scaling
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);

			GlStateManager.popMatrix();

			GlStateManager.enableLighting();

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			// RenderHelper.enableStandardItemLighting();

			GlStateManager.popMatrix();
		}
	}

	private static void render(Tessellator tessellator){

		BufferBuilder buffer = tessellator.getBuffer();

		double widthOuter = 0.6d;
		double heightOuter = 0.7d;
		double widthInner = 0.3d;
		double heightInner = 0.4d;
		double depth = 0.2d;

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		buffer.pos(-widthOuter, heightInner, -depth).tex(0, 0.2).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(-widthInner, heightOuter, -depth).tex(0.2, 0).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();

		buffer.pos(widthInner, heightOuter, -depth).tex(0.8, 0).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, heightInner, 0).tex(0.8, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthOuter, heightInner, -depth).tex(1, 0.2).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, heightInner, 0).tex(0.8, 0.2).color(200, 200, 255, 255).endVertex();

		buffer.pos(widthOuter, -heightInner, -depth).tex(1, 0.8).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, -heightInner, 0).tex(0.8, 0.8).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthInner, -heightOuter, -depth).tex(0.8, 1).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, -heightInner, 0).tex(0.8, 0.8).color(200, 200, 255, 255).endVertex();

		buffer.pos(-widthInner, -heightOuter, -depth).tex(0.2, 1).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, -heightInner, 0).tex(0.2, 0.8).color(200, 200, 255, 255).endVertex();
		buffer.pos(-widthOuter, -heightInner, -depth).tex(0, 0.8).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, -heightInner, 0).tex(0.2, 0.8).color(200, 200, 255, 255).endVertex();

		buffer.pos(-widthOuter, heightInner, -depth).tex(0, 0.2).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();

		tessellator.draw();

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthInner, heightInner, 0).tex(0.8, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(-widthInner, -heightInner, 0).tex(0.2, 0.8).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthInner, -heightInner, 0).tex(0.8, 0.8).color(200, 200, 255, 255).endVertex();

		tessellator.draw();
	}

}
