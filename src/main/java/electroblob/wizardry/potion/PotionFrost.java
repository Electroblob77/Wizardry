package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class PotionFrost extends Potion {
	
	private static final ResourceLocation potionIcon = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_icon.png");

	public PotionFrost(int id, boolean isBadEffect, int liquidColour) {
		super(id, isBadEffect, liquidColour);
		this.setPotionName("potion.frost");
		// With -0.5 as the 'amount', frost 1 slows the entity down by a half and frost 2 roots it to the spot
		this.func_111184_a(SharedMonsterAttributes.movementSpeed, "35dded48-2f19-4541-8510-b29e2dc2cd51", -Wizardry.FROST_SLOWNESS_PER_LEVEL, 2);
		//More UUIDs: 85602e0b-4801-4a87-94f3-bf617c97014e
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength) {
		// Nothing here because this potion works on attribute modifiers.
    }
	
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
		
        mc.renderEngine.bindTexture(potionIcon);
        this.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);

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
