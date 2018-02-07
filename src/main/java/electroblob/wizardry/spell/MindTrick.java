package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class MindTrick extends Spell {

	public MindTrick(){
		super(Tier.BASIC, 10, Element.NECROMANCY, "mind_trick", SpellType.ATTACK, 40, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				8 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(!world.isRemote){

				if(target instanceof EntityPlayer){

					target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA,
							(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));

				}else if(target instanceof EntityLiving){

					((EntityLiving)target).setAttackTarget(null);
					target.addPotionEffect(new PotionEffect(WizardryPotions.mind_trick,
							(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
			}else{
				for(int i = 0; i < 10; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
							target.posX - 0.25 + world.rand.nextDouble() * 0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25
									+ world.rand.nextDouble() * 0.5,
							target.posZ - 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
				}
			}

			target.playSound(WizardrySounds.SPELL_DEFLECTION, 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			if(!world.isRemote){
				if(target instanceof EntityPlayer){

					target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA,
							(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));

				}else if(target instanceof EntityLiving){

					((EntityLiving)target).setAttackTarget(null);
					target.addPotionEffect(new PotionEffect(WizardryPotions.mind_trick,
							(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));

				}
			}else{
				for(int i = 0; i < 10; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
							target.posX - 0.25 + world.rand.nextDouble() * 0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25
									+ world.rand.nextDouble() * 0.5,
							target.posZ - 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
				}
			}

			target.playSound(WizardrySounds.SPELL_DEFLECTION, 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() != null && event.getSource().getEntity() instanceof EntityLivingBase){
			// Cancels the mind trick effect if the creature takes damage
			// This has been moved to within an (event.getSource().getEntity() instanceof EntityLivingBase) check so it
			// doesn't
			// crash the game with a ConcurrentModificationException. If you think about it, mind trick only ought to be
			// cancelled if something attacks the entity since potions, drowning, cacti etc. don't affect the targeting.
			if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_trick)){
				event.getEntityLiving().removePotionEffect(WizardryPotions.mind_trick);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// Mind trick
		// If the target is null already, no need to set it to null, or infinite loops will occur.
		if((event.getEntityLiving().isPotionActive(WizardryPotions.mind_trick)
				|| event.getEntityLiving().isPotionActive(WizardryPotions.fear))
				&& event.getEntityLiving() instanceof EntityLiving && event.getTarget() != null){
			((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}
	}
}
