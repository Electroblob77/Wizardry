package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Snowball extends Spell {

	public Snowball(){
		super("snowball", EnumAction.NONE, false);
		addProperties(RANGE);
		soundValues(0.5f, 0.4f, 0.2f);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			// Trajectory calculation - see SpellProjectile for a more detailed explanation
			float g = 0.03f;
			float launchHeight = caster.getEyeHeight();
			float range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);
			float velocity = MathHelper.sqrt(MathHelper.sqrt(g*g * (launchHeight*launchHeight + range*range)) - g*launchHeight);

			EntitySnowball snowball = new EntitySnowball(world, caster);
			snowball.shoot(caster, caster.rotationPitch, caster.rotationYaw, 0.0f, velocity, 1.0f);
			world.spawnEntity(snowball);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		caster.swingArm(hand);
		return true;
	}

}
