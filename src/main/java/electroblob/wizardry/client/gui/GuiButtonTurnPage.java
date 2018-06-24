package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class GuiButtonTurnPage extends GuiButton {

	/** True for pointing right (next page), false for pointing left (previous page). */
	private final boolean nextPage;

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/gui/handbook.png");

	public GuiButtonTurnPage(int id, int x, int y, boolean isNextPage){
		super(id, x, y, 23, 13, "");
		this.nextPage = isNextPage;
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){
		
		if(this.visible){
			
			boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			minecraft.getTextureManager().bindTexture(texture);
			int k = 0;
			int l = 192;

			if(flag){
				k += 23;
			}

			if(!this.nextPage){
				l += 13;
			}

			WizardryUtilities.drawTexturedRect(this.x, this.y, k, l, 23, 13, 288, 256);
		}
	}
}
