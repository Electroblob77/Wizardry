package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/** A <b>curse</b> is a permanent potion effect, which is displayed in the inventory with a special background and
 * no timer. It also allows for longer potion effect names by wrapping them onto two lines. */
public class Curse extends PotionMagicEffect {
	
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Wizardry.MODID, "textures/gui/curse_background.png");

	public Curse(boolean isBadEffect, int liquidColour, ResourceLocation texture){
		super(isBadEffect, liquidColour, texture);
	}

	@Override
	public boolean shouldRenderInvText(PotionEffect effect){
		return false;
	}
	
	@Override
	public List<ItemStack> getCurativeItems(){
		return new ArrayList<>(); // Cannot be cured!
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc){
		
		mc.renderEngine.bindTexture(BACKGROUND);
		electroblob.wizardry.client.DrawingUtils.drawTexturedRect(x, y, 0, 0, 140, 32, 256, 256);
		
		super.renderInventoryEffect(x, y, effect, mc);
		
		String name = net.minecraft.client.resources.I18n.format(this.getName());

		// Amplifier 0 (which would be I) is not rendered and the tooltips only go up to X (amplifier 9)
		// The vanilla implementation uses elseifs and only goes up to 4... how lazy.
		if(effect.getAmplifier() > 0 && effect.getAmplifier() < 10){
			name = name + " " + net.minecraft.client.resources.I18n.format("enchantment.level." + (effect.getAmplifier() + 1));
		}

		List<String> lines = mc.fontRenderer.listFormattedStringToWidth(name, 100);
		
		int i=0;
		for(String line : lines){
			int h = lines.size() == 1 ? 5 : i * (mc.fontRenderer.FONT_HEIGHT + 1);
			mc.fontRenderer.drawStringWithShadow(line, (float)(x + 10 + 18), (float)(y + 6 + h), 0xbf00ee);
			i++;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha){
		
		net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, 1);
		mc.renderEngine.bindTexture(BACKGROUND);
		electroblob.wizardry.client.DrawingUtils.drawTexturedRect(x, y, 141, 0, 24, 24, 256, 256);
		super.renderHUDEffect(x, y, effect, mc, alpha);
	}

}
