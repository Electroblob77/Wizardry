package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Evade extends Spell {

	private static final String EVADE_VELOCITY = "evade_velocity";

	private static final float UPWARD_VELOCITY = 0.25f;

	public Evade(){
		super("evade", EnumAction.NONE, false);
		addProperties(EVADE_VELOCITY);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!caster.onGround) return false; // Prevents cheesing with cooldown upgrades to effectively fly super-fast

		Vec3d look = caster.getLookVec();
		// We want a horizontal only vector
		look = look.subtract(0, look.y, 0).normalize();

		Vec3d evadeDirection;
		if(caster.moveStrafing == 0){
			// If the caster isn't strafing, pick a random direction
			evadeDirection = look.rotateYaw(world.rand.nextBoolean() ? (float)Math.PI/2f : (float)-Math.PI/2f);
		}else{
			// Otherwise, evade always moves whichever direction the caster was already strafing
			evadeDirection = look.rotateYaw(Math.signum(caster.moveStrafing) * (float)Math.PI/2f);
		}

		evadeDirection = evadeDirection.scale(getProperty(EVADE_VELOCITY).floatValue() * modifiers.get(SpellModifiers.POTENCY));
		caster.addVelocity(evadeDirection.x, UPWARD_VELOCITY, evadeDirection.z);

		return true;
	}

}
