package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.gui.GuiSpellDisplay;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.PacketSpellQuickAccess;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

/** Event handler class responsible for handling wizardry's controls. */
//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class WizardryControlHandler {

	static boolean NkeyPressed = false;
	static boolean BkeyPressed = false;
	static boolean[] quickAccessKeyPressed = new boolean[ClientProxy.SPELL_QUICK_ACCESS.length];

	// Changed to a tick event to allow mouse button keybinds
	// The 'lag' that happened previously was actually because the code only fired when a keyboard key was pressed!
	@SubscribeEvent
	public static void onTickEvent(TickEvent.ClientTickEvent event){

		if(event.phase == TickEvent.Phase.END) return; // Only really needs to be once per tick

		if(Wizardry.proxy instanceof ClientProxy){

			EntityPlayer player = Minecraft.getMinecraft().player;

			if(player != null){

				ItemStack wand = getWandInUse(player);
				if(wand == null) return;

				if(ClientProxy.NEXT_SPELL.isKeyDown() && Minecraft.getMinecraft().inGameHasFocus){
					if(!NkeyPressed){
						NkeyPressed = true;
						selectNextSpell(wand);
					}
				}else{
					NkeyPressed = false;
				}

				if(ClientProxy.PREVIOUS_SPELL.isKeyDown() && Minecraft.getMinecraft().inGameHasFocus){
					if(!BkeyPressed){
						BkeyPressed = true;
						// Packet building
						selectPreviousSpell(wand);
					}
				}else{
					BkeyPressed = false;
				}

				for(int i = 0; i < ClientProxy.SPELL_QUICK_ACCESS.length; i++){
					if(ClientProxy.SPELL_QUICK_ACCESS[i].isKeyDown() && Minecraft.getMinecraft().inGameHasFocus){
						if(!quickAccessKeyPressed[i]){
							quickAccessKeyPressed[i] = true;
							// Packet building
							selectSpell(wand, i);
						}
					}else{
						quickAccessKeyPressed[i] = false;
					}
				}

			}
		}
	}
	
	// Shift-scrolling to change spells
	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event){

		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack wand = getWandInUse(player);
		if(wand == null) return;

		if(Minecraft.getMinecraft().inGameHasFocus && !wand.isEmpty() && event.getDwheel() != 0 && player.isSneaking()
				&& Wizardry.settings.shiftScrolling){

			event.setCanceled(true);
			
			int d = Wizardry.settings.reverseScrollDirection ? -event.getDwheel() : event.getDwheel();

			if(d > 0){
				selectNextSpell(wand);
			}else if(d < 0){
				selectPreviousSpell(wand);
			}
		}
	}

	private static ItemStack getWandInUse(EntityPlayer player){

		ItemStack wand = player.getHeldItemMainhand();

		// Only bother sending packets if the player is holding a spellcasting item with more than one spell slot
		if(!(wand.getItem() instanceof ISpellCastingItem) || ((ISpellCastingItem)wand.getItem()).getSpells(wand).length < 2){
			wand = player.getHeldItemOffhand();
			if(!(wand.getItem() instanceof ISpellCastingItem) || ((ISpellCastingItem)wand.getItem()).getSpells(wand).length < 2) return null;
		}

		return wand;
	}
	
	private static void selectNextSpell(ItemStack wand){
		// Packet building
		IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.NEXT_SPELL_KEY);
		WizardryPacketHandler.net.sendToServer(msg);
		// GUI switch animation
		((ISpellCastingItem)wand.getItem()).selectNextSpell(wand); // Makes sure the spell is set immediately for the client
		GuiSpellDisplay.playSpellSwitchAnimation(true);
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.ITEM_WAND_SWITCH_SPELL, 1));
	}
	
	private static void selectPreviousSpell(ItemStack wand){
		// Packet building
		IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.PREVIOUS_SPELL_KEY);
		WizardryPacketHandler.net.sendToServer(msg);
		// GUI switch animation
		((ISpellCastingItem)wand.getItem()).selectPreviousSpell(wand); // Makes sure the spell is set immediately for the client
		GuiSpellDisplay.playSpellSwitchAnimation(false);
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.ITEM_WAND_SWITCH_SPELL, 1));
	}

	private static void selectSpell(ItemStack wand, int index){
		// GUI switch animation
		if(((ISpellCastingItem)wand.getItem()).selectSpell(wand, index)){ // Makes sure the spell is set immediately for the client
			// Packet building (no point sending it unless the client-side spell selection succeeded
			IMessage msg = new PacketSpellQuickAccess.Message(index);
			WizardryPacketHandler.net.sendToServer(msg);

			GuiSpellDisplay.playSpellSwitchAnimation(true); // This will do, it's only an animation
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.ITEM_WAND_SWITCH_SPELL, 1));
		}
	}

}
