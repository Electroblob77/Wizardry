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
import net.minecraft.world.World;

public class FrostAxe extends Spell {

	public FrostAxe() {
		super(EnumTier.ADVANCED, 45, EnumElement.ICE, "frost_axe", EnumSpellType.UTILITY, 50, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		ItemStack frostaxe = new ItemStack(Wizardry.frostAxe);
		frostaxe.stackTagCompound = new NBTTagCompound();
		frostaxe.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);

		if(!caster.inventory.hasItem(Wizardry.frostAxe) && caster.inventory.addItemStackToInventory(frostaxe)){

			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, world, x1, y1, z1, 0, -0.02d, 0, 40 + world.rand.nextInt(10));
				}
			}
			
			ExtendedPlayer.get(caster).frostAxeDuration = 0;
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0f, 1.0f);
			return true;
		}
		return false;
	}


}
