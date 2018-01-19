package electroblob.wizardry.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSpectralSword extends ItemSword implements IConjuredItem {

	public ItemSpectralSword(ToolMaterial material) {
		super(material);
		this.setMaxDamage(getBaseDuration());
		this.setNoRepair();
		this.setCreativeTab(null);
	}
	
	@Override
	public int getBaseDuration(){
		return 600;
	}
	
	@Override
	public int getMaxDamage(ItemStack stack){
        return this.getMaxDamageFromNBT(stack);
    }
	
	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// onUpdate() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){
		
		if(oldStack != null || newStack != null){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}
		
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) entity.replaceItemInInventory(slot, null);
		stack.setItemDamage(damage + 1);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack par2ItemStack){
        return false;
    }
	
	@Override
	public int getItemEnchantability(){
        return 0;
    }
	
	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player){
        return false;
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems){
        subItems.add(new ItemStack(this, 1));
    }

}
