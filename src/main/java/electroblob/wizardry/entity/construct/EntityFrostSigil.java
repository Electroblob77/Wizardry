package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityFrostSigil extends EntityMagicConstruct {

	public EntityFrostSigil(World world){
		super(world);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){
					
					WizardryUtilities.attackEntityWithoutKnockback(target, this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FROST)
							: DamageSource.MAGIC, 8);

					if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
						target.addPotionEffect(new PotionEffect(WizardryPotions.frost, 200, 1));

					this.playSound(WizardrySounds.SPELL_FREEZE, 1.0f, 1.0f);

					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble() * 0.3;
			double angle = rand.nextDouble() * Math.PI * 2;
			ParticleBuilder.create(Type.SNOW)
			.pos(this.posX + radius * Math.cos(angle), this.posY + 0.1, this.posZ + radius * Math.sin(angle))
			.vel(0, 0, 0) // Required since default for snow is not stationary
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
