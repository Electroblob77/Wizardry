package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.packet.PacketBombExplosion;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;

public class EntitySmokeBomb extends EntityBomb {

	public EntitySmokeBomb(World world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){

		if(!this.world.isRemote){

			// Notify clients to play the explosion effect
			WizardryPacketHandler.net.sendToAllAround(
					new PacketBombExplosion.Message(PacketBombExplosion.SMOKE_BOMB, posX, posY, posZ, blastMultiplier),
					new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 64));

			this.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_SMASH, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_SMOKE, 1.2F, 1.0f);

			double range = Spells.smoke_bomb.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			int duration = Spells.smoke_bomb.getProperty(Spell.EFFECT_DURATION).intValue();

			for(EntityLivingBase target : targets){
				if(target != this.getThrower()){
					target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, duration, 0));
				}
			}
		}

		this.setDead();
	}
}
