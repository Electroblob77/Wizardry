package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ScreenShakeHandler {

	/** The remaining time for which the screen shake effect will be active. */
	private static int screenShakeCounter = 0;
	private static final float SHAKINESS = 0.5f;

	/** Starts the client-side screen shake effect. */
	public static void shakeScreen(float intensity){
		if(Wizardry.settings.screenShake){
			screenShakeCounter = (int)(intensity / SHAKINESS);
			Minecraft.getMinecraft().player.rotationPitch -= intensity * 0.5f; // Start halfway down
		}
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.player == Minecraft.getMinecraft().player && event.phase == TickEvent.Phase.END){

			if(Wizardry.settings.screenShake){
				if(screenShakeCounter > 0){
					float magnitude = screenShakeCounter * SHAKINESS;
					Minecraft.getMinecraft().player.rotationPitch += screenShakeCounter % 2 == 0 ? magnitude : -magnitude;
					screenShakeCounter--;
				}
			}else{
				screenShakeCounter = 0;
			}
		}
	}

}
