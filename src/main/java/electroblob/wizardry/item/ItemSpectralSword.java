package electroblob.wizardry.item;

import com.google.common.collect.Multimap;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSpectralSword extends ItemSword implements IConjuredItem {

	private EnumRarity rarity = EnumRarity.COMMON;

	public ItemSpectralSword(ToolMaterial material){
		super(material);
		setMaxDamage(1200);
		setNoRepair();
		setCreativeTab(null);
		addAnimationPropertyOverrides();
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack){

		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);

		if(slot == EntityEquipmentSlot.MAINHAND){
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(POTENCY_MODIFIER,
					"Potency modifier", IConjuredItem.getDamageMultiplier(stack) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
		}

		return multimap;
	}

	public Item setRarity(EnumRarity rarity){
		this.rarity = rarity;
		return this;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return rarity;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.conjure_sword);
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
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) entity.replaceItemInInventory(slot, ItemStack.EMPTY);
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

}
