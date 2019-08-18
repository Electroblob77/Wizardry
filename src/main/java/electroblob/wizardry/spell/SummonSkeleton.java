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

public class SummonSkeleton extends SpellMinion<EntitySkeletonMinion> {

	public SummonSkeleton(){
		super("summon_skeleton", EntitySkeletonMinion::new);
		this.soundValues(7, 0.6f, 0);
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
		minion.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		minion.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
	}

}
