package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityDarknessOrb extends EntityMagicProjectile {
	
	public EntityDarknessOrb(World world){
		super(world);
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){
		
		Entity target = rayTrace.entityHit;

		if(target != null && !MagicDamage.isEntityImmune(DamageType.WITHER, target)){

			float damage = Spells.darkness_orb.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			target.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.WITHER).setProjectile(),
					damage);

			if(target instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.WITHER, target))
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.WITHER,
						Spells.darkness_orb.getProperty(Spell.EFFECT_DURATION).intValue(),
						Spells.darkness_orb.getProperty(Spell.EFFECT_STRENGTH).intValue()));

			this.playSound(WizardrySounds.ENTITY_DARKNESS_ORB_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
		}

		this.setDead();
	}

	public void onUpdate(){

		super.onUpdate();

		if(world.isRemote){
			
			float brightness = rand.nextFloat() * 0.2f;
			
			ParticleBuilder.create(Type.SPARKLE, this).time(20 + rand.nextInt(10))
			.clr(brightness, 0.0f, brightness).spawn(world);
			
			ParticleBuilder.create(Type.DARK_MAGIC, this).clr(0.1f, 0.0f, 0.0f).spawn(world);
		}

		// Cancels out the slowdown effect in EntityThrowable
		this.motionX /= 0.99;
		this.motionY /= 0.99;
		this.motionZ /= 0.99;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public int getLifetime(){
		return 60;
	}
}
