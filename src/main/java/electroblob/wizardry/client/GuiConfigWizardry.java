package electroblob.wizardry.client;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.client.config.DummyConfigElement.DummyCategoryElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import cpw.mods.fml.client.config.IConfigElement;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class GuiConfigWizardry extends GuiConfig {

	public GuiConfigWizardry(GuiScreen parent){
		super(parent, getConfigEntries(), Wizardry.MODID, false, false, Wizardry.NAME + " - " + StatCollector.translateToLocal("config.title.general"));
		//this.titleLine2 = "File location: " + Wizardry.config.getConfigFile().getAbsolutePath();
	}
	
	private static List<IConfigElement> getConfigEntries(){
		List<IConfigElement> configList = new ArrayList<IConfigElement>(1);
		configList.add(new DummyCategoryElement("spellsConfig", "config.category.spells", SpellsCategory.class));
		configList.add(new DummyCategoryElement("resistancesConfig", "config.category.resistances", ResistancesCategory.class));
		configList.add(new DummyCategoryElement("idsConfig", "config.category.ids", IDsCategory.class));
		configList.addAll(new ConfigElement(Wizardry.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
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
                    (new ConfigElement(Wizardry.config.getCategory(Wizardry.SPELLS_CATEGORY))).getChildElements(), 
                    this.owningScreen.modID, Wizardry.SPELLS_CATEGORY, false, false,
                    Wizardry.NAME + " - " + StatCollector.translateToLocal("config.title.spells"));
        	
        	spellsMenu.titleLine2 = StatCollector.translateToLocal("config.subtitle.spells");
        	
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
                    (new ConfigElement(Wizardry.config.getCategory(Wizardry.RESISTANCES_CATEGORY))).getChildElements(), 
                    this.owningScreen.modID, Wizardry.RESISTANCES_CATEGORY, false, false,
                    Wizardry.NAME + " - " + StatCollector.translateToLocal("config.title.resistances"));
        	
        	idsMenu.titleLine2 = StatCollector.translateToLocal("config.subtitle.resistances");
        	
            return idsMenu;
        }
    }
    
    /** IDs category of the config gui. */
    public static class IDsCategory extends CategoryEntry
    {
        public IDsCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
        {
            super(owningScreen, owningEntryList, prop);
        }
        
        @Override
        protected GuiScreen buildChildScreen()
        {
            // This GuiConfig object specifies the configID of the object and as such will force-save when it is closed.
        	// The parent GuiConfig object's entryList will also be refreshed to reflect the changes.
        	GuiConfig idsMenu = new GuiConfig(this.owningScreen, 
                    (new ConfigElement(Wizardry.config.getCategory(Wizardry.IDS_CATEGORY))).getChildElements(), 
                    this.owningScreen.modID, Wizardry.IDS_CATEGORY, false, false,
                    Wizardry.NAME + " - " + StatCollector.translateToLocal("config.title.ids"));
        	
        	idsMenu.titleLine2 = StatCollector.translateToLocal("config.subtitle.ids");
        	
            return idsMenu;
        }
    }
}
