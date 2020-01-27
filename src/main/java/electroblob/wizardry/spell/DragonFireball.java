package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DragonFireball extends Spell {

	public static final String ACCELERATION = "acceleration";

	public DragonFireball(){
		super("dragon_fireball", EnumAction.NONE, false);
		addProperties(ACCELERATION);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		if(!world.isRemote){

			EntityDragonFireball fireball = new EntityDragonFireball(world, caster, 1, 1, 1);

			fireball.setPosition(caster.posX + look.x, caster.posY + look.y + 1.3, caster.posZ + look.z);

			double acceleration = getProperty(ACCELERATION).doubleValue() * modifiers.get(WizardryItems.range_upgrade);

			fireball.accelerationX = look.x * acceleration;
			fireball.accelerationY = look.y * acceleration;
			fireball.accelerationZ = look.z * acceleration;

			world.spawnEntity(fireball);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){

				EntityDragonFireball fireball = new EntityDragonFireball(world, caster, 1, 1, 1);

				double dx = target.posX - caster.posX;
				double dy = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F)
						- (caster.posY + (double)(caster.height / 2.0F));
				double dz = target.posZ - caster.posZ;

				double acceleration = getProperty(ACCELERATION).doubleValue();

				fireball.accelerationX = dx / caster.getDistance(target) * acceleration;
				fireball.accelerationY = dy / caster.getDistance(target) * acceleration;
				fireball.accelerationZ = dz / caster.getDistance(target) * acceleration;

				fireball.setPosition(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);

				world.spawnEntity(fireball);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override){
		return true;
	}

}
