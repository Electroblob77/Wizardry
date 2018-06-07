package electroblob.wizardry.client;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.GuiEditArray;
import cpw.mods.fml.client.config.GuiEditArrayEntries;
import cpw.mods.fml.client.config.GuiSelectString;
import cpw.mods.fml.client.config.GuiUtils;
import cpw.mods.fml.client.config.GuiEditArrayEntries.StringEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import cpw.mods.fml.client.config.IConfigElement;

public class EntityNameEntry extends StringEntry {

	protected final GuiButtonExt btnValue;
	protected Object entityClass;

    public EntityNameEntry(GuiEditArray owningScreen, GuiEditArrayEntries owningEntryList, IConfigElement configElement, Object value)
    {
        super(owningScreen, owningEntryList, configElement, value);
        this.btnValue = new GuiButtonExt(0, 0, 0, owningEntryList.controlWidth, 18, I18n.format(this.textFieldValue.getText()));
        //this.btnValue.enabled = owningScreen.enabled;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected)
    {
        //super.drawEntry(slotIndex, x, y, listWidth, slotHeight, tessellator, mouseX, mouseY, isSelected);
        this.btnValue.xPosition = listWidth / 4;
        this.btnValue.yPosition = y;

        String trans = I18n.format(this.textFieldValue.getText());
        if (!trans.equals(this.textFieldValue.getText()))
            this.btnValue.displayString = trans;
        else
            this.btnValue.displayString = this.textFieldValue.getText();
        //btnValue.packedFGColour = value ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);

        this.btnValue.drawButton(owningEntryList.mc, mouseX, mouseY);
    }

    /**
     * Returns true if the mouse has been pressed on this control.
     */
    @Override
    public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        if (this.btnValue.mousePressed(owningEntryList.mc, x, y))
        {
            btnValue.func_146113_a(owningEntryList.mc.getSoundHandler());
            Minecraft.getMinecraft().displayGuiScreen(new GuiSelectString(this.owningScreen, configElement, index, EntityList.classToStringMapping, this.getValue(), true));
            owningEntryList.recalculateState();
            return true;
        }

        return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
    }

    /**
     * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
     */
    @Override
    public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        this.btnValue.mouseReleased(x, y);
        super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
    }

    @Override
    public Object getValue()
    {
        return this.textFieldValue.getText();
    }

}
