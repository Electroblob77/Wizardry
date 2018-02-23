package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ChainLightning extends Spell {

	public ChainLightning(){
		super(Tier.ADVANCED, 25, Element.LIGHTNING, "chain_lightning", SpellType.ATTACK, 50, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// First shot has range 10 (this is the only range affected by upgrades) and does 5 hearts of damage.
		// Chains to up to 5 secondary targets within a range of 5 of the primary target, and then to up to 2
		// tertiary targets per secondary target within a range of 5 of that. Secondary targets are dealt 4 hearts
		// of damage; tertiary targets are dealt 3 hearts of damage.

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade), 8.0f);

		// Anything can be attacked with the initial arc, because the player has control over where it goes. If they
		// hit a minion or an ally, it's their problem!
		if(rayTrace != null && rayTrace.entityHit != null && WizardryUtilities.isLiving(rayTrace.entityHit)){

			Entity target = rayTrace.entityHit;

			if(!world.isRemote){
				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + caster.height / 2, caster.posZ, target.posX,
						target.posY + target.height / 2, target.posZ);
				world.spawnEntity(arc);
			}else{
				for(int i = 0; i < 8; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
							target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				}
			}

			target.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);

			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						10.0f * modifiers.get(SpellModifiers.DAMAGE));
			}

			// Secondary chaining effect
			double seekerRange = 5.0d;

			List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
					target.posX, target.posY + target.height / 2, target.posZ, world);
			
			secondaryTargets.removeIf(e -> e instanceof EntityArmorStand);

			for(int i = 0; i < Math.min(secondaryTargets.size(), 5); i++){

				EntityLivingBase secondaryTarget = secondaryTargets.get(i);

				if(secondaryTarget != target && WizardryUtilities.isValidTarget(caster, secondaryTarget)){

					if(!world.isRemote){
						EntityArc arc = new EntityArc(world);
						arc.setEndpointCoords(target.posX, target.posY + target.height / 2, target.posZ,
								secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height / 2,
								secondaryTarget.posZ);
						world.spawnEntity(arc);
					}else{
						for(int j = 0; j < 8; j++){
							Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
									secondaryTarget.posX + world.rand.nextFloat() - 0.5,
									secondaryTarget.getEntityBoundingBox().minY + secondaryTarget.height / 2
											+ world.rand.nextFloat() * 2 - 1,
									secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
							world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
									secondaryTarget.posX + world.rand.nextFloat() - 0.5,
									secondaryTarget.getEntityBoundingBox().minY + secondaryTarget.height / 2
											+ world.rand.nextFloat() * 2 - 1,
									secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
						}
					}

					secondaryTarget.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);

					if(MagicDamage.isEntityImmune(DamageType.SHOCK, secondaryTarget)){
						if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist",
								secondaryTarget.getName(), this.getNameForTranslationFormatted()));
					}else{
						secondaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
								8.0f * modifiers.get(SpellModifiers.DAMAGE));
					}

					// Tertiary chaining effect

					List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
							secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height / 2,
							secondaryTarget.posZ, world);
					
					tertiaryTargets.removeIf(e -> e instanceof EntityArmorStand);

					for(int j = 0; j < Math.min(tertiaryTargets.size(), 2); j++){

						EntityLivingBase tertiaryTarget = (EntityLivingBase)tertiaryTargets.get(j);

						if(tertiaryTarget != target && !secondaryTargets.contains(tertiaryTarget)
								&& WizardryUtilities.isValidTarget(caster, tertiaryTarget)){

							if(!world.isRemote){
								EntityArc arc = new EntityArc(world);
								arc.setEndpointCoords(secondaryTarget.posX,
										secondaryTarget.posY + secondaryTarget.height / 2, secondaryTarget.posZ,
										tertiaryTarget.posX, tertiaryTarget.posY + tertiaryTarget.height / 2,
										tertiaryTarget.posZ);
								world.spawnEntity(arc);
							}else{
								for(int k = 0; k < 8; k++){
									Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
											tertiaryTarget.posX + world.rand.nextFloat() - 0.5,
											tertiaryTarget.getEntityBoundingBox().minY + tertiaryTarget.height / 2
													+ world.rand.nextFloat() * 2 - 1,
											tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
									world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
											tertiaryTarget.posX + world.rand.nextFloat() - 0.5,
											tertiaryTarget.getEntityBoundingBox().minY + tertiaryTarget.height / 2
													+ world.rand.nextFloat() * 2 - 1,
											tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
								}
							}

							tertiaryTarget.playSound(WizardrySounds.SPELL_SPARK, 1.0F,
									world.rand.nextFloat() * 0.4F + 1.5F);

							if(MagicDamage.isEntityImmune(DamageType.SHOCK, tertiaryTarget)){
								if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist",
										tertiaryTarget.getName(), this.getNameForTranslationFormatted()));
							}else{
								tertiaryTarget.attackEntityFrom(
										MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
										6.0f * modifiers.get(SpellModifiers.DAMAGE));
							}
						}
					}
				}
			}

			caster.swingArm(hand);
			return true;
		}
		return false;
	}

}
