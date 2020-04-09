package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class PotionFrost extends PotionMagicEffect implements ICustomPotionParticles {

	public PotionFrost(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/frost.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":frost");
		// With -0.5 as the 'amount', frost 1 slows the entity down by a half and frost 2 roots it to the spot
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"35dded48-2f19-4541-8510-b29e2dc2cd51", -Constants.FROST_SLOWNESS_PER_LEVEL, 2);
		// More UUIDs: 85602e0b-4801-4a87-94f3-bf617c97014e
	}

	@Override
	public void spawnCustomParticle(World world, double x, double y, double z){
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).time(15 + world.rand.nextInt(5)).spawn(world);
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
