package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class SummonSpiritHorse extends Spell {

	public SummonSpiritHorse() {
		super(EnumTier.ADVANCED, 50, EnumElement.EARTH, "summon_spirit_horse", EnumSpellType.MINION, 150, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		ExtendedPlayer properties = ExtendedPlayer.get(caster);
		
		if(!properties.hasSpiritHorse){
			if(!world.isRemote){
				double x1 = caster.posX + world.rand.nextDouble()*4 - 2;
				double z1 = caster.posZ + world.rand.nextDouble()*4 - 2;
				// Allows for height variation.
				if(WizardryUtilities.getNearestFloorLevel(world, (int)x1, (int)caster.posY, (int)z1, 5) == -1){
					return false;
				}
				double y1 = Math.max(caster.posY, WizardryUtilities.getNearestFloorLevel(world, (int)x1, (int)caster.posY, (int)z1, 5));
				
				EntitySpiritHorse horse = new EntitySpiritHorse(world);
				if(Wizardry.showSummonedCreatureNames) horse.setCustomNameTag(StatCollector.translateToLocalFormatted("entity.wizardry.summonedcreature.nameplate", caster.getCommandSenderName(), horse.getCommandSenderName()));
				horse.setPosition(x1, y1, z1);
				horse.setTamedBy(caster);
				horse.setHorseSaddled(true);
				world.spawnEntityInWorld(horse);
			}
			properties.hasSpiritHorse = true;
			world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}


}
