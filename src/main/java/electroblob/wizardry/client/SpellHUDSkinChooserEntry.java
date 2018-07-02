package electroblob.wizardry.client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import electroblob.wizardry.client.gui.GuiSelectHUDSkin;
import electroblob.wizardry.client.gui.GuiSpellDisplay;
import net.minecraftforge.client.gui.ForgeGuiFactory.ForgeConfigGui.ModIDEntry;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.SelectValueEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * Custom config GUI for spell HUD skin selection; displays a list of all the loaded skins and a preview of the currently
 * selected skin. based off of {@link ModIDEntry} from Forge.
 */
public class SpellHUDSkinChooserEntry extends SelectValueEntry {
	
    public SpellHUDSkinChooserEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
    {
        super(owningScreen, owningEntryList, prop, getSelectableValues());
        if (this.selectableValues.size() == 0)
            this.btnValue.enabled = false;
    }

    private static Map<Object, String> getSelectableValues(){
    	return GuiSpellDisplay.getSkins().entrySet().stream().collect(Collectors.toMap(Entry::getKey,
    			e -> e.getValue().getName()));
    }
    
    @Override // Copied from superclass to use custom child screen GUI class
    public void valueButtonPressed(int slotIndex){
        mc.displayGuiScreen(new GuiSelectHUDSkin(this.owningScreen, configElement, slotIndex, selectableValues, currentValue, enabled()));
    }

}
