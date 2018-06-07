package electroblob.wizardry.potion;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionMagicEffect extends Potion {
	
	private static final ResourceLocation potionIcons = new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons.png");
	private final int textureIndex;
	
	public PotionMagicEffect(int id, boolean isBadEffect, int liquidColour, int textureIndex) {
		super(id, isBadEffect, liquidColour);
		this.textureIndex = textureIndex;
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength) {
		// Nothing here because this potion works on events.
    }
	
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
        mc.renderEngine.bindTexture(potionIcons);
        this.drawTexturedRect(x + 6, y + 7, 18*(textureIndex%4), 18*(textureIndex/4), 18, 18, 72, 72);
        
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();

    }
	
	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into account. Client side only, of course.
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 */
	public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
	{
	    float f = 1F / (float)textureWidth;
	    float f1 = 1F / (float)textureHeight;
	    net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.instance;
	    tessellator.startDrawingQuads();
	    tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
	    tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
	    tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
	    tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
	    tessellator.draw();
	}

}
