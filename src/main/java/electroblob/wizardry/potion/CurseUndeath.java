package electroblob.wizardry.potion;

import c4.conarm.common.armor.utils.ArmorHelper;
import c4.conarm.lib.tinkering.TinkersArmor;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.integration.conarm.WizardryConstructsArmoryIntegration;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CurseUndeath extends Curse {

	public CurseUndeath(boolean isBadEffect, int liquiidColour){
		super(isBadEffect, liquiidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icon_curse_of_undeath.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":curse_of_undeath");
	}

	@Override
	public boolean isReady(int duration, int amplifier){
		return true;
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength){

		// Adapted from EntityZombie
		if(entitylivingbase.world.isDaytime() && !entitylivingbase.world.isRemote){
			
			float f = entitylivingbase.getBrightness();

			if(f > 0.5F && entitylivingbase.world.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F
					&& entitylivingbase.world.canSeeSky(new BlockPos(entitylivingbase.posX,
					entitylivingbase.posY + (double)entitylivingbase.getEyeHeight(), entitylivingbase.posZ))){

				boolean flag = true;
				ItemStack itemstack = entitylivingbase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

				if(!itemstack.isEmpty()){

					if(WizardryConstructsArmoryIntegration.isTinkersArmor(itemstack) && entitylivingbase instanceof EntityPlayer){
						EntityPlayer player = (EntityPlayer) entitylivingbase;
						WizardryConstructsArmoryIntegration.damageArmor(itemstack, DamageSource.MAGIC, entitylivingbase.world.rand.nextInt(2), player);
					} else if(itemstack.isItemStackDamageable()){
						itemstack.setItemDamage(itemstack.getItemDamage() + entitylivingbase.world.rand.nextInt(2));

						if(itemstack.getItemDamage() >= itemstack.getMaxDamage()){
							entitylivingbase.renderBrokenItemStack(itemstack);
							entitylivingbase.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}

					flag = false;
				}

				if(flag){
					entitylivingbase.setFire(8);
				}
			}
		}
	}
}
