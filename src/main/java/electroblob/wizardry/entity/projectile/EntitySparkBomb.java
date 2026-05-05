package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.packet.PacketBombExplosion;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;

public class EntitySparkBomb extends EntityBomb {

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public EntitySparkBomb(World world){
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
				// This is if the spark bomb gets a direct hit
				float damage = Spells.spark_bomb.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

				this.playSound(WizardrySounds.ENTITY_SPARK_BOMB_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

				entityHit.attackEntityFrom(
						MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(),
						damage);
			}

			double seekerRange = Spells.spark_bomb.getProperty(Spell.EFFECT_RADIUS).doubleValue() * blastMultiplier;

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(seekerRange, this.posX, this.posY,
					this.posZ, this.world);

			List<Integer> secondaryTargetIDs = new ArrayList<>();

			for(int i = 0; i < Math.min(targets.size(), Spells.spark_bomb.getProperty(SECONDARY_MAX_TARGETS).intValue()); i++){

				boolean flag = targets.get(i) != entityHit && targets.get(i) != this.getThrower()
						&& !(targets.get(i) instanceof EntityPlayer
								&& ((EntityPlayer)targets.get(i)).isCreative());

				if(flag){
					EntityLivingBase target = targets.get(i);

					target.playSound(WizardrySounds.ENTITY_SPARK_BOMB_CHAIN, 1.0F, rand.nextFloat() * 0.4F + 1.5F);

					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
							Spells.spark_bomb.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);

					secondaryTargetIDs.add(target.getEntityId());
				}
			}

			// Notify clients to play the explosion effect, including lightning arcs to secondary targets
			int[] idArray = secondaryTargetIDs.stream().mapToInt(Integer::intValue).toArray();
			WizardryPacketHandler.net.sendToAllAround(
					new PacketBombExplosion.Message(PacketBombExplosion.SPARK_BOMB, posX, posY + height / 2, posZ,
							blastMultiplier, idArray),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 64));

			this.playSound(WizardrySounds.ENTITY_SPARK_BOMB_HIT_BLOCK, 0.5f, 0.5f);
		}

		this.setDead();
	}
}
