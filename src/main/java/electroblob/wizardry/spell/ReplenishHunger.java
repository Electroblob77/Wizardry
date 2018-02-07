package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ReplenishHunger extends Spell {

	public ReplenishHunger(){
		super(Tier.APPRENTICE, 10, Element.HEALING, "replenish_hunger", SpellType.UTILITY, 30, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.getFoodStats().needFood()){
			int foodAmount = (int)(6 * modifiers.get(SpellModifiers.DAMAGE));
			// Fixed issue #6: Changed to addStats, since setFoodLevel is client-side only
			caster.getFoodStats().addStats(foodAmount, foodAmount * 0.1f);
			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F
							+ world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 1.0f, 0.7f, 0.3f);
				}
			}
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
					world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}

}
