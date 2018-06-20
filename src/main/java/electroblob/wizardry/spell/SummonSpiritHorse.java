package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSpiritHorse extends Spell {

	public SummonSpiritHorse(){
		super("summon_spirit_horse", Tier.ADVANCED, Element.EARTH, SpellType.MINION, 50, 150, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData properties = WizardData.get(caster);

		if(!properties.hasSpiritHorse){
			if(!world.isRemote){

				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
				if(pos == null) return false;

				EntitySpiritHorse horse = new EntitySpiritHorse(world);
				horse.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				horse.setTamedBy(caster);
				horse.setHorseSaddled(true);
				world.spawnEntity(horse);
			}
			properties.hasSpiritHorse = true;
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
					world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}

}
