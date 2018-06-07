package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Metamorphosis extends Spell {

	public Metamorphosis() {
		super(EnumTier.APPRENTICE, 15, EnumElement.NECROMANCY, "metamorphosis", EnumSpellType.UTILITY, 30, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){
			
			Entity entityHit = rayTrace.entityHit;
			double xPos = entityHit.posX;
			double yPos = entityHit.posY;
			double zPos = entityHit.posZ;
			
			EntityLiving newEntity = null;
			boolean flag = false;
			
			if(entityHit instanceof EntityPig){
				newEntity = new EntityPigZombie(world);
			}
			else if(entityHit instanceof EntityPigZombie){
				newEntity = new EntityPig(world);
			}
			else if(entityHit instanceof EntitySkeleton){
				if(((EntitySkeleton)entityHit).getSkeletonType() == 0){
					((EntitySkeleton)entityHit).setSkeletonType(1);
				}else{
					((EntitySkeleton)entityHit).setSkeletonType(0);
				}
				flag = true;
			}
			else if(entityHit instanceof EntityCow && !(entityHit instanceof EntityMooshroom)){
				newEntity = new EntityMooshroom(world);
			}
			else if(entityHit instanceof EntityMooshroom){
				newEntity = new EntityCow(world);
			}
			else if(entityHit instanceof EntityChicken){
				newEntity = new EntityBat(world);
			}
			else if(entityHit instanceof EntityBat){
				newEntity = new EntityChicken(world);
			}
			else if(entityHit instanceof EntitySlime && !(entityHit instanceof EntityMagmaCube)){
				newEntity = new EntityMagmaCube(world);
			}
			else if(entityHit instanceof EntityMagmaCube){
				newEntity = new EntitySlime(world);
			}
			else if(entityHit instanceof EntitySpider && !(entityHit instanceof EntityCaveSpider)){
				newEntity = new EntityCaveSpider(world);
			}
			else if(entityHit instanceof EntityCaveSpider){
				newEntity = new EntitySpider(world);
			}
			
			if(newEntity != null || flag){
				
				if(!world.isRemote && newEntity != null){
				//Transfers attributes from the old entity to the new one.
				newEntity.setHealth(((EntityLiving)entityHit).getHealth());
				//newEntity.writeToNBT(entityHit.getEntityData());
				
				entityHit.setDead();
				newEntity.setPosition(xPos, yPos, zPos);
				world.spawnEntityInWorld(newEntity);
				}

				if(world.isRemote){
					for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
						// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
						double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
						double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
						double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
						//world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), 0.2f, 0.0f, 0.1f);
					}
					for(int i=0;i<5;i++){
						Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, xPos, yPos, zPos, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
		    		}
				}
				
				caster.swingItem();
				world.playSoundAtEntity(caster, "wizardry:effect", 0.5F, 0.8f);
				return true;
			}
		}
		return false;
	}


}
