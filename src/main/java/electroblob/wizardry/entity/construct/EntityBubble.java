package electroblob.wizardry.entity.construct;

import java.lang.ref.WeakReference;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EntityBubble extends EntityMagicConstruct {

	public boolean isDarkOrb;

	private WeakReference<EntityLivingBase> rider;

	public EntityBubble(World world){
		super(world);
	}

	public EntityBubble(World world, double x, double y, double z, EntityLivingBase caster, int lifetime,
			boolean isDarkOrb, float damageMultiplier){
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		// this.setSize(0.1f, 0.1f);
		this.isDarkOrb = isDarkOrb;
	}

	@Override
	public double getMountedYOffset(){
		return 0.1;
	}

	@Override
	public boolean shouldRiderSit(){
		return false;
	}

	public void onUpdate(){

		super.onUpdate();

		// Synchronises the rider field
		if((this.rider == null || this.rider.get() == null)
				&& WizardryUtilities.getRider(this) instanceof EntityLivingBase
				&& !WizardryUtilities.getRider(this).isDead){
			this.rider = new WeakReference<EntityLivingBase>((EntityLivingBase)WizardryUtilities.getRider(this));
		}

		// Prevents dismounting
		if(WizardryUtilities.getRider(this) == null && this.rider != null && this.rider.get() != null
				&& !this.rider.get().isDead){
			this.rider.get().startRiding(this);
		}

		// Stops the bubble bursting instantly.
		if(this.ticksExisted < 1 && !isDarkOrb) ((EntityLivingBase)WizardryUtilities.getRider(this)).hurtTime = 0;

		this.move(MoverType.SELF, 0, 0.03, 0);

		if(isDarkOrb){

			if(WizardryUtilities.getRider(this) != null && this.ticksExisted % 30 == 0){
				if(this.getCaster() != null){
					WizardryUtilities.getRider(this).attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.MAGIC),
							1 * damageMultiplier);
				}else{
					WizardryUtilities.getRider(this).attackEntityFrom(DamageSource.MAGIC, 1 * damageMultiplier);
				}
			}

			for(int i = 0; i < 5; i++){
				this.world.spawnParticle(EnumParticleTypes.PORTAL,
						this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
						this.posY + this.rand.nextDouble() * (double)this.height + 0.5d,
						this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width,
						(this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(),
						(this.rand.nextDouble() - 0.5D) * 2.0D);
			}
			if(lifetime - this.ticksExisted == 75){
				this.playSound(SoundEvents.BLOCK_PORTAL_TRIGGER, 1.5f, 1.0f);
			}else if(this.ticksExisted % 100 == 1 && this.ticksExisted < 150){
				this.playSound(SoundEvents.BLOCK_PORTAL_AMBIENT, 1.5f, 1.0f);
			}
		}

		// Bubble bursts if the entity is hurt (see event handler) or killed, or if the bubble has existed for more than
		// 10 seconds.
		if(WizardryUtilities.getRider(this) == null && this.ticksExisted > 1){
			if(!this.isDarkOrb) this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.5f, 1.0f);
			this.setDead();
		}
	}

	@Override
	public void despawn(){
		if(WizardryUtilities.getRider(this) != null){
			((EntityLivingBase)WizardryUtilities.getRider(this)).dismountEntity(this);
		}
		if(!this.isDarkOrb) this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.5f, 1.0f);
		super.despawn();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		isDarkOrb = nbttagcompound.getBoolean("isDarkOrb");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("isDarkOrb", isDarkOrb);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeBoolean(this.isDarkOrb);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		this.isDarkOrb = data.readBoolean();
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		// Bursts bubble when the creature inside takes damage
		if(event.getEntityLiving().getRidingEntity() instanceof EntityBubble
				&& !((EntityBubble)event.getEntityLiving().getRidingEntity()).isDarkOrb){
			event.getEntityLiving().getRidingEntity().playSound(SoundEvents.ENTITY_ITEM_PICKUP, 1.5f, 1.0f);
			event.getEntityLiving().getRidingEntity().setDead();
		}
	}

}
