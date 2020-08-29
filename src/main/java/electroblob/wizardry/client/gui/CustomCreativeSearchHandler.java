package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.Locale;

@EventBusSubscriber(Side.CLIENT)
public class CustomCreativeSearchHandler {

	private static final int SEARCH_TOOLTIP_HOVER_TIME = 20;

	private static final Style TOOLTIP_SYNTAX = new Style().setColor(TextFormatting.YELLOW);
	private static final Style TOOLTIP_BODY = new Style().setColor(TextFormatting.WHITE);

	/** Reflected into {@code GuiContainerCreative#searchField} */
	private static final Field searchField;

	private static GuiTextField currentSearchField = null;

	private static int searchBarHoverTime;

	static {
		searchField = ObfuscationReflectionHelper.findField(GuiContainerCreative.class, "field_147062_A");
	}

	@SubscribeEvent
	public static void onInitGuiEvent(GuiScreenEvent.InitGuiEvent.Post event){
		// Reduces reflection as much as possible - searchField is created on GUI init and never reassigned so we need
		// not (and should not!) use reflection to retrieve it every time a key is typed
		if(event.getGui() instanceof GuiContainerCreative){
			try{
				currentSearchField = (GuiTextField)searchField.get(event.getGui());
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public static void onKeyboardInputEvent(GuiScreenEvent.KeyboardInputEvent.Post event){
		// Custom creative tab search behaviour
		if(event.getGui() instanceof GuiContainerCreative){

			GuiContainerCreative gui = (GuiContainerCreative)event.getGui();
			CreativeTabs tab = CreativeTabs.CREATIVE_TAB_ARRAY[gui.getSelectedTabIndex()];

			if(tab == WizardryTabs.SPELLS){

				GuiContainerCreative.ContainerCreative container = (GuiContainerCreative.ContainerCreative)gui.inventorySlots;
				container.itemList.clear(); // Required or duplicates will appear!
				String searchText = currentSearchField.getText().toLowerCase(Locale.ROOT);
				tab.displayAllRelevantItems(container.itemList);

				if(!searchText.isEmpty()){
					container.itemList.removeIf(s -> !Spell.byMetadata(s.getMetadata()).matches(searchText));
					container.scrollTo(0); // Seems to refresh the GUI somehow so it displays correctly
				}
			}
		}
	}

	@SubscribeEvent
	public static void onClientTickEvent(ClientTickEvent event){
		if(event.phase == Phase.END && searchBarHoverTime > 0 && searchBarHoverTime < SEARCH_TOOLTIP_HOVER_TIME){
			searchBarHoverTime++;
		}
	}

	@SubscribeEvent
	public static void onDrawScreenPostEvent(GuiScreenEvent.DrawScreenEvent.Post event){
		if(currentSearchField != null && DrawingUtils.isPointInRegion(currentSearchField.x, currentSearchField.y, currentSearchField.width, currentSearchField.height, event.getMouseX(), event.getMouseY())){
			if(searchBarHoverTime == 0){
				searchBarHoverTime++;
			}else if(searchBarHoverTime == SEARCH_TOOLTIP_HOVER_TIME){
				event.getGui().drawHoveringText(I18n.format("container." + Wizardry.MODID + ":arcane_workbench.search_tooltip",
						TOOLTIP_SYNTAX.getFormattingCode(), TOOLTIP_BODY.getFormattingCode()), event.getMouseX(), event.getMouseY());
			}
		}else{
			searchBarHoverTime = 0;
		}
	}

}
