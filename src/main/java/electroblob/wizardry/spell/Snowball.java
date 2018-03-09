package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Snowball extends Spell {

	public Snowball(){
		super(Tier.BASIC, 1, Element.ICE, "snowball", SpellType.ATTACK, 1, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			EntitySnowball snowball = new EntitySnowball(world, caster);
			snowball.shoot(caster, caster.rotationPitch, caster.rotationYaw, 0.0f, 1.5f, 1.0f);
			world.spawnEntity(snowball);
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_SNOWBALL_THROW, 0.5F,
				0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		caster.swingArm(hand);
		return true;
	}

}
