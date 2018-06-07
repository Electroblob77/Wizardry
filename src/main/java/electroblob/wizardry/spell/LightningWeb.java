package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityStormElemental;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class LightningWeb extends Spell {

	public LightningWeb() {
		super(EnumTier.MASTER, 15, EnumElement.LIGHTNING, "lightning_web", EnumSpellType.ATTACK, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier, 2.0f);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			Entity target = rayTrace.entityHit;
			
			if(!world.isRemote){
				
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){
					EntityArc arc = new EntityArc(world);
					// The look vec stuff performs a translation on the start point to line it up with the wand.
					// EDIT: removed due to 1st/3rd person render differences.
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ,
							target.posX, target.posY + target.height/2, target.posZ);
					arc.lifetime = 1;
					arc.setOffset(caster.getLookVec().zCoord * 0.5, caster.getLookVec().xCoord * 0.5);
					world.spawnEntityInWorld(arc);
				}
				
				if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
					if(!world.isRemote && ticksInUse == 1) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
				}else{
					// This motion stuff removes knockback, which is desirable for continuous spells.
					double motionX = target.motionX;
					double motionY = target.motionY;
					double motionZ = target.motionZ;
	
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 5.0f * damageMultiplier);
	
					target.motionX = motionX;
					target.motionY = motionY;
					target.motionZ = motionZ;
				}

				if(ticksInUse == 1){
					world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, 1.0f);
				}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
					world.playSoundAtEntity(caster, "wizardry:electricityb", 1.0F, 1.0f);
				}

			}else{
				for(int i=0;i<5;i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
				}
			}

			// Secondary chaining effect
			double seekerRange = 5.0d;

			List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, target.posX, target.posY + target.height/2, target.posZ, world);			
			// This is a MUCH better way of filtering the secondary targets!
			secondaryTargets.remove(target);
			if(secondaryTargets.size() > 5) secondaryTargets = secondaryTargets.subList(0, 5);

			for(EntityLivingBase secondaryTarget : secondaryTargets){

				if(WizardryUtilities.isValidTarget(caster, secondaryTarget)){

					if(!world.isRemote){
						// This statement means the arc only spawns every other tick.
						if(ticksInUse % 2 == 0){
							EntityArc arc = new EntityArc(world);
							arc.setEndpointCoords(target.posX, target.posY + 1.2, target.posZ,
									secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ);
							arc.lifetime = 1;
							world.spawnEntityInWorld(arc);
						}
						
						if(MagicDamage.isEntityImmune(DamageType.SHOCK, secondaryTarget)){
							if(!world.isRemote && ticksInUse == 1) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", secondaryTarget.getCommandSenderName(), this.getDisplayNameWithFormatting()));
						}else{
							// This motion stuff removes knockback, which is desirable for continuous spells.
							double motionX = secondaryTarget.motionX;
							double motionY = secondaryTarget.motionY;
							double motionZ = secondaryTarget.motionZ;
	
							secondaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 4.0f * damageMultiplier);
	
							secondaryTarget.motionX = motionX;
							secondaryTarget.motionY = motionY;
							secondaryTarget.motionZ = motionZ;
						}

						if(ticksInUse == 1){
							world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, 1.0f);
						}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
							world.playSoundAtEntity(caster, "wizardry:electricityb", 1.0F, 1.0f);
						}

					}else{
						for(int i=0;i<5;i++){
							Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, secondaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + world.rand.nextFloat()*2 - 1, secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
						}
					}

					// Tertiary chaining effect

					List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ, world);
					tertiaryTargets.remove(target);
					tertiaryTargets.removeAll(secondaryTargets);
					if(tertiaryTargets.size() > 2) tertiaryTargets = tertiaryTargets.subList(0, 2);

					for(EntityLivingBase tertiaryTarget : tertiaryTargets){

						if(WizardryUtilities.isValidTarget(caster, tertiaryTarget)){

							if(!world.isRemote){
								// This statement means the arc only spawns every other tick.
								if(ticksInUse % 2 == 0){
									EntityArc arc = new EntityArc(world);
									arc.setEndpointCoords(secondaryTarget.posX, secondaryTarget.posY + 1.2, secondaryTarget.posZ,
											tertiaryTarget.posX, tertiaryTarget.posY + tertiaryTarget.height/2, tertiaryTarget.posZ);
									arc.lifetime = 1;
									world.spawnEntityInWorld(arc);
								}
								
								if(MagicDamage.isEntityImmune(DamageType.SHOCK, tertiaryTarget)){
									if(!world.isRemote && ticksInUse == 1) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", tertiaryTarget.getCommandSenderName(), this.getDisplayNameWithFormatting()));
								}else{
									// This motion stuff removes knockback, which is desirable for continuous spells.
									double motionX = tertiaryTarget.motionX;
									double motionY = tertiaryTarget.motionY;
									double motionZ = tertiaryTarget.motionZ;
	
									tertiaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 3.0f * damageMultiplier);
	
									tertiaryTarget.motionX = motionX;
									tertiaryTarget.motionY = motionY;
									tertiaryTarget.motionZ = motionZ;
								}

								if(ticksInUse == 1){
									world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, 1.0f);
								}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
									world.playSoundAtEntity(caster, "wizardry:electricityb", 1.0F, 1.0f);
								}

							}else{
								for(int i=0;i<5;i++){
									Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, tertiaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(tertiaryTarget) + tertiaryTarget.height/2 + world.rand.nextFloat()*2 - 1, tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
								}
							}
						}
					}
				}
			}

			return true;

		}else{
			if(!world.isRemote){
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){
					EntityArc arc = new EntityArc(world);
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ,
							caster.posX + caster.getLookVec().xCoord * 8, caster.posY + caster.eyeHeight + caster.getLookVec().yCoord * 8, caster.posZ + caster.getLookVec().zCoord * 8);
					arc.lifetime = 1;
					//arc.setOffset(entityplayer.getLookVec().zCoord * 0.5, entityplayer.getLookVec().xCoord * 0.5);
					world.spawnEntityInWorld(arc);
				}
			}

			if(ticksInUse == 1){
				world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, 1.0f);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				world.playSoundAtEntity(caster, "wizardry:electricityb", 1.0F, 1.0f);
			}

			return true;
		}
	}


}
