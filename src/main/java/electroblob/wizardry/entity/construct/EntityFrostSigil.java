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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityFrostSigil extends EntityScaledConstruct {

	public EntityFrostSigil(World world){
		super(world);
		setSize(Spells.frost_sigil.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(width/2, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){
					
					EntityUtils.attackEntityWithoutKnockback(target, this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FROST)
							: DamageSource.MAGIC, Spells.frost_sigil.getProperty(Spell.DAMAGE).floatValue()
							* damageMultiplier);

					if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
						target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
								Spells.frost_sigil.getProperty(Spell.EFFECT_DURATION).intValue(),
								Spells.frost_sigil.getProperty(Spell.EFFECT_STRENGTH).intValue()));

					this.playSound(WizardrySounds.ENTITY_FROST_SIGIL_TRIGGER, 1.0f, 1.0f);

					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = (0.5 + rand.nextDouble() * 0.3) * width/2;
			float angle = rand.nextFloat() * (float)Math.PI * 2;
			ParticleBuilder.create(Type.SNOW)
			.pos(this.posX + radius * MathHelper.cos(angle), this.posY + 0.1, this.posZ + radius * MathHelper.sin(angle))
			.vel(0, 0, 0) // Required since default for snow is not stationary
			.spawn(world);
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
