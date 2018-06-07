package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class FlamingAxe extends Spell {

	public FlamingAxe() {
		super(EnumTier.ADVANCED, 45, EnumElement.FIRE, "flaming_axe", EnumSpellType.UTILITY, 50, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		ItemStack flamingaxe = new ItemStack(Wizardry.flamingAxe);
		flamingaxe.stackTagCompound = new NBTTagCompound();
		flamingaxe.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);

		if(!caster.inventory.hasItem(Wizardry.flamingAxe) && caster.inventory.addItemStackToInventory(flamingaxe)){

			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					world.spawnParticle("flame", x1, y1, z1, 0, 0, 0);
				}
			}
			
			ExtendedPlayer.get(caster).flamingAxeDuration = 0;
			world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
			return true;
		}
		return false;
	}


}
