package electroblob.wizardry;

import java.util.Set;

import electroblob.wizardry.client.GuiConfigWizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

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
 
    @SuppressWarnings("deprecation") // It's a bit of Forge that hasn't been tidied up, but we have to implement it.
	@Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
    {
        return null;
    }
}