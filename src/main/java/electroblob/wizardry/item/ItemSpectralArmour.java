package electroblob.wizardry.item;

import java.util.List;
import java.util.Locale;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumElement;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

public class ItemSpectralArmour extends ItemArmor {

	/** Armour types: 0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots */
	public ItemSpectralArmour(ArmorMaterial material, int renderIndex, int armourType) {
		
		super(material, renderIndex, armourType);
		
		this.setCreativeTab(null);
		this.setMaxDamage(1200);
		
		// Sets item icon texture and unlocalised name according to element and armour type.
		switch(armourType){
		case 0:
			this.setTextureName("wizardry:spectral_helmet");
			this.setUnlocalizedName("spectral_helmet");
			break;
		case 1:
			this.setTextureName("wizardry:spectral_chestplate");
			this.setUnlocalizedName("spectral_chestplate");
			break;
		case 2:
			this.setTextureName("wizardry:spectral_leggings");
			this.setUnlocalizedName("spectral_leggings");
			break;
		case 3:
			this.setTextureName("wizardry:spectral_boots");
			this.setUnlocalizedName("spectral_boots");
			break;
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack)
    {
        return stack.hasTagCompound()? (int)(getMaxDamage()*stack.stackTagCompound.getFloat("durationMultiplier")) : getMaxDamage();
    }
	
	// Overridden to stop the enchantment trick making the name turn blue.
	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_)
    {
        return EnumRarity.common;
    }
	
	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemstack) {
		// Allows the 'cheaty' damage bar rendering code to start working.
		if(itemstack.getItemDamage() == 0){
			itemstack.setItemDamage(1);
		}
		ExtendedPlayer properties = ExtendedPlayer.get(player);
		if(properties.conjuredArmourDuration > this.getMaxDamage(itemstack)){
			((EntityPlayer)player).inventory.armorInventory[3-this.armorType] = null;
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
	
	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
    {
        return false;
    }
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
    {
		if(slot == 2) return "wizardry:textures/armour/spectral_armour_legs.png";
		
		return "wizardry:textures/armour/spectral_armour.png";
    }

}
