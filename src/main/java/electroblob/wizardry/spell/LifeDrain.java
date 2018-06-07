package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class LifeDrain extends Spell {

	public LifeDrain() {
		super(EnumTier.APPRENTICE, 10, EnumElement.NECROMANCY, "life_drain", EnumSpellType.ATTACK, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		Vec3 look = caster.getLookVec();

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			if(ticksInUse % 12 == 0){
				// This motion stuff removes knockback, which is desirable for continuous spells.
				double motionX = target.motionX;
				double motionY = target.motionY;
				double motionZ = target.motionZ;

				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 2.0f * damageMultiplier);

				target.motionX = motionX;
				target.motionY = motionY;
				target.motionZ = motionZ;

				caster.heal(1);
			}
		}
		if(world.isRemote){
			for(int i=5; i<(int)(25*rangeMultiplier); i+=2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				//world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				if(i % 5 == 0){
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
				}
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, -0.05*look.xCoord*i, -0.05*look.yCoord*i, -0.05*look.zCoord*i, 8 + world.rand.nextInt(6), 0.5f, 0.0f, 0.0f);
			}
		}
		if(ticksInUse % 18 == 0){
			if(ticksInUse == 0) world.playSoundAtEntity(caster, "wizardry:darkaura", 1.0F, 0.6f);
			world.playSoundAtEntity(caster, "wizardry:crackle", 2.0F, 1.0f);
		}
		return true;
	}


}
