package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemSpectralBow extends ItemBow implements IConjuredItem {

	public ItemSpectralBow(){
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
					return itemstack.getItem() == ItemSpectralBow.this
							? (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F
							: 0.0F;
				}
			}
		});
		this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter(){
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn){
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F
						: 0.0F;
			}
		});
		addAnimationPropertyOverrides();
	}

	@Override
	@SideOnly(Side.CLIENT)
	// Why does this still exist? Item models deal with this now, right?
	public boolean isFull3D(){
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.conjure_bow);
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
		if(damage > stack.getMaxDamage()) entity.replaceItemInInventory(slot, ItemStack.EMPTY);
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

		ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, world, player, hand,
				true);

		if(ret != null) return ret;

		player.setActiveHand(hand);

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
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

			EntityPlayer entityplayer = (EntityPlayer)entity;

			int i = this.getMaxItemUseDuration(stack) - timeLeft;
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, (EntityPlayer)entity, i, true);
			if(i < 0) return;

			float f = getArrowVelocity(i);

			if((double)f >= 0.1D){

				if(!world.isRemote){

					ItemArrow itemarrow = (ItemArrow)Items.ARROW;
					EntityArrow entityarrow = itemarrow.createArrow(world, new ItemStack(itemarrow), entityplayer);
					entityarrow.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F,
							f * 3.0F, 1.0F);

					if(f == 1.0F){
						entityarrow.setIsCritical(true);
					}

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

					if(j > 0){
						entityarrow.setDamage(entityarrow.getDamage() + (double)j * 0.5D + 0.5D);
					}

					int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

					if(k > 0){
						entityarrow.setKnockbackStrength(k);
					}

					if(EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0){
						entityarrow.setFire(100);
					}

					entityarrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;

					entityarrow.setDamage(entityarrow.getDamage() * IConjuredItem.getDamageMultiplier(stack));

					world.spawnEntity(entityarrow);
				}

				world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ,
						SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F,
						1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

				entityplayer.addStat(StatList.getObjectUseStats(this));
			}
		}
	}

}
