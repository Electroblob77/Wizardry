package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod.EventBusSubscriber
public class PotionDecay extends PotionMagicEffect {

	public PotionDecay(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/decay.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":decay");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"85602e0b-4801-4a87-94f3-bf617c97014e", -Constants.DECAY_SLOWNESS_PER_LEVEL, 2);
	}

	@Override
	public boolean isReady(int duration, int amplifier){
		// Copied from the vanilla wither effect. It does the timing stuff. 25 is the number of ticks between hits at
		// amplifier 0
		int k = 25 >> amplifier;
		return k > 0 ? duration % k == 0 : true;
	}

	@Override
	public void performEffect(EntityLivingBase host, int strength){
		host.attackEntityFrom(DamageSource.WITHER, 1);
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		
		// This can't be in performEffect because that method is called at a certain frequency which depends on the
		// amplifier of the potion effect, and is too slow for this purpose.
		
		EntityLivingBase target = event.getEntityLiving();

		// Do the timing check first, it'll cut out 95% of calls to all subsequent conditions
		if(target.ticksExisted % Constants.DECAY_SPREAD_INTERVAL == 0 && !target.world.isRemote
				&& target.isPotionActive(WizardryPotions.decay) && target.onGround){

			List<Entity> entities = target.world.getEntitiesWithinAABBExcludingEntity(target,
					target.getEntityBoundingBox());
			
			for(Entity entity : entities){
				if(entity instanceof EntityDecay) return; // Don't spawn another decay if there's already one there
			}
			
			// The victim spreading the decay is the 'caster' here, so that it can actually wear off, otherwise it
			// just gets infected with its own decay and the effect lasts forever.
			EntityDecay decay = new EntityDecay(target.world);
			decay.setCaster(target);
			decay.setPosition(target.posX, target.posY, target.posZ);
			target.world.spawnEntity(decay);
		}
	}

}
