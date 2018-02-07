package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityIceLance;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class IceLance extends Spell {

	public IceLance(){
		super(Tier.ADVANCED, 20, Element.ICE, "ice_lance", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			EntityIceLance iceLance = new EntityIceLance(world, caster, 2 * modifiers.get(WizardryItems.range_upgrade),
					modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntity(iceLance);
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F,
				world.rand.nextFloat() * 0.4F + 0.8F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				EntityIceLance iceLance = new EntityIceLance(world, caster, target,
						2 * modifiers.get(WizardryItems.range_upgrade), 4, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(iceLance);
			}
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
