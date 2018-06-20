package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFirebomb extends EntityBomb {

	public EntityFirebomb(World world){
		super(world);
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){
		
		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the firebomb gets a direct hit
			float damage = 5 * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(),
					damage);

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit)) entityHit.setFire(10);
		}

		// Particle effect
		if(world.isRemote){
			
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
			
			for(int i = 0; i < 60 * blastMultiplier; i++){
				
				ParticleBuilder.create(Type.MAGIC_FIRE, rand, posX, posY, posZ, 2*blastMultiplier, false)
				.lifetime(15 + rand.nextInt(5)).scale(2 + rand.nextFloat()).spawn(world);
				
				ParticleBuilder.create(Type.DARK_MAGIC, rand, posX, posY, posZ, 2*blastMultiplier, false)
				.colour(1.0f, 0.2f + rand.nextFloat() * 0.4f, 0.0f).spawn(world);
			}
		}

		if(!this.world.isRemote){

			this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);

			double range = 3.0d * blastMultiplier;

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()
						&& !MagicDamage.isEntityImmune(DamageType.FIRE, target)){
					// Splash damage does not count as projectile damage
					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE),
							4.0f * damageMultiplier);
					target.setFire(7);
				}
			}

			this.setDead();
		}
	}

}
