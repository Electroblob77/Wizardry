package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Levitation extends Spell {

	public Levitation(){
		super(Tier.ADVANCED, 10, Element.SORCERY, "levitation", SpellType.UTILITY, 0, EnumAction.BOW, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.fallDistance = 0;

		caster.motionY = caster.motionY < 0.5d ? caster.motionY + 0.1d : caster.motionY;

		if(world.isRemote){
			Wizardry.proxy.spawnParticle(Type.SPARKLE, world,
					caster.posX - 0.25d + world.rand.nextDouble() / 2,
					WizardryUtilities.getPlayerEyesPos(caster) - 1.5f,
					caster.posZ - 0.25d + world.rand.nextDouble() / 2, 0, -0.1F, 0, 15, 0.5f, 1.0f, 0.7f);
		}
		if(ticksInUse % 24 == 0 && world.isRemote){
			Wizardry.proxy.playMovingSound(caster, WizardrySounds.SPELL_LOOP_SPARKLE, 0.5F, 1.0f, false);
		}
		return true;
	}

}
