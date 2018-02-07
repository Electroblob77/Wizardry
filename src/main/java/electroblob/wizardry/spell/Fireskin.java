package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Fireskin extends Spell {

	public Fireskin(){
		super(Tier.ADVANCED, 40, Element.FIRE, "fireskin", SpellType.DEFENCE, 250, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Cannot be cast when it has already been cast
		if(!caster.isPotionActive(WizardryPotions.fireskin)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(WizardryPotions.fireskin,
						(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0));
			}
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			// Cannot be cast when it has already been cast
			if(!caster.isPotionActive(WizardryPotions.fireskin)){
				if(!world.isRemote){
					caster.addPotionEffect(new PotionEffect(WizardryPotions.fireskin,
							(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
				caster.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
				return true;
			}
			return false;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
