package electroblob.wizardry.client;

import java.util.ArrayList;
import java.util.List;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiConfigWizardry extends GuiConfig {

	public GuiConfigWizardry(GuiScreen parent){
		super(parent, getConfigEntries(), Wizardry.MODID, false, false, Wizardry.NAME + " - " + I18n.format("config.wizardry.title.general"));
		//this.titleLine2 = "File location: " + Wizardry.config.getConfigFile().getAbsolutePath();
	}
	
	private static List<IConfigElement> getConfigEntries(){
		List<IConfigElement> configList = new ArrayList<IConfigElement>(1);
		configList.add(new DummyCategoryElement("spellsConfig", "config.wizardry.category." + Settings.SPELLS_CATEGORY, SpellsCategory.class));
		configList.add(new DummyCategoryElement("resistancesConfig", "config.wizardry.category." + Settings.RESISTANCES_CATEGORY, ResistancesCategory.class));
		configList.addAll(new ConfigElement(Wizardry.settings.getConfigCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		return configList;
	}
	
	/** Spells category of the config gui. This adds a button which opens up the spells category config. */
    public static class SpellsCategory extends CategoryEntry
    {
        public SpellsCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
        {
            super(owningScreen, owningEntryList, prop);
        }
        
        @Override
        protected GuiScreen buildChildScreen()
        {
            // This GuiConfig object specifies the configID of the object and as such will force-save when it is closed.
        	// The parent GuiConfig object's entryList will also be refreshed to reflect the changes.
        	GuiConfig spellsMenu = new GuiConfig(this.owningScreen, 
                    (new ConfigElement(Wizardry.settings.getConfigCategory(Settings.SPELLS_CATEGORY))).getChildElements(), 
                    this.owningScreen.modID, Settings.SPELLS_CATEGORY, false, false,
                    Wizardry.NAME + " - " + I18n.format("config.wizardry.title." + Settings.SPELLS_CATEGORY));
        	
        	spellsMenu.titleLine2 = I18n.format("config.wizardry.subtitle." + Settings.SPELLS_CATEGORY);
        	
            return spellsMenu;
        }
    }
    
    /** Resistances category of the config gui. */
    public static class ResistancesCategory extends CategoryEntry
    {
        public ResistancesCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
        {
            super(owningScreen, owningEntryList, prop);
        }
        
        @Override
        protected GuiScreen buildChildScreen()
        {
            // This GuiConfig object specifies the configID of the object and as such will force-save when it is closed.
        	// The parent GuiConfig object's entryList will also be refreshed to reflect the changes.
        	GuiConfig idsMenu = new GuiConfig(this.owningScreen, 
                    (new ConfigElement(Wizardry.settings.getConfigCategory(Settings.RESISTANCES_CATEGORY))).getChildElements(), 
                    this.owningScreen.modID, Settings.RESISTANCES_CATEGORY, false, false,
                    Wizardry.NAME + " - " + I18n.format("config.wizardry.title." + Settings.RESISTANCES_CATEGORY));
        	
        	idsMenu.titleLine2 = I18n.format("config.wizardry.subtitle." + Settings.RESISTANCES_CATEGORY);
        	
            return idsMenu;
        }
    }
}
