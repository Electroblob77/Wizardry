package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntitySpark;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class HomingSpark extends Spell {

	public HomingSpark() {
		super(EnumTier.APPRENTICE, 10, EnumElement.LIGHTNING, "homing_spark", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntitySpark spark = new EntitySpark(world, caster, damageMultiplier);
			world.spawnEntityInWorld(spark);
			world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		}
		caster.swingItem();
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntitySpark spark = new EntitySpark(world, caster, damageMultiplier);
				spark.directTowards(target, 0.5f);
				world.spawnEntityInWorld(spark);
				world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
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
