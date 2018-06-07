package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class PotionDecay extends Potion {
	
	private static final ResourceLocation potionIcon = new ResourceLocation(Wizardry.MODID, "textures/gui/decay_icon.png");

	public PotionDecay(int id, boolean isBadEffect, int liquidColour) {
		super(id, isBadEffect, liquidColour);
		this.setPotionName("potion.decay");
		this.func_111184_a(SharedMonsterAttributes.movementSpeed, "85602e0b-4801-4a87-94f3-bf617c97014e", -Wizardry.DECAY_SLOWNESS_PER_LEVEL, 2);
	}
	
	@Override
	public boolean isReady(int p_76397_1_, int p_76397_2_) {
		// Copied from the vanilla wither effect. It does the timing stuff. 25 is the number of ticks between hits at
		// amplifier 0
		int k = 25 >> p_76397_2_;
		return k > 0 ? p_76397_1_ % k == 0 : true;
	}
	
	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength) {
		entitylivingbase.attackEntityFrom(DamageSource.wither, 1);
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
