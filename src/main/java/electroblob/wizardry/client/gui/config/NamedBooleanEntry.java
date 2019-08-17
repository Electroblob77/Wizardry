package electroblob.wizardry.client.gui.config;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.client.config.IConfigElement;

/**
 * Same as {@link net.minecraftforge.fml.client.config.GuiConfigEntries.BooleanEntry}, but instead of simply
 * displaying 'true' or 'false', allows the two display strings to be specified in the lang file.
 */
// BooleanEntry's constructors are private, so I had to copy the whole goddamn class to change one method. Thanks Forge.
public class NamedBooleanEntry extends GuiConfigEntries.ButtonEntry {

	protected final boolean beforeValue;
	protected boolean       currentValue;

	private static final String DEFAULT_KEY = "config.ebwizardry.generic";

	public NamedBooleanEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement){
		super(owningScreen, owningEntryList, configElement);
		this.beforeValue = Boolean.valueOf(configElement.get().toString());
		this.currentValue = beforeValue;
		this.btnValue.enabled = enabled();
		updateValueButtonText();
	}

	// This is the only method that's any different
	@Override
	public void updateValueButtonText(){

		String langKey = configElement.getLanguageKey() + "." + currentValue;
		this.btnValue.displayString = I18n.format(langKey);
		// If the key is unspecified, it defaults to the generic 'Enabled'/'Disabled' keys and adds a red/green colour
		if(this.btnValue.displayString.equals(langKey)){
			this.btnValue.displayString = I18n.format(DEFAULT_KEY + "." + currentValue);
			btnValue.packedFGColour = currentValue ? GuiUtils.getColorCode('a', true) : GuiUtils.getColorCode('c', true);
		}
	}

	// Everything from here down is the same as BooleanEntry

	@Override
	public void valueButtonPressed(int slotIndex){
		if(enabled()) currentValue = !currentValue;
	}

	@Override
	public boolean isDefault(){
		return currentValue == Boolean.valueOf(configElement.getDefault().toString());
	}

	@Override
	public void setToDefault(){
		if(enabled()){
			currentValue = Boolean.valueOf(configElement.getDefault().toString());
			updateValueButtonText();
		}
	}

	@Override
	public boolean isChanged(){
		return currentValue != beforeValue;
	}

	@Override
	public void undoChanges(){
		if(enabled()){
			currentValue = beforeValue;
			updateValueButtonText();
		}
	}

	@Override
	public boolean saveConfigElement(){
		if(enabled() && isChanged()){
			configElement.set(currentValue);
			return configElement.requiresMcRestart();
		}
		return false;
	}

	@Override
	public Boolean getCurrentValue(){
		return currentValue;
	}

	@Override
	public Boolean[] getCurrentValues(){
		return new Boolean[]{getCurrentValue()};
	}
}
