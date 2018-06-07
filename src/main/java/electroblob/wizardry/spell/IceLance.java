package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityIceLance;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class IceLance extends Spell {

	public IceLance() {
		super(EnumTier.ADVANCED, 20, EnumElement.ICE, "ice_lance", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityIceLance iceLance = new EntityIceLance(world, caster, 2*rangeMultiplier, damageMultiplier);
			world.spawnEntityInWorld(iceLance);
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityIceLance iceLance = new EntityIceLance(world, caster, target, 2*rangeMultiplier, 4, damageMultiplier);
				world.spawnEntityInWorld(iceLance);
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
