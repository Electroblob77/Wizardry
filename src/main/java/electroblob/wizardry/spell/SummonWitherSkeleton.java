package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SummonWitherSkeleton extends Spell {

	public SummonWitherSkeleton() {
		super(EnumTier.ADVANCED, 35, EnumElement.NECROMANCY, "summon_wither_skeleton", EnumSpellType.MINION, 150, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			double x = caster.posX + world.rand.nextDouble()*4 - 2;
			double z = caster.posZ + world.rand.nextDouble()*4 - 2;
			// Allows for height variation.
			if(WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 5) == -1){
				return false;
			}
			double y = Math.max(caster.posY, WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 5));
			
			EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
			if(Wizardry.showSummonedCreatureNames) skeleton.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), skeleton.getCommandSenderName()));
			skeleton.setSkeletonType(1);
			skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
			skeleton.setEquipmentDropChance(0, 0.0f);
			world.spawnEntityInWorld(skeleton);
		}
		world.playSoundAtEntity(caster, "wizardry:darkaura", 7.0f, 0.6f);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){

		if(!world.isRemote){
			double x = caster.posX + world.rand.nextDouble()*4 - 2;
			double z = caster.posZ + world.rand.nextDouble()*4 - 2;
			// Allows for height variation.
			if(WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 5) == -1){
				return false;
			}
			double y = Math.max(caster.posY, WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 5));
			
			EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
			if(Wizardry.showSummonedCreatureNames) skeleton.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), skeleton.getCommandSenderName()));
			skeleton.setSkeletonType(1);
			skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
			skeleton.setEquipmentDropChance(0, 0.0f);
			world.spawnEntityInWorld(skeleton);
		}
		world.playSoundAtEntity(caster, "wizardry:darkaura", 7.0f, 0.6f);
		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
