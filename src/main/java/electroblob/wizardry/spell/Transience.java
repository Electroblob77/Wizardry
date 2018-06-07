package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Transience extends Spell {

	public Transience() {
		super(EnumTier.ADVANCED, 50, EnumElement.HEALING, "transience", EnumSpellType.DEFENCE, 100, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		ExtendedPlayer properties = ExtendedPlayer.get(caster);
		
		if(!caster.isPotionActive(Wizardry.transience)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(Wizardry.transience.id, (int)(400*durationMultiplier), 0, true));
				caster.addPotionEffect(new PotionEffect(Potion.invisibility.id, (int)(400*durationMultiplier), 0, true));
				world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
			}
			return true;
		}
		return false;
	}


}
