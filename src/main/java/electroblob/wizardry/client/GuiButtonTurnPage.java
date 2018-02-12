package electroblob.wizardry.client;

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

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(Minecraft par1Minecraft, int par2, int par3){
		if(this.visible){
			boolean flag = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width
					&& par3 < this.y + this.height;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			par1Minecraft.getTextureManager().bindTexture(texture);
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
