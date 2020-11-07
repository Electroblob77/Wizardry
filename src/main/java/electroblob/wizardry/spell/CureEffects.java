package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;

public class CureEffects extends SpellBuff {

	public CureEffects(){
		super("cure_effects", 0.8f, 0.8f, 1);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){

		if(!caster.getActivePotionEffects().isEmpty()){

			ItemStack milk = new ItemStack(Items.MILK_BUCKET);

			boolean flag = false;

			for(PotionEffect effect : new ArrayList<>(caster.getActivePotionEffects())){ // Get outta here, CMEs
				// The PotionEffect version (as opposed to Potion) does not call cleanup callbacks
				if(effect.isCurativeItem(milk)){
					caster.removePotionEffect(effect.getPotion());
					flag = true;
				}
			}
			
			return flag;
		}
		
		return false;
	}

}
