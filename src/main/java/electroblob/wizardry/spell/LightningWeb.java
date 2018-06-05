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
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class LightningWeb extends Spell {

	public LightningWeb(){
		super(Tier.MASTER, 15, Element.LIGHTNING, "lightning_web", SpellType.ATTACK, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade), 2.0f);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && WizardryUtilities.isLiving(rayTrace.entityHit)){

			Entity target = rayTrace.entityHit;

			if(!world.isRemote){

				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){
					EntityArc arc = new EntityArc(world);
					// The look vec stuff performs a translation on the start point to line it up with the wand.
					// EDIT: removed due to 1st/3rd person render differences.
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ, target.posX,
							target.posY + target.height / 2, target.posZ);
					arc.lifetime = 1;
					world.spawnEntity(arc);
				}

				if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
					if(!world.isRemote && ticksInUse == 1)
						caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
								this.getNameForTranslationFormatted()));
				}else{
					// This motion stuff removes knockback, which is desirable for continuous spells.
					double motionX = target.motionX;
					double motionY = target.motionY;
					double motionZ = target.motionZ;

					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
							5.0f * modifiers.get(SpellModifiers.DAMAGE));

					target.motionX = motionX;
					target.motionY = motionY;
					target.motionZ = motionZ;
				}
			}else{
				for(int i = 0; i < 5; i++){
					Wizardry.proxy.spawnParticle(Type.SPARK, world,
							target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
				}
			}

			// Secondary chaining effect
			double seekerRange = 5.0d;

			List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
					target.posX, target.posY + target.height / 2, target.posZ, world);
			// This is a MUCH better way of filtering the secondary targets!
			secondaryTargets.remove(target);
			if(secondaryTargets.size() > 5) secondaryTargets = secondaryTargets.subList(0, 5);

			for(EntityLivingBase secondaryTarget : secondaryTargets){

				if(WizardryUtilities.isValidTarget(caster, secondaryTarget)){

					if(!world.isRemote){
						// This statement means the arc only spawns every other tick.
						if(ticksInUse % 2 == 0){
							EntityArc arc = new EntityArc(world);
							arc.setEndpointCoords(target.posX, target.posY + 1.2, target.posZ, secondaryTarget.posX,
									secondaryTarget.posY + secondaryTarget.height / 2, secondaryTarget.posZ);
							arc.lifetime = 1;
							world.spawnEntity(arc);
						}

						if(MagicDamage.isEntityImmune(DamageType.SHOCK, secondaryTarget)){
							if(!world.isRemote && ticksInUse == 1)
								caster.sendMessage(new TextComponentTranslation("spell.resist",
										secondaryTarget.getName(), this.getNameForTranslationFormatted()));
						}else{
							// This motion stuff removes knockback, which is desirable for continuous spells.
							double motionX = secondaryTarget.motionX;
							double motionY = secondaryTarget.motionY;
							double motionZ = secondaryTarget.motionZ;

							secondaryTarget.attackEntityFrom(
									MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
									4.0f * modifiers.get(SpellModifiers.DAMAGE));

							secondaryTarget.motionX = motionX;
							secondaryTarget.motionY = motionY;
							secondaryTarget.motionZ = motionZ;
						}
					}else{
						for(int i = 0; i < 5; i++){
							Wizardry.proxy.spawnParticle(Type.SPARK, world,
									secondaryTarget.posX + world.rand.nextFloat() - 0.5,
									secondaryTarget.getEntityBoundingBox().minY + secondaryTarget.height / 2
											+ world.rand.nextFloat() * 2 - 1,
									secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
						}
					}

					// Tertiary chaining effect

					List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
							secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height / 2,
							secondaryTarget.posZ, world);
					tertiaryTargets.remove(target);
					tertiaryTargets.removeAll(secondaryTargets);
					if(tertiaryTargets.size() > 2) tertiaryTargets = tertiaryTargets.subList(0, 2);

					for(EntityLivingBase tertiaryTarget : tertiaryTargets){

						if(WizardryUtilities.isValidTarget(caster, tertiaryTarget)){

							if(!world.isRemote){
								// This statement means the arc only spawns every other tick.
								if(ticksInUse % 2 == 0){
									EntityArc arc = new EntityArc(world);
									arc.setEndpointCoords(secondaryTarget.posX, secondaryTarget.posY + 1.2,
											secondaryTarget.posZ, tertiaryTarget.posX,
											tertiaryTarget.posY + tertiaryTarget.height / 2, tertiaryTarget.posZ);
									arc.lifetime = 1;
									world.spawnEntity(arc);
								}

								if(MagicDamage.isEntityImmune(DamageType.SHOCK, tertiaryTarget)){
									if(!world.isRemote && ticksInUse == 1)
										caster.sendMessage(new TextComponentTranslation("spell.resist",
												tertiaryTarget.getName(), this.getNameForTranslationFormatted()));
								}else{
									// This motion stuff removes knockback, which is desirable for continuous spells.
									double motionX = tertiaryTarget.motionX;
									double motionY = tertiaryTarget.motionY;
									double motionZ = tertiaryTarget.motionZ;

									tertiaryTarget.attackEntityFrom(
											MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
											3.0f * modifiers.get(SpellModifiers.DAMAGE));

									tertiaryTarget.motionX = motionX;
									tertiaryTarget.motionY = motionY;
									tertiaryTarget.motionZ = motionZ;
								}
							}else{
								for(int i = 0; i < 5; i++){
									Wizardry.proxy.spawnParticle(Type.SPARK, world,
											tertiaryTarget.posX + world.rand.nextFloat() - 0.5,
											tertiaryTarget.getEntityBoundingBox().minY + tertiaryTarget.height / 2
													+ world.rand.nextFloat() * 2 - 1,
											tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
								}
							}
						}
					}
				}
			}

			if(ticksInUse == 1){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0F, 1.0f);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_LIGHTNING, 1.0F, 1.0f);
			}

			return true;

		}else{
			if(!world.isRemote){
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){
					EntityArc arc = new EntityArc(world);
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ,
							caster.posX + caster.getLookVec().x * 8,
							caster.posY + caster.eyeHeight + caster.getLookVec().y * 8,
							caster.posZ + caster.getLookVec().z * 8);
					arc.lifetime = 1;
					// arc.setOffset(entityplayer.getLookVec().zCoord * 0.5, entityplayer.getLookVec().xCoord * 0.5);
					world.spawnEntity(arc);
				}
			}

			if(ticksInUse == 1){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0F, 1.0f);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_LIGHTNING, 1.0F, 1.0f);
			}

			return true;
		}
	}

}
