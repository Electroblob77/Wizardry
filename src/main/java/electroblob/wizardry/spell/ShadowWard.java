package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class ShadowWard extends Spell {

	public ShadowWard() {
		super(Tier.ADVANCED, 10, Element.NECROMANCY, "shadow_ward", SpellType.DEFENCE, 0, EnumAction.BLOCK, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(world.isRemote){
			double dx = -1 + 2*world.rand.nextFloat();
			double dy = -1 + world.rand.nextFloat();
			double dz = -1 + 2*world.rand.nextFloat();
			world.spawnParticle(EnumParticleTypes.PORTAL, caster.posX, WizardryUtilities.getPlayerEyesPos(caster), caster.posZ, dx, dy, dz);
		}
		
		if(ticksInUse % 50 == 0){
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_PORTAL_AMBIENT, 0.6f, 1.5f);
		}
		
		return true;
	}


}
