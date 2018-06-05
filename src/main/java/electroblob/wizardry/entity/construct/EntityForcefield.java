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
		this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 3, this.posY - 3, this.posZ - 3, this.posX + 3,
				this.posY + 3, this.posZ + 3));
	}

	public EntityForcefield(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
		// y-3 because it needs to be centred on the given position
		// Damage multiplier is 1 because forcefields do no damage!
		super(world, x, y - 3, z, caster, lifetime, 1.0f);
		this.height = 6;
		this.width = 6;
		this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 3, this.posY - 3, this.posZ - 3, this.posX + 3,
				this.posY + 3, this.posZ + 3));
	}

	public boolean canBeCollidedWith(){
		return !this.isDead;
	}

	public AxisAlignedBB getCollisionBox(Entity par1Entity){
		return par1Entity.getEntityBoundingBox();
	}

	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.5, this.posX, this.posY + 3,
					this.posZ, this.world);
			for(EntityLivingBase target : targets){
				if(this.isValidTarget(target)){
					double multiplier = (3.5 - target.getDistance(this.posX, this.posY + 3, this.posZ)) * 0.1;
					target.addVelocity((target.posX - this.posX) * multiplier,
							(target.posY - (this.posY + 3)) * multiplier, (target.posZ - this.posZ) * multiplier);
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
				.lifetime(48 + this.rand.nextInt(12))
				.colour(brightness, brightness, 1.0f);
			}
		}
	}

	public boolean attackEntityFrom(DamageSource source, float par2){

		if(source != null && source.getImmediateSource() != null){
			// Now works for any source of damage.
			source.getImmediateSource().playSound(WizardrySounds.SPELL_DEFLECTION, 0.3f, 1.3f);
		}
		super.attackEntityFrom(source, par2);
		return false;
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	public boolean canRenderOnFire(){
		return false;
	}

}
