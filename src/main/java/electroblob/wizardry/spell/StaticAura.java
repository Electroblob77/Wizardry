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

public class StaticAura extends Spell {

	public StaticAura() {
		super(EnumTier.ADVANCED, 40, EnumElement.LIGHTNING, "static_aura", EnumSpellType.DEFENCE, 250, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		// Cannot be cast when it has already been cast
		if(!caster.isPotionActive(Wizardry.staticAura)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(Wizardry.staticAura.id, (int)(600*durationMultiplier), 0, true));
				world.playSoundAtEntity(caster, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(target != null){
			// Cannot be cast when it has already been cast
			if(!caster.isPotionActive(Wizardry.staticAura)){
				if(!world.isRemote){
					caster.addPotionEffect(new PotionEffect(Wizardry.staticAura.id, (int)(600*durationMultiplier), 0, true));
					world.playSoundAtEntity(caster, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
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
