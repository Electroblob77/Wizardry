package electroblob.wizardry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class LockCameraHandler {
    public static float currentEventCameraPitch;
    public static float currentEventCameraYaw;
    public static float lockedEventCameraPitch;
    public static float lockedEventCameraYaw;
    public static float lockedCameraPitch;
    public static float lockedCameraYaw;
    public static float lockedPlayerPitch;
    public static float lockedPlayerYaw;
    public static boolean isCameraLocked;

    public static void lockCurrentCamera(){
        lockedEventCameraPitch = currentEventCameraPitch;
        lockedEventCameraYaw = currentEventCameraYaw;
        lockedCameraPitch = Minecraft.getMinecraft().player.cameraPitch;
        lockedCameraYaw = Minecraft.getMinecraft().player.cameraYaw;
        lockedPlayerPitch = Minecraft.getMinecraft().player.rotationPitch;
        lockedPlayerYaw = Minecraft.getMinecraft().player.rotationYaw;
        isCameraLocked = true;
    }

    public static void unlockCurrentCamera(){
        isCameraLocked = false;
    }

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        if(playerSP != null){
            if(isCameraLocked){

                playerSP.prevRotationPitch = lockedPlayerPitch;
                playerSP.rotationPitch = lockedPlayerPitch;
                playerSP.prevRotationYaw = lockedPlayerYaw;
                playerSP.rotationYaw = lockedPlayerYaw;

                playerSP.prevCameraPitch = lockedCameraPitch;
                playerSP.cameraPitch = lockedCameraPitch;
                playerSP.prevCameraYaw = lockedCameraYaw;
                playerSP.cameraYaw = lockedCameraYaw;

                event.setPitch(lockedEventCameraPitch);
                event.setYaw(lockedEventCameraYaw);

            }else{
                currentEventCameraPitch = event.getPitch();
                currentEventCameraYaw = event.getYaw();
            }
        }
    }
}
