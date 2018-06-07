package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class MagicMissile extends Spell {

	public MagicMissile() {
		super(EnumTier.BASIC, 5, EnumElement.MAGIC, "magic_missile", EnumSpellType.ATTACK, 10, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityMagicMissile magicMissile = new EntityMagicMissile(world, caster, 2*rangeMultiplier, damageMultiplier);
			world.spawnEntityInWorld(magicMissile);
		}
		
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:magic", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
		
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				EntityMagicMissile magicMissile = new EntityMagicMissile(world, caster, target, 2*rangeMultiplier, 4, damageMultiplier);
				world.spawnEntityInWorld(magicMissile);
			}
			
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:magic", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
	
}
