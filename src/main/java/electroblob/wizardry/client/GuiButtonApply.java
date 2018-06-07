package electroblob.wizardry.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
class GuiButtonApply extends GuiButton {
	
    private static final ResourceLocation texture = new ResourceLocation("wizardry:textures/gui/handbook.png");

    public GuiButtonApply(int id, int x, int y){
    	
        super(id, x, y, 32, 16, StatCollector.translateToLocal("container.arcaneWorkbench.apply"));
    }

	/**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft minecraft, int par2, int par3){
    	// Whether the button is highlighted
        this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

    	int k = 100;
    	int l = 220;
    	int colour = 14737632;
    	
        if(this.enabled){
        	if(this.field_146123_n){
        		k += this.width*2;
        		colour = 16777120;
        	}
        }else{
        	k += this.width;
        	colour = 10526880;
        }
        
        this.drawTexturedRect(this.xPosition, this.yPosition, k, l, this.width, this.height, 256, 256);
        this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, colour);

    }

    /**
     * Draws a textured rectangle, taking the size of the image and the bit needed into account.
     * @param x The x position of the rectangle
     * @param y The y position of the rectangle
     * @param u The x position of the top left corner of the section of the image wanted
     * @param v The y position of the top left corner of the section of the image wanted
     * @param width The width of the section
     * @param height The height of the section
     * @param textureWidth The width of the actual image.
     * @param textureHeight The height of the actual image.
     */
    public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
    {
        float f = 1F / (float)textureWidth;
        float f1 = 1F / (float)textureHeight;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
        tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
        tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
        tessellator.draw();
    }
}
