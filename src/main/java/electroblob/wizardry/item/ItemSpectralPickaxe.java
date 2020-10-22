package electroblob.wizardry.item;

import electroblob.wizardry.registry.Spells;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemSpectralPickaxe extends ItemPickaxe implements IConjuredItem {

	private EnumRarity rarity = EnumRarity.COMMON;

	public ItemSpectralPickaxe(ToolMaterial material){
		super(material);
		setMaxDamage(1200);
		setNoRepair();
		setCreativeTab(null);
		addAnimationPropertyOverrides();
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
		return this.getMaxDamageFromNBT(stack, Spells.conjure_pickaxe);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
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
	public float getDestroySpeed(ItemStack stack, IBlockState state){
		float speed = super.getDestroySpeed(stack, state);
		return speed > 1 ? speed * IConjuredItem.getDamageMultiplier(stack) : speed;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState){
		// Reuses the standard bonus amplifier calculation from SpellBuff to increase the mining level at advanced and master tier
		return super.getHarvestLevel(stack, toolClass, player, blockState) + (int)((IConjuredItem.getDamageMultiplier(stack) - 1) / 0.4);
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
