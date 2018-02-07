package electroblob.wizardry;

import org.lwjgl.input.Keyboard;

import electroblob.wizardry.client.ClientProxy;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class WizardryKeyHandler {

	boolean NkeyPressed = false;
	boolean BkeyPressed = false;
	boolean NkeyAlreadyPressed = false;
	boolean BkeyAlreadyPressed = false;

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event){

		// Key pressed
		if(Keyboard.getEventKeyState()){

			if(Wizardry.proxy instanceof ClientProxy){

				if(ClientProxy.NEXT_SPELL.isPressed() && Minecraft.getMinecraft().inGameHasFocus){
					if(!NkeyPressed){
						NkeyPressed = true;
					}else{
						NkeyAlreadyPressed = true;
					}
					if(!NkeyAlreadyPressed){
						// Packet building
						IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.NEXT_SPELL_KEY);
						WizardryPacketHandler.net.sendToServer(msg);
					}
				}

				if(ClientProxy.PREVIOUS_SPELL.isPressed() && Minecraft.getMinecraft().inGameHasFocus){
					if(!BkeyPressed){
						BkeyPressed = true;
					}else{
						BkeyAlreadyPressed = true;
					}
					if(!BkeyAlreadyPressed){
						// Packet building
						IMessage msg = new PacketControlInput.Message(
								PacketControlInput.ControlType.PREVIOUS_SPELL_KEY);
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
