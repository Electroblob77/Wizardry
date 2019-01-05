package electroblob.wizardry.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonInvisible extends GuiButton {

	public GuiButtonInvisible(int id, int x, int y, int width, int height){
		super(id, x, y, width, height, "");
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
	}

}
