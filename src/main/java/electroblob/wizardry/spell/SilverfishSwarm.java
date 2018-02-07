package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SilverfishSwarm extends Spell {

	public SilverfishSwarm(){
		super(Tier.MASTER, 80, Element.EARTH, "silverfish_swarm", SpellType.MINION, 300, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			for(int i = 0; i < 20; i++){
				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 3, 6);
				// The spell instantly fails if no space was found (see javadoc for the above method).
				if(pos == null) return false;

				EntitySilverfishMinion silverfish = new EntitySilverfishMinion(world, pos.getX() + 0.5, pos.getY(),
						pos.getZ() + 0.5, caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)));
				world.spawnEntity(silverfish);
			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		// Can't possibly get this far if nothing was spawned.
		return true;
	}

}
