package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityFlamecatcherArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Flamecatcher;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemFlamecatcher extends ItemBow implements IConjuredItem {

	public static final float DRAW_TIME = 10;

	public ItemFlamecatcher(){
		super();
		setMaxDamage(1200);
		setNoRepair();
		setCreativeTab(null);
		this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter(){
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn){
				if(entityIn == null){
					return 0.0F;
				}else{
					ItemStack itemstack = entityIn.getActiveItemStack();
					return itemstack.getItem() == ItemFlamecatcher.this
							? (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / DRAW_TIME : 0;
				}
			}
		});
		this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter(){
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn){
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1 : 0;
			}
		});
		addAnimationPropertyOverrides();
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.EPIC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	// Why does this still exist? Item models deal with this now, right?
	public boolean isFull3D(){
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.flamecatcher);
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
			if(oldStack.getItem() == newStack.getItem() && !slotChanged)
			// This code should only run on the client side, so using Minecraft is ok.
				// Why the heck was this here?
					//&& !net.minecraft.client.Minecraft.getMinecraft().player.isHandActive())
				return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	// Copied fixes from ItemWand made possible by recently-added Forge hooks

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return true;
		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return false;
		return super.shouldCauseBlockBreakReset(oldStack, newStack);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setItemDamage(damage + 1);
	}

	// The following two methods re-route the displayed durability through the proxies in order to override the pausing
	// of the item timer when the bow is being pulled.

	@Override
	public double getDurabilityForDisplay(ItemStack stack){
		return Wizardry.proxy.getConjuredBowDurability(stack);
	}

	public double getDefaultDurabilityForDisplay(ItemStack stack){
		return super.getDurabilityForDisplay(stack);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		int shotsLeft = stack.getTagCompound().getInteger(Flamecatcher.SHOTS_REMAINING_NBT_KEY);
		if(shotsLeft == 0) return ActionResult.newResult(EnumActionResult.PASS, stack);

		ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, world, player, hand,
				true);

		if(ret != null) return ret;

		player.setActiveHand(hand);

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);

	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack stack2){
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

//	@Override
//	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count){
//		// player.getItemInUseMaxCount() is named incorrectly; you only have to look at the method to see what it really
//		// does.
//		if(stack.getItemDamage() + player.getItemInUseMaxCount() > stack.getMaxDamage())
//			player.replaceItemInInventory(player.getActiveHand() == EnumHand.MAIN_HAND ? 98 : 99, ItemStack.EMPTY);
//	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int timeLeft){
		// Decreases the timer by the amount it should have been decreased while the bow was in use.
		if(!world.isRemote) stack.setItemDamage(stack.getItemDamage() + (this.getMaxItemUseDuration(stack) - timeLeft));

		if(entity instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)entity;

			int charge = this.getMaxItemUseDuration(stack) - timeLeft;
			charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, (EntityPlayer)entity, charge, true);
			if(charge < 0) return;

			if(stack.getTagCompound() != null){
				int shotsLeft = stack.getTagCompound().getInteger(Flamecatcher.SHOTS_REMAINING_NBT_KEY) - 1;
				stack.getTagCompound().setInteger(Flamecatcher.SHOTS_REMAINING_NBT_KEY, shotsLeft);
				if(shotsLeft == 0 && !world.isRemote){
					stack.setItemDamage(getMaxDamage(stack) - getAnimationFrames());
				}
			}

			float velocity = (float)charge / DRAW_TIME;
			velocity = (velocity * velocity + velocity * 2) / 3;

			if(velocity > 1) velocity = 1;

			if((double)velocity >= 0.1D){

				if(!world.isRemote){
					EntityFlamecatcherArrow arrow = new EntityFlamecatcherArrow(world);
					arrow.aim(player, EntityFlamecatcherArrow.SPEED * velocity);
					world.spawnEntity(arrow);
				}

				world.playSound(null, player.posX, player.posY, player.posZ,
						WizardrySounds.ITEM_FLAMECATCHER_SHOOT, WizardrySounds.SPELLS, 1, 1);

				world.playSound(null, player.posX, player.posY, player.posZ,
						WizardrySounds.ITEM_FLAMECATCHER_FLAME, WizardrySounds.SPELLS, 1, 1);
			}
		}
	}

}
