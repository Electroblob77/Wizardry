package electroblob.wizardry.client.renderer.overlay;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(Side.CLIENT)
public class RenderFrostOverlay {

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_overlay.png");

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET){

			if(Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.frost)){

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableAlpha();

				WizardryClientEventHandler.renderScreenOverlay(event.getResolution(), SCREEN_OVERLAY_TEXTURE);

				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
		}
	}

}
