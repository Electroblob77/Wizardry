package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class SpiderSwarm extends Spell {

	public SpiderSwarm() {
		super(EnumTier.ADVANCED, 45, EnumElement.EARTH, "spider_swarm", EnumSpellType.MINION, 200, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		boolean flag = false;
		
		if(!world.isRemote){
			for(int i=0;i<5;i++){
				double x = caster.posX + world.rand.nextDouble()*6 - 3;
				double z = caster.posZ + world.rand.nextDouble()*6 - 3;
				// Allows for height variation.
				if(world.getTopSolidOrLiquidBlock((int)x, (int)z) - caster.posY < 6){
					flag = true;
					double y = Math.max(caster.posY, world.getTopSolidOrLiquidBlock((int)x, (int)z));
					EntitySpiderMinion spider = new EntitySpiderMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
					world.spawnEntityInWorld(spider);
				}
			}
		}
		
		if(flag){
			world.playSoundAtEntity(caster, "random.fizz", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		}
		// If no spiders were spawned (like in a 1x1 hole or something) then the spell does not use up mana.
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		boolean flag = false;
		
		if(!world.isRemote){
			for(int i=0;i<5;i++){
				double x = caster.posX + world.rand.nextDouble()*6 - 3;
				double z = caster.posZ + world.rand.nextDouble()*6 - 3;
				// Allows for height variation.
				if(world.getTopSolidOrLiquidBlock((int)x, (int)z) - caster.posY < 6){
					flag = true;
					double y = Math.max(caster.posY, world.getTopSolidOrLiquidBlock((int)x, (int)z));
					EntitySpiderMinion spider = new EntitySpiderMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
					world.spawnEntityInWorld(spider);
				}
			}
		}
		
		if(flag){
			world.playSoundAtEntity(caster, "random.fizz", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		}
		// If no spiders were spawned (like in a 1x1 hole or something) then the spell does not use up mana.
		return flag;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
}
