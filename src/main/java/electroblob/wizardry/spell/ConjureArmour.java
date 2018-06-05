package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ConjureArmour extends Spell {

	public ConjureArmour(){
		super(Tier.ADVANCED, 45, Element.HEALING, "conjure_armour", SpellType.DEFENCE, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		ItemStack armour;
		boolean flag = false;

		// A blank "ench" tag is set to trick the renderer into showing the enchantment effect on the actual armour model.

		// Used this rather than getArmorInventoryList because I need to access the slot itself.
		for(EntityEquipmentSlot slot : WizardryUtilities.ARMOUR_SLOTS){
			
			if(caster.getItemStackFromSlot(slot).isEmpty() &&
					!WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.SPECTRAL_ARMOUR_MAP.get(slot))){
				
				armour = new ItemStack(WizardryItems.SPECTRAL_ARMOUR_MAP.get(slot));
				IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.duration_upgrade));
				armour.getTagCompound().setTag("ench", new NBTTagList());
				caster.setItemStackToSlot(slot, armour);
				flag = true;
			}
		}

		if(flag){

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F
							+ world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 0.7f, 0.9f, 1.0f);
				}
			}

			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
		}

		return flag;
	}

}
