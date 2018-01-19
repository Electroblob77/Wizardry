package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class SixthSense extends Spell {

	public SixthSense() {
		super(Tier.APPRENTICE, 20, Element.EARTH, "sixth_sense", SpellType.UTILITY, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
				
		// Cannot be cast when it has already been cast
		if(!world.isRemote){
			caster.addPotionEffect(new PotionEffect(WizardryPotions.sixth_sense, (int)(400*modifiers.get(WizardryItems.duration_upgrade)), (int)((modifiers.get(WizardryItems.range_upgrade)-1f)/Constants.RANGE_INCREASE_PER_LEVEL)));
		}
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SHOOT, 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
