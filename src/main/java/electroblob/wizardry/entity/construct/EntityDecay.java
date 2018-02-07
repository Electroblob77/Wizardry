package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityDecay extends EntityMagicConstruct {

	public int textureIndex = 0;
	public static final int LIFETIME = 400;

	public EntityDecay(World par1World){
		super(par1World);
		textureIndex = this.rand.nextInt(10);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	public EntityDecay(World par1World, double x, double y, double z, EntityLivingBase caster){
		super(par1World, x, y, z, caster, LIFETIME, 1);
		textureIndex = this.rand.nextInt(10);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.rand.nextInt(700) == 0 && this.ticksExisted + 100 < LIFETIME)
			this.playSound(SoundEvents.BLOCK_LAVA_AMBIENT, 0.2F + rand.nextFloat() * 0.2F,
					0.6F + rand.nextFloat() * 0.15F);

		if(!this.world.isRemote){
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY,
					this.posZ, this.world);
			for(EntityLivingBase target : targets){
				if(target != this.getCaster()){
					// If this check wasn't here the potion would be reapplied every tick and hence the entity would be
					// damaged each tick.
					// In this case, we do want particles to be shown.
					if(!target.isPotionActive(WizardryPotions.decay))
						target.addPotionEffect(new PotionEffect(WizardryPotions.decay, LIFETIME, 0));
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = rand.nextDouble() * 0.8;
			double angle = rand.nextDouble() * Math.PI * 2;
			float brightness = rand.nextFloat() * 0.4f;
			Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, this.posX + radius * Math.cos(angle),
					this.posY, this.posZ + radius * Math.sin(angle), 0, 0, 0, 0, brightness, 0, brightness + 0.1f);
		}
	}

	protected void entityInit(){
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){

	}

	/**
	 * Checks using a Vec3dd to determine if this entity is within range of that vector to be rendered. Args: Vec3dD
	 */
	public boolean isInRangeToRenderVec3dD(Vec3d par1Vec3d){
		return true;
	}
}
