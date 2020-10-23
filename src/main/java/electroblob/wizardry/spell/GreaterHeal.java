package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;

public class GreaterHeal extends SpellBuff {

	public GreaterHeal(){
		super("greater_heal", 1, 1, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(HEALTH);
	}
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		
		if(caster.getHealth() < caster.getMaxHealth() && caster.getHealth() > 0){
			Heal.heal(caster, getProperty(HEALTH).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			return true;
		}
		
		return false; 
	}

}
