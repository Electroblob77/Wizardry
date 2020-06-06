package electroblob.wizardry.client.renderer.overlay;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(Side.CLIENT)
public class RenderTargetPointers {

	private static final ResourceLocation POINTER_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/pointer.png");
	private static final ResourceLocation TARGET_POINTER_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/target_pointer.png");

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event){

		Minecraft mc = Minecraft.getMinecraft();
		WizardData data = WizardData.get(mc.player);
		RenderManager renderManager = event.getRenderer().getRenderManager();

		ItemStack wand = mc.player.getHeldItemMainhand();

		if(!(wand.getItem() instanceof ISpellCastingItem)){
			wand = mc.player.getHeldItemOffhand();
		}

		// Target selection pointer
		if(mc.player.isSneaking() && wand.getItem() instanceof ISpellCastingItem && WizardryUtilities.isLiving(event.getEntity())
				&& data != null && data.selectedMinion != null){

			// -> Moved this in here so it isn't called every tick
			RayTraceResult rayTrace = RayTracer.standardEntityRayTrace(mc.world, mc.player, 16, false);

			if(rayTrace != null && rayTrace.entityHit == event.getEntity()){

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();

				GlStateManager.pushMatrix();

				GlStateManager.disableCull();
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
				// Disabling depth test allows it to be seen through everything.
				GlStateManager.disableDepth();
				GlStateManager.color(1, 1, 1, 1);

				GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height + 0.5, event.getZ());

				// This counteracts the reverse rotation behaviour when in front f5 view.
				// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
				float yaw = mc.gameSettings.thirdPersonView == 2 ? renderManager.playerViewX : -renderManager.playerViewX;
				GlStateManager.rotate(180 - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				mc.renderEngine.bindTexture(TARGET_POINTER_TEXTURE);

				buffer.pos(-0.2, 0.24, 0).tex(0, 0).endVertex();
				buffer.pos(0.2, 0.24, 0).tex(9f / 16f, 0).endVertex();
				buffer.pos(0.2, -0.24, 0).tex(9f / 16f, 11f / 16f).endVertex();
				buffer.pos(-0.2, -0.24, 0).tex(0, 11f / 16f).endVertex();

				tessellator.draw();

				GlStateManager.enableCull();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();

				GlStateManager.popMatrix();
			}
		}

		// Summoned creature selection pointer
		if(data != null && data.selectedMinion != null && data.selectedMinion.get() == event.getEntity()){

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			// Disabling depth test allows it to be seen through everything.
			GlStateManager.disableDepth();
			GlStateManager.color(1, 1, 1, 1);

			GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height + 0.5, event.getZ());

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? renderManager.playerViewX : -renderManager.playerViewX;
			GlStateManager.rotate(180 - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			mc.renderEngine.bindTexture(POINTER_TEXTURE);

			buffer.pos(-0.2, 0.24, 0).tex(0, 0).endVertex();
			buffer.pos(0.2, 0.24, 0).tex(9f / 16f, 0).endVertex();
			buffer.pos(0.2, -0.24, 0).tex(9f / 16f, 11f / 16f).endVertex();
			buffer.pos(-0.2, -0.24, 0).tex(0, 11f / 16f).endVertex();

			tessellator.draw();

			GlStateManager.enableCull();
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();

			GlStateManager.popMatrix();
		}
	}

}
