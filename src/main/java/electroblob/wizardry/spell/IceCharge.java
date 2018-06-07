package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityIceCharge;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class IceCharge extends Spell {

	public IceCharge() {
		super(EnumTier.ADVANCED, 20, EnumElement.ICE, "ice_charge", EnumSpellType.ATTACK, 30, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityIceCharge icecharge = new EntityIceCharge(world, caster, damageMultiplier, blastMultiplier);
			world.spawnEntityInWorld(icecharge);
		}
		
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityIceCharge icecharge = new EntityIceCharge(world, caster, damageMultiplier, blastMultiplier);
				icecharge.directTowards(target, 1.5f);
				world.spawnEntityInWorld(icecharge);
			}
			
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
