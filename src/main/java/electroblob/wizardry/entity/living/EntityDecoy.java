package electroblob.wizardry.entity.living;

import java.util.UUID;

import cpw.mods.fml.common.network.ByteBufUtils;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIControlledByPlayer;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityDecoy extends EntitySummonedCreature {
	
	public EntityDecoy(World world){
		super(world);
	}

	public EntityDecoy(World world, double x, double y, double z, EntityLivingBase caster, int lifetime) {
		super(world, x, y, z, caster, lifetime);
		// Decoys just wander around aimlessly, watching anything living.
		this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityLivingBase.class, 6.0F));
        this.tasks.addTask(3, new EntityAILookIdle(this));
        this.setAlwaysRenderNameTag(caster instanceof EntityPlayer);
	}
	
	@Override
	public void despawn(){
		super.despawn();
		for(int i=0; i<20; i++){
			Wizardry.proxy.spawnParticle(EnumParticleType.DUST, worldObj, this.posX + (this.rand.nextDouble()-0.5)*this.width,
					this.boundingBox.minY + this.rand.nextDouble()*this.height, this.posZ + (this.rand.nextDouble()-0.5)*this.width,
					0, 0, 0, 40, 0.2f, 1.0f, 0.8f);
		}
	}
	
	@Override
	public boolean isEntityInvulnerable() {
		return true;
	}
	
	@Override
	public ItemStack getHeldItem() {
		if(this.getCaster() instanceof EntityPlayer || this.getCaster() == null) return super.getHeldItem();
		// Tricks the renderer into rendering the decoy's arm in the right place.
		return this.getCaster().getHeldItem();
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if(this.getCaster() == null || this.getCaster().isDead){
			this.despawn();
		}
	}
	
	@Override
	protected boolean isAIEnabled(){
		return true;
	}
	
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
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
		this.setCaster((EntityLivingBase)this.worldObj.getEntityByID(data.readInt()));
	}

}
