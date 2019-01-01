package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

public class EntityForceArrow extends EntityMagicArrow {

	/** Creates a new force arrow in the given world. */
	public EntityForceArrow(World world){
		super(world);
	}

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST, 1.0F, 1.0F);
		if(this.world.isRemote)
			ParticleBuilder.create(Type.FLASH).pos(posX, posY, posZ).scale(1.3f).clr(0.75f, 1, 0.85f).spawn(world);
	}

	@Override
	public void tickInGround(){
		this.setDead();
	}

	@Override
	public void onBlockHit(){
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST, 1.0F, 1.0F);
		if(this.world.isRemote){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3d vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).scale(1.3f).clr(0.75f, 1, 0.85f).spawn(world);
			vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(WizardryUtilities.ANTI_Z_FIGHTING_OFFSET));
			ParticleBuilder.create(Type.SCORCH).pos(vec).face(hit.sideHit).clr(0, 1, 0.5f).spawn(world);
		}
	}

	@Override
	public void tickInAir(){
		if(this.ticksExisted > 20){
			this.setDead();
		}
	}

	@Override
	public double getDamage(){
		return 7.0d;
	}

	@Override
	public DamageType getDamageType(){
		return DamageType.FORCE;
	}

	@Override
	public boolean doGravity(){
		return false;
	}

	@Override
	public boolean doDeceleration(){
		return false;
	}

	@Override
	protected void entityInit(){
		// auto generated
	}

}