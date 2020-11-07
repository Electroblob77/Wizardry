package electroblob.wizardry.spell;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class PocketFurnace extends Spell {

	public static final String ITEMS_SMELTED = "items_smelted";

	public PocketFurnace(){
		super("pocket_furnace", SpellActions.IMBUE, false);
		addProperties(ITEMS_SMELTED);
		soundValues(1, 0.75f, 0);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		int usesLeft = (int)(getProperty(ITEMS_SMELTED).floatValue() * modifiers.get(SpellModifiers.POTENCY));

		ItemStack stack, result;

		for(int i = 0; i < caster.inventory.getSizeInventory() && usesLeft > 0; i++){

			stack = caster.inventory.getStackInSlot(i);

			if(!stack.isEmpty() && !world.isRemote){

				result = FurnaceRecipes.instance().getSmeltingResult(stack);

				if(!result.isEmpty() && !(stack.getItem() instanceof ItemTool) && !(stack.getItem() instanceof ItemSword)
						&& !(stack.getItem() instanceof ItemArmor)
						&& !Settings.containsMetaItem(Wizardry.settings.pocketFurnaceItemBlacklist, stack)){

					if(stack.getCount() <= usesLeft){
						ItemStack stack2 = new ItemStack(result.getItem(), stack.getCount(), result.getItemDamage());
						if(InventoryUtils.doesPlayerHaveItem(caster, result.getItem())){
							caster.inventory.addItemStackToInventory(stack2);
							caster.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						}else{
							caster.inventory.setInventorySlotContents(i, stack2);
						}
						usesLeft -= stack.getCount();
					}else{
						caster.inventory.decrStackSize(i, usesLeft);
						caster.inventory.addItemStackToInventory(
								new ItemStack(result.getItem(), usesLeft, result.getItemDamage()));
						usesLeft = 0;
					}
				}
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		if(world.isRemote){
			for(int i = 0; i < 10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
				double y1 = (double)((float)caster.posY + caster.getEyeHeight() - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
				world.spawnParticle(EnumParticleTypes.FLAME, x1, y1, z1, 0, 0.01F, 0);
			}
		}

		return usesLeft < 5;
	}

}
