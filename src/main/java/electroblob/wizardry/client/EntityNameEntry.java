package electroblob.wizardry.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiEditArray;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries.StringEntry;
import net.minecraftforge.fml.client.config.GuiSelectString;
import net.minecraftforge.fml.client.config.IConfigElement;

/** [NYI] Intended as a way of choosing entities by name from all those currently registered, within the config file, so
 * that users don't have to look up the entity IDs. I can't get this to work correctly at the moment. */
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
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
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

        this.btnValue.drawButton(owningEntryList.getMC(), mouseX, mouseY);
    }

    @Override
    public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
        if (this.btnValue.mousePressed(owningEntryList.getMC(), x, y))
        {
            btnValue.playPressSound(owningEntryList.getMC().getSoundHandler());
            // Some sort of type incompatiblity meant that I had to do this first.
            Map<Object, String> map = new HashMap<Object, String>(EntityList.CLASS_TO_NAME);
            Minecraft.getMinecraft().displayGuiScreen(new GuiSelectString(this.owningScreen, configElement, index, map, this.getValue(), true));
            owningEntryList.recalculateState();
            return true;
        }

        return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
    }

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
