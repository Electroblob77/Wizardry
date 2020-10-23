package electroblob.wizardry.entity.construct;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
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

		this.move(MoverType.SELF, motionX, 0, motionZ);

		if(this.world.isRemote){

			float areaFactor = (width * width) / 36; // Ensures cloud/raindrop density stays the same for different sizes

			for(int i = 0; i < 2 * areaFactor; i++) ParticleBuilder.create(Type.CLOUD, this)
					.clr(0.3f, 0.3f, 0.3f).shaded(true).spawn(world);
		}

		boolean stormcloudRingActive = getCaster() instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)getCaster(), WizardryItems.ring_stormcloud);

		List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
				this.getEntityBoundingBox().expand(0, -10, 0));

		targets.removeIf(t -> !this.isValidTarget(t));

		float damage = Spells.stormcloud.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier;

		for(EntityLivingBase target : targets){

			if(target.ticksExisted % 150 == 0){ // Use target's lifetime so they don't all get hit at once, looks better

				if(!this.world.isRemote){
					EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(
							this, this.getCaster(), MagicDamage.DamageType.SHOCK), damage);
				}else{
					ParticleBuilder.create(Type.LIGHTNING).pos(target.posX, posY + height/2, target.posZ)
							.target(target).scale(2).spawn(world);
					ParticleBuilder.spawnShockParticles(world, target.posX, target.posY + target.height, target.posZ);
				}

				target.playSound(WizardrySounds.ENTITY_STORMCLOUD_THUNDER, 1, 1.6f);
				target.playSound(WizardrySounds.ENTITY_STORMCLOUD_ATTACK, 1, 1);

				if(stormcloudRingActive) this.lifetime -= 40; // Each strike prolongs the lifetime by 2 seconds with the ring
			}
		}

		if(stormcloudRingActive){
			EntityUtils.getLivingWithinRadius(width * 3, posX, posY, posZ, world).stream()
					.filter(this::isValidTarget).min(Comparator.comparingDouble(this::getDistanceSq)).ifPresent(e -> {
				Vec3d vel = e.getPositionVector().subtract(this.getPositionVector()).normalize().scale(0.2);
				this.motionX = vel.x;
				this.motionZ = vel.z;
			});
		}

	}

}
