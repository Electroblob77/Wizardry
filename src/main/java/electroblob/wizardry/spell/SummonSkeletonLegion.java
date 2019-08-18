package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntityStrayMinion;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSkeletonLegion extends SpellMinion<EntitySkeletonMinion> {

	public SummonSkeletonLegion(){
		super("summon_skeleton_legion", EntitySkeletonMinion::new);
		this.soundValues(1, 1.1f, 0.1f);
	}

	@Override
	protected EntitySkeletonMinion createMinion(World world, EntityLivingBase caster, SpellModifiers modifiers){
		if(caster instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)caster, WizardryItems.charm_minion_variants)){
			return new EntityStrayMinion(world);
		}else{
			return super.createMinion(world, caster, modifiers);
		}
	}

	@Override
	protected void addMinionExtras(EntitySkeletonMinion minion, BlockPos pos, EntityLivingBase caster, SpellModifiers modifiers, int alreadySpawned){
		
		if(alreadySpawned % 2 == 0){
			// Archers
			minion.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		}else{
			// Swordsmen
			minion.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
		}
		
		minion.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
		minion.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
		minion.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
		minion.setDropChance(EntityEquipmentSlot.HEAD, 0.0f);
		minion.setDropChance(EntityEquipmentSlot.CHEST, 0.0f);
	}

}
