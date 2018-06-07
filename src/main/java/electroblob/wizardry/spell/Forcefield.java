package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Forcefield extends Spell {

	public Forcefield() {
		super(EnumTier.ADVANCED, 45, EnumElement.HEALING, "forcefield", EnumSpellType.DEFENCE, 200, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.onGround){
			if(!world.isRemote){
				EntityForcefield forcefield = new EntityForcefield(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*durationMultiplier));
				world.spawnEntityInWorld(forcefield);
			}
			world.playSoundAtEntity(caster, "wizardry:largeaura", 1.0f, 1.0f);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			// Wizards can no longer cast forcefield when they are inside one
			if(caster.onGround && world.getEntitiesWithinAABB(EntityForcefield.class, caster.boundingBox).isEmpty()){
				if(!world.isRemote){
					EntityForcefield forcefield = new EntityForcefield(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*durationMultiplier));
					world.spawnEntityInWorld(forcefield);
				}
				world.playSoundAtEntity(caster, "wizardry:largeaura", 1.0f, 1.0f);
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
