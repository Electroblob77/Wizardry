package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.packet.PacketBombExplosion;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

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

		if(!this.world.isRemote){

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

			// Notify clients to play the explosion effect
			WizardryPacketHandler.net.sendToAllAround(
					new PacketBombExplosion.Message(PacketBombExplosion.POISON_BOMB, posX, posY, posZ, blastMultiplier),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 64));

			this.playSound(WizardrySounds.ENTITY_POISON_BOMB_SMASH, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_POISON_BOMB_POISON, 1.2F, 1.0f);

			double range = Spells.poison_bomb.getProperty(Spell.EFFECT_RADIUS).floatValue() * blastMultiplier;

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(range, this.posX, this.posY,
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
		}

		this.setDead();
	}
}
