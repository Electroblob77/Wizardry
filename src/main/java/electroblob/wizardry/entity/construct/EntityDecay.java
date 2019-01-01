package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityDecay extends EntityMagicConstruct {

	public int textureIndex = 0;

	public EntityDecay(World world){
		super(world);
		textureIndex = this.rand.nextInt(10);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.rand.nextInt(700) == 0 && this.ticksExisted + 100 < lifetime)
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
						target.addPotionEffect(new PotionEffect(WizardryPotions.decay, lifetime, 0));
				}
			}
			
		}else if(this.rand.nextInt(15) == 0){
			
			double radius = rand.nextDouble() * 0.8;
			double angle = rand.nextDouble() * Math.PI * 2;
			float brightness = rand.nextFloat() * 0.4f;
			
			ParticleBuilder.create(Type.DARK_MAGIC)
			.pos(this.posX + radius * Math.cos(angle), this.posY, this.posZ + radius * Math.sin(angle))
			.clr(brightness, 0, brightness + 0.1f)
			.spawn(world);
		}
	}

	@Override protected void entityInit(){}

	@Override protected void readEntityFromNBT(NBTTagCompound nbttagcompound){}

	@Override protected void writeEntityToNBT(NBTTagCompound nbttagcompound){}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}
	
}
