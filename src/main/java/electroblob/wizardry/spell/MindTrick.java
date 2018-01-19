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

public class MindTrick extends Spell {

	public MindTrick() {
		super(Tier.BASIC, 10, Element.NECROMANCY, "mind_trick", SpellType.ATTACK, 40, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 8*modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(!world.isRemote){
				
				if(target instanceof EntityPlayer){
					
					target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (int)(300*modifiers.get(WizardryItems.duration_upgrade)), 0));
					
				}else if(target instanceof EntityLiving){
					
					((EntityLiving)target).setAttackTarget(null);
					target.addPotionEffect(new PotionEffect(WizardryPotions.mind_trick, (int)(300*modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
			}else{
				for(int i=0; i<10; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
				}
			}
			
			target.playSound(WizardrySounds.SPELL_DEFLECTION, 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers) {

		if(target != null){
			if(!world.isRemote){
				if(target instanceof EntityPlayer){
					
					target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (int)(300*modifiers.get(WizardryItems.duration_upgrade)), 0));
					
				}else if(target instanceof EntityLiving){
					
					((EntityLiving)target).setAttackTarget(null);
					target.addPotionEffect(new PotionEffect(WizardryPotions.mind_trick, (int)(300*modifiers.get(WizardryItems.duration_upgrade)), 0));
					
				}
			}else{
				for(int i=0; i<10; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
				}
			}
			
			target.playSound(WizardrySounds.SPELL_DEFLECTION, 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs() {
		return true;
	}
}
