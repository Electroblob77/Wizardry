package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySpark extends EntityMagicProjectile {

	public EntitySpark(World world){
		super(world);
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){
		
		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){

			float damage = Spells.homing_spark.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
			entityHit.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(),
					DamageType.SHOCK), damage);

		}

		this.playSound(WizardrySounds.ENTITY_HOMING_SPARK_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

		// Particle effect
		if(world.isRemote){
			for(int i = 0; i < 8; i++){
				double x = this.posX + rand.nextDouble() - 0.5;
				double y = this.posY + this.height / 2 + rand.nextDouble() - 0.5;
				double z = this.posZ + rand.nextDouble() - 0.5;
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
			}
		}

		this.setDead();
	}

	@Override
	public float getSeekingStrength(){
		return Spells.homing_spark.getProperty(Spell.SEEKING_STRENGTH).floatValue();
	}

	@Override
	public int getLifetime(){
		return 50;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
