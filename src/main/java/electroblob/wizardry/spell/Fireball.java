package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Fireball extends Spell {

	public Fireball(){
		super(Tier.APPRENTICE, 10, Element.FIRE, "fireball", SpellType.ATTACK, 15, EnumAction.NONE, false);
		// Does 2.5 hearts of damage and 5 seconds of fire, for reference.
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		if(!world.isRemote){
			EntitySmallFireball fireball = new EntitySmallFireball(world, caster, 1, 1, 1);
			fireball.setPosition(caster.posX + look.xCoord, caster.posY + look.yCoord + 1.3, caster.posZ + look.zCoord);
			fireball.accelerationX = look.xCoord * 0.1;
			fireball.accelerationY = look.yCoord * 0.1;
			fireball.accelerationZ = look.zCoord * 0.1;
			world.spawnEntity(fireball);
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){

				EntitySmallFireball fireball = new EntitySmallFireball(world, caster, 1, 1, 1);

				double dx = target.posX - caster.posX;
				double dy = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F)
						- (caster.posY + (double)(caster.height / 2.0F));
				double dz = target.posZ - caster.posZ;

				fireball.accelerationX = dx / caster.getDistanceToEntity(target) * 0.1;
				fireball.accelerationY = dy / caster.getDistanceToEntity(target) * 0.1;
				fireball.accelerationZ = dz / caster.getDistanceToEntity(target) * 0.1;

				fireball.setPosition(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);

				world.spawnEntity(fireball);
			}

			caster.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
