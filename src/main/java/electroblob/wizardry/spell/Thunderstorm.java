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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Thunderstorm extends Spell {

	public Thunderstorm(){
		super(Tier.MASTER, 100, Element.LIGHTNING, "thunderstorm", SpellType.ATTACK, 250, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(world.canBlockSeeSky(new BlockPos(caster))){

			for(int r = 0; r < 10; r++){

				double radius = 4 + world.rand.nextDouble() * 6 * modifiers.get(WizardryItems.blast_upgrade);
				double angle = world.rand.nextDouble() * Math.PI * 2;

				double x = caster.posX + radius * Math.cos(angle);
				double z = caster.posZ + radius * Math.sin(angle);
				double y = WizardryUtilities.getNearestFloorLevel(world, new BlockPos(x, caster.posY, z), 10);

				if(!world.isRemote){
					EntityLightningBolt entitylightning = new EntityLightningBolt(world, x, y, z, false);
					world.addWeatherEffect(entitylightning);
				}

				// Code for eventhandler recognition; for achievements and such like. Left in for future use.
				// NBTTagCompound entityNBT = entitylightning.getEntityData();
				// entityNBT.setInteger("summoningPlayer", entityplayer.entityId);

				// Secondary chaining effect
				double seekerRange = 10.0d;

				List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, x,
						y + 1, z, world);

				// For this spell there is no limit to the amount of secondary targets!
				for(EntityLivingBase secondaryTarget : secondaryTargets){

					if(WizardryUtilities.isValidTarget(caster, secondaryTarget)){

						if(!world.isRemote){
							EntityArc arc = new EntityArc(world);
							arc.setEndpointCoords(x, y + 1, z, secondaryTarget.posX,
									secondaryTarget.posY + secondaryTarget.height / 2, secondaryTarget.posZ);
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

						secondaryTarget.playSound(WizardrySounds.SPELL_SPARK, 1.0F,
								world.rand.nextFloat() * 0.4F + 1.5F);

						secondaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
								10.0f * modifiers.get(SpellModifiers.DAMAGE));

						// Tertiary chaining effect

						List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
								secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height / 2,
								secondaryTarget.posZ, world);

						for(int j = 0; j < Math.min(tertiaryTargets.size(), 3); j++){

							EntityLivingBase tertiaryTarget = (EntityLivingBase)tertiaryTargets.get(j);

							if(!secondaryTargets.contains(tertiaryTarget)
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

								tertiaryTarget.attackEntityFrom(
										MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
										8.0f * modifiers.get(SpellModifiers.DAMAGE));
							}
						}
					}
				}
			}

			return true;
		}

		return false;
	}

}
