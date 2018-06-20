package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSpiritWolf extends Spell {

	public SummonSpiritWolf(){
		super("summon_spirit_wolf", Tier.APPRENTICE, Element.EARTH, SpellType.MINION, 25, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData properties = WizardData.get(caster);

		if(!properties.hasSpiritWolf){
			if(!world.isRemote){

				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
				if(pos == null) return false;

				EntitySpiritWolf wolf = new EntitySpiritWolf(world);
				wolf.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				wolf.setTamed(true);
				wolf.setOwnerId(caster.getUniqueID());
				world.spawnEntity(wolf);
			}
			properties.hasSpiritWolf = true;
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
					world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}

}
