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

public class EntityLightningDisc extends EntityMagicProjectile {
	
	public EntityLightningDisc(World world){
		super(world);
		this.width = 2.0f;
		this.height = 0.5f;
	}

	@Override
	protected void onImpact(RayTraceResult result){
		
		Entity entityHit = result.entityHit;

		if(entityHit != null){
			float damage = Spells.lightning_disc.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
			entityHit.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(),
					DamageType.SHOCK), damage);
		}

		this.playSound(WizardrySounds.ENTITY_LIGHTNING_DISC_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

		if(result.typeOfHit == RayTraceResult.Type.BLOCK) this.setDead();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		// Particle effect
		if(world.isRemote){
			for(int i = 0; i < 8; i++){
				ParticleBuilder.create(Type.SPARK).pos(this.posX + rand.nextFloat() * 2 - 1,
						this.posY, this.posZ + rand.nextFloat() * 2 - 1).spawn(world);
			}
		}

		// Cancels out the slowdown effect in EntityThrowable
		this.motionX /= 0.99;
		this.motionY /= 0.99;
		this.motionZ /= 0.99;
	}

	@Override
	public float getSeekingStrength(){
		return Spells.lightning_disc.getProperty(Spell.SEEKING_STRENGTH).floatValue();
	}

	@Override
	public int getLifetime(){
		return 30;
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
