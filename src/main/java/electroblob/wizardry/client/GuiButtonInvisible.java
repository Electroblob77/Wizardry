package electroblob.wizardry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class GuiButtonInvisible extends GuiButton {

    public GuiButtonInvisible(int id, int x, int y, int width, int height){
    	
        super(id, x, y, width, height, "");
    }

	/**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft par1Minecraft, int par2, int par3){
        this.hovered = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
    }
    
}
