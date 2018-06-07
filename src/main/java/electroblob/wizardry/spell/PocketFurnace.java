package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;

public class PocketFurnace extends Spell {

	public PocketFurnace() {
		super(EnumTier.APPRENTICE, 30, EnumElement.FIRE, "pocket_furnace", EnumSpellType.UTILITY, 40, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		int usesLeft = 5;
		
		ItemStack stack, result;
		
		for(int i=0; i<caster.inventory.getSizeInventory() && usesLeft > 0; i++){
			
			stack = caster.inventory.getStackInSlot(i);
			
			if(stack != null){

				result = FurnaceRecipes.smelting().getSmeltingResult(stack);
				
				if(result != null){
					if(stack.stackSize <= usesLeft){
						ItemStack stack2 = new ItemStack(result.getItem(), stack.stackSize, result.getItemDamage());
						if(caster.inventory.hasItem(result.getItem())){
							caster.inventory.addItemStackToInventory(stack2);
							caster.inventory.setInventorySlotContents(i, null);
						}else{
							caster.inventory.setInventorySlotContents(i, stack2);
						}
						usesLeft -= stack.stackSize;
					}else{
						caster.inventory.decrStackSize(i, usesLeft);
						caster.inventory.addItemStackToInventory(new ItemStack(result.getItem(), usesLeft, result.getItemDamage()));
						usesLeft = 0;
					}
				}
			}
		}
		
		caster.playSound("fire.fire", 1, 0.75f);
		
		if(world.isRemote){
			for(int i=0; i<10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
				world.spawnParticle("flame", x1, y1, z1, 0, 0.01F, 0);
			}
		}
		
		return usesLeft < 5;
	}


}
