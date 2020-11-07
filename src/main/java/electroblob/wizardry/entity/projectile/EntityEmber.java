package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Disintegration;
import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityEmber extends EntityMagicProjectile {

	private int extraLifetime;

	public EntityEmber(World world){
		super(world);
	}

	public EntityEmber(World world, EntityLivingBase caster){
		super(world);
		this.thrower = caster;
		extraLifetime = rand.nextInt(30);
		this.setSize(0.1f, 0.1f);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(){
		return null;//this.getEntityBoundingBox();
	}

	@Override
	public int getLifetime(){
		return Spells.disintegration.getProperty(Disintegration.EMBER_LIFETIME).intValue() + extraLifetime;
	}

	@Override
	protected void onImpact(RayTraceResult result){

//		if(result.entityHit != null){
//			result.entityHit.setFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue());
//		}

		if(result.typeOfHit == RayTraceResult.Type.BLOCK){
			this.inGround = true;
			this.collided = true;
			if(result.sideHit.getAxis() == EnumFacing.Axis.X) motionX = 0;
			if(result.sideHit.getAxis() == EnumFacing.Axis.Y){
				motionY = 0;
				this.collidedVertically = true;
			}
			if(result.sideHit.getAxis() == EnumFacing.Axis.Z) motionZ = 0;
		}
	}

	@Override
	public void applyEntityCollision(Entity entity){

		super.applyEntityCollision(entity);

		if(entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHealth() > 0){
			entity.setFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue());
		}
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.collidedVertically){
			this.motionY += this.getGravityVelocity();
			this.motionX *= 0.5;
			this.motionZ *= 0.5;
		}

		world.getEntitiesInAABBexcluding(thrower, this.getEntityBoundingBox(), e -> e instanceof EntityLivingBase)
				.stream().filter(e -> !(e instanceof EntityLivingBase) || ((EntityLivingBase)e).getHealth() > 0)
				.forEach(e -> e.setFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue()));

		// Copied from ParticleLava
		if(this.rand.nextFloat() > (float)this.ticksExisted / this.getLifetime()){
			this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
		}
	}
}
