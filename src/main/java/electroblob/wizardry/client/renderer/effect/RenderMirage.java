package electroblob.wizardry.client.renderer.effect;

import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Random;

@EventBusSubscriber(Side.CLIENT)
public class RenderMirage {

	private static final int BLINK_PERIOD_1 = 47; // Use two prime numbers to make it look as chaotic as possible
	private static final int BLINK_PERIOD_2 = 71;

	private static final double MAX_OFFSET = 3;

	private static final Random random = new Random(); // Seed will be set later

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Pre<?> event){
		if(event.getEntity().isPotionActive(WizardryPotions.mirage)){
			random.setSeed(event.getEntity().ticksExisted / BLINK_PERIOD_1
						 + event.getEntity().ticksExisted / BLINK_PERIOD_2);
			GlStateManager.pushMatrix();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE); // GhoOOoOostly OooOOOo00oOOo
			GlStateManager.color(1, 1, 1, 0.5f);
			GlStateManager.translate((random.nextDouble() * 2 - 1) * MAX_OFFSET,
					(random.nextDouble() * 2 - 1) * MAX_OFFSET, (random.nextDouble() * 2 - 1) * MAX_OFFSET);
		}
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<?> event){
		if(event.getEntity().isPotionActive(WizardryPotions.mirage)){
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.popMatrix();
		}
	}

}
