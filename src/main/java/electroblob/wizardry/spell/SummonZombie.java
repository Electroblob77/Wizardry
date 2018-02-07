package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonZombie extends Spell {

	public SummonZombie(){
		super(Tier.BASIC, 10, Element.NECROMANCY, "summon_zombie", SpellType.MINION, 40, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;
			EntityZombieMinion zombie = new EntityZombieMinion(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
					caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)));
			world.spawnEntity(zombie);
		}
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SUMMONING, 7.0f, 0.6f);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(!world.isRemote){

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntityZombieMinion zombie = new EntityZombieMinion(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
					caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)));
			world.spawnEntity(zombie);
		}
		caster.playSound(WizardrySounds.SPELL_SUMMONING, 7.0f, 0.6f);
		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
