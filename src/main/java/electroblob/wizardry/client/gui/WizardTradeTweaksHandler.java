package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class WizardTradeTweaksHandler {

	private static final ResourceLocation NEW_SPELL_ICON = new ResourceLocation(Wizardry.MODID, "textures/gui/container/new_spell_indicator.png");

	private static int tradeIndex; // Mirrors GuiMerchant#selectedMerchantRecipe (don't want to reflect into it every frame)

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onGuiOpenEvent(GuiOpenEvent event){
		if(event.getGui() instanceof GuiMerchant) tradeIndex = 0;
	}

	@SubscribeEvent
	public static void onActionPerformedPostEvent(ActionPerformedEvent.Post event){

		if(event.getGui() instanceof GuiMerchant){

			MerchantRecipeList recipes = ((GuiMerchant)event.getGui()).getMerchant().getRecipes(Minecraft.getMinecraft().player);

			if(recipes == null) return;

			if(event.getButton().id == 1){ // Next
				tradeIndex = Math.min(tradeIndex + 1, recipes.size());
			}else if(event.getButton().id == 2){ // Previous
				tradeIndex = Math.max(tradeIndex - 1, 0);
			}
		}
	}

	// Brute-force fix for crystals not showing up when a wizard is given a spell book in the trade GUI.
	@SubscribeEvent
	public static void onGuiDrawForegroundEvent(GuiContainerEvent.DrawForeground event){

		if(event.getGuiContainer() instanceof GuiMerchant){

			GuiMerchant gui = (GuiMerchant)event.getGuiContainer();
			// Note that gui.getMerchant() returns an NpcMerchant, not an EntityWizard.
			MerchantRecipeList trades = gui.getMerchant().getRecipes(Minecraft.getMinecraft().player);

			if(trades == null) return;

			// Using == the specific item rather than instanceof because that's how trades do it.
			if(gui.inventorySlots.getSlot(0).getStack().getItem() == WizardryItems.spell_book
					|| gui.inventorySlots.getSlot(1).getStack().getItem() == WizardryItems.spell_book){

				for(MerchantRecipe trade : trades){
					if(trade.getItemToBuy().getItem() == WizardryItems.spell_book && trade.getSecondItemToBuy().isEmpty()){
						Slot slot = gui.inventorySlots.getSlot(2);
						// It still doesn't look quite right because the slot highlight is behind the item, but it'll do
						// until/unless I find a better solution.
						DrawingUtils.drawItemAndTooltip(gui, trade.getItemToSell(), slot.xPos, slot.yPos, event.getMouseX(), event.getMouseY(),
								gui.getSlotUnderMouse() == slot);
					}
				}
			}

			// New spell indicator
			if(gui.inventorySlots instanceof ContainerMerchant){

				// Can't use getCurrentRecipe because that only gets updated when the correct items are given
				MerchantRecipe recipe = trades.get(tradeIndex);

				if(recipe != null && recipe.getItemToSell().getItem() instanceof ItemSpellBook){

					EntityPlayer player = Minecraft.getMinecraft().player;

					if(!WizardData.get(player).hasSpellBeenDiscovered(Spell.byMetadata(recipe.getItemToSell().getMetadata()))){

						int x = gui.inventorySlots.getSlot(2).xPos + 14;
						int y = gui.inventorySlots.getSlot(2).yPos - 17;
						int w = 8;
						int h = 8;

						RenderHelper.enableGUIStandardItemLighting();
						GlStateManager.color(1, 1, 1);
						Minecraft.getMinecraft().renderEngine.bindTexture(NEW_SPELL_ICON);
						DrawingUtils.drawTexturedRect(x, y, 0, Math.max(player.ticksExisted/2 % 40 - 36, 0) * 8, w, h, 8, 32);
						RenderHelper.disableStandardItemLighting();

//						int mouseX = event.getMouseX() - gui.getGuiLeft();
//						int mouseY = event.getMouseY() - gui.getGuiTop();
//
//						if(mouseX >= x + 1 && mouseX < x + w + 1 && mouseY >= y - 1 && mouseY < y + h + 1){
//							GuiUtils.drawHoveringText(Collections.singletonList("You haven't discovered this spell yet"), mouseX,
//									mouseY, gui.width, gui.height, 150, Minecraft.getMinecraft().fontRenderer);
//						}
					}
				}
			}
		}
	}

}
