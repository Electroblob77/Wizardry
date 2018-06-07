package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.EnumAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Fireball extends Spell {

	public Fireball() {
		super(EnumTier.APPRENTICE, 10, EnumElement.FIRE, "fireball", EnumSpellType.ATTACK, 15, EnumAction.none, false);
		// Does 2.5 hearts of damage and 5 seconds of fire, for reference.
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		Vec3 look = caster.getLookVec();

		if(!world.isRemote){
			EntitySmallFireball fireball = new EntitySmallFireball(world, caster, 1, 1, 1);
			fireball.setPosition(
					caster.posX + look.xCoord,
					caster.posY + look.yCoord + 1.3,
					caster.posZ + look.zCoord);
			fireball.accelerationX = look.xCoord * 0.1;
			fireball.accelerationY = look.yCoord * 0.1;
			fireball.accelerationZ = look.zCoord * 0.1;
			world.spawnEntityInWorld(fireball);
			world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
		}

		caster.swingItem();
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				
				EntitySmallFireball fireball = new EntitySmallFireball(world, caster, 1, 1, 1);
				
				double dx = target.posX - caster.posX;
		        double dy = target.boundingBox.minY + (double)(target.height / 2.0F) - (caster.posY + (double)(caster.height / 2.0F));
		        double dz = target.posZ - caster.posZ;
		        
		        fireball.accelerationX = dx/caster.getDistanceToEntity(target) * 0.1;
		        fireball.accelerationY = dy/caster.getDistanceToEntity(target) * 0.1;
		        fireball.accelerationZ = dz/caster.getDistanceToEntity(target) * 0.1;
		        
		        fireball.setPosition(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
		        
				world.spawnEntityInWorld(fireball);
				// playAuxSFX specifically is server side, normal playSound is both I think
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
