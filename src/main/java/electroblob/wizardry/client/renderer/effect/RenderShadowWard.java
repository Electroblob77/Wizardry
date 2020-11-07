package electroblob.wizardry.client.renderer.effect;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
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
public class RenderShadowWard {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/shadow_ward.png");

	// First person
	@SubscribeEvent
	public static void onRenderWorldLastEvent(RenderWorldLastEvent event){
		// Only render in first person
		if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0){

			EntityPlayer player = Minecraft.getMinecraft().player;

			if(EntityUtils.isCasting(player, Spells.shadow_ward)){

				GlStateManager.pushMatrix();

				GlStateManager.enableBlend();
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				//GlStateManager.shadeModel(GL11.GL_SMOOTH);
				GlStateManager.disableLighting();
				//GlStateManager.disableAlpha();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

				GlStateManager.translate(0, 1.2, 0);
				GlStateManager.rotate(-player.rotationYaw, 0, 1, 0);
				GlStateManager.rotate(player.rotationPitch, 1, 0, 0);

				Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

				GlStateManager.pushMatrix();

				GlStateManager.translate(0, 0, 1.2);
				GlStateManager.rotate(player.ticksExisted * -2, 0, 0, 1);
				GlStateManager.scale(1.1, 1.1, 1.1);

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
				buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();
				buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
				buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();

				tessellator.draw();

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
				buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();
				buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
				buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();

				tessellator.draw();

				GlStateManager.popMatrix();

				//GlStateManager.shadeModel(GL11.GL_FLAT);
				GlStateManager.enableLighting();
				GlStateManager.disableBlend();

				GlStateManager.popMatrix();

			}
		}
	}

	// Third person
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Post event){

		EntityPlayer player = event.getEntityPlayer();

		if(EntityUtils.isCasting(player, Spells.shadow_ward)){

			GlStateManager.pushMatrix();

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.translate(event.getX(), event.getY(), event.getZ());

			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(-player.renderYawOffset, 0, 1, 0);

			Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.translate(0, 1.2, 0);
			GlStateManager.rotate(player.ticksExisted * -2, 0, 0, 1);
			GlStateManager.scale(1.1, 1.1, 1.1);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
			buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
			buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.enableLighting();
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();

		}
	}

}
