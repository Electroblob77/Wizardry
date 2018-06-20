package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;

public class CureEffects extends SpellBuff {

	public CureEffects(){
		super("cure_effects", Tier.APPRENTICE, Element.HEALING, SpellType.DEFENCE, 25, 40, WizardrySounds.SPELL_HEAL, 0.8f, 0.8f, 1);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		
		if(!caster.getActivePotionEffects().isEmpty()){
			caster.clearActivePotions();
			return true;
		}
		
		return false;
	}

}
