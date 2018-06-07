package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Levitation extends Spell {

	public Levitation() {
		super(EnumTier.ADVANCED, 10, EnumElement.SORCERY, "levitation", EnumSpellType.UTILITY, 0, EnumAction.bow, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		caster.fallDistance = 0;

		caster.motionY = caster.motionY < 0.5d ? caster.motionY + 0.1d : caster.motionY;
		
		if(world.isRemote){
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX - 0.25d + world.rand.nextDouble()/2, WizardryUtilities.getPlayerEyesPos(caster) - 1.5f, caster.posZ - 0.25d + world.rand.nextDouble()/2, 0, -0.1F, 0, 15, 0.5f, 1.0f, 0.7f);
		}
		if(ticksInUse % 24 == 0 && world.isRemote){
			Wizardry.proxy.playMovingSound(caster, "wizardry:sparkle", 0.5F, 1.0f, false);
		}
		return true;
	}


}
