package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

public class EntityBlizzard extends EntityScaledConstruct {

	public EntityBlizzard(World world){
		super(world);
		// TODO: Set the size properly and do whatever forcefield does to allow block and entity interaction inside it
		// 		 (Probably need to do this for several others too)
		setSize(Spells.blizzard.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 3);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void onUpdate(){

		if(this.ticksExisted % 120 == 1){
			this.playSound(WizardrySounds.ENTITY_BLIZZARD_AMBIENT, 1.0f, 1.0f);
		}

		super.onUpdate();

		// This is a good example of why you might define a spell base property without necessarily using it in the
		// spell - in fact, blizzard doesn't even have a spell class (yet)
		double radius = Spells.blizzard.getProperty(Spell.EFFECT_RADIUS).doubleValue() * sizeMultiplier;

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(radius, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					if(this.getCaster() != null){
						EntityUtils.attackEntityWithoutKnockback(target,
								MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.FROST),
								1 * damageMultiplier);
					}else{
						EntityUtils.attackEntityWithoutKnockback(target, DamageSource.MAGIC,
								1 * damageMultiplier);
					}
				}

				// All entities are slowed, even the caster (except those immune to frost effects)
				if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
					target.addPotionEffect(new PotionEffect(WizardryPotions.frost, 20, 0));
			}
			
		}else{
			
			for(int i=0; i<6; i++){
				double speed = (rand.nextBoolean() ? 1 : -1) * (0.1 + 0.05 * rand.nextDouble());
				ParticleBuilder.create(Type.SNOW).pos(this.posX, this.posY + rand.nextDouble() * height, this.posZ).vel(0, 0, 0)
				.time(100).scale(2).spin(rand.nextDouble() * (radius - 0.5) + 0.5, speed).shaded(true).spawn(world);
			}

			for(int i=0; i<3; i++){
				double speed = (rand.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * rand.nextDouble());
				ParticleBuilder.create(Type.CLOUD).pos(this.posX, this.posY + rand.nextDouble() * (height - 0.5), this.posZ)
						.clr(0xffffff).shaded(true).spin(rand.nextDouble() * (radius - 1) + 0.5, speed).spawn(world);
			}
		}
	}
}
