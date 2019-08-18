package electroblob.wizardry;

import electroblob.wizardry.client.gui.config.GuiConfigWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Set;

public class WizardryGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance){
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories(){
		return null;
	}

	@Override
	public boolean hasConfigGui(){
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen){
		return new GuiConfigWizardry(parentScreen);
	}
}