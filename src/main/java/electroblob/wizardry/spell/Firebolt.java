package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityFirebolt;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Firebolt extends Spell {

	public Firebolt() {
		super(EnumTier.APPRENTICE, 10, EnumElement.FIRE, "firebolt", EnumSpellType.ATTACK, 10, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityFirebolt firebolt = new EntityFirebolt(world, caster, damageMultiplier);
			world.spawnEntityInWorld(firebolt);
			world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
		}
		
		caster.swingItem();
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				EntityFirebolt firebolt = new EntityFirebolt(world, caster, damageMultiplier);
				firebolt.directTowards(target, 2.5f);
				world.spawnEntityInWorld(firebolt);
				world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
			}
			
			caster.swingItem();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
