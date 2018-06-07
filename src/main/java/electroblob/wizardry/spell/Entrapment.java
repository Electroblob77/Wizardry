package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.construct.EntityBubble;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Entrapment extends Spell {

	public Entrapment() {
		super(EnumTier.ADVANCED, 35, EnumElement.NECROMANCY, "entrapment", EnumSpellType.ATTACK, 75, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			EntityLivingBase entity = (EntityLivingBase) rayTrace.entityHit;
			if(!world.isRemote){
				entity.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1.0f * damageMultiplier);
				
				EntityBubble entitybubble = new EntityBubble(world, entity.posX, entity.posY, entity.posZ, caster, (int)(200*durationMultiplier), true, damageMultiplier);
				world.spawnEntityInWorld(entitybubble);
				entity.mountEntity(entitybubble);
			}
		}
		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;

				world.spawnParticle("portal", x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
			}
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.3F + 0.7F);
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){

			if(!world.isRemote){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1.0f * damageMultiplier);
				// Deprecated in favour of entity riding method
				//entity.addPotionEffect(new PotionEffect(Wizardry.bubblePotion.id, 200, 0));
				EntityBubble entitybubble = new EntityBubble(world, target.posX, target.posY, target.posZ, caster, (int)(200*durationMultiplier), true, damageMultiplier);
				world.spawnEntityInWorld(entitybubble);
				target.mountEntity(entitybubble);

			}
			if(world.isRemote){

				double dx = (target.posX - caster.posX)/caster.getDistanceToEntity(target);
				double dy = (target.posY - caster.posY)/caster.getDistanceToEntity(target);
				double dz = (target.posZ - caster.posZ)/caster.getDistanceToEntity(target);
				
				for(int i=1; i<25; i+=2){

					double x1 = caster.posX + dx*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double z1 = caster.posZ + dz*i/2 + world.rand.nextFloat()/5 - 0.1f;

					world.spawnParticle("portal", x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
				}
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.3F + 0.7F);
			return true;
		}

		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
