package electroblob.wizardry.client;

import org.lwjgl.input.Keyboard;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.gui.GuiSpellDisplay;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Event handler class responsible for handling wizardry's controls. */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class WizardryControlHandler {

	static boolean NkeyPressed = false;
	static boolean BkeyPressed = false;
	static boolean NkeyAlreadyPressed = false;
	static boolean BkeyAlreadyPressed = false;

	@SubscribeEvent
	public static void onKeyInput(InputEvent.KeyInputEvent event){

		// Key pressed
		if(Keyboard.getEventKeyState()){

			if(Wizardry.proxy instanceof ClientProxy){
				
				EntityPlayer player = Minecraft.getMinecraft().player;
				ItemStack wand = player.getHeldItemMainhand();

				if(!(wand.getItem() instanceof ItemWand)){
					wand = player.getHeldItemOffhand();
					// If the player isn't holding a wand, then nothing else needs to be done.
					if(!(wand.getItem() instanceof ItemWand)) return;
				}

				if(ClientProxy.NEXT_SPELL.isPressed() && Minecraft.getMinecraft().inGameHasFocus){
					if(!NkeyPressed){
						NkeyPressed = true;
					}else{
						NkeyAlreadyPressed = true;
					}
					if(!NkeyAlreadyPressed){
						selectNextSpell(wand);
					}
				}

				if(ClientProxy.PREVIOUS_SPELL.isPressed() && Minecraft.getMinecraft().inGameHasFocus){
					if(!BkeyPressed){
						BkeyPressed = true;
					}else{
						BkeyAlreadyPressed = true;
					}
					if(!BkeyAlreadyPressed){
						selectPreviousSpell(wand);
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
	
	// Shift-scrolling to change spells
	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event){

		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack wand = player.getHeldItemMainhand();

		if(!(wand.getItem() instanceof ItemWand)){
			wand = player.getHeldItemOffhand();
			// If the player isn't holding a wand, then nothing else needs to be done.
			if(!(wand.getItem() instanceof ItemWand)) return;
		}

		if(Minecraft.getMinecraft().inGameHasFocus && !wand.isEmpty() && event.getDwheel() != 0 && player.isSneaking()
				&& Wizardry.settings.enableShiftScrolling){

			event.setCanceled(true);
			
			int d = Wizardry.settings.reverseScrollDirection ? -event.getDwheel() : event.getDwheel();

			if(d > 0){
				selectNextSpell(wand);
			}else if(d < 0){
				selectPreviousSpell(wand);
			}
		}
	}
	
	private static void selectNextSpell(ItemStack wand){
		// Packet building
		IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.NEXT_SPELL_KEY);
		WizardryPacketHandler.net.sendToServer(msg);
		// GUI switch animation
		WandHelper.selectNextSpell(wand); // Makes sure the spell is set immediately for the client
		GuiSpellDisplay.playSpellSwitchAnimation(true);
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.SELECT_SPELL, 1));
	}
	
	private static void selectPreviousSpell(ItemStack wand){
		// Packet building
		IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.PREVIOUS_SPELL_KEY);
		WizardryPacketHandler.net.sendToServer(msg);
		// GUI switch animation
		WandHelper.selectPreviousSpell(wand); // Makes sure the spell is set immediately for the client
		GuiSpellDisplay.playSpellSwitchAnimation(false);
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.SELECT_SPELL, 1));
	}
}
