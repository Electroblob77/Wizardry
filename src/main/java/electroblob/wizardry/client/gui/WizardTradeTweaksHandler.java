package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;

@EventBusSubscriber(Side.CLIENT)
public class WizardTradeTweaksHandler {

	private static final ResourceLocation NEW_SPELL_ICON = new ResourceLocation(Wizardry.MODID, "textures/gui/container/new_spell_indicator.png");

	private static final int ICON_WIDTH = 8;
	private static final int ICON_HEIGHT = 8;

	private static final int ANIMATION_FRAMES = 4;
	private static final int ANIMATION_FRAME_TIME = 2; // In ticks
	private static final int ANIMATION_PERIOD = 40; // In frame durations

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

        GuiContainer gui = event.getGuiContainer();

        MerchantRecipeList trades = null;

        // Add else ifs for other mods that modify the GUI here, if necessary. This is a bit of a hack, but it works for mod compatibility.
        if (gui instanceof GuiMerchant){

            trades = ((GuiMerchant)gui).getMerchant().getRecipes(Minecraft.getMinecraft().player);

        } else if (gui.getClass().getName().equals("com.fuzs.gamblingstyle.client.gui.GuiVillager")) {

            try {

                Field merchantField = gui.getClass().getDeclaredField("merchant");

                merchantField.setAccessible(true);

                IMerchant merchant = (IMerchant)merchantField.get(gui);

                trades = merchant.getRecipes(Minecraft.getMinecraft().player);

            } catch(Exception e) {

                Wizardry.logger.error("Failed to access Gambling Style merchant", e);
                return;
            }

        } else {
            return;
        }

        if (trades == null) return;

        // Using == the specific item rather than instanceof because that's how trades do it.
        if (gui.inventorySlots.getSlot(0).getStack().getItem() == WizardryItems.spell_book || gui.inventorySlots.getSlot(1).getStack().getItem() == WizardryItems.spell_book) {

            for (MerchantRecipe trade : trades) {

                if (trade.getItemToBuy().getItem() == WizardryItems.spell_book && trade.getSecondItemToBuy().isEmpty()) {

                    Slot slot = gui.inventorySlots.getSlot(2);

                    DrawingUtils.drawItemAndTooltip(
                            gui,
                            trade.getItemToSell(),
                            slot.xPos,
                            slot.yPos,
                            event.getMouseX(),
                            event.getMouseY(),
                            gui.getSlotUnderMouse() == slot
                    );
                }
            }
        }

        // Vanilla merchant-only new spell indicator
        if (gui instanceof GuiMerchant) {

            if (tradeIndex < trades.size()) {

                MerchantRecipe recipe = trades.get(tradeIndex);

                if (recipe != null && recipe.getItemToSell().getItem() instanceof ItemSpellBook) {

                    EntityPlayer player = Minecraft.getMinecraft().player;
                    Spell spell = Spell.byMetadata(recipe.getItemToSell().getMetadata());

                    if (Wizardry.settings.discoveryMode
                            && !player.isCreative()
                            && Wizardry.proxy.shouldDisplayDiscovered(
                            spell,
                            recipe.getItemToSell())
                            && WizardData.get(player) != null
                            && !WizardData.get(player)
                            .hasSpellBeenDiscovered(spell)
                    ) {

                        int x = gui.inventorySlots.getSlot(2).xPos + 14;
                        int y = gui.inventorySlots.getSlot(2).yPos - 17;

                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.color(1, 1, 1);
                        Minecraft.getMinecraft().renderEngine.bindTexture(NEW_SPELL_ICON);

                        int frame = Math.max(player.ticksExisted / ANIMATION_FRAME_TIME % ANIMATION_PERIOD - (ANIMATION_PERIOD - ANIMATION_FRAMES), 0);

                        DrawingUtils.drawTexturedRect(
                                x,
                                y,
                                0,
                                frame * ICON_HEIGHT,
                                ICON_WIDTH,
                                ICON_HEIGHT,
                                ICON_WIDTH,
                                ICON_HEIGHT * ANIMATION_FRAMES
                        );

                        RenderHelper.disableStandardItemLighting();
                    }
                }
            }
        }
    }

}
