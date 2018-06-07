package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CurseOfSoulbinding extends Spell {

	public CurseOfSoulbinding() {
		super(EnumTier.ADVANCED, 35, EnumElement.NECROMANCY, "curse_of_soulbinding", EnumSpellType.ATTACK, 100, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		Vec3 look = caster.getLookVec();

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase && ExtendedPlayer.get(caster) != null){
			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;
			ExtendedPlayer.get(caster).soulbind(target);
		}
		
		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				//world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.4f, 0.0f, 0.0f);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), 1.0f, 0.8f, 1.0f);
			}
		}
		
		caster.swingItem();
		world.playSoundAtEntity(caster, "mob.wither.spawn", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

}
