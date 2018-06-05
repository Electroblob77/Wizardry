package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityHealAura extends EntityMagicConstruct {

	public EntityHealAura(World world){
		super(world);
		this.height = 1.0f;
		this.width = 5.0f;
	}

	public EntityHealAura(World world, double x, double y, double z, EntityLivingBase caster, int lifetime,
			float damageMultiplier){
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 1.0f;
		this.width = 5.0f;
	}

	public void onUpdate(){

		if(this.ticksExisted % 25 == 1){
			this.playSound(WizardrySounds.SPELL_LOOP_SPARKLE, 0.1f, 1.0f);
		}

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(2.5d, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					if(target.isEntityUndead()){

						double velX = target.motionX;
						double velY = target.motionY;
						double velZ = target.motionZ;

						if(this.getCaster() != null){
							target.attackEntityFrom(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.RADIANT),
									1 * damageMultiplier);
						}else{
							target.attackEntityFrom(DamageSource.MAGIC, 1 * damageMultiplier);
						}

						// Removes knockback
						target.motionX = velX;
						target.motionY = velY;
						target.motionZ = velZ;
					}

				}else if(target.getHealth() < target.getMaxHealth() && this.ticksExisted % 5 == 0){
					target.heal(1 * damageMultiplier);
				}
			}
		}else{
			for(int i=1; i<3; i++){
				float brightness = 0.5f + (rand.nextFloat() * 0.5f);
				double radius = rand.nextDouble() * 2.0;
				double angle = rand.nextDouble() * Math.PI * 2;
				ParticleBuilder.create(Type.SPARKLE)
				.pos(this.posX + radius * Math.cos(angle), this.posY, this.posZ + radius * Math.sin(angle))
				.vel(0, 0.05, 0)
				.lifetime(48 + this.rand.nextInt(12))
				.colour(1.0f, 1.0f, brightness)
				.spawn(world);
			}
		}
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	public boolean canRenderOnFire(){
		return false;
	}

}
