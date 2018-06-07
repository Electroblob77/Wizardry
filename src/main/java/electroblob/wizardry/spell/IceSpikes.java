package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class IceSpikes extends Spell {

	public IceSpikes() {
		super(EnumTier.ADVANCED, 30, EnumElement.ICE, "ice_spikes", EnumSpellType.ATTACK, 75, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(20*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK && rayTrace.sideHit == 1){
			
			if(!world.isRemote){
				
				double x = rayTrace.blockX;
				double y = rayTrace.blockY;
				double z = rayTrace.blockZ;
				
				for(int i=0; i<(int)(18*blastMultiplier); i++){
					
					float angle = (float)(world.rand.nextFloat()*Math.PI*2);
					double radius = 0.5 + world.rand.nextDouble()*2*blastMultiplier;
					
					double x1 = x + radius*MathHelper.sin(angle);
					double z1 = z + radius*MathHelper.cos(angle);
					double y1 = WizardryUtilities.getNearestFloorLevel(world, MathHelper.floor_double(x1), (int)y, MathHelper.floor_double(z1), 2) - 1;
					
					if(y1 > -1){
						EntityIceSpike icespike = new EntityIceSpike(world, x1, y1, z1, caster, 30 + world.rand.nextInt(15), damageMultiplier);
						world.spawnEntityInWorld(icespike);
					}
				}
			}
			
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				
				double x = target.posX;
				double y = target.posY;
				double z = target.posZ;
				
				for(int i=0; i<(int)(18*blastMultiplier); i++){
					
					float angle = (float)(world.rand.nextFloat()*Math.PI*2);
					double radius = 0.5 + world.rand.nextDouble()*2*blastMultiplier;
					
					double x1 = x + radius*MathHelper.sin(angle);
					double z1 = z + radius*MathHelper.cos(angle);
					double y1 = WizardryUtilities.getNearestFloorLevel(world, MathHelper.floor_double(x1), (int)y, MathHelper.floor_double(z1), 2) - 1;
					
					if(y1 > -1){
						EntityIceSpike icespike = new EntityIceSpike(world, x1, y1, z1, caster, 30 + world.rand.nextInt(15), damageMultiplier);
						world.spawnEntityInWorld(icespike);
					}
				}
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, 1.0F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
	
}
