package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityThunderbolt;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Thunderbolt extends Spell {

	public Thunderbolt() {
		super(EnumTier.BASIC, 10, EnumElement.LIGHTNING, "thunderbolt", EnumSpellType.ATTACK, 15, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(!world.isRemote){
			EntityThunderbolt thunderbolt = new EntityThunderbolt(world, caster, damageMultiplier);
			world.spawnEntityInWorld(thunderbolt);
			world.playSoundAtEntity(caster, "wizardry:ice", 0.8F, world.rand.nextFloat() * 0.2F + 0.8F);
		}
		
		caster.swingItem();
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityThunderbolt thunderbolt = new EntityThunderbolt(world, caster, damageMultiplier);
				thunderbolt.directTowards(target, 2.5f);
				world.spawnEntityInWorld(thunderbolt);
				world.playSoundAtEntity(caster, "wizardry:ice", 0.8F, world.rand.nextFloat() * 0.2F + 0.8F);
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
