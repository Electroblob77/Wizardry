package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Levitation extends Spell {

	public Levitation(){
		super("levitation", Tier.ADVANCED, Element.SORCERY, SpellType.UTILITY, 10, 0, EnumAction.BOW, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.fallDistance = 0;

		caster.motionY = caster.motionY < 0.5 ? caster.motionY + 0.1 : caster.motionY;

		if(world.isRemote){
			double x = caster.posX - 0.25 + world.rand.nextDouble() * 0.5;
			double y = caster.getEntityBoundingBox().minY;
			double z = caster.posZ - 0.25 + world.rand.nextDouble() * 0.5;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(0.5f, 1, 0.7f).spawn(world);
		}
		if(ticksInUse % 24 == 0 && world.isRemote){
			Wizardry.proxy.playMovingSound(caster, WizardrySounds.SPELL_LOOP_SPARKLE, 0.5f, 1, false);
		}
		return true;
	}

}
