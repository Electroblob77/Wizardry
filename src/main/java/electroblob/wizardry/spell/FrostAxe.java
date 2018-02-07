package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class FrostAxe extends Spell {

	public FrostAxe(){
		super(Tier.ADVANCED, 45, Element.ICE, "frost_axe", SpellType.UTILITY, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		ItemStack frostaxe = new ItemStack(WizardryItems.frost_axe);

		IConjuredItem.setDurationMultiplier(frostaxe, modifiers.get(WizardryItems.duration_upgrade));

		if(!WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.frost_axe)
				&& ConjureBow.conjureItemInInventory(caster, frostaxe)){

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F
							+ world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(WizardryParticleType.SNOW, world, x1, y1, z1, 0, -0.02d, 0,
							40 + world.rand.nextInt(10));
				}
			}

			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0f, 1.0f);
			return true;
		}
		return false;
	}

}
