package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;

public class GreaterHeal extends SpellBuff {

	public GreaterHeal(){
		super("greater_heal", Tier.ADVANCED, Element.HEALING, SpellType.DEFENCE, 15, 40, WizardrySounds.SPELL_HEAL, 1, 1, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		
		if(caster.getHealth() < caster.getMaxHealth() && caster.getHealth() > 0){
			caster.heal(8 * modifiers.get(SpellModifiers.POTENCY));
			return true;
		}
		
		return false; 
	}

}
