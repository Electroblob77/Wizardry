package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Class for all potions that work on events only. */
public class PotionMagicEffect extends Potion {
	
	private static final ResourceLocation ICONS = new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons.png");
	private final int textureIndex;
	
	public PotionMagicEffect(boolean isBadEffect, int liquidColour, int textureIndex) {
		super(isBadEffect, liquidColour);
		this.textureIndex = textureIndex;
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength) {
		// Nothing here because this potion works on events.
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc){
        mc.renderEngine.bindTexture(ICONS);
        WizardryUtilities.drawTexturedRect(x + 6, y + 7, 18*(textureIndex%4), 18*(textureIndex/4), 18, 18, 72, 72);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		mc.renderEngine.bindTexture(ICONS);
		WizardryUtilities.drawTexturedRect(x + 3, y + 3, 18*(textureIndex%4), 18*(textureIndex/4), 18, 18, 72, 72);
	}

}
