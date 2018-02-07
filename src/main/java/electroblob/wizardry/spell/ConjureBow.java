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

public class ConjureBow extends Spell {

	public ConjureBow(){
		super(Tier.APPRENTICE, 40, Element.SORCERY, "conjure_bow", SpellType.UTILITY, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		ItemStack bow = new ItemStack(WizardryItems.spectral_bow);

		IConjuredItem.setDurationMultiplier(bow, modifiers.get(WizardryItems.duration_upgrade));

		if(!WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.spectral_bow)
				&& conjureItemInInventory(caster, bow)){
			for(int i = 0; i < 10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
				if(world.isRemote){
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 0.7f, 0.9f, 1.0f);
				}
			}
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
			return true;
		}
		return false;
	}

	// TODO: When spells get superclassed, this method needs to be in the conjuration superclass.
	/** Adds the given item to the given player's inventory, placing it in the main hand if the main hand is empty. */
	public static boolean conjureItemInInventory(EntityPlayer caster, ItemStack item){
		if(caster.getHeldItemMainhand() == null){
			caster.setHeldItem(EnumHand.MAIN_HAND, item);
			return true;
		}else{
			return caster.inventory.addItemStackToInventory(item);
		}
	}

}
