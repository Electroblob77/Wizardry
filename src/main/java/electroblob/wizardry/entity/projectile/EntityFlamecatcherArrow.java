package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityFlamecatcherArrow extends EntityMagicArrow {

	public static final float SPEED = 3;

	/** Creates a new magic missile in the given world. */
	public EntityFlamecatcherArrow(World world){
		super(world);
	}

	@Override public double getDamage(){ return Spells.flamecatcher.getProperty(Spell.DAMAGE).floatValue(); }

	@Override public int getLifetime(){ return (int)(Spells.flamecatcher.getProperty(Spell.RANGE).floatValue() / SPEED); }

	@Override public boolean doGravity(){ return false; } // Zero gravity arrows!

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		entityHit.setFire(Spells.flamecatcher.getProperty(Spell.BURN_DURATION).intValue());
		this.playSound(WizardrySounds.ENTITY_FLAMECATCHER_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
		if(this.world.isRemote) ParticleBuilder.create(Type.FLASH).pos(posX, posY, posZ).clr(0xff6d00).spawn(world);
	}
	
	@Override
	public void onBlockHit(RayTraceResult hit){
		if(this.world.isRemote){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3d vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).clr(0xff6d00).fade(0.85f, 0.5f, 0.8f).spawn(world);
		}
	}

	@Override
	public void tickInAir(){

		if(this.world.isRemote){

			ParticleBuilder.create(Type.MAGIC_FIRE, rand, posX, posY, posZ, 0.03, false)
					.time(20 + rand.nextInt(10)).spawn(world);

			if(this.ticksExisted > 1){ // Don't spawn particles behind where it started!
				double x = posX - motionX / 2;
				double y = posY - motionY / 2;
				double z = posZ - motionZ / 2;
				ParticleBuilder.create(Type.MAGIC_FIRE, rand, x, y, z, 0.03, false)
						.time(20 + rand.nextInt(10)).spawn(world);
			}
		}
	}

	@Override
	protected void entityInit(){
		if(world != null && world.isRemote){
			ParticleBuilder.create(Type.FLASH).entity(this).time(this.getLifetime()).scale(1.5f).clr(0xffb800).spawn(world);
		}
	}

}