package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Entrapment;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.ref.WeakReference;

@Mod.EventBusSubscriber
public class EntityBubble extends EntityMagicConstruct {

	public boolean isDarkOrb;

	private WeakReference<EntityLivingBase> rider;

	public EntityBubble(World world){
		super(world);
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
				&& EntityUtils.getRider(this) instanceof EntityLivingBase
				&& !EntityUtils.getRider(this).isDead){
			this.rider = new WeakReference<>((EntityLivingBase)EntityUtils.getRider(this));
		}

		// Prevents dismounting
		if(EntityUtils.getRider(this) == null && this.rider != null && this.rider.get() != null
				&& !this.rider.get().isDead){
			this.rider.get().startRiding(this);
		}

		// Stops the bubble bursting instantly.
		if(this.ticksExisted < 1 && !isDarkOrb) ((EntityLivingBase)EntityUtils.getRider(this)).hurtTime = 0;

		this.move(MoverType.SELF, 0, 0.03, 0);

		if(isDarkOrb){

			if(EntityUtils.getRider(this) != null
					&& EntityUtils.getRider(this).ticksExisted % Spells.entrapment.getProperty(Entrapment.DAMAGE_INTERVAL).intValue() == 0){
				if(this.getCaster() != null){
					EntityUtils.getRider(this).attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.MAGIC),
							1 * damageMultiplier);
				}else{
					EntityUtils.getRider(this).attackEntityFrom(DamageSource.MAGIC, 1 * damageMultiplier);
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
				this.playSound(WizardrySounds.ENTITY_ENTRAPMENT_VANISH, 1.5f, 1.0f);
			}else if(this.ticksExisted % 100 == 1 && this.ticksExisted < 150){
				this.playSound(WizardrySounds.ENTITY_ENTRAPMENT_AMBIENT, 1.5f, 1.0f);
			}
		}

		// Bubble bursts if the entity is hurt (see event handler) or killed, or if the bubble has existed for more than
		// 10 seconds.
		if(EntityUtils.getRider(this) == null && this.ticksExisted > 1){
			if(!this.isDarkOrb) this.playSound(WizardrySounds.ENTITY_BUBBLE_POP, 1.5f, 1.0f);
			this.setDead();
		}
	}

	@Override
	public void despawn(){
		if(EntityUtils.getRider(this) != null){
			((EntityLivingBase)EntityUtils.getRider(this)).dismountEntity(this);
		}
		if(!this.isDarkOrb) this.playSound(WizardrySounds.ENTITY_BUBBLE_POP, 1.5f, 1.0f);
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
			event.getEntityLiving().getRidingEntity().playSound(WizardrySounds.ENTITY_BUBBLE_POP, 1.5f, 1.0f);
			event.getEntityLiving().getRidingEntity().setDead();
		}
	}

}
