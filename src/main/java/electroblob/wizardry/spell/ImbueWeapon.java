package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ImbueWeapon extends Spell {

	public ImbueWeapon(){
		super("imbue_weapon", Tier.APPRENTICE, Element.SORCERY, SpellType.UTILITY, 20, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Won't work if the weapon already has the enchantment
		if(WizardData.get(caster) != null){

			for(ItemStack stack : WizardryUtilities.getPrioritisedHotbarAndOffhand(caster)){

				if(stack.getItem() instanceof ItemSword
						&& !EnchantmentHelper.getEnchantments(stack).containsKey(WizardryEnchantments.magic_sword)
						&& WizardData.get(caster).getImbuementDuration(WizardryEnchantments.magic_sword) <= 0){
					// The enchantment level as determined by the damage multiplier. The + 0.5f is so that
					// weird float processing doesn't incorrectly round it down.
					stack.addEnchantment(WizardryEnchantments.magic_sword, modifiers.get(SpellModifiers.POTENCY) == 1.0f
							? 1
							: (int)((modifiers.get(SpellModifiers.POTENCY) - 1.0f) / Constants.DAMAGE_INCREASE_PER_TIER
									+ 0.5f));
					WizardData.get(caster).setImbuementDuration(WizardryEnchantments.magic_sword,
							(int)(900 * modifiers.get(WizardryItems.duration_upgrade)));

				}else if(stack.getItem() instanceof ItemBow
						&& !EnchantmentHelper.getEnchantments(stack).containsKey(WizardryEnchantments.magic_bow)
						&& WizardData.get(caster).getImbuementDuration(WizardryEnchantments.magic_bow) <= 0){
					// The enchantment level as determined by the damage multiplier. The + 0.5f is so that
					// weird float processing doesn't incorrectly round it down.
					stack.addEnchantment(WizardryEnchantments.magic_bow, modifiers.get(SpellModifiers.POTENCY) == 1.0f
							? 1
							: (int)((modifiers.get(SpellModifiers.POTENCY) - 1.0f) / Constants.DAMAGE_INCREASE_PER_TIER
									+ 0.5f));
					WizardData.get(caster).setImbuementDuration(WizardryEnchantments.magic_bow,
							(int)(900 * modifiers.get(WizardryItems.duration_upgrade)));

				}else{
					continue;
				}

				if(world.isRemote){
					for(int i=0; i<10; i++){
						double x = caster.posX + world.rand.nextDouble() * 2 - 1;
						double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
						double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
						ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.9f, 0.7f, 1).spawn(world);
					}
				}

				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1, 1);
				return true;
			}
		}
		return false;
	}

}
