package electroblob.wizardry.client.renderer.overlay;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(Side.CLIENT)
public class RenderBlinkEffect {

	/** The remaining time for which the blink screen overlay effect will be displayed in first-person. Since this is
	 * only for the first-person player (the instance of which is itself stored in a static variable), this can simply
	 * be stored statically here, rather than needing to be in {@code WizardData}. */
	private static int blinkEffectTimer;
	/** The number of ticks the blink effect lasts for. */
	private static final int BLINK_EFFECT_DURATION = 8;

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/blink_overlay.png");

	/** Starts the first-person blink overlay effect. */
	public static void playBlinkEffect(){
		if(Wizardry.settings.blinkEffect) blinkEffectTimer = BLINK_EFFECT_DURATION;
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.player == Minecraft.getMinecraft().player && event.phase == TickEvent.Phase.END){

			if(Wizardry.settings.blinkEffect){
				if(blinkEffectTimer > 0) blinkEffectTimer--;
			}else{
				blinkEffectTimer = 0;
			}
		}
	}

	@SubscribeEvent
	public static void onFOVUpdateEvent(FOVUpdateEvent event){

		if(blinkEffectTimer > 0){
			float f = ((float)Math.max(blinkEffectTimer - 2, 0))/BLINK_EFFECT_DURATION;
			event.setNewfov(event.getFov() + f * f * 0.7f);
		}
	}

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET){

			if(blinkEffectTimer > 0){

				float alpha = ((float)blinkEffectTimer)/BLINK_EFFECT_DURATION;

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, alpha);
				GlStateManager.disableAlpha();

				WizardryClientEventHandler.renderScreenOverlay(event.getResolution(), SCREEN_OVERLAY_TEXTURE);

				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
		}
	}

}
