package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySparkBomb extends EntityBomb {

	public EntitySparkBomb(World world){
		super(world);
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){
		
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST_FAR, 0.5f, 0.5f);

		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the spark bomb gets a direct hit
			float damage = 6 * damageMultiplier;

			this.playSound(SoundEvents.ENTITY_GENERIC_HURT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(),
					damage);

		}

		// Particle effect
		if(world.isRemote){
			ParticleBuilder.spawnShockParticles(world, posX, posY + height/2, posZ);
		}

		double seekerRange = 5.0d * blastMultiplier;

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, this.posX, this.posY,
				this.posZ, this.world);

		for(int i = 0; i < Math.min(targets.size(), 4); i++){

			boolean flag = targets.get(i) != entityHit && targets.get(i) != this.getThrower()
					&& !(targets.get(i) instanceof EntityPlayer
							&& ((EntityPlayer)targets.get(i)).capabilities.isCreativeMode);

			// Detects (client side) if target is the thrower, to stop particles being spawned around them.
			//if(flag && world.isRemote && targets.get(i).getEntityId() == this.casterID) flag = false;

			if(flag){

				EntityLivingBase target = targets.get(i);

				if(!this.world.isRemote){

					target.playSound(WizardrySounds.ENTITY_SPARK_BOMB_CHAIN, 1.0F, rand.nextFloat() * 0.4F + 1.5F);

					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
							5.0f * damageMultiplier);

				}else{
					ParticleBuilder.create(Type.LIGHTNING).pos(this.getPositionVector()).target(target).spawn(world);
					ParticleBuilder.spawnShockParticles(world, target.posX, target.getEntityBoundingBox().minY + target.height/2, target.posZ);
				}
			}
		}

		this.setDead();
	}
}
