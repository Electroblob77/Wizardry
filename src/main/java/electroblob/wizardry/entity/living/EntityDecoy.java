package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryParticleType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class EntityDecoy extends EntitySummonedCreature {
	
	public EntityDecoy(World world){
		super(world);
	}

	public EntityDecoy(World world, double x, double y, double z, EntityLivingBase caster, int lifetime) {
		super(world, x, y, z, caster, lifetime);
        this.setAlwaysRenderNameTag(caster instanceof EntityPlayer);
	}
	
	@Override
	protected void initEntityAI() {
		// Decoys just wander around aimlessly, watching anything living.
		this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityLivingBase.class, 6.0F));
        this.tasks.addTask(3, new EntityAILookIdle(this));
	}
	
	@Override
	public void onDespawn(){
		super.onDespawn();
		for(int i=0; i<20; i++){
			Wizardry.proxy.spawnParticle(WizardryParticleType.DUST, worldObj, this.posX + (this.rand.nextDouble()-0.5)*this.width,
					this.getEntityBoundingBox().minY + this.rand.nextDouble()*this.height, this.posZ + (this.rand.nextDouble()-0.5)*this.width,
					0, 0, 0, 40, 0.2f, 1.0f, 0.8f);
		}
	}
	
	@Override
	public boolean isEntityInvulnerable(DamageSource source){
		return true;
	}
	
	@Override
	public boolean isSneaking(){
		return false;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if(this.getCaster() == null || this.getCaster().isDead){
			this.setDead();
			this.onDespawn();
		}
	}
	
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}
	
	@Override
	public ITextComponent getDisplayName(){
		if(getCaster() instanceof EntityPlayer){
			return this.getCaster().getDisplayName();
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		return getCaster() instanceof EntityPlayer;
	}

	@Override
	public boolean hasRangedAttack() {
		return false;
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		if(this.getCaster() != null) data.writeInt(this.getCaster().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		if(!data.isReadable()) return;
		this.setCasterReference(new WeakReference<EntityLivingBase>((EntityLivingBase)this.worldObj.getEntityByID(data.readInt())));
	}

}
