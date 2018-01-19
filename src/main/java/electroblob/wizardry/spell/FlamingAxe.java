package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class FlamingAxe extends Spell {

	public FlamingAxe() {
		super(Tier.ADVANCED, 45, Element.FIRE, "flaming_axe", SpellType.UTILITY, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		ItemStack flamingaxe = new ItemStack(WizardryItems.flaming_axe);
		
		IConjuredItem.setDurationMultiplier(flamingaxe, modifiers.get(WizardryItems.duration_upgrade));

		if(!WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.flaming_axe) && ConjureBow.conjureItemInInventory(caster, flamingaxe)){

			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					world.spawnParticle(EnumParticleTypes.FLAME, x1, y1, z1, 0, 0, 0);
				}
			}
			
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
			return true;
		}
		return false;
	}


}
