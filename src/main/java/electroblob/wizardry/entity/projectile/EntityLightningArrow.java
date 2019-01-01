package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityLightningArrow extends EntityMagicArrow {

	/** Creates a new lightning arrow in the given world. */
	public EntityLightningArrow(World world){
		super(world);
	}

	@Override public double getDamage(){ return 7.0d; }

	@Override public DamageType getDamageType(){ return DamageType.SHOCK; }

	@Override public boolean doGravity(){ return false; }

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){

		if(world.isRemote){
			for(int j = 0; j < 8; j++){
				ParticleBuilder.create(Type.SPARK, rand, posX, posY + height / 2, posZ, 1, false).spawn(world);
			}
		}
		
		this.playSound(WizardrySounds.SPELL_SPARK, 1.0F, 1.0F);
	@Override
	public void onBlockHit(RayTraceResult hit){
		if(this.world.isRemote){
			Vec3d vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(WizardryUtilities.ANTI_Z_FIGHTING_OFFSET));
			ParticleBuilder.create(Type.SCORCH).pos(vec).face(hit.sideHit).clr(0.4f, 0.8f, 1).scale(0.6f).spawn(world);
		}
	}

	@Override
	public void tickInAir(){

		if(this.ticksExisted > 20){
			this.setDead();
		}

		if(world.isRemote){
			ParticleBuilder.create(Type.SPARK).pos(posX, posY, posZ).spawn(world);
		}

	}

	@Override
	protected void entityInit(){}

}