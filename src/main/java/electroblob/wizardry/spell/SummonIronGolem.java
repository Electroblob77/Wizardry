package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SummonIronGolem extends Spell {

	public SummonIronGolem() {
		super(EnumTier.MASTER, 175, EnumElement.SORCERY, "summon_iron_golem", EnumSpellType.MINION, 400, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		double x = caster.posX + world.rand.nextDouble()*4 - 2;
		double z = caster.posZ + world.rand.nextDouble()*4 - 2;
		
		// Allows for height variation.
		if(WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 5) == -1){
			return false;
		}
		
		double y = Math.max(caster.posY, WizardryUtilities.getNearestFloorLevel(world, (int)x, (int)caster.posY, (int)z, 5));

		if(!world.isRemote){
			EntityIronGolem golem = new EntityIronGolem(world);
			if(Wizardry.showSummonedCreatureNames) golem.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), golem.getCommandSenderName()));
			golem.setPosition(x, y, z);
			world.spawnEntityInWorld(golem);
		}
		for(int i=0; i<10; i++){
			double x1 = (double)((float)x + world.rand.nextFloat()*2 - 1.0F);
			double y1 = (double)((float)y + 0.5F + world.rand.nextFloat());
			double z1 = (double)((float)z + world.rand.nextFloat()*2 - 1.0F);
			if(world.isRemote){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0, 0, 48 + world.rand.nextInt(12), 0.6f, 0.6f, 1.0f);
			}
		}
		world.playSoundAtEntity(caster, "mob.wither.spawn", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
