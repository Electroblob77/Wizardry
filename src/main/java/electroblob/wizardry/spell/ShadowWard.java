package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class ShadowWard extends Spell {

	public ShadowWard() {
		super(EnumTier.ADVANCED, 10, EnumElement.NECROMANCY, "shadow_ward", EnumSpellType.DEFENCE, 0, EnumAction.block, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(world.isRemote){
			double dx = -1 + 2*world.rand.nextFloat();
			double dy = -1 + world.rand.nextFloat();
			double dz = -1 + 2*world.rand.nextFloat();
			world.spawnParticle("portal", caster.posX, WizardryUtilities.getPlayerEyesPos(caster), caster.posZ, dx, dy, dz);
		}
		
		if(ticksInUse % 50 == 0){
			world.playSoundAtEntity(caster, "portal.portal", 0.6f, 1.5f);
		}
		
		return true;
	}


}
