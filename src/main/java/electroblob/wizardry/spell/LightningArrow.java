package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityLightningArrow;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class LightningArrow extends Spell {

	public LightningArrow() {
		super(EnumTier.APPRENTICE, 15, EnumElement.LIGHTNING, "lightning_arrow", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityLightningArrow lightningArrow = new EntityLightningArrow(world, caster, 2*rangeMultiplier, damageMultiplier);
			world.spawnEntityInWorld(lightningArrow);
		}
		
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, world.rand.nextFloat() * 0.3F + 1.3F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityLightningArrow lightningArrow = new EntityLightningArrow(world, caster, target, 2*rangeMultiplier, 4, damageMultiplier);
				world.spawnEntityInWorld(lightningArrow);
			}
			
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, world.rand.nextFloat() * 0.3F + 1.3F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
