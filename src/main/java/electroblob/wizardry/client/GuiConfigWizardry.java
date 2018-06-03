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
		super(parent, getConfigEntries(), Wizardry.MODID, false, false,
				Wizardry.NAME + " - " + I18n.format("config." + Wizardry.MODID + ".title.general"));
		// this.titleLine2 = "File location: " + Wizardry.config.getConfigFile().getAbsolutePath();
	}

	private static List<IConfigElement> getConfigEntries(){
		
		List<IConfigElement> configList = new ArrayList<IConfigElement>(1);
		
		configList.add(new DummyCategoryElement("spellsConfig", "config." + Wizardry.MODID + ".category." + Settings.SPELLS_CATEGORY, SpellsCategory.class));
		configList.add(new DummyCategoryElement("resistancesConfig", "config." + Wizardry.MODID + ".category." + Settings.RESISTANCES_CATEGORY, ResistancesCategory.class));
		
		configList.addAll(new ConfigElement(Wizardry.settings.getConfigCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		
		return configList;
	}
	
	// The reason this system is so convoluted is that it's designed for use with the @Config annotation. The problem is,
	// I'm not sure whether that will play well with the load phases. Hmmm...
	
	public static abstract class CategoryBase extends CategoryEntry {
		
		private final String category;
		
		public CategoryBase(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop, String category){
			super(owningScreen, owningEntryList, prop);
			this.category = category;
		}

		@Override
		protected GuiScreen buildChildScreen(){
			// This GuiConfig object specifies the configID of the object and as such will force-save when it is closed.
			// The parent GuiConfig object's entryList will also be refreshed to reflect the changes.
			GuiConfig childScreen = new GuiConfig(this.owningScreen,
					(new ConfigElement(Wizardry.settings.getConfigCategory(category))).getChildElements(),
					this.owningScreen.modID, category, false, false,
					Wizardry.NAME + " - " + I18n.format("config." + Wizardry.MODID + ".title." + category));

			childScreen.titleLine2 = I18n.format("config." + Wizardry.MODID + ".subtitle." + category);

			return childScreen;
		}
	}
	
	/** Resistances category of the config gui. */
	public static class SpellsCategory extends CategoryBase {
		
		public SpellsCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop, Settings.SPELLS_CATEGORY);
		}
	}

	/** Resistances category of the config gui. */
	public static class ResistancesCategory extends CategoryBase {
		
		public ResistancesCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop, Settings.RESISTANCES_CATEGORY);
		}
	}
}
