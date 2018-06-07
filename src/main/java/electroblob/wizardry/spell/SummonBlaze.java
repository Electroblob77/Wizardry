package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SummonBlaze extends Spell {

	public SummonBlaze() {
		super(EnumTier.ADVANCED, 40, EnumElement.FIRE, "summon_blaze", EnumSpellType.MINION, 200, EnumAction.bow, false);
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
			
			EntityBlazeMinion blaze = new EntityBlazeMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
			if(Wizardry.showSummonedCreatureNames) blaze.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), blaze.getCommandSenderName()));
			world.spawnEntityInWorld(blaze);
		}
		
		world.playSoundAtEntity(caster, "mob.wither.idle", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
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
			
			EntityBlazeMinion blaze = new EntityBlazeMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
			if(Wizardry.showSummonedCreatureNames) blaze.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), blaze.getCommandSenderName()));
			world.spawnEntityInWorld(blaze);
		}
		
		world.playSoundAtEntity(caster, "mob.wither.idle", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
