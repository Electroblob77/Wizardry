package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityLightningSigil extends EntityMagicConstruct {

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public EntityLightningSigil(World world){
		super(world);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.ticksExisted > 600 && this.getCaster() == null && !this.world.isRemote){
			this.setDead();
		}

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY,
				this.posZ, this.world);

		for(EntityLivingBase target : targets){

			if(this.isValidTarget(target)){

				double velX = target.motionX;
				double velY = target.motionY;
				double velZ = target.motionZ;

				// Only works if target is actually damaged to account for hurtResistantTime
				if(target.attackEntityFrom(getCaster() != null ? MagicDamage.causeIndirectMagicDamage(this, getCaster(),
						DamageType.SHOCK) : DamageSource.MAGIC, Spells.lightning_sigil.getProperty(Spell.DIRECT_DAMAGE)
						.floatValue() * damageMultiplier)){

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					this.playSound(WizardrySounds.ENTITY_LIGHTNING_SIGIL_TRIGGER, 1.0f, 1.0f);

					// Secondary chaining effect
					double seekerRange = Spells.lightning_sigil.getProperty(Spell.EFFECT_RADIUS).doubleValue();

					List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
							target.posX, target.posY + target.height / 2, target.posZ, world);

					for(int j = 0; j < Math.min(secondaryTargets.size(),
							Spells.lightning_sigil.getProperty(SECONDARY_MAX_TARGETS).floatValue()); j++){

						EntityLivingBase secondaryTarget = secondaryTargets.get(j);

						if(secondaryTarget != target && this.isValidTarget(secondaryTarget)){

							if(world.isRemote){
								
								ParticleBuilder.create(Type.LIGHTNING).entity(target)
								.pos(0, target.height/2, 0).target(secondaryTarget).spawn(world);
								
								ParticleBuilder.spawnShockParticles(world, secondaryTarget.posX,
										secondaryTarget.getEntityBoundingBox().minY + secondaryTarget.height / 2,
										secondaryTarget.posZ);
							}

							secondaryTarget.playSound(WizardrySounds.ENTITY_LIGHTNING_SIGIL_TRIGGER, 1.0F,
									world.rand.nextFloat() * 0.4F + 1.5F);

							secondaryTarget.attackEntityFrom(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK),
									Spells.lightning_sigil.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
						}

					}
					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}

		if(this.world.isRemote && this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble() * 0.3;
			float angle = rand.nextFloat() * (float)Math.PI * 2;;
			ParticleBuilder.create(Type.SPARK)
			.pos(this.posX + radius * MathHelper.cos(angle), this.posY + 0.1, this.posZ + radius * MathHelper.sin(angle))
			.spawn(world);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
