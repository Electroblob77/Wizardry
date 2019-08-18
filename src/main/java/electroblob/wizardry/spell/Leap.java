package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Leap extends Spell {

	public static final String HORIZONTAL_SPEED = "horizontal_speed";
	public static final String VERTICAL_SPEED = "vertical_speed";

	public Leap(){
		super("leap", EnumAction.NONE, false);
		addProperties(HORIZONTAL_SPEED, VERTICAL_SPEED);
		soundValues(0.5f, 1, 0);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.onGround){

			caster.motionY = getProperty(VERTICAL_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);
			double horizontalSpeed = getProperty(HORIZONTAL_SPEED).floatValue();
			caster.addVelocity(caster.getLookVec().x * horizontalSpeed, 0, caster.getLookVec().z * horizontalSpeed);

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x = caster.posX + world.rand.nextFloat() - 0.5F;
					double y = caster.getEntityBoundingBox().minY;
					double z = caster.posZ + world.rand.nextFloat() - 0.5F;
					world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
				}
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

}
