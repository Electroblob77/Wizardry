package electroblob.wizardry.client.gui.config;

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

import java.util.ArrayList;
import java.util.List;

public class GuiConfigWizardry extends GuiConfig {

	public GuiConfigWizardry(GuiScreen parent){
		super(parent, getConfigEntries(), Wizardry.MODID, false, false,
				Wizardry.NAME + " - " + I18n.format("config." + Wizardry.MODID + ".title.general"));
		// this.titleLine2 = "File location: " + Wizardry.config.getConfigFile().getAbsolutePath();
	}

	private static List<IConfigElement> getConfigEntries(){
		
		List<IConfigElement> configList = new ArrayList<>(1);

		configList.add(new DummyCategoryElement("gameplayConfig",			"config." + Wizardry.MODID + ".category." + Settings.GAMEPLAY_CATEGORY, 		GameplayCategory.class));
		configList.add(new DummyCategoryElement("difficultyConfig",		"config." + Wizardry.MODID + ".category." + Settings.DIFFICULTY_CATEGORY, 	DifficultyCategory.class));
		configList.add(new DummyCategoryElement("worldgenConfig",			"config." + Wizardry.MODID + ".category." + Settings.WORLDGEN_CATEGORY, 		WorldgenCategory.class));
		configList.add(new DummyCategoryElement("tweaksConfig",			"config." + Wizardry.MODID + ".category." + Settings.TWEAKS_CATEGORY, 		TweaksCategory.class));
		configList.add(new DummyCategoryElement("commandsConfig",			"config." + Wizardry.MODID + ".category." + Settings.COMMANDS_CATEGORY, 		CommandsCategory.class));
		configList.add(new DummyCategoryElement("clientConfig",			"config." + Wizardry.MODID + ".category." + Settings.CLIENT_CATEGORY, 		ClientCategory.class));
		configList.add(new DummyCategoryElement("spellsConfig",			"config." + Wizardry.MODID + ".category." + Settings.SPELLS_CATEGORY, 		SpellsCategory.class));
		configList.add(new DummyCategoryElement("artefactsConfig",		"config." + Wizardry.MODID + ".category." + Settings.ARTEFACTS_CATEGORY, 	ArtefactsCategory.class));
		configList.add(new DummyCategoryElement("resistancesConfig",		"config." + Wizardry.MODID + ".category." + Settings.RESISTANCES_CATEGORY, 	ResistancesCategory.class));
		configList.add(new DummyCategoryElement("compatibilityConfig",	"config." + Wizardry.MODID + ".category." + Settings.COMPATIBILITY_CATEGORY, CompatibilityCategory.class));

		configList.addAll(new ConfigElement(Wizardry.settings.getConfigCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
		
		return configList;
	}
	
	// The reason this system is so convoluted is that it's designed for use with the @Config annotation. The problem is,
	// I'm not sure whether that will play well with the load phases. Hmmm...

	public static abstract class CategoryBase extends CategoryEntry {
		
		public CategoryBase(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override
		protected GuiScreen buildChildScreen(){
			
			String category = this.getCategory();
			
			// This GuiConfig object specifies the configID of the object and as such will force-save when it is closed.
			// The parent GuiConfig object's entryList will also be refreshed to reflect the changes.
			GuiConfig childScreen = new GuiConfig(this.owningScreen,
					(new ConfigElement(Wizardry.settings.getConfigCategory(category))).getChildElements(),
					this.owningScreen.modID, category, false, false,
					Wizardry.NAME + " - " + I18n.format("config." + Wizardry.MODID + ".title." + category));

			childScreen.titleLine2 = I18n.format("config." + Wizardry.MODID + ".subtitle." + category);

			return childScreen;
		}
		
		protected abstract String getCategory();
	}
	
	/** Gameplay category of the config gui. */
	public static class GameplayCategory extends CategoryBase {
		
		public GameplayCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}
		
		@Override protected String getCategory() { return Settings.GAMEPLAY_CATEGORY; }
	}

	/** Difficulty category of the config gui. */
	public static class DifficultyCategory extends CategoryBase {

		public DifficultyCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.DIFFICULTY_CATEGORY; }
	}

	/** Worldgen category of the config gui. */
	public static class WorldgenCategory extends CategoryBase {

		public WorldgenCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.WORLDGEN_CATEGORY; }
	}

	/** Tweaks category of the config gui. */
	public static class TweaksCategory extends CategoryBase {

		public TweaksCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.TWEAKS_CATEGORY; }
	}

	/** Commands category of the config gui. */
	public static class CommandsCategory extends CategoryBase {

		public CommandsCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.COMMANDS_CATEGORY; }
	}

	/** Client category of the config gui. */
	public static class ClientCategory extends CategoryBase {

		public ClientCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.CLIENT_CATEGORY; }
	}
	
	/** Spells category of the config gui. */
	public static class SpellsCategory extends CategoryBase {
		
		public SpellsCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}
		
		@Override protected String getCategory() { return Settings.SPELLS_CATEGORY; }
	}

	/** Artefacts category of the config gui. */
	public static class ArtefactsCategory extends CategoryBase {

		public ArtefactsCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.ARTEFACTS_CATEGORY; }
	}

	/** Resistances category of the config gui. */
	public static class ResistancesCategory extends CategoryBase {
		
		public ResistancesCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}
		
		@Override protected String getCategory() { return Settings.RESISTANCES_CATEGORY; }
	}

	/** Commands category of the config gui. */
	public static class CompatibilityCategory extends CategoryBase {

		public CompatibilityCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop){
			super(owningScreen, owningEntryList, prop);
		}

		@Override protected String getCategory() { return Settings.COMPATIBILITY_CATEGORY; }
	}
}
