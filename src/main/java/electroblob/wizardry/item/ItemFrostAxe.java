package electroblob.wizardry.item;

import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ItemFrostAxe extends ItemAxe {

	public ItemFrostAxe(ToolMaterial material) {
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
			properties.frostAxeDuration++;
			if(properties.frostAxeDuration > this.getMaxDamage(itemstack)){
				((EntityPlayer)entity).inventory.consumeInventoryItem(itemstack.getItem());
				properties.frostAxeDuration = 0;
			}
		}
	}
	
	/**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase wielder)
    {
		if(!MagicDamage.isEntityImmune(DamageType.FROST, target)) target.addPotionEffect(new PotionEffect(Wizardry.frost.id, 160, 1));
        return false;
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
