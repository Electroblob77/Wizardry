package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.entity.EntityShield;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Shield extends Spell {

	public Shield() {
		super(EnumTier.APPRENTICE, 5, EnumElement.HEALING, "shield", EnumSpellType.DEFENCE, 0, EnumAction.block, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		caster.addPotionEffect(new PotionEffect(Potion.resistance.id, 10, 0, true));
		
		if(ExtendedPlayer.get(caster).shield == null){
			ExtendedPlayer.get(caster).shield = new EntityShield(world, caster);
			if(!world.isRemote){
				world.spawnEntityInWorld(ExtendedPlayer.get(caster).shield);
			}
		}
		if(ticksInUse == 0){
			world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
		}
		return true;
	}


}
