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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Thunderstorm extends Spell {

	public Thunderstorm() {
		super(EnumTier.MASTER, 100, EnumElement.LIGHTNING, "thunderstorm", EnumSpellType.ATTACK, 250, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(world.canBlockSeeTheSky((int)caster.posX, (int)caster.posY, (int)caster.posZ)){
		
			for(int r=0; r<10; r++){
	    		
	    		double radius = 4 + world.rand.nextDouble()*6*blastMultiplier;
	    		double angle = world.rand.nextDouble()*Math.PI*2;
	    		
	        	double x = caster.posX + radius*Math.cos(angle);
	        	double z = caster.posZ + radius*Math.sin(angle);
	        	double y = WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 10);
	            
	        	if(!world.isRemote){
		            EntityLightningBolt entitylightning = new EntityLightningBolt(world, x, y, z);
		            world.addWeatherEffect(entitylightning);
	        	}
	            
	            // Code for eventhandler recognition; for achievements and such like. Left in for future use.
	            //NBTTagCompound entityNBT = entitylightning.getEntityData();
	            //entityNBT.setInteger("summoningPlayer", entityplayer.entityId);
	            
	        	//Secondary chaining effect
				double seekerRange = 10.0d;
				
				List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, x, y+1, z, world);
				
				// For this spell there is no limit to the amount of secondary targets!
				for(EntityLivingBase secondaryTarget : secondaryTargets){
					
					if(WizardryUtilities.isValidTarget(caster, secondaryTarget)){
						
						if(!world.isRemote){
							EntityArc arc = new EntityArc(world);
							arc.setEndpointCoords(x, y+1, z,
									secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ);
							world.spawnEntityInWorld(arc);
						}else{
							for(int j=0;j<8;j++){
								Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, secondaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + world.rand.nextFloat()*2 - 1, secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
								world.spawnParticle("largesmoke", secondaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + world.rand.nextFloat()*2 - 1, secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				    		}
						}
						
						world.playSoundAtEntity(secondaryTarget, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
						
						secondaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 10.0f * damageMultiplier);
						
						//Tertiary chaining effect
						
						List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ, world);
						
						for(int j=0; j<Math.min(tertiaryTargets.size(), 3); j++){
							
							EntityLivingBase tertiaryTarget = (EntityLivingBase)tertiaryTargets.get(j);
							
							if(!secondaryTargets.contains(tertiaryTarget) && WizardryUtilities.isValidTarget(caster, tertiaryTarget)){
								
								if(!world.isRemote){
									EntityArc arc = new EntityArc(world);
									arc.setEndpointCoords(secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ,
											tertiaryTarget.posX, tertiaryTarget.posY + tertiaryTarget.height/2, tertiaryTarget.posZ);
									world.spawnEntityInWorld(arc);
								}else{
									for(int k=0;k<8;k++){
										Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, tertiaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(tertiaryTarget) + tertiaryTarget.height/2 + world.rand.nextFloat()*2 - 1, tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
										world.spawnParticle("largesmoke", tertiaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(tertiaryTarget) + tertiaryTarget.height/2 + world.rand.nextFloat()*2 - 1, tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
						    		}
								}
								
								world.playSoundAtEntity(tertiaryTarget, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
								
								tertiaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 8.0f * damageMultiplier);
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
