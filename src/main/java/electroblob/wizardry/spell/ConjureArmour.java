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
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ConjureArmour extends Spell {

	public ConjureArmour() {
		super(Tier.ADVANCED, 45, Element.HEALING, "conjure_armour", SpellType.DEFENCE, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		ItemStack armour;
		boolean flag = false;

		// Note that armourInventory is the other way round! Also note that a blank "ench" tag is set to trick
		// the renderer into showing the enchantment effect on the actual armour model.
		
		if(caster.inventory.armorInventory[3] == null && !WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.spectral_helmet)){
			armour = new ItemStack(WizardryItems.spectral_helmet);
			IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.duration_upgrade));
			armour.getTagCompound().setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[3] = armour;
			flag = true;
		}

		if(caster.inventory.armorInventory[2] == null && !WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.spectral_chestplate)){
			armour = new ItemStack(WizardryItems.spectral_chestplate);
			IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.duration_upgrade));
			armour.getTagCompound().setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[2] = armour;
			flag = true;
		}

		if(caster.inventory.armorInventory[1] == null && !WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.spectral_leggings)){
			armour = new ItemStack(WizardryItems.spectral_leggings);
			IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.duration_upgrade));
			armour.getTagCompound().setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[1] = armour;
			flag = true;
		}

		if(caster.inventory.armorInventory[0] == null && !WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.spectral_boots)){
			armour = new ItemStack(WizardryItems.spectral_boots);
			IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.duration_upgrade));
			armour.getTagCompound().setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[0] = armour;
			flag = true;
		}

		if(flag){
			
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.7f, 0.9f, 1.0f);
				}
			}
			
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
		}
		
		return flag;
	}


}
