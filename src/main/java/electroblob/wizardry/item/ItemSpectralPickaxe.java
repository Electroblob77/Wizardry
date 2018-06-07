package electroblob.wizardry.item;

import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemSpectralPickaxe extends ItemPickaxe{

	public ItemSpectralPickaxe(ToolMaterial material) {
		super(material);
		this.setMaxDamage(600);
		this.setNoRepair();
		this.setCreativeTab(null);
	}
	
	@Override
	public int getMaxDamage(ItemStack stack)
    {
        return stack.hasTagCompound()? (int)(getMaxDamage()*stack.stackTagCompound.getFloat("durationMultiplier")) : getMaxDamage();
    }
	
	@Override
	public void onUpdate(ItemStack itemstack, World par2World, Entity entity, int par4, boolean par5) {
		// Allows the 'cheaty' damage bar rendering code to start working.
		if(itemstack.getItemDamage() == 0){
			itemstack.setItemDamage(1);
		}
		if(entity instanceof EntityPlayer){
			ExtendedPlayer properties = ExtendedPlayer.get((EntityPlayer)entity);
			properties.conjuredPickaxeDuration++;
			if(properties.conjuredPickaxeDuration > this.getMaxDamage(itemstack)){
				((EntityPlayer)entity).inventory.consumeInventoryItem(itemstack.getItem());
				properties.conjuredPickaxeDuration = 0;
			}
		}
	}
	
	@Override
	public int getDisplayDamage(ItemStack stack){
		return Wizardry.proxy.getConjuredItemDisplayDamage(stack);
    }
	
	@Override
	public boolean hasEffect(ItemStack par1ItemStack, int pass){
		return true;
	}
	
	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
    {
        return false;
    }
	
	@Override
	public int getItemEnchantability()
    {
        return 0;
    }
	
	//Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
    {
        return false;
    }

}
