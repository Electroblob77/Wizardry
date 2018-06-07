package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.EntityMagicSlime;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Slime extends Spell {

	public Slime() {
		super(EnumTier.ADVANCED, 20, EnumElement.EARTH, "slime", EnumSpellType.ATTACK, 50, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 8*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;
			
			if(target instanceof EntitySlime){
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else if(!(target instanceof EntityMagicSlime)){
				
				if(target instanceof EntitySkeleton) caster.triggerAchievement(Wizardry.slimeSkeleton);
				
				if(!world.isRemote){
					EntityMagicSlime slime = new EntityMagicSlime(world, (int)(200*durationMultiplier));
					slime.setPosition(target.posX, target.posY, target.posZ);
					slime.mountEntity(target);
					world.spawnEntityInWorld(slime);
				}
			}
		}

		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;

				world.spawnParticle("slime", x1, y1, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.2f, 0.8f, 0.1f);
			}
		}
		
		caster.swingItem();
		world.playSoundAtEntity(caster, "mob.slime.attack", 1.0F, 0.5F);
		world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null && !(target instanceof EntitySlime) && !(target instanceof EntityMagicSlime)){
			
			if(!world.isRemote){
				EntityMagicSlime slime = new EntityMagicSlime(world, (int)(200*durationMultiplier));
				slime.setPosition(target.posX, target.posY, target.posZ);
				slime.mountEntity(target);
				world.spawnEntityInWorld(slime);
			}
			
			if(world.isRemote){
				
				double dx = (target.posX - caster.posX)/caster.getDistanceToEntity(target);
				double dy = (target.posY - caster.posY)/caster.getDistanceToEntity(target);
				double dz = (target.posZ - caster.posZ)/caster.getDistanceToEntity(target);
				
				for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
					
					double x1 = caster.posX + dx*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double z1 = caster.posZ + dz*i/2 + world.rand.nextFloat()/5 - 0.1f;

					world.spawnParticle("slime", x1, y1, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.2f, 0.8f, 0.1f);
				}
			}
			
			caster.swingItem();
			world.playSoundAtEntity(caster, "mob.slime.attack", 1.0F, 0.5F);
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
			return true;
		
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
