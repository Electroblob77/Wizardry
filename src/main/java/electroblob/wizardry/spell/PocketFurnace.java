package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class PocketFurnace extends Spell {

	public PocketFurnace() {
		super(Tier.APPRENTICE, 30, Element.FIRE, "pocket_furnace", SpellType.UTILITY, 40, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		int usesLeft = 5;
		
		ItemStack stack, result;
		
		for(int i=0; i<caster.inventory.getSizeInventory() && usesLeft > 0; i++){
			
			stack = caster.inventory.getStackInSlot(i);
			
			if(stack != null){

				result = FurnaceRecipes.instance().getSmeltingResult(stack);
				
				if(result != null){
					if(stack.stackSize <= usesLeft){
						ItemStack stack2 = new ItemStack(result.getItem(), stack.stackSize, result.getItemDamage());
						if(WizardryUtilities.doesPlayerHaveItem(caster, result.getItem())){
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
		
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, 1, 0.75f);
		
		if(world.isRemote){
			for(int i=0; i<10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
				world.spawnParticle(EnumParticleTypes.FLAME, x1, y1, z1, 0, 0.01F, 0);
			}
		}
		
		return usesLeft < 5;
	}


}
