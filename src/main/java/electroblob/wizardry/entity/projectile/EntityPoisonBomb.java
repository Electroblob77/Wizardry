package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class EntityPoisonBomb extends EntityBomb {

	public EntityPoisonBomb(World world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){
		
		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the poison bomb gets a direct hit
			float damage = Spells.poison_bomb.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.POISON).setProjectile(),
					damage);

			if(entityHit instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.POISON, entityHit))
				((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(MobEffects.POISON,
						Spells.poison_bomb.getProperty(Spell.DIRECT_EFFECT_DURATION).intValue(),
						Spells.poison_bomb.getProperty(Spell.DIRECT_EFFECT_STRENGTH).intValue()));
		}

		// Particle effect
		if(world.isRemote){
			
			ParticleBuilder.create(Type.FLASH).pos(this.getPositionVector()).scale(5 * blastMultiplier)
			.clr(0.2f + rand.nextFloat() * 0.3f, 0.6f, 0.0f).spawn(world);
			
			for(int i = 0; i < 60 * blastMultiplier; i++){
				
				ParticleBuilder.create(Type.SPARKLE, rand, posX, posY, posZ, 2*blastMultiplier, false).time(35)
				.scale(2).clr(0.2f + rand.nextFloat() * 0.3f, 0.6f, 0.0f).spawn(world);
				
				ParticleBuilder.create(Type.DARK_MAGIC, rand, posX, posY, posZ, 2*blastMultiplier, false)
				.clr(0.2f + rand.nextFloat() * 0.2f, 0.8f, 0.0f).spawn(world);
			}
			// Spawning this after the other particles fixes the rendering colour bug. It's a bit of a cheat, but it
			// works pretty well.
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		if(!this.world.isRemote){

			this.playSound(WizardrySounds.ENTITY_POISON_BOMB_SMASH, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_POISON_BOMB_POISON, 1.2F, 1.0f);

			double range = Spells.poison_bomb.getProperty(Spell.EFFECT_RADIUS).floatValue() * blastMultiplier;

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()
						&& !MagicDamage.isEntityImmune(DamageType.POISON, target)){
					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.POISON),
							Spells.poison_bomb.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
					target.addPotionEffect(new PotionEffect(MobEffects.POISON,
							Spells.poison_bomb.getProperty(Spell.SPLASH_EFFECT_DURATION).intValue(),
							Spells.poison_bomb.getProperty(Spell.SPLASH_EFFECT_STRENGTH).intValue()));
				}
			}

			this.setDead();
		}
	}
}
