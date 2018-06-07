package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Fireskin extends Spell {

	public Fireskin() {
		super(EnumTier.ADVANCED, 40, EnumElement.FIRE, "fireskin", EnumSpellType.DEFENCE, 250, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
				
		// Cannot be cast when it has already been cast
		if(!caster.isPotionActive(Wizardry.fireskin)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(Wizardry.fireskin.id, (int)(600*durationMultiplier), 0, true));
				world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(target != null){
			// Cannot be cast when it has already been cast
			if(!caster.isPotionActive(Wizardry.fireskin)){
				if(!world.isRemote){
					caster.addPotionEffect(new PotionEffect(Wizardry.fireskin.id, (int)(600*durationMultiplier), 0, true));
					world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
				}
				return true;
			}
			return false;
		}
		
		return false;
	}

	@Override
	public boolean canBeCastByNPCs() {
		return true;
	}
	
}
