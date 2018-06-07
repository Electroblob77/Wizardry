package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityEarthquake;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Earthquake extends Spell {

	public Earthquake() {
		super(EnumTier.MASTER, 75, EnumElement.EARTH, "earthquake", EnumSpellType.ATTACK, 250, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.onGround){
			
			if(!world.isRemote){

				world.spawnEntityInWorld(new EntityEarthquake(world, caster.posX, caster.boundingBox.minY, caster.posZ, caster, (int)(20*blastMultiplier), damageMultiplier));
				world.playSoundAtEntity(caster, "wizardry:rumble", 2, 1);
				
			}else{
				
				world.spawnParticle("largeexplode", caster.posX, caster.boundingBox.minY + 0.1, caster.posZ, 0, 0, 0);
				
				double particleX, particleZ;
				
				for(int i=0;i<40;i++){
					
					particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
					particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
					
					Block block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
					if(block != null){
						Wizardry.proxy.spawnDigParticle(world, particleX, caster.boundingBox.minY, particleZ, particleX - caster.posX, 0, particleZ - caster.posZ, block);
					}
				}
			}
			
			caster.swingItem();
			
			return true;
		}
		return false;
	}


}
