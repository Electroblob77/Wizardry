package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class SilverfishSwarm extends Spell {

	public SilverfishSwarm() {
		super(EnumTier.MASTER, 80, EnumElement.EARTH, "silverfish_swarm", EnumSpellType.MINION, 300, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		boolean flag = false;
		
		if(!world.isRemote){
			for(int i=0;i<20;i++){
				double x = caster.posX + world.rand.nextDouble()*6 - 3;
				double z = caster.posZ + world.rand.nextDouble()*6 - 3;
				// Allows for height variation.
				if(world.getTopSolidOrLiquidBlock((int)x, (int)z) - caster.posY < 6){
					flag = true;
					double y = Math.max(caster.posY, world.getTopSolidOrLiquidBlock((int)x, (int)z));
					EntitySilverfishMinion silverfish = new EntitySilverfishMinion(world, x, y, z, caster, (int)(600*durationMultiplier));
					world.spawnEntityInWorld(silverfish);
				}
			}
		}
		
		if(flag){
			world.playSoundAtEntity(caster, "random.fizz", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		}
		// If no spiders were spawned (like in a 1x1 hole or something) then the spell does not use up mana.
		return flag;
	}


}
