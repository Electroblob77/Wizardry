package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class EntitySmokeBomb extends EntityBomb {

	public EntitySmokeBomb(World world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){

		// Particle effect
		if(world.isRemote){
			
			ParticleBuilder.create(Type.FLASH).pos(this.getPositionVector()).scale(5 * blastMultiplier).clr(0, 0, 0).spawn(world);
			
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
						
			for(int i = 0; i < 60 * blastMultiplier; i++){

				float brightness = rand.nextFloat() * 0.1f + 0.1f;
				ParticleBuilder.create(Type.CLOUD, rand, posX, posY, posZ, 2*blastMultiplier, false)
						.clr(brightness, brightness, brightness).time(80 + this.rand.nextInt(12)).shaded(true).spawn(world);

				brightness = rand.nextFloat() * 0.3f;
				ParticleBuilder.create(Type.DARK_MAGIC, rand, posX, posY, posZ, 2*blastMultiplier, false)
				.clr(brightness, brightness, brightness).spawn(world);
			}
		}

		if(!this.world.isRemote){

			this.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_SMASH, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_SMOKE, 1.2F, 1.0f);

			double range = Spells.smoke_bomb.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			int duration = Spells.smoke_bomb.getProperty(Spell.EFFECT_DURATION).intValue();

			for(EntityLivingBase target : targets){
				if(target != this.getThrower()){
					target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, duration, 0));
				}
			}

			this.setDead();
		}
	}
}
