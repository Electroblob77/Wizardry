package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Shield;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
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

			if(WizardData.get(player).getVariable(Shield.SHIELD_KEY) != null && WizardryUtilities.isCasting(player, Spells.shield)){

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

				GlStateManager.translate(0, 0, 0.8);

				Tessellator tessellator = Tessellator.getInstance();

				Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

				render(tessellator);

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

		if(WizardData.get(player).getVariable(Shield.SHIELD_KEY) != null && WizardryUtilities.isCasting(player, Spells.shield)){

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			// For some reason, the old blend function (GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA) caused the inner
			// edges to appear black, so I have changed it to this, which looks very slightly different.
			GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			Vec3d delta = player.getPositionEyes(event.getPartialRenderTick())
					.subtract(Minecraft.getMinecraft().player.getPositionEyes(event.getPartialRenderTick()));
			GlStateManager.translate(delta.x, delta.y, delta.z);

			GlStateManager.translate(0, 1.3, 0);

			// GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(-player.renderYawOffset, 0, 1, 0);
			// GlStateManager.rotate(-player.rotationPitch, 1, 0, 0);

			GlStateManager.translate(0, 0, 0.8);

			Tessellator tessellator = Tessellator.getInstance();

			Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

			render(tessellator);

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
