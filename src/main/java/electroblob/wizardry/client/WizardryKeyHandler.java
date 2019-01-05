package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class WizardryKeyHandler {

	static boolean NkeyPressed = false;
	static boolean BkeyPressed = false;

	// Changed to a tick event to allow mouse button keybinds
	// The 'lag' that happened previously was actually because the code only fired when a keyboard key was pressed!
	@SubscribeEvent
	public static void onTickEvent(TickEvent.ClientTickEvent event){

		if(event.phase == TickEvent.Phase.END) return; // Only really needs to be once per tick

		if(Wizardry.proxy instanceof ClientProxy){

			EntityPlayer player = Minecraft.getMinecraft().player;

			if(player != null){

				ItemStack wand = player.getHeldItemMainhand();

				if(!(wand.getItem() instanceof ItemWand)){
					wand = player.getHeldItemOffhand();
					// If the player isn't holding a wand, then nothing else needs to be done.
					if(!(wand.getItem() instanceof ItemWand)) return;
				}
			}

			if(ClientProxy.NEXT_SPELL.isKeyDown() && Minecraft.getMinecraft().inGameHasFocus){
				if(!NkeyPressed){
					NkeyPressed = true;
					// Packet building
					IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.NEXT_SPELL_KEY);
					WizardryPacketHandler.net.sendToServer(msg);
				}
			}else{
				NkeyPressed = false;
			}

			if(ClientProxy.PREVIOUS_SPELL.isKeyDown() && Minecraft.getMinecraft().inGameHasFocus){
				if(!BkeyPressed){
					BkeyPressed = true;
					// Packet building
					IMessage msg = new PacketControlInput.Message(
							PacketControlInput.ControlType.PREVIOUS_SPELL_KEY);
					WizardryPacketHandler.net.sendToServer(msg);
				}
			}else{
				BkeyPressed = false;
			}
		}
	}
}
