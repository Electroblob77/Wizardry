package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Transience extends Spell {

	public Transience() {
		super(Tier.ADVANCED, 50, Element.HEALING, "transience", SpellType.DEFENCE, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!caster.isPotionActive(WizardryPotions.transience)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(WizardryPotions.transience, (int)(400*modifiers.get(WizardryItems.duration_upgrade)), 0));
				caster.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, (int)(400*modifiers.get(WizardryItems.duration_upgrade)), 0, false, false));
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
			}
			return true;
		}
		return false;
	}


}
