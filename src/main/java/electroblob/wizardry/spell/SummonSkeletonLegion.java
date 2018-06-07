package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SummonSkeletonLegion extends Spell {

	public SummonSkeletonLegion() {
		super(EnumTier.MASTER, 100, EnumElement.NECROMANCY, "summon_skeleton_legion", EnumSpellType.MINION, 400, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		boolean flag = false;
		
		if(!world.isRemote){
			for(int i=0;i<3;i++){
				double x = caster.posX + world.rand.nextDouble()*6 - 3;
				double z = caster.posZ + world.rand.nextDouble()*6 - 3;
				// Allows for height variation.
				if(world.getTopSolidOrLiquidBlock((int)x, (int)z) - caster.posY < 6){
					flag = true;
					double y = Math.max(caster.posY, world.getTopSolidOrLiquidBlock((int)x, (int)z));
					EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, x, y, z, caster, (int)(1200*durationMultiplier));
					if(Wizardry.showSummonedCreatureNames) skeleton.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), skeleton.getCommandSenderName()));
					skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.bow));
					skeleton.setCurrentItemOrArmor(4, new ItemStack(Items.chainmail_helmet));
					skeleton.setCurrentItemOrArmor(3, new ItemStack(Items.chainmail_chestplate));
					skeleton.setEquipmentDropChance(0, 0.0f);
					skeleton.setEquipmentDropChance(3, 0.0f);
					skeleton.setEquipmentDropChance(4, 0.0f);
					world.spawnEntityInWorld(skeleton);
				}
			}
			
			for(int i=0;i<3;i++){
				double x = caster.posX + world.rand.nextDouble()*6 - 3;
				double z = caster.posZ + world.rand.nextDouble()*6 - 3;
				// Allows for height variation.
				if(world.getTopSolidOrLiquidBlock((int)x, (int)z) - caster.posY < 6){
					flag = true;
					double y = Math.max(caster.posY, world.getTopSolidOrLiquidBlock((int)x, (int)z));
					EntitySkeletonMinion skeleton = new EntitySkeletonMinion(world, x, y, z, caster, (int)(1200*durationMultiplier));
					if(Wizardry.showSummonedCreatureNames) skeleton.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), skeleton.getCommandSenderName()));
					skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword));
					skeleton.setCurrentItemOrArmor(4, new ItemStack(Items.chainmail_helmet));
					skeleton.setCurrentItemOrArmor(3, new ItemStack(Items.chainmail_chestplate));
					skeleton.setEquipmentDropChance(0, 0.0f);
					skeleton.setEquipmentDropChance(3, 0.0f);
					skeleton.setEquipmentDropChance(4, 0.0f);
					world.spawnEntityInWorld(skeleton);
				}
			}
		}
		
		if(flag){
			world.playSoundAtEntity(caster, "mob.wither.spawn", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		}
		
		// If no skeletons were spawned (like in a 1x1 hole or something) then the spell does not use up mana.
		return flag;
	}


}
