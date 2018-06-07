package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Tornado extends Spell {

	public Tornado() {
		super(EnumTier.ADVANCED, 35, EnumElement.EARTH, "tornado", EnumSpellType.ATTACK, 80, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			double x = caster.posX + caster.getLookVec().xCoord;
			double y = caster.posY;
			double z = caster.posZ + caster.getLookVec().zCoord;
			
			EntityTornado tornado = new EntityTornado(world, x, y, z, caster, (int)(200*durationMultiplier), caster.getLookVec().xCoord/3, caster.getLookVec().zCoord/3, damageMultiplier);
			world.spawnEntityInWorld(tornado);
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				double x = caster.posX + caster.getLookVec().xCoord;
				double y = caster.posY;
				double z = caster.posZ + caster.getLookVec().zCoord;
				
				EntityTornado tornado = new EntityTornado(world, x, y, z, caster, (int)(200*durationMultiplier), caster.getLookVec().xCoord/3, caster.getLookVec().zCoord/3, damageMultiplier);
				world.spawnEntityInWorld(tornado);
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
