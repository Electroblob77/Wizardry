package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class PotionFrost extends Potion implements ICustomPotionParticles {

	private static final ResourceLocation ICON = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_icon.png");

	public PotionFrost(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour);
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion.wizardry:frost");
		// With -0.5 as the 'amount', frost 1 slows the entity down by a half and frost 2 roots it to the spot
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"35dded48-2f19-4541-8510-b29e2dc2cd51", -Constants.FROST_SLOWNESS_PER_LEVEL, 2);
		// More UUIDs: 85602e0b-4801-4a87-94f3-bf617c97014e
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength){
		// Nothing here because this potion works on attribute modifiers.
	}

	@Override
	public void spawnCustomParticle(World world, double x, double y, double z){
		Wizardry.proxy.spawnParticle(WizardryParticleType.SNOW, world, x, y, z, 0, -0.02, 0,
				15 + world.rand.nextInt(5));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc){
		mc.renderEngine.bindTexture(ICON);
		WizardryUtilities.drawTexturedRect(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha){
		mc.renderEngine.bindTexture(ICON);
		WizardryUtilities.drawTexturedRect(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
	}

	@SubscribeEvent
	public static void onBreakSpeedEvent(BreakSpeed event){
		if(event.getEntityPlayer().isPotionActive(WizardryPotions.frost)){
			// Amplifier + 1 because it starts at 0
			event.setNewSpeed(event.getOriginalSpeed() * (1 - Constants.FROST_FATIGUE_PER_LEVEL
					* (event.getEntityPlayer().getActivePotionEffect(WizardryPotions.frost).getAmplifier() + 1)));
		}
	}

}
