package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class SixthSense extends Spell {

	public SixthSense() {
		super(EnumTier.APPRENTICE, 20, EnumElement.EARTH, "sixth_sense", EnumSpellType.UTILITY, 100, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		ExtendedPlayer properties = ExtendedPlayer.get(caster);
		
		// Cannot be cast when it has already been cast
		if(!world.isRemote){
			caster.addPotionEffect(new PotionEffect(Wizardry.sixthSense.id, (int)(400*durationMultiplier), (int)((rangeMultiplier-1f)/Wizardry.RANGE_INCREASE_PER_LEVEL), true));
			world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		}
		return true;
	}


}
