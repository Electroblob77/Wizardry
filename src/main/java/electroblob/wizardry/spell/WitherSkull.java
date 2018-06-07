package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.EnumAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class WitherSkull extends Spell {

	public WitherSkull() {
		super(EnumTier.ADVANCED, 20, EnumElement.NECROMANCY, "wither_skull", EnumSpellType.ATTACK, 30, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		if(!world.isRemote){
			EntityWitherSkull witherskull = new EntityWitherSkull(world, caster, 1, 1, 1);
			witherskull.setPosition(
					caster.posX + look.xCoord,
					caster.posY + look.yCoord + 1.3,
					caster.posZ + look.zCoord);
			witherskull.accelerationX = look.xCoord * 0.1;
			witherskull.accelerationY = look.yCoord * 0.1;
			witherskull.accelerationZ = look.zCoord * 0.1;
			world.spawnEntityInWorld(witherskull);
			world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		}
		caster.swingItem();
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				
				EntityWitherSkull witherskull = new EntityWitherSkull(world, caster, 1, 1, 1);
				
				double dx = target.posX - caster.posX;
		        double dy = target.boundingBox.minY + (double)(target.height / 2.0F) - (caster.posY + (double)(caster.height / 2.0F));
		        double dz = target.posZ - caster.posZ;
		        
		        witherskull.accelerationX = dx/caster.getDistanceToEntity(target) * 0.1;
		        witherskull.accelerationY = dy/caster.getDistanceToEntity(target) * 0.1;
		        witherskull.accelerationZ = dz/caster.getDistanceToEntity(target) * 0.1;
		        
		        witherskull.setPosition(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
		        
				world.spawnEntityInWorld(witherskull);
				world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
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
