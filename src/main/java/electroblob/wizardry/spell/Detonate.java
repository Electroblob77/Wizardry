package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Detonate extends Spell {

	public Detonate() {
		super(EnumTier.ADVANCED, 45, EnumElement.FIRE, "detonate", EnumSpellType.ATTACK, 50, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(16*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			if(!world.isRemote){
				List targets = WizardryUtilities.getEntitiesWithinRadius(3.0d*blastMultiplier, (rayTrace.blockX+0.5), (rayTrace.blockY+0.5), (rayTrace.blockZ+0.5), world);
				for(int i=0;i<targets.size();i++){
					if(targets.get(i) instanceof EntityLivingBase){
						((EntityLivingBase)targets.get(i)).attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
								//Damage decreases with distance but cannot be less than 0, naturally.
								Math.max(12.0f - (float)((EntityLivingBase)targets.get(i)).getDistance((rayTrace.blockX+0.5), (rayTrace.blockY+0.5), (rayTrace.blockZ+0.5))*4, 0) * damageMultiplier);
					}
				}
				world.playSoundEffect((rayTrace.blockX+0.5), (rayTrace.blockY+0.5), (rayTrace.blockZ+0.5), "random.explode", 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
			}
			if(world.isRemote){
				double dx = (rayTrace.blockX+0.5) - caster.posX;
				double dy = (rayTrace.blockY+0.5) - WizardryUtilities.getPlayerEyesPos(caster);
				double dz = (rayTrace.blockZ+0.5) - caster.posZ;
				world.spawnParticle("hugeexplosion", (rayTrace.blockX+0.5), (rayTrace.blockY+0.5), (rayTrace.blockZ+0.5), 0, 0, 0);
				for(int i=1;i<5;i++){
	    			world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
	    			world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
	    		}
			}
			caster.swingItem();
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			if(!world.isRemote){
				List targets = WizardryUtilities.getEntitiesWithinRadius(3.0d, target.posX, target.posY, target.posZ, world);
				for(int i=0;i<targets.size();i++){
					if(targets.get(i) instanceof EntityLivingBase){
						((EntityLivingBase)targets.get(i)).attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
								// Damage decreases with distance but cannot be less than 0, naturally.
								Math.max(12.0f - (float)((EntityLivingBase)targets.get(i)).getDistance(target.posX, target.posY, target.posZ)*4, 0) * damageMultiplier);
					}
				}
				world.playSoundEffect(target.posX, target.posY, target.posZ, "random.explode", 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
			}
			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = target.posY - (caster.posY + caster.getEyeHeight());
				double dz = target.posZ - caster.posZ;
				world.spawnParticle("hugeexplosion", target.posX, target.posY, target.posZ, 0, 0, 0);
				for(int i=1;i<5;i++){
	    			world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
	    			world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
	    		}
			}
			caster.swingItem();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
