package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityDarknessOrb;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class DarknessOrb extends Spell {

	public DarknessOrb() {
		super(EnumTier.ADVANCED, 20, EnumElement.NECROMANCY, "darkness_orb", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityDarknessOrb darknessorb = new EntityDarknessOrb(world, caster, damageMultiplier);
			world.spawnEntityInWorld(darknessorb);
			world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		}
		
		caster.swingItem();
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				EntityDarknessOrb darknessorb = new EntityDarknessOrb(world, caster, damageMultiplier);
				darknessorb.directTowards(target, 0.5f);
				world.spawnEntityInWorld(darknessorb);
				world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
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
