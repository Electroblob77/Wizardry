package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityHealAura extends EntityScaledConstruct {

	public EntityHealAura(World world){
		super(world);
		setSize(Spells.healing_aura.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 1);
	}

	@Override
	public void onUpdate(){

		if(this.ticksExisted % 25 == 1){
			this.playSound(WizardrySounds.ENTITY_HEAL_AURA_AMBIENT, 0.1f, 1.0f);
		}

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(width/2, posX, posY, posZ, world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					if(target.isEntityUndead()){

						double velX = target.motionX;
						double velY = target.motionY;
						double velZ = target.motionZ;

						if(this.getCaster() != null){
							target.attackEntityFrom(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.RADIANT),
									Spells.healing_aura.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier);
						}else{
							target.attackEntityFrom(DamageSource.MAGIC, Spells.healing_aura.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier);
						}

						// Removes knockback
						target.motionX = velX;
						target.motionY = velY;
						target.motionZ = velZ;
					}

				}else if(target.getHealth() < target.getMaxHealth() && target.ticksExisted % 5 == 0){
					target.heal(Spells.healing_aura.getProperty(Spell.HEALTH).floatValue() * damageMultiplier);
				}
			}
		}else{
			for(int i=1; i<3; i++){
				float brightness = 0.5f + (rand.nextFloat() * 0.5f);
				double radius = rand.nextDouble() * (width/2);
				float angle = rand.nextFloat() * (float)Math.PI * 2;
				ParticleBuilder.create(Type.SPARKLE)
				.pos(this.posX + radius * MathHelper.cos(angle), this.posY, this.posZ + radius * MathHelper.sin(angle))
				.vel(0, 0.05, 0)
				.time(48 + this.rand.nextInt(12))
				.clr(1.0f, 1.0f, brightness)
				.spawn(world);
			}
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
