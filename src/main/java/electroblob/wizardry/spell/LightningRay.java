package electroblob.wizardry.spell;

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

public class LightningRay extends Spell {

	public LightningRay() {
		super(EnumTier.APPRENTICE, 5, EnumElement.LIGHTNING, "lightning_ray", EnumSpellType.ATTACK, 0, EnumAction.none, true);
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
					
					world.spawnEntityInWorld(arc);
				}
				
				if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
					if(!world.isRemote && ticksInUse == 1) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
				}else{
					// This motion stuff removes knockback, which is desirable for continuous spells.
					double motionX = target.motionX;
					double motionY = target.motionY;
					double motionZ = target.motionZ;
					
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 3.0f * damageMultiplier);
	
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
			return true;
		}else{
			if(!world.isRemote){
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){
					
					EntityArc arc = new EntityArc(world);
					
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ,
							caster.posX + caster.getLookVec().xCoord * 8, caster.posY + caster.eyeHeight + caster.getLookVec().yCoord * 8, caster.posZ + caster.getLookVec().zCoord * 8);
					
					arc.lifetime = 1;
					
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
