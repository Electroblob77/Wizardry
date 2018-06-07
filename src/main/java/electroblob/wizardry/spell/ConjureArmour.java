package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class ConjureArmour extends Spell {

	public ConjureArmour() {
		super(EnumTier.ADVANCED, 45, EnumElement.HEALING, "conjure_armour", EnumSpellType.DEFENCE, 50, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		ItemStack armour;
		boolean flag = false;

		// Note that armourInventory is the other way round! Also note that a blank "ench" tag is set to trick
		// the renderer into showing the enchantment effect on the actual armour model.
		
		if(caster.inventory.armorInventory[3] == null && !caster.inventory.hasItem(Wizardry.spectralHelmet)){
			armour = new ItemStack(Wizardry.spectralHelmet);
			armour.stackTagCompound = new NBTTagCompound();
			armour.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);
			armour.stackTagCompound.setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[3] = armour;
			flag = true;
		}

		if(caster.inventory.armorInventory[2] == null && !caster.inventory.hasItem(Wizardry.spectralChestplate)){
			armour = new ItemStack(Wizardry.spectralChestplate);
			armour.stackTagCompound = new NBTTagCompound();
			armour.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);
			armour.stackTagCompound.setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[2] = armour;
			flag = true;
		}

		if(caster.inventory.armorInventory[1] == null && !caster.inventory.hasItem(Wizardry.spectralLeggings)){
			armour = new ItemStack(Wizardry.spectralLeggings);
			armour.stackTagCompound = new NBTTagCompound();
			armour.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);
			armour.stackTagCompound.setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[1] = armour;
			flag = true;
		}

		if(caster.inventory.armorInventory[0] == null && !caster.inventory.hasItem(Wizardry.spectralBoots)){
			armour = new ItemStack(Wizardry.spectralBoots);
			armour.stackTagCompound = new NBTTagCompound();
			armour.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);
			armour.stackTagCompound.setTag("ench", new NBTTagList());
			caster.inventory.armorInventory[0] = armour;
			flag = true;
		}

		if(flag){
			
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.7f, 0.9f, 1.0f);
				}
			}
			
			ExtendedPlayer.get(caster).conjuredArmourDuration = 0;
			world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
		}
		
		return flag;
	}


}
