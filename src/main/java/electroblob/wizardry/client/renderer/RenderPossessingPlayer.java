package electroblob.wizardry.client.renderer;

import electroblob.wizardry.spell.Possession;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RenderPossessingPlayer {

	@SubscribeEvent
	@SuppressWarnings("unchecked") // Can't check it due to type erasure
	public static void onRenderPlayerPreEvent(RenderPlayerEvent.Pre event){

		EntityPlayer player = event.getEntityPlayer();
		EntityLiving possessee = Possession.getPossessee(player);

		if(possessee != null){
			// I reject your renderer and substitute my own!
			Render<EntityLiving> renderer = (Render<EntityLiving>)event.getRenderer().getRenderManager().entityRenderMap.get(possessee.getClass());
			float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getPartialRenderTick();
			possessee.swingProgress = player.swingProgress;
			possessee.prevSwingProgress = player.prevSwingProgress;
			possessee.renderYawOffset = player.renderYawOffset;
			possessee.prevRenderYawOffset = player.prevRenderYawOffset;
			possessee.rotationYawHead = player.rotationYawHead;
			possessee.prevRotationYawHead = player.prevRotationYawHead;
			possessee.rotationPitch = player.rotationPitch;
			possessee.prevRotationPitch = player.prevRotationPitch;
			possessee.limbSwing = player.limbSwing;
			possessee.limbSwingAmount = player.limbSwingAmount;
			possessee.prevLimbSwingAmount = player.prevLimbSwingAmount;
			renderer.doRender(possessee, event.getX(), event.getY(), event.getZ(), yaw, event.getPartialRenderTick());
			event.setCanceled(true);
		}
	}
}
