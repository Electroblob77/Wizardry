package electroblob.wizardry.client.gui.handbook;

import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;

//@SideOnly(Side.CLIENT)
public class HandbookToast implements IToast {

    private final Section section;

    public HandbookToast(Section section){
        this.section = section;
    }

    public IToast.Visibility draw(GuiToast toastGui, long delta){

        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);

        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 32, 160, 32);

        boolean firstPart = delta < 1500L;

        int a = firstPart ? MathHelper.floor(MathHelper.clamp((float)(1500L - delta) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864
                          : MathHelper.floor(MathHelper.clamp((float)(delta - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;

        String s = firstPart ? I18n.format("handbook.toast.title") : section.title;

        int c = firstPart ? -11534256 : -16777216;

        List<String> list = toastGui.getMinecraft().fontRenderer.listFormattedStringToWidth(s, 125);

        int h = 16 - list.size() * toastGui.getMinecraft().fontRenderer.FONT_HEIGHT / 2;

        for(String line : list){
            toastGui.getMinecraft().fontRenderer.drawString(line, 30, h, c | a);
            h += toastGui.getMinecraft().fontRenderer.FONT_HEIGHT;
        }

        RenderHelper.enableGUIStandardItemLighting();
        toastGui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(null, new ItemStack(WizardryItems.wizard_handbook), 8, 8);

        return delta >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }

}