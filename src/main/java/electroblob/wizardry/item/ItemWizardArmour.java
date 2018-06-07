package electroblob.wizardry.item;

import java.util.List;
import java.util.Locale;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumElement;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ISpecialArmor;

public class ItemWizardArmour extends ItemArmor implements ISpecialArmor {
	
	/*
	 * In case I ever wonder what I was doing: what I think went wrong is that during development of wizardry 1.2,
	 * I did of course go through everything and make it dedicated server-compatible. I also fixed the lag spike bug
	 * caused by this very class. HOWEVER, I must have done that AFTER making everything dedicated server-compatible,
	 * and in my ignorance introduced a field which instantiated ModelBiped, which is of course client-only. This is
	 * now fixed and the fix is rolled back to 1.2.1 (but not 1.2), along with the arguably more important fix in
	 * EntityWizard. Now, for some reason importing ModelBiped in a common class is fine, as long as you don't
	 * instantiate it or reflect the class in question. In fact, the Item class does exactly this, and that's forge's
	 * doing, so it can't possibly break things.
	 */

	public EnumElement element;

	/** Armour types: 0 = helmet, 1 = chestplate, 2 = leggings, 3 = boots */
	public ItemWizardArmour(ArmorMaterial material, int renderIndex, int armourType, EnumElement element) {
		super(material, renderIndex, armourType);
		this.element = element;
		this.setCreativeTab(Wizardry.tabWizardry);
		
		// Sets item icon texture and unlocalised name according to element and armour type.
		switch(armourType){
		case 0:
			this.setTextureName(this.element == null ? "wizardry:wizard_hat" :
        	"wizardry:wizard_hat_" + this.element.unlocalisedName);
			this.setUnlocalizedName(this.element == null ? "wizard_hat" : "wizard_hat_" + this.element.unlocalisedName);
			break;
		case 1:
			this.setTextureName(this.element == null ? "wizardry:wizard_robe" :
        	"wizardry:wizard_robe_" + this.element.unlocalisedName);
			this.setUnlocalizedName(this.element == null ? "wizard_robe" : "wizard_robe_" + this.element.unlocalisedName);
			break;
		case 2:
			this.setTextureName(this.element == null ? "wizardry:wizard_leggings" :
        	"wizardry:wizard_leggings_" + this.element.unlocalisedName);
			this.setUnlocalizedName(this.element == null ? "wizard_leggings" : "wizard_leggings_" + this.element.unlocalisedName);
			break;
		case 3:
			this.setTextureName(this.element == null ? "wizardry:wizard_boots" :
        	"wizardry:wizard_boots_" + this.element.unlocalisedName);
			this.setUnlocalizedName(this.element == null ? "wizard_boots" : "wizard_boots_" + this.element.unlocalisedName);
			break;
		}
	}
	
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		
		if(par1ItemStack.hasTagCompound() && par1ItemStack.stackTagCompound.getBoolean("legendary")) par3List.add("\u00A7d" + StatCollector.translateToLocal("item.wizardArmour.legendary"));
		if(element != null) par3List.add("\u00A78" + StatCollector.translateToLocalFormatted("item.wizardArmour.buff", (int)(Wizardry.COST_REDUCTION_PER_ARMOUR*100) + "%", element.getDisplayName()));
		par3List.add("\u00A79" + StatCollector.translateToLocalFormatted("item.wizardArmour.mana", (this.getMaxDamage(par1ItemStack) - this.getDamage(par1ItemStack)), this.getMaxDamage(par1ItemStack)));
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_){
		
        return ((this.element == null ? "" : this.element.colour) + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(p_77653_1_) + ".name")).trim();
    }
	
	@Override
	public boolean hasEffect(ItemStack stack, int pass){
		return stack.hasTagCompound() && stack.stackTagCompound.getBoolean("legendary");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armourSlot){
		
		ModelBiped model = Wizardry.proxy.getWizardArmourModel();
		
		// Legs use modelBiped
		if(armourSlot == 2) return null;
		
		if(model != null){
			model.bipedHead.showModel = armourSlot == 0;
			model.bipedHeadwear.showModel = false;
			model.bipedBody.showModel = armourSlot == 1 || armourSlot == 2;
			model.bipedRightArm.showModel = armourSlot == 1;
			model.bipedLeftArm.showModel = armourSlot == 1;
			model.bipedRightLeg.showModel = armourSlot == 2 || armourSlot == 3;
			model.bipedLeftLeg.showModel = armourSlot == 2 || armourSlot == 3;
			
			model.isSneak = entityLiving.isSneaking();
			model.isRiding = entityLiving.isRiding();
			model.isChild = entityLiving.isChild();
			model.heldItemRight = entityLiving.getHeldItem() != null ? 1 : 0;
			
			if(entityLiving instanceof EntityPlayer){
				if(entityLiving.getHeldItem() != null){
					if(entityLiving.getHeldItem().getItem().getItemUseAction(entityLiving.getHeldItem()) == EnumAction.bow){
						model.aimedBow = ((EntityPlayer)entityLiving).getItemInUseDuration() > 0;
					}
					if(entityLiving.getHeldItem().getItem().getItemUseAction(entityLiving.getHeldItem()) == EnumAction.block){
						if(((EntityPlayer)entityLiving).getItemInUseDuration() > 0) model.heldItemRight = 3;
					}
				}
			}
		}
		
        return model;
    }
	
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type){
		
		// Returns a completely transparent texture if the player is invisible. This is such an annoyingly easy
		// fix, considering how long I spent trying to do this before - a bit of lateral thinking was all it took.
		// Do note however that a texture pack could override this.
		if(entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isInvisible()) return "wizardry:textures/armour/invisible_armour.png";
			
		if(slot == 2) return this.element == null ? "wizardry:textures/armour/wizard_armour_legs.png" :
        	"wizardry:textures/armour/wizard_armour_" + this.element.unlocalisedName + "_legs.png";
		
        return this.element == null ? "wizardry:textures/armour/wizard_armour.png" :
        	"wizardry:textures/armour/wizard_armour_" + this.element.unlocalisedName + ".png";
    }
	
	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
    {
        return false;
    }

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot){
		if (!source.isUnblockable() && armor.getItemDamage() < armor.getMaxDamage()){
			if(armor.hasTagCompound() && armor.stackTagCompound.getBoolean("legendary")){
				// Legendary armour gives full 10 shields, like diamond.
				return new ArmorProperties(0, ArmorMaterial.DIAMOND.getDamageReductionAmount(slot) / 25D, armor.getMaxDamage() + 1 - armor.getItemDamage());
			}else{
				return new ArmorProperties(0, this.damageReduceAmount / 25D, armor.getMaxDamage() + 1 - armor.getItemDamage());
			}
        }else{
        	return new ArmorProperties(0, 0, 0);
        }
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		if(armor.getItemDamage() < armor.getMaxDamage()){
			if(armor.hasTagCompound() && armor.stackTagCompound.getBoolean("legendary")){
				// Legendary armour gives full 10 shields, like diamond.
				return ArmorMaterial.DIAMOND.getDamageReductionAmount(slot);
			}else{
				return this.getArmorMaterial().getDamageReductionAmount(slot);
			}
		}else{
			return 0;
		}
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot){
		if(stack.getItemDamage() < stack.getMaxDamage()){
			if(stack.getItemDamage() + damage > stack.getMaxDamage()){
				// Note for reference: this is the method to use, despite how it sounds attemptDamageItem() is called
				// by this one, not the other way round.
				stack.damageItem(stack.getMaxDamage() - stack.getItemDamage(), entity);
			}else{
				stack.damageItem(damage, entity);
			}
		}
	}

}
