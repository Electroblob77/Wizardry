package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Flight extends Spell {

	public Flight(){
		super("flight", Tier.MASTER, Element.EARTH, SpellType.UTILITY, 10, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!caster.isInWater() && !caster.isElytraFlying()){
			// The division thingy checks if the look direction is the opposite way to the velocity. If this is the
			// case then the velocity should be added regardless of the player's current speed.
			if((Math.abs(caster.motionX) < 0.6 || caster.motionX / caster.getLookVec().x < 0)
					&& (Math.abs(caster.motionZ) < 0.6 || caster.motionZ / caster.getLookVec().z < 0)){
				caster.addVelocity(caster.getLookVec().x / 20, 0, caster.getLookVec().z / 20);
			}
			// y velocity is handled separately to stop the player from falling from the sky when they reach maximum
			// horizontal speed.
			if(Math.abs(caster.motionY) < 0.6 || caster.motionY / caster.getLookVec().y < 0){
				caster.motionY += caster.getLookVec().y / 20 + 0.075;
			}
			caster.fallDistance = 0.0f;
		}
		
		if(world.isRemote){
			double x = caster.posX - 1 + world.rand.nextDouble() * 2;
			double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ - 1 + world.rand.nextDouble() * 2;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(0.8f, 1, 0.5f).spawn(world);
			x = caster.posX - 1 + world.rand.nextDouble() * 2;
			y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			z = caster.posZ - 1 + world.rand.nextDouble() * 2;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(1, 1, 1).spawn(world);
		}
		
		if(ticksInUse % 24 == 0){
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_ENDERDRAGON_FLAP, 0.5F, 1.0f);
		}
		
		return true;
	}

}
