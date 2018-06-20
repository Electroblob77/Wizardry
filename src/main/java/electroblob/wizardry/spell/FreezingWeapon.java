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

public class FreezingWeapon extends Spell {

	/**
	 * The NBT tag name for storing the level of frost enchantment in the arrow's tag compound. (There's nothing
	 * stopping you from using this elsewhere to shoot freezing arrows if you want to...)
	 */
	public static final String FREEZING_ARROW_NBT_KEY = "frostLevel";

	public FreezingWeapon(){
		super("freezing_weapon", Tier.ADVANCED, Element.ICE, SpellType.UTILITY, 35, 70, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Won't work if the weapon already has the enchantment
		if(WizardData.get(caster) != null
				&& WizardData.get(caster).getImbuementDuration(WizardryEnchantments.freezing_weapon) <= 0){

			for(ItemStack stack : WizardryUtilities.getPrioritisedHotbarAndOffhand(caster)){

				if((stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemBow)
						&& !EnchantmentHelper.getEnchantments(stack).containsKey(WizardryEnchantments.freezing_weapon)){
					// The enchantment level as determined by the damage multiplier. The + 0.5f is so that
					// weird float processing doesn't incorrectly round it down.
					stack.addEnchantment(WizardryEnchantments.freezing_weapon,
							modifiers.get(SpellModifiers.POTENCY) == 1.0f ? 1
									: (int)((modifiers.get(SpellModifiers.POTENCY) - 1.0f)
											/ Constants.DAMAGE_INCREASE_PER_TIER + 0.5f));

					WizardData.get(caster).setImbuementDuration(WizardryEnchantments.freezing_weapon,
							(int)(900 * modifiers.get(WizardryItems.duration_upgrade)));

					if(world.isRemote){
						for(int i=0; i<10; i++){
							double x = caster.posX + world.rand.nextDouble() * 2 - 1;
							double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
							double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
							ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).colour(0.9f, 0.7f, 1).spawn(world);
						}
					}

					WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1, 1);
					return true;

				}
			}
		}
		return false;
	}

}
