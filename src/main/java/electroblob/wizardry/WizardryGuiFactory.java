package electroblob.wizardry;

import java.util.Set;

import cpw.mods.fml.client.IModGuiFactory;
import electroblob.wizardry.client.GuiConfigWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class WizardryGuiFactory implements IModGuiFactory {
	
    @Override
    public void initialize(Minecraft minecraftInstance) 
    {
 
    }
 
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return GuiConfigWizardry.class;
    }
 
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
    {
        return null;
    }
 
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
    {
        return null;
    }
}