package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityForcefield extends EntityMagicConstruct {

	public EntityForcefield(World world){
		super(world);
		this.height = 6;
		this.width = 6;
		// y-3 because it needs to be centred on the given position
		this.setEntityBoundingBox(new AxisAlignedBB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3));
	}

	@Override
	public boolean canBeCollidedWith(){
		return !this.isDead;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entity){
		return entity.getEntityBoundingBox();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){
			// TESTME: This used to say posY+3, but I'm pretty sure that's wrong because of how the bounding box was set...
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.5, posX, posY, posZ, world);
			
			for(EntityLivingBase target : targets){
				if(this.isValidTarget(target)){
					double multiplier = (3.5 - target.getDistance(this.posX, this.posY, this.posZ)) * 0.1;
					target.addVelocity((target.posX - this.posX) * multiplier,
							(target.posY - this.posY) * multiplier, (target.posZ - this.posZ) * multiplier);
					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
					}
				}
			}
		}else{
			for(int i = 1; i < 40; i++){

				// Generates a spherical pattern of particles
				float brightness = 0.5f + (rand.nextFloat() * 0.5f);
				double radius = 3;
				double yaw = rand.nextDouble() * Math.PI * 2;
				double pitch = (rand.nextDouble() - 0.5) * Math.PI;
				
				ParticleBuilder.create(Type.DUST)
				.pos(this.posX + radius * Math.cos(yaw) * Math.cos(pitch), this.posY + 3 + radius * Math.sin(pitch),
						this.posZ + radius * Math.sin(yaw) * Math.cos(pitch))
				.time(48 + this.rand.nextInt(12))
				.clr(brightness, brightness, 1.0f).spawn(world);
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage){

		if(source != null && source.getImmediateSource() != null){
			// Now works for any source of damage.
			source.getImmediateSource().playSound(WizardrySounds.SPELL_DEFLECTION, 0.3f, 1.3f);
		}
		super.attackEntityFrom(source, damage);
		return false;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
