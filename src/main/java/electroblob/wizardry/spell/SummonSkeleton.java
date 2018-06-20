package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class SummonSkeleton extends SpellMinion<EntitySkeletonMinion> {

	public SummonSkeleton(){
		super("summon_skeleton", Tier.APPRENTICE, Element.NECROMANCY, 15, 50, EntitySkeletonMinion::new, 600, WizardrySounds.SPELL_SUMMONING);
		this.soundValues(7, 0.6f, 0);
	}
	
	@Override
	protected void addMinionExtras(EntitySkeletonMinion minion, EntityLivingBase caster, SpellModifiers modifiers, int alreadySpawned){
		minion.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
		minion.setDropChance(EntityEquipmentSlot.MAINHAND, 0.0f);
	}

}
