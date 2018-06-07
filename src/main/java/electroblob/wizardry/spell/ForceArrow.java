package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityForceArrow;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class ForceArrow extends Spell {

	public ForceArrow() {
		super(EnumTier.APPRENTICE, 15, EnumElement.SORCERY, "force_arrow", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityForceArrow forceArrow = new EntityForceArrow(world, caster, 1*rangeMultiplier, damageMultiplier);
			world.spawnEntityInWorld(forceArrow);
		}
		
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 1.4f);
		world.playSoundAtEntity(caster, "wizardry:magic", 1.0F, 1.2F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityForceArrow forceArrow = new EntityForceArrow(world, caster, target, 1*rangeMultiplier, 2, damageMultiplier);
				world.spawnEntityInWorld(forceArrow);
			}
			
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 1.4f);
			world.playSoundAtEntity(caster, "wizardry:magic", 1.0F, 1.2F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
