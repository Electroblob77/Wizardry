package electroblob.wizardry.item;

import javax.swing.Icon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

public class ItemSpectralBow extends ItemBow {

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
	
	public ItemSpectralBow() {
		super();
		this.setMaxDamage(600);
		this.setNoRepair();
		this.setCreativeTab(null);
	}
	
	@Override
	public boolean isFull3D(){
		return true;
	}
	
	@Override
	public int getMaxDamage(ItemStack stack)
    {
        return stack.hasTagCompound()? (int)(getMaxDamage()*stack.stackTagCompound.getFloat("durationMultiplier")) : getMaxDamage();
    }
	
	@Override
    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_)
    {
        ArrowNockEvent event = new ArrowNockEvent(p_77659_3_, p_77659_1_);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
        {
            return event.result;
        }

        // Spectral bow always fires, regardless of whether you have an arrow or not.
        p_77659_3_.setItemInUse(p_77659_1_, this.getMaxItemUseDuration(p_77659_1_));

        return p_77659_1_;
    }
	
	@Override
	public void onUpdate(ItemStack itemstack, World par2World, Entity entity, int par4, boolean par5) {
		// Allows the 'cheaty' damage bar rendering code to start working.
		if(itemstack.getItemDamage() == 0){
			itemstack.setItemDamage(1);
		}
		if(entity instanceof EntityPlayer){
			ExtendedPlayer properties = ExtendedPlayer.get((EntityPlayer)entity);
			properties.conjuredBowDuration++;
			if(properties.conjuredBowDuration > this.getMaxDamage(itemstack)){
				((EntityPlayer)entity).inventory.consumeInventoryItem(itemstack.getItem());
				properties.conjuredBowDuration = 0;
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
	
	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player)
    {
        return false;
    }
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon("wizardry:spectral_bow_standby");
        this.iconArray = new IIcon[bowPullIconNameArray.length];

        for (int i = 0; i < this.iconArray.length; ++i)
        {
            this.iconArray[i] = par1IconRegister.registerIcon("wizardry:spectral_bow_" + bowPullIconNameArray[i]);
        }
    }
	
	@Override
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
             if(player.getItemInUse() == null) return this.itemIcon;
             int time = stack.getMaxItemUseDuration() - useRemaining;
             if (time >= 18)
             {
                     return iconArray[2];
             }
             else if (time > 9)
             {
                     return iconArray[1];
             }
             else if (time > 0)
             {
                     return iconArray[0];
             }             
             return itemIcon;
    }

	@Override
	public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4)
    {
        int j = this.getMaxItemUseDuration(par1ItemStack) - par4;

        ArrowLooseEvent event = new ArrowLooseEvent(par3EntityPlayer, par1ItemStack, j);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
        {
            return;
        }
        j = event.charge;
        
        float f = (float)j / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;

        if ((double)f < 0.1D)
        {
            return;
        }

        if (f > 1.0F)
        {
            f = 1.0F;
        }

        EntityArrow entityarrow = new EntityArrow(par2World, par3EntityPlayer, f * 2.0F);

        if (f == 1.0F)
        {
            entityarrow.setIsCritical(true);
        }

        int k = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, par1ItemStack);

        if (k > 0)
        {
            entityarrow.setDamage(entityarrow.getDamage() + (double)k * 0.5D + 0.5D);
        }

        int l = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, par1ItemStack);

        if (l > 0)
        {
            entityarrow.setKnockbackStrength(l);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, par1ItemStack) > 0)
        {
            entityarrow.setFire(100);
        }

        par1ItemStack.damageItem(1, par3EntityPlayer);
        par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

        entityarrow.canBePickedUp = 2;

        if (!par2World.isRemote)
        {
            par2World.spawnEntityInWorld(entityarrow);
        }
    }
}
