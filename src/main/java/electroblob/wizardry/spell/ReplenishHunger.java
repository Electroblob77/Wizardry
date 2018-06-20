package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ReplenishHunger extends SpellBuff {

	public ReplenishHunger(){
		super("replenish_hunger", Tier.APPRENTICE, Element.HEALING, SpellType.BUFF, 10, 30, WizardrySounds.SPELL_HEAL, 1, 0.7f, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}
	
	@Override public boolean canBeCastByNPCs(){ return false; }
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		return true; // In this case the best solution is to remove the functionality of this method and override cast.
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.getFoodStats().needFood()){
			int foodAmount = (int)(4 * modifiers.get(SpellModifiers.POTENCY));
			// Fixed issue #6: Changed to addStats, since setFoodLevel is client-side only
			caster.getFoodStats().addStats(foodAmount, foodAmount * 0.1f);
			return super.cast(world, caster, hand, ticksInUse, modifiers);
		}
		
		return false;
	}

}
