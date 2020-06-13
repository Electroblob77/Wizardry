package electroblob.wizardry.client.gui;

import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Resurrection;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiButtonResurrect extends GuiButton {

	private static int timeSinceDeath = -1;

	private final String translationKey;

	public GuiButtonResurrect(int id, int x, int y, String translationKey){
		super(id, x, y, I18n.format(translationKey + "_wait", Resurrection.getRemainingWaitTime(timeSinceDeath)));
		this.translationKey = translationKey;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks){
		int waitTime = Resurrection.getRemainingWaitTime(timeSinceDeath);
		this.enabled = waitTime == 0;
		this.displayString = I18n.format(translationKey + (waitTime == 0 ? "_ready" : "_wait"), waitTime);
		super.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	// Event handlers

	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event){
		if(event.phase == TickEvent.Phase.START && timeSinceDeath >= 0) timeSinceDeath++;
	}

	@SubscribeEvent
	public static void onGuiScreenInitEvent(GuiScreenEvent.InitGuiEvent event){

		if(event.getGui() instanceof GuiGameOver && ItemArtefact.isArtefactActive(Minecraft.getMinecraft().player, WizardryItems.amulet_resurrection)
				&& InventoryUtils.getHotbar(Minecraft.getMinecraft().player).stream().anyMatch(s -> Resurrection.canStackResurrect(s, Minecraft.getMinecraft().player))){

			event.getButtonList().add(new GuiButtonResurrect(event.getButtonList().size(), event.getGui().width / 2 - 100,
					event.getGui().height / 4 + 120, "spell." + Spells.resurrection.getRegistryName() + ".button"));
			timeSinceDeath = 0;
		}
	}

	@SubscribeEvent
	public static void onGuiScreenActionPerformedEvent(GuiScreenEvent.ActionPerformedEvent event){

		if(event.getGui() instanceof GuiGameOver){

			ItemStack stack = InventoryUtils.getHotbar(Minecraft.getMinecraft().player).stream()
					.filter(s -> Resurrection.canStackResurrect(s, Minecraft.getMinecraft().player)).findFirst().orElse(null);

			if(stack != null){

				if(event.getButton() instanceof GuiButtonResurrect && timeSinceDeath >= 0){
					// Cast resurrection on the client player and notify the server to do the same
					// ISpellCastingItem#canCast already checked in Resurrection#canStackResurrect
					((ISpellCastingItem)stack.getItem()).cast(stack, Spells.resurrection, Minecraft.getMinecraft().player, EnumHand.MAIN_HAND, 0, new SpellModifiers());
					WizardryPacketHandler.net.sendToServer(new PacketControlInput.Message(PacketControlInput.ControlType.RESURRECT_BUTTON));

				}else if(!Minecraft.getMinecraft().world.getGameRules().getBoolean("keepInventory")){
					// Any other button drops the wand (N.B. this should be inside the stack != null check or it'll send
					// packets unnecessarily and generate incorrect warnings
					WizardryPacketHandler.net.sendToServer(new PacketControlInput.Message(PacketControlInput.ControlType.CANCEL_RESURRECT));
				}

				timeSinceDeath = -1;
			}
		}
	}

}
