package electroblob.wizardry;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import electroblob.wizardry.client.ClientProxy;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import net.minecraft.client.Minecraft;

public class WizardryKeyHandler {

    boolean NkeyPressed = false;
    boolean BkeyPressed = false;
    boolean NkeyAlreadyPressed = false;
    boolean BkeyAlreadyPressed = false;
    
    @SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		
		// Key pressed
		if(Keyboard.getEventKeyState()){
			
			if(Wizardry.proxy instanceof ClientProxy){
				
				ClientProxy proxy = (ClientProxy)Wizardry.proxy;
				
			    if(proxy.nextSpell.isPressed() && Minecraft.getMinecraft().inGameHasFocus){
		     		if(!NkeyPressed){
		     			NkeyPressed = true;
		     		}else{
		     			NkeyAlreadyPressed = true;
		     		}
		     		if(!NkeyAlreadyPressed){
		     			// Packet building
		            	IMessage msg = new PacketControlInput.Message(1);
		            	WizardryPacketHandler.net.sendToServer(msg);
		     		}
			    }
			    
			    if(proxy.previousSpell.isPressed() && Minecraft.getMinecraft().inGameHasFocus){
			    	if(!BkeyPressed){
		     			BkeyPressed = true;
		     		}else{
		     			BkeyAlreadyPressed = true;
		     		}
		    		if(!BkeyAlreadyPressed){
		     			// Packet building
		            	IMessage msg = new PacketControlInput.Message(2);
		            	WizardryPacketHandler.net.sendToServer(msg);
		    		}
			    }
			}
		}
		
		// Key released
		else{
			if(NkeyPressed){
				NkeyPressed = false;
				NkeyAlreadyPressed = false;
			}else if(BkeyPressed){
				BkeyPressed = false;
				BkeyAlreadyPressed = false;
			}
		}
	}
}
