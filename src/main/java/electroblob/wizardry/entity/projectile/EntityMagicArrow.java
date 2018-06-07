package electroblob.wizardry.entity.projectile;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.core.helpers.UUIDUtil;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** This class was copied from EntityArrow in the 1.7.10 update as part of the overhaul and major cleanup
 * of the code for the projectiles. It provides a unifying superclass for all <b>directed</b> projectiles (i.e. not
 * spherical stuff like snowballs), namely magic missile, ice shard, force arrow, lightning arrow and dart.
 * All other projectiles should extend {@link EntityMagicProjectile}.
 * <p>
 * This class handles saving of the damage multiplier and all shared logic. Methods are provided which are
 * triggered at useful points during the entity update cycle as well as a few getters for various properties.
 * Override any of these to change the behaviour (no need to call super for any of them).
 * @since Wizardry 1.0
 */
public abstract class EntityMagicArrow extends Entity implements IProjectile, IEntityAdditionalSpawnData
{
    private int blockX = -1;
    private int blockY = -1;
    private int blockZ = -1;
    /** The block the arrow is stuck in */
    private Block stuckInBlock;
    /** The metadata of the block the arrow is stuck in */
    private int inData;
    private boolean inGround;
    /** Seems to be some sort of timer for animating an arrow. */
    public int arrowShake;
    /** The owner of this arrow. */
    private WeakReference<EntityLivingBase> shootingEntity;
    /** The UUID of the caster. Note that this is only for loading purposes; during normal updates
	 * the actual entity instance is stored (so that getEntityByUUID is not called constantly),
	 * so this will not always be synced (this is why it is private). */
	private UUID casterUUID;
    int ticksInGround;
    int ticksInAir;
    /** The amount of knockback an arrow applies when it hits a mob. */
    private int knockbackStrength;
    /** The damage multiplier for the arrow. Normally this isn't set directly, since it can be done via the
     * constructor. An exception is where other entities need to pass in their multipliers, e.g. ice charge. */
	public float damageMultiplier = 1.0f;

    /** Basic shell constructor. Should only be used by the client. */
    public EntityMagicArrow(World world)
    {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
    }

    /** Creates a projectile at position xyz in world, with no motion. Do not create a projectile with this 
     * constructor and then call setVelocity() as that method is, bizarrely, client-side only. */
    public EntityMagicArrow(World world, double x, double y, double z)
    {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
        this.setPosition(x, y, z);
        this.yOffset = 0.0F;
    }

    /** Creates a projectile at the position of the caster, pointing at the given target. The trajectory seems to be altered 
     * slightly by a random amount determined by the aimingError parameter. For reference, skeletons set this to 10 on easy, 6 on
     * normal and 2 on hard difficulty. */
    public EntityMagicArrow(World world, EntityLivingBase caster, Entity target, float speed, float aimingError, float damageMultiplier)
    {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.shootingEntity = new WeakReference(caster);
        this.damageMultiplier = damageMultiplier;

        this.posY = caster.posY + (double)caster.getEyeHeight() - 0.10000000149011612D;
        double d0 = target.posX - caster.posX;
        double d1 = this.doGravity() ? target.boundingBox.minY + (double)(target.height / 3.0F) - this.posY
        		: target.boundingBox.minY + (double)(target.height / 2.0F) - this.posY;
        double d2 = target.posZ - caster.posZ;
        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);

        if (d3 >= 1.0E-7D)
        {
            float f2 = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
            float f3 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
            double d4 = d0 / d3;
            double d5 = d2 / d3;
            this.setLocationAndAngles(caster.posX + d4, this.posY, caster.posZ + d5, f2, f3);
            this.yOffset = 0.0F;
            // f4 depends on the horizontal distance between the two entities and accounts for bullet drop,
            // but of course if gravity is ignored this should be 0.
            float f4 = this.doGravity() ? (float)d3 * 0.2F : 0;
            this.setThrowableHeading(d0, d1 + (double)f4, d2, speed, aimingError);
        }
    }

    /** Creates a projectile pointing in the direction the caster is looking, with the given speed. 
     * USE THIS CONSTRUCTOR FOR NORMAL SPELLS. */
    public EntityMagicArrow(World world, EntityLivingBase caster, float speed, float damageMultiplier)
    {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.shootingEntity = new WeakReference(caster);
        this.damageMultiplier = damageMultiplier;

        this.setSize(0.5F, 0.5F);
        this.setLocationAndAngles(caster.posX, caster.posY + (double)caster.getEyeHeight(), caster.posZ, caster.rotationYaw, caster.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI));
        this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI));
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, speed * 1.5F, 1.0F);
    }
    
    /** Subclasses must override this to set their own base damage. */
    public abstract double getDamage();

    /** Override this to specify the damage type dealt. Defaults to {@link DamageType#MAGIC}. */
    public DamageType getDamageType(){
		return DamageType.MAGIC;
	}
    
    /** Override this to disable gravity. Returns true by default. */
    public boolean doGravity(){
    	return true;
    }
    
    /** Override this to disable deceleration (generally speaking, this isn't noticeable unless gravity
     * is turned off). Returns true by default. */
    public boolean doDeceleration(){
    	return true;
    }
    
    /** Override this to allow the projectile to pass through mobs intact (the onEntityHit method will
     * still be called and damage will still be applied). Returns false by default. */
    public boolean doOverpenetration(){
    	return false;
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void setThrowableHeading(double x, double y, double z, float speed, float randomness)
    {
        float f2 = MathHelper.sqrt_double(x * x + y * y + z * z);
        x /= (double)f2;
        y /= (double)f2;
        z /= (double)f2;
        x += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)randomness;
        y += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)randomness;
        z += this.rand.nextGaussian() * (double)(this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * (double)randomness;
        x *= (double)speed;
        y *= (double)speed;
        z *= (double)speed;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f3 = MathHelper.sqrt_double(x * x + z * z);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(y, (double)f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     * - Allows the arrow to stick in blocks, I think.
     */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_)
    {
        this.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
        this.setRotation(p_70056_7_, p_70056_8_);
    }

    /**
     * Sets the velocity to the args. Args: x, y, z. THIS IS CLIENT SIDE ONLY! DO NOT USE IN COMMON OR SERVER CODE!
     */
    @SideOnly(Side.CLIENT)
    public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
    {
        this.motionX = p_70016_1_;
        this.motionY = p_70016_3_;
        this.motionZ = p_70016_5_;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(p_70016_1_ * p_70016_1_ + p_70016_5_ * p_70016_5_);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(p_70016_1_, p_70016_5_) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(p_70016_3_, (double)f) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }
    
    /** Called each tick when the projectile is in a block. Defaults to setDead(), but can be overridden to
     * change the behaviour. */
    public void tickInGround(){
    	this.setDead();
    }
    
    /** Called each tick when the projectile is in the air. Override to add particles and such like. */
    public void tickInAir(){}
    
    /** Called when the projectile hits an entity. Override to add potion effects and such like. */
    public void onEntityHit(EntityLivingBase entityHit){}
    
    /** Called when the projectile hits a block. Override to add sound effects and such like. */
    public void onBlockHit(){}

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate(){
    	
        super.onUpdate();

        if(this.getShootingEntity() == null && this.casterUUID != null){
			Entity entity = WizardryUtilities.getEntityByUUID(worldObj, casterUUID);
			if(entity instanceof EntityLivingBase){
				this.shootingEntity = new WeakReference<EntityLivingBase>((EntityLivingBase)entity);
			}
		}
        
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f) * 180.0D / Math.PI);
        }

        Block block = this.worldObj.getBlock(this.blockX, this.blockY, this.blockZ);

        if (block.getMaterial() != Material.air)
        {
            block.setBlockBoundsBasedOnState(this.worldObj, this.blockX, this.blockY, this.blockZ);
            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBoxFromPool(this.worldObj, this.blockX, this.blockY, this.blockZ);

            if (axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ)))
            {
                this.inGround = true;
            }
        }

        if (this.arrowShake > 0)
        {
            --this.arrowShake;
        }

        // When the arrow is in the ground
        if (this.inGround)
        {
        	++this.ticksInGround;
        	this.tickInGround();
        }
        // When the arrow is in the air
        else
        {
            ++this.ticksInAir;
            
            this.tickInAir();
            
            Vec3 vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.func_147447_a(vec31, vec3, false, true, false);
            vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            
            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            int i;
            float f1;

            for (i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != this.getShootingEntity() || this.ticksInAir >= 5))
                {
                    f1 = 0.3F;
                    AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand((double)f1, (double)f1, (double)f1);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null)
                    {
                        double d1 = vec31.distanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            // Players that are considered invulnerable to the caster allow the projectile to pass straight through them.
            if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.entityHit;

                if (entityplayer.capabilities.disableDamage || this.getShootingEntity() instanceof EntityPlayer && !((EntityPlayer)this.getShootingEntity()).canAttackPlayer(entityplayer))
                {
                    movingobjectposition = null;
                }
            }

            // If the arrow hits something
            if (movingobjectposition != null)
            {
            	// If the arrow hits an entity
                if (movingobjectposition.entityHit != null)
                {
                    DamageSource damagesource = null;

                    if (this.getShootingEntity() == null)
                    {
                        damagesource = DamageSource.causeThrownDamage(this, this);
                    }
                    else
                    {
                        damagesource = MagicDamage.causeIndirectEntityMagicDamage(this, (EntityLivingBase)this.getShootingEntity(), this.getDamageType()).setProjectile();
                    }

                    if (movingobjectposition.entityHit.attackEntityFrom(damagesource, (float)(this.getDamage()*this.damageMultiplier)))
                    {
                        if (movingobjectposition.entityHit instanceof EntityLivingBase)
                        {
                            EntityLivingBase entityHit = (EntityLivingBase)movingobjectposition.entityHit;
                            
                            this.onEntityHit(entityHit);

                            if (this.knockbackStrength > 0)
                            {
                                float f4 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);

                                if (f4 > 0.0F)
                                {
                                    movingobjectposition.entityHit.addVelocity(this.motionX * (double)this.knockbackStrength * 0.6000000238418579D / (double)f4, 0.1D, this.motionZ * (double)this.knockbackStrength * 0.6000000238418579D / (double)f4);
                                }
                            }

                            // Thorns enchantment
                            if (this.getShootingEntity() != null && this.getShootingEntity() instanceof EntityLivingBase)
                            {
                                EnchantmentHelper.func_151384_a(entityHit, this.getShootingEntity());
                                EnchantmentHelper.func_151385_b((EntityLivingBase)this.getShootingEntity(), entityHit);
                            }

                            if (this.getShootingEntity() != null && movingobjectposition.entityHit != this.getShootingEntity() && movingobjectposition.entityHit instanceof EntityPlayer && this.getShootingEntity() instanceof EntityPlayerMP)
                            {
                                ((EntityPlayerMP)this.getShootingEntity()).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        if (!(movingobjectposition.entityHit instanceof EntityEnderman) && !this.doOverpenetration())
                        {
                            this.setDead();
                        }
                    }
                    else
                    {
                        if(!this.doOverpenetration()) this.setDead();

                    	// Was the 'rebound' that happened when entities were immune to damage
                    	/*
                        this.motionX *= -0.10000000149011612D;
                        this.motionY *= -0.10000000149011612D;
                        this.motionZ *= -0.10000000149011612D;
                        this.rotationYaw += 180.0F;
                        this.prevRotationYaw += 180.0F;
                        this.ticksInAir = 0;
                        */
                    }
                }
                // If the arrow hits a block
                else
                {
                    this.blockX = movingobjectposition.blockX;
                    this.blockY = movingobjectposition.blockY;
                    this.blockZ = movingobjectposition.blockZ;
                    this.stuckInBlock = this.worldObj.getBlock(this.blockX, this.blockY, this.blockZ);
                    this.inData = this.worldObj.getBlockMetadata(this.blockX, this.blockY, this.blockZ);
                    this.motionX = (double)((float)(movingobjectposition.hitVec.xCoord - this.posX));
                    this.motionY = (double)((float)(movingobjectposition.hitVec.yCoord - this.posY));
                    this.motionZ = (double)((float)(movingobjectposition.hitVec.zCoord - this.posZ));
                    //f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    //this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
                    //this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
                    //this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
                    //this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    this.inGround = true;
                    this.arrowShake = 7;
                    
                    this.onBlockHit();

                    if (this.stuckInBlock.getMaterial() != Material.air)
                    {
                        this.stuckInBlock.onEntityCollidedWithBlock(this.worldObj, this.blockX, this.blockY, this.blockZ, this);
                    }
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            //f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

            //for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f2) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
            //{
            //    ;
            //}

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
            {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F)
            {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
            {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            
            float f3 = 0.99F;

            if (this.isInWater())
            {
                for (int l = 0; l < 4; ++l)
                {
                    float f4 = 0.25F;
                    this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double)f4, this.posY - this.motionY * (double)f4, this.posZ - this.motionZ * (double)f4, this.motionX, this.motionY, this.motionZ);
                }

                f3 = 0.8F;
            }

            if (this.isWet())
            {
                this.extinguish();
            }

            if(this.doDeceleration()){
	            this.motionX *= (double)f3;
	            this.motionY *= (double)f3;
	            this.motionZ *= (double)f3;
            }
            
            if(this.doGravity()) this.motionY -= 0.05;
            
            this.setPosition(this.posX, this.posY, this.posZ);
            this.func_145775_I();
        }
    }

	/**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tag)
    {
        tag.setShort("xTile", (short)this.blockX);
        tag.setShort("yTile", (short)this.blockY);
        tag.setShort("zTile", (short)this.blockZ);
        tag.setShort("life", (short)this.ticksInGround);
        tag.setByte("inTile", (byte)Block.getIdFromBlock(this.stuckInBlock));
        tag.setByte("inData", (byte)this.inData);
        tag.setByte("shake", (byte)this.arrowShake);
        tag.setByte("inGround", (byte)(this.inGround ? 1 : 0));
        tag.setFloat("damageMultiplier", this.damageMultiplier);
		if(this.getShootingEntity() != null){
        	tag.setString("casterUUID", this.getShootingEntity().getUniqueID().toString());
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tag)
    {
        this.blockX = tag.getShort("xTile");
        this.blockY = tag.getShort("yTile");
        this.blockZ = tag.getShort("zTile");
        this.ticksInGround = tag.getShort("life");
        this.stuckInBlock = Block.getBlockById(tag.getByte("inTile") & 255);
        this.inData = tag.getByte("inData") & 255;
        this.arrowShake = tag.getByte("shake") & 255;
        this.inGround = tag.getByte("inGround") == 1;
        this.damageMultiplier = tag.getFloat("damageMultiplier");
		String string = tag.getString("casterUUID");
		// Fixed issue #3:
		// Uses regex to verify that the string is a valid UUID (I can't see why it wouldn't be, but it was reported
		// on the issue tracker so I've fixed it).
		if(WizardryUtilities.verifyUUIDString(string))
			casterUUID = UUID.fromString(string);
    }
    
    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    /**
     * Sets the amount of knockback the arrow applies when it hits a mob.
     */
    public void setKnockbackStrength(int p_70240_1_)
    {
        this.knockbackStrength = p_70240_1_;
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    public boolean canAttackWithItem()
    {
        return false;
    }
    
    public void writeSpawnData(ByteBuf buffer){
    	if(this.getShootingEntity() != null) buffer.writeInt(this.getShootingEntity().getEntityId());
    }
    
    public void readSpawnData(ByteBuf buffer){
    	if(buffer.isReadable()) this.shootingEntity = new WeakReference((EntityLivingBase)this.worldObj.getEntityByID(buffer.readInt()));
    }

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the
	 * entity may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported
	 * to another dimension, or this construct simply had no caster in the first place.
	 */
	public EntityLivingBase getShootingEntity() {
		return shootingEntity == null ? null : shootingEntity.get();
	}
	
	public void setShootingEntity(EntityLivingBase entity) {
		shootingEntity = new WeakReference(entity);
	}
}