package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityBlackHole extends EntityMagicConstruct {

	public int[] randomiser;
	public int[] randomiser2;

	public EntityBlackHole(World world){
		super(world);
		this.width = 6.0f;
		this.height = 3.0f;
		randomiser = new int[30];
		for(int i=0; i<randomiser.length; i++){
			randomiser[i] = this.rand.nextInt(10);
		}
		randomiser2 = new int[30];
		for(int i=0; i<randomiser2.length; i++){
			randomiser2[i] = this.rand.nextInt(10);
		}
	}

	public EntityBlackHole(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.width = 6.0f;
		this.height = 3.0f;
		randomiser = new int[30];
		for(int i=0; i<randomiser.length; i++){
			randomiser[i] = this.rand.nextInt(10);
		}
		randomiser2 = new int[30];
		for(int i=0; i<randomiser2.length; i++){
			randomiser2[i] = this.rand.nextInt(10);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		randomiser = nbttagcompound.getIntArray("randomiser");
		randomiser2 = nbttagcompound.getIntArray("randomiser2");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setIntArray("randomiser", randomiser);
		nbttagcompound.setIntArray("randomiser2", randomiser2);
	}

	public void onUpdate(){

		super.onUpdate();

		//System.out.println("Client side: " + this.worldObj.isRemote + ", Caster: " + this.caster);

		// Particle effect. Finishes 40 ticks before the end so the particles disappear at the same time.
		if(this.ticksExisted + 40 < this.lifetime){
			for (int i=0; i<5; i++){
				//this.worldObj.spawnParticle("portal", this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height - 0.75D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
				this.worldObj.spawnParticle("portal", this.posX, this.posY, this.posZ, (this.rand.nextDouble() - 0.5D) * 4.0D, (this.rand.nextDouble() - 0.5D) * 4.0D - 1, (this.rand.nextDouble() - 0.5D) * 4.0D);
			}
		}

		if(this.lifetime - this.ticksExisted == 75){
			this.worldObj.playSoundAtEntity(this, "portal.trigger", 1.5f, 1.0f);
		}else if(this.ticksExisted % 80 == 1 && this.ticksExisted + 80 < this.lifetime){
			this.worldObj.playSoundAtEntity(this, "portal.portal", 1.5f, 1.0f);
		}

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(6.0d, this.posX, this.posY, this.posZ, this.worldObj);

		if(!this.worldObj.isRemote){

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					// Sucks the target in
					if(this.posX > target.posX && target.motionX < 1){
						target.motionX+=0.1;
					}else if(this.posX < target.posX && target.motionX > -1){
						target.motionX-=0.1;
					}

					if(this.posY > target.posY && target.motionY < 1){
						target.motionY+=0.1;
					}else if(this.posY < target.posY && target.motionY > -1){
						target.motionY-=0.1;
					}

					if(this.posZ > target.posZ && target.motionZ < 1){
						target.motionZ+=0.1;
					}else if(this.posZ < target.posZ && target.motionZ > -1){
						target.motionZ-=0.1;
					}

					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
					}

					if(this.getDistanceToEntity(target) <= 2){
						// Damages the target if it is close enough
						if(this.getCaster() != null){
							target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.MAGIC), 2*damageMultiplier);
						}else{
							target.attackEntityFrom(DamageSource.magic, 2*damageMultiplier);
						}
					}
				}
				/*
				else if(targets.get(i) instanceof EntityItem && !this.worldObj.isRemote){
					EntityItem target = (EntityItem)targets.get(i);
					// Sucks the item in
					if(this.posX > target.posX && target.motionX < 1){
						target.motionX+=0.1;
					}else if(this.posX < target.posX && target.motionX > -1){
						target.motionX-=0.1;
					}
					if(this.posY > target.posY && target.motionY < 1){
						target.motionY+=0.1;
					}else if(this.posY < target.posY && target.motionY > -1){
						target.motionY-=0.1;
					}
					if(this.posZ > target.posZ && target.motionZ < 1){
						target.motionZ+=0.1;
					}else if(this.posZ < target.posZ && target.motionZ > -1){
						target.motionZ-=0.1;
					}
				}
				 */
			}
		}
	}

	/**
	 * Checks using a Vec3d to determine if this entity is within range of that vector to be rendered. Args: vec3D
	 */
	public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
	{
		return true;
	}

}
