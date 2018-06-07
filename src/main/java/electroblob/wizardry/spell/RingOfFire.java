package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.construct.EntityFireRing;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class RingOfFire extends Spell {

	public RingOfFire() {
		super(EnumTier.ADVANCED, 30, EnumElement.FIRE, "ring_of_fire", EnumSpellType.ATTACK, 100, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.onGround){
			if(!world.isRemote){
				EntityFireRing firering = new EntityFireRing(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*durationMultiplier), damageMultiplier);
				world.spawnEntityInWorld(firering);
				world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			if(caster.onGround && world.getEntitiesWithinAABB(EntityFireRing.class, caster.boundingBox).isEmpty()){
				if(!world.isRemote){
					EntityFireRing firering = new EntityFireRing(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*durationMultiplier), damageMultiplier);
					world.spawnEntityInWorld(firering);
					world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
				}
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
