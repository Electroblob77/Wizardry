package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class LightningDisc extends Spell {

	public LightningDisc() {
		super(EnumTier.ADVANCED, 25, EnumElement.LIGHTNING, "lightning_disc", EnumSpellType.ATTACK, 60, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			EntityLightningDisc lightningdisc = new EntityLightningDisc(world, caster, damageMultiplier);
			world.spawnEntityInWorld(lightningdisc);
			world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, world.rand.nextFloat() * 0.3F + 0.8F);
		}
		
		caster.swingItem();
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityLightningDisc lightningdisc = new EntityLightningDisc(world, caster, damageMultiplier);
				lightningdisc.directTowards(target, 1.2f);
				world.spawnEntityInWorld(lightningdisc);
				world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0F, world.rand.nextFloat() * 0.3F + 0.8F);
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
