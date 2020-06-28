package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.List;

public class EntityStormcloud extends EntityScaledConstruct {

	public EntityStormcloud(World world){
		super(world);
		setSize(Spells.stormcloud.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 2);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void onUpdate(){

		super.onUpdate();

		if(this.world.isRemote){

			float areaFactor = (width * width) / 36; // Ensures cloud/raindrop density stays the same for different sizes

			for(int i = 0; i < 2 * areaFactor; i++) ParticleBuilder.create(Type.CLOUD, this)
					.clr(0.3f, 0.3f, 0.3f).shaded(true).spawn(world);
		}

		List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
				this.getEntityBoundingBox().expand(0, -10, 0));

		float damage = Spells.stormcloud.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier;

		for(EntityLivingBase target : targets){

			if(this.isValidTarget(target)){

				if(target.ticksExisted % 150 == 0){ // Use target's lifetime so they don't all get hit at once, looks better

					if(!this.world.isRemote){
						EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(
								this, this.getCaster(), MagicDamage.DamageType.SHOCK), damage);
					}else{
						ParticleBuilder.create(Type.LIGHTNING).pos(target.posX, posY + height/2, target.posZ)
								.target(target).scale(2).spawn(world);
						ParticleBuilder.spawnShockParticles(world, target.posX, target.getEntityBoundingBox().minY + target.height, target.posZ);
					}

					target.playSound(WizardrySounds.ENTITY_STORMCLOUD_THUNDER, 1, 1.6f);
					target.playSound(WizardrySounds.ENTITY_STORMCLOUD_ATTACK, 1, 1);
				}
			}
		}

	}

}
