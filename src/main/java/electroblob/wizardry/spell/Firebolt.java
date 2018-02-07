package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityFirebolt;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Firebolt extends Spell {

	public Firebolt(){
		super(Tier.APPRENTICE, 10, Element.FIRE, "firebolt", SpellType.ATTACK, 10, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			EntityFirebolt firebolt = new EntityFirebolt(world, caster, modifiers.get(SpellModifiers.DAMAGE));
			firebolt.motionX *= 2.5;
			firebolt.motionY *= 2.5;
			firebolt.motionZ *= 2.5;
			world.spawnEntity(firebolt);
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
				EntityFirebolt firebolt = new EntityFirebolt(world, caster, modifiers.get(SpellModifiers.DAMAGE));
				firebolt.directTowards(target, 2.5f);
				world.spawnEntity(firebolt);
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
