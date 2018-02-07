package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityIceCharge;
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

public class IceCharge extends Spell {

	public IceCharge(){
		super(Tier.ADVANCED, 20, Element.ICE, "ice_charge", SpellType.ATTACK, 30, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			EntityIceCharge icecharge = new EntityIceCharge(world, caster, modifiers.get(SpellModifiers.DAMAGE),
					modifiers.get(WizardryItems.blast_upgrade));
			world.spawnEntity(icecharge);
		}

		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F,
				world.rand.nextFloat() * 0.4F + 1.4F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				EntityIceCharge icecharge = new EntityIceCharge(world, caster, modifiers.get(SpellModifiers.DAMAGE),
						modifiers.get(WizardryItems.blast_upgrade));
				icecharge.directTowards(target, 1.5f);
				world.spawnEntity(icecharge);
			}

			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
