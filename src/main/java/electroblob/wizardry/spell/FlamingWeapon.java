package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class FlamingWeapon extends Spell {

	public FlamingWeapon() {
		super(EnumTier.ADVANCED, 35, EnumElement.FIRE, "flaming_weapon", EnumSpellType.UTILITY, 70, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		// Won't work if the weapon already has the enchantment
		if(ExtendedPlayer.get(caster) != null && ExtendedPlayer.get(caster).flamingWeaponDuration <= 0){

			// Isolates just the hotbar
			List hotbar = ((ContainerPlayer)caster.openContainer).inventorySlots.subList(36, 45);

			for(Object slot : hotbar){

				if(slot instanceof Slot){

					ItemStack stack = ((Slot)slot).getStack();

					if(stack != null){
						
						if((stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemBow) && !EnchantmentHelper.getEnchantments(stack).containsKey(Wizardry.flamingWeapon.effectId)){
							// The enchantment level as determined by the damage multiplier. The + 0.5f is so that
							// weird float processing doesn't incorrectly round it down.
							stack.addEnchantment(Wizardry.flamingWeapon, damageMultiplier == 1.0f ? 1 : (int)((damageMultiplier - 1.0f)/Wizardry.DAMAGE_INCREASE_PER_TIER + 0.5f));
							
							ExtendedPlayer.get(caster).flamingWeaponDuration = (int)(900*durationMultiplier);
							
							if(world.isRemote){
								for(int i=0; i<10; i++){
									double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
									double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
									double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
									Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.9f, 0.7f, 1.0f);
								}
							}
	
							world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
							return true;
							
						}
					}
				}
			}
		}
		return false;
	}


}
