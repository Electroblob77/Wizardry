package electroblob.wizardry.client.gui;

import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.Locale;

@EventBusSubscriber(Side.CLIENT)
public class CustomCreativeSearchHandler {

	/** Reflected into {@code GuiContainerCreative#searchField} */
	private static final Field searchField;

	private static GuiTextField currentSearchField = null;

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

}
