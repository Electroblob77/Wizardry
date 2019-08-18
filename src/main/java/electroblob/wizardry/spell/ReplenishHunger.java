package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ReplenishHunger extends SpellBuff {

	public static final String HUNGER_POINTS = "hunger_points";
	public static final String SATURATION_MODIFIER = "saturation_modifier";

	public ReplenishHunger(){
		super("replenish_hunger", 1, 0.7f, 0.3f);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(HUNGER_POINTS, SATURATION_MODIFIER);
	}
	
	@Override public boolean canBeCastByNPCs(){ return false; }
	
	@Override
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		return true; // In this case the best solution is to remove the functionality of this method and override cast.
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.getFoodStats().needFood()){
			int foodAmount = (int)(getProperty(HUNGER_POINTS).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			// Fixed issue #6: Changed to addStats, since setFoodLevel is client-side only
			caster.getFoodStats().addStats(foodAmount, getProperty(SATURATION_MODIFIER).floatValue());
			return super.cast(world, caster, hand, ticksInUse, modifiers);
		}
		
		return false;
	}

}
