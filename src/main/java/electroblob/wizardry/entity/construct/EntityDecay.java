package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityDecay extends EntityMagicConstruct {
	
	public int textureIndex = 0;
	public static final int LIFETIME = 400;
	
	public EntityDecay(World par1World) {
		super(par1World);
		textureIndex = this.rand.nextInt(10);
		this.height = 0.2f;
		this.width = 2.0f;
	}
	
	public EntityDecay(World par1World, double x, double y, double z, EntityLivingBase caster) {
		super(par1World, x, y, z, caster, LIFETIME, 1);
		textureIndex = this.rand.nextInt(10);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){
		
		super.onUpdate();
		
		if(this.rand.nextInt(700) == 0 && this.ticksExisted+100 < LIFETIME) this.playSound("liquid.lava", 0.2F + rand.nextFloat() * 0.2F, 0.6F + rand.nextFloat() * 0.15F);
		
		if(!this.worldObj.isRemote){
			List targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY, this.posZ, this.worldObj);
			for(int i=0; i<targets.size(); i++){
				if(targets.get(i) instanceof EntityLivingBase && targets.get(i) != this.getCaster()){
					
					EntityLivingBase target = (EntityLivingBase)targets.get(i);
					// If this check wasn't here the potion would be reapplied every tick and hence the entity would be damaged each tick.
					if(!target.isPotionActive(Wizardry.decay)) target.addPotionEffect(new PotionEffect(Wizardry.decay.id, LIFETIME, 0, false));
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = rand.nextDouble()*0.8;
			double angle = rand.nextDouble()*Math.PI*2;
			float brightness = rand.nextFloat()*0.4f;
			Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, worldObj, this.posX + radius*Math.cos(angle), this.posY, this.posZ + radius*Math.sin(angle), 0, 0, 0, 0, brightness, 0, brightness+0.1f);
		}
	}
	
	protected void entityInit(){}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {

	}

	/**
     * Checks using a Vec3d to determine if this entity is within range of that vector to be rendered. Args: vec3D
     */
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
    {
        return true;
    }
}
