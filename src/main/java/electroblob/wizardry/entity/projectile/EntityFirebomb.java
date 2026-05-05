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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;

public class EntityFirebomb extends EntityBomb {

	public EntityFirebomb(World world){
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
				// This is if the firebomb gets a direct hit
				float damage = Spells.firebomb.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

				entityHit.attackEntityFrom(
						MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(),
						damage);

				if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit))
					entityHit.setFire(Spells.firebomb.getProperty(Spell.BURN_DURATION).intValue());
			}

			// Notify clients to play the explosion effect
			WizardryPacketHandler.net.sendToAllAround(
					new PacketBombExplosion.Message(PacketBombExplosion.FIREBOMB, posX, posY, posZ, blastMultiplier),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 64));

			this.playSound(WizardrySounds.ENTITY_FIREBOMB_SMASH, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_FIREBOMB_FIRE, 1, 1);

			double range = Spells.firebomb.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()
						&& !MagicDamage.isEntityImmune(DamageType.FIRE, target)){
					// Splash damage does not count as projectile damage
					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE),
							Spells.firebomb.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
					target.setFire(Spells.firebomb.getProperty(Spell.BURN_DURATION).intValue());
				}
			}
		}

		this.setDead();
	}

}
