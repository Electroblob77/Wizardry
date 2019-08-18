package electroblob.wizardry.item;

import electroblob.wizardry.registry.Spells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSpectralArmour extends ItemArmor implements IConjuredItem {

	public ItemSpectralArmour(ArmorMaterial material, int renderIndex, EntityEquipmentSlot armourType){
		super(material, renderIndex, armourType);
		setCreativeTab(null);
		setMaxDamage(1200);
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.conjure_armour);
	}

	// Overridden to stop the enchantment trick making the name turn blue.
	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.COMMON;
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// onUpdate() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) player.inventory.clearMatchingItems(this, -1, 1, null);
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

	@Override
	public boolean isEnchantable(ItemStack stack){
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book){
		return false;
	}

	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player){
		return false;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){

		if(slot == EntityEquipmentSlot.LEGS) return "ebwizardry:textures/armour/spectral_armour_legs.png";

		return "ebwizardry:textures/armour/spectral_armour.png";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
			EntityEquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default){
		net.minecraft.client.renderer.GlStateManager.enableBlend();
		net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(
				net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
				net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE,
				net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
		);
		return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
	}

}
