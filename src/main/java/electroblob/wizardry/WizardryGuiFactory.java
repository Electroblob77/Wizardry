package electroblob.wizardry;

import java.util.Set;

import electroblob.wizardry.client.gui.GuiConfigWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

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