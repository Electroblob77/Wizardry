package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Decay extends Spell {

	public Decay(){
		super(EnumTier.ADVANCED, 50, EnumElement.NECROMANCY, "decay", EnumSpellType.ATTACK, 200, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(12*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){

			int x = rayTrace.blockX;
			int y = rayTrace.blockY;
			int z = rayTrace.blockZ;
			
			if(world.getBlock(x, y+1, z).isNormalCube()) return false;
			
			if(!world.isRemote){
				
				world.spawnEntityInWorld(new EntityDecay(world, x+0.5, y+1, z+0.5, caster));
				
				for(int i=0;i<5;i++){
					double x1 = x + world.rand.nextDouble()*4 - 2;
					double z1 = z + world.rand.nextDouble()*4 - 2;
					// Allows for height variation.
					if(world.getTopSolidOrLiquidBlock((int)x1, (int)z1) - y < 6){
						double y1 = Math.max(y, world.getTopSolidOrLiquidBlock((int)x1, (int)z1));
						world.spawnEntityInWorld(new EntityDecay(world, x1+0.5, y1, z1+0.5, caster));
					}
				}
			}
			
			world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
			caster.swingItem();
			return true;
		}
		
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			int x = MathHelper.floor_double(target.posX);
			int y = (int)(int)target.boundingBox.minY;
			int z = MathHelper.floor_double(target.posZ);
			
			if(world.getBlock(x, y, z).isNormalCube()) return false;
			
			if(!world.isRemote){
				
				world.spawnEntityInWorld(new EntityDecay(world, x+0.5, y+1, z+0.5, caster));
				
				for(int i=0;i<5;i++){
					double x1 = x + world.rand.nextDouble()*4 - 2;
					double z1 = z + world.rand.nextDouble()*4 - 2;
					// Allows for height variation.
					if(world.getTopSolidOrLiquidBlock((int)x1, (int)z1) - y < 6){
						double y1 = Math.max(y, world.getTopSolidOrLiquidBlock((int)x1, (int)z1));
						world.spawnEntityInWorld(new EntityDecay(world, x1+0.5, y1, z1+0.5, caster));
					}
				}
			}
			
			world.playSoundAtEntity(caster, "mob.wither.shoot", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
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
