package electroblob.wizardry.potion;

import electroblob.wizardry.client.DrawingUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * As of Wizardry 4.2, this class is used by all of wizardry's potions. Potions that work solely on events
 * instantiate this class directly, all other potions extend it.
 */
public class PotionMagicEffect extends Potion {

	private final ResourceLocation texture;

	public PotionMagicEffect(boolean isBadEffect, int liquidColour, ResourceLocation texture){
		super(isBadEffect, liquidColour);
		this.texture = texture;
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength){
		// Nothing here because this potion works on events.
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc){
		drawIcon(x + 6, y + 7, effect, mc);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha){
		net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, alpha);
		drawIcon(x + 3, y + 3, effect, mc);
	}
	
	@SideOnly(Side.CLIENT)
	protected void drawIcon(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc){
		mc.renderEngine.bindTexture(texture);
		DrawingUtils.drawTexturedRect(x, y, 0, 0, 18, 18, 18, 18);
	}

}
