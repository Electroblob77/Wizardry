package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityFireSigil extends EntityMagicConstruct {

	public EntityFireSigil(World world){
		super(world);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(width/2, posX, posY, posZ, world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					target.attackEntityFrom(this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FIRE)
							: DamageSource.MAGIC, Spells.fire_sigil.getProperty(Spell.DAMAGE).floatValue()
							* damageMultiplier);

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target))
						target.setFire(Spells.fire_sigil.getProperty(Spell.BURN_DURATION).intValue());

					this.playSound(WizardrySounds.ENTITY_FIRE_SIGIL_TRIGGER, 1, 1);

					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble() * 0.3;
			float angle = rand.nextFloat() * (float)Math.PI * 2;;
			world.spawnParticle(EnumParticleTypes.FLAME, this.posX + radius * MathHelper.cos(angle), this.posY + 0.1,
					this.posZ + radius * MathHelper.sin(angle), 0, 0, 0);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
