package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SummonSpiritWolf extends Spell {

	public SummonSpiritWolf() {
		super(EnumTier.APPRENTICE, 25, EnumElement.EARTH, "summon_spirit_wolf", EnumSpellType.MINION, 100, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		ExtendedPlayer properties = ExtendedPlayer.get(caster);
		
		if(!properties.hasSpiritWolf){
			if(!world.isRemote){
				double x1 = caster.posX + world.rand.nextDouble()*4 - 2;
				double z1 = caster.posZ + world.rand.nextDouble()*4 - 2;
				// Allows for height variation.
				if(WizardryUtilities.getNearestFloorLevel(world, (int)x1, (int)caster.posY, (int)z1, 5) == -1){
					return false;
				}
				double y1 = Math.max(caster.posY, WizardryUtilities.getNearestFloorLevel(world, (int)x1, (int)caster.posY, (int)z1, 5));
				
				EntitySpiritWolf wolf = new EntitySpiritWolf(world);
				if(Wizardry.showSummonedCreatureNames) wolf.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), wolf.getCommandSenderName()));
				wolf.setPosition(x1, y1, z1);
				wolf.setTamed(true);
				wolf.func_152115_b(caster.getUniqueID().toString());
				world.spawnEntityInWorld(wolf);
			}
			properties.hasSpiritWolf = true;
			world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}

}
