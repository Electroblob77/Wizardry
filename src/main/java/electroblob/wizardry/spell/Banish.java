package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityBubble;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Banish extends Spell {

	public Banish() {
		super(EnumTier.APPRENTICE, 15, EnumElement.NECROMANCY, "banish", EnumSpellType.ATTACK, 40, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			double radius = (8 + world.rand.nextDouble()*8) * rangeMultiplier;
			double angle = world.rand.nextDouble()*Math.PI*2;
			
			int x = MathHelper.floor_double(target.posX + Math.sin(angle)*radius);
			int z = MathHelper.floor_double(target.posZ - Math.cos(angle)*radius);
			int y = WizardryUtilities.getNearestFloorLevel(world, x, (int)caster.boundingBox.minY, z, (int)radius);
			
			if(world.isRemote){
				for(int i=0;i<10;i++){
					double dx1 = target.posX;
					double dy1 = target.boundingBox.minY + target.height*world.rand.nextFloat();
					double dz1 = target.posZ;
					world.spawnParticle("portal", dx1, dy1, dz1, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
				}
			}
			
			if(y > -1){
				
				// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
				// not teleport 1 block above the ground.
				if(!world.getBlock(x, y, z).getMaterial().blocksMovement()){
					y--;
				}
				
				if(world.getBlock(x, y + 1, z).getMaterial().blocksMovement() || world.getBlock(x, y + 2, z).getMaterial().blocksMovement()){
					return false;
				}
				
				if(!world.isRemote){
					target.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
				}
				
				world.playSoundAtEntity(target, "mob.endermen.portal", 1.0F, 1.0f);
			}
		}
		
		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;

				world.spawnParticle("portal", x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.2f, 0.0f, 0.2f);
			}
		}
		
		world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
		caster.swingItem();
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			double radius = (8 + world.rand.nextDouble()*8) * rangeMultiplier;
			double angle = world.rand.nextDouble()*Math.PI*2;
			
			int x = MathHelper.floor_double(target.posX + Math.sin(angle)*radius);
			int z = MathHelper.floor_double(target.posZ - Math.cos(angle)*radius);
			int y = WizardryUtilities.getNearestFloorLevel(world, x, (int)caster.boundingBox.minY, z, (int)radius);
			
			if(world.isRemote){
				
				double dx = (target.posX - caster.posX)/caster.getDistanceToEntity(target);
				double dy = (target.posY - caster.posY)/caster.getDistanceToEntity(target);
				double dz = (target.posZ - caster.posZ)/caster.getDistanceToEntity(target);
				
				for(int i=1; i<25; i+=2){

					double x1 = caster.posX + dx*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double z1 = caster.posZ + dz*i/2 + world.rand.nextFloat()/5 - 0.1f;

					world.spawnParticle("portal", x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.2f, 0.0f, 0.2f);
				}
				
				for(int i=0;i<10;i++){
					double dx1 = target.posX;
					double dy1 = target.boundingBox.minY + target.height*world.rand.nextFloat();
					double dz1 = target.posZ;
					world.spawnParticle("portal", dx1, dy1, dz1, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
				}
			}
			
			if(y > -1){
				
				// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
				// not teleport 1 block above the ground.
				if(!world.getBlock(x, y, z).getMaterial().blocksMovement()){
					y--;
				}
				
				if(world.getBlock(x, y + 1, z).getMaterial().blocksMovement() || world.getBlock(x, y + 2, z).getMaterial().blocksMovement()){
					return false;
				}
				
				if(!world.isRemote){
					target.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
				}
				
				world.playSoundAtEntity(target, "mob.endermen.portal", 1.0F, 1.0f);
			}
		}
		
		world.playSoundAtEntity(caster, "mob.endermen.portal", 1.0F, 1.0f);
		caster.swingItem();
		return true;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
	
}
