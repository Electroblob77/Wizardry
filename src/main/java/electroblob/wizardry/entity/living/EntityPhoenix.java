package electroblob.wizardry.entity.living;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class EntityPhoenix extends EntitySummonedCreature implements IRangedAttackMob
{
    /** Random offset used in floating behaviour */
    private float heightOffset = 0.5F;
    
    private double AISpeed = 0.5;

    /** ticks until heightOffset is randomized */
    private int heightOffsetUpdateTime;
    private int field_70846_g;
    
    private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, AISpeed, 1, 1, 15.0F);

	public EntityPhoenix(World world){
        super(world);
    }
	
    public EntityPhoenix(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
    	
        super(world, x, y, z, caster, lifetime);
        this.isImmuneToFire = true;
        this.height = 2.0f;
        this.tasks.addTask(0, new EntityAIWatchClosest(this, EntityLivingBase.class, 0));
        this.tasks.addTask(1, this.aiArrowAttack);
        //this.tasks.addTask(2, new EntityAIWander(this, AISpeed));
        this.tasks.addTask(3, new EntityAILookIdle(this));
        //this.targetTasks.addTask(0, new EntityAIMoveTowardsTarget(this, 1, 10));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, this.targetSelector));

        this.setAIMoveSpeed((float)AISpeed);
        
        this.experienceValue = 0;
        
        // Prevents the phoenix from attacking instantly on spawn.
        this.attackTime = 60;
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    protected boolean isAIEnabled()
    {
        return true;
    }
    
    @Override
	public boolean hasRangedAttack() {
		return true;
	}

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
        //this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(3.0d);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Byte((byte)0));
        this.dataWatcher.addObject(20, new Byte((byte)0));
        this.dataWatcher.addObject(21, new Integer(0));
    }
    
    /**
     * (Copied from EntityWither) Returns the target entity ID if present, or -1 if not
     */
    public int getTargetId()
    {
        return this.dataWatcher.getWatchableObjectInt(21);
    }

    public void setTargetId(int id)
    {
        this.dataWatcher.updateObject(21, Integer.valueOf(id));
    }

    /** Retrieves the attacking boolean from the datawatcher. */
    public boolean getIsAttacking()
    {
        return (this.dataWatcher.getWatchableObjectByte(20) & 1) != 0;
    }
    
    /** Sets the attacking boolean in the datawatcher. */
    public void setAttacking(boolean par1)
    {
        byte b0 = this.dataWatcher.getWatchableObjectByte(20);

        if (par1)
        {
            b0 = (byte)(b0 | 1);
        }
        else
        {
            b0 &= -2;
        }

        this.dataWatcher.updateObject(20, Byte.valueOf(b0));
    }
    
    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.blaze.breathe";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.blaze.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.blaze.death";
    }

    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float par1)
    {
        return 15728880;
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float par1)
    {
        return 1.0F;
    }

    @Override
	public void onSpawn(){
    	if(this.worldObj.isRemote){
    		for(int i=0;i<15;i++){
    			this.worldObj.spawnParticle("flame", this.posX + this.rand.nextFloat(), this.posY + 1 + this.rand.nextFloat(), this.posZ + this.rand.nextFloat(), 0, 0, 0);
    		}
    	}
    }

    @Override
	public void despawn(){
    	if(this.worldObj.isRemote){
    		for(int i=0;i<15;i++){
    			this.worldObj.spawnParticle("flame", this.posX + this.rand.nextFloat(), this.posY + 1 + this.rand.nextFloat(), this.posZ + this.rand.nextFloat(), 0, 0, 0);
    		}
    	}
		super.despawn();
	}
    
    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
    	// Makes the phoenix hover.
    	if(this.posY - WizardryUtilities.getNearestFloorLevel(worldObj, (int)this.posX, (int)this.posY, (int)this.posZ, 4) > 3){
    		this.motionY = -0.1;
    	}else if(this.posY - WizardryUtilities.getNearestFloorLevel(worldObj, (int)this.posX, (int)this.posY, (int)this.posZ, 4) < 2){
    		this.motionY = 0.1;
    	}else{
    		this.motionY = 0.0;
    	}

    	// Living sound
        if (this.rand.nextInt(24) == 0)
        {
            this.worldObj.playSoundEffect(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, "fire.fire", 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F);
        }

        // Flapping sound effect
		if(this.ticksExisted % 22 == 0){
			this.worldObj.playSoundAtEntity(this, "mob.enderdragon.wings", 1.0F, 1.0f);
		}

        for (int i = 0; i < 2; ++i)
        {
            this.worldObj.spawnParticle("flame", this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.height/2 + this.rand.nextDouble() * (double)this.height/2, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0D, -0.1D, 0.0D);
        }

        // Attack particles (flame ray)
		if(this.worldObj.isRemote && this.getIsAttacking()){
			
			Entity target = this.worldObj.getEntityByID(this.getTargetId());
			
			if(target != null){
			
				//Vec3 look = this.getLookVec();
				
				// This bit does a normalised vector towards the target.
				double x = (target.posX - this.posX)/this.getDistanceToEntity(target); //-MathHelper.sin((float)Math.toRadians(this.rotationYawHead));
				double y = ((WizardryUtilities.getEntityFeetPos(target) + target.height/2) - (this.posY + this.getEyeHeight()))/this.getDistanceToEntity(target); //-MathHelper.sin((float)Math.toRadians(this.rotationPitch));
				double z = (target.posZ - this.posZ)/this.getDistanceToEntity(target); //MathHelper.cos((float)Math.toRadians(this.rotationYawHead));
				
				for(int i=0; i<20; i++){
					// Starts a little bit forwards so it looks like it comes from the mouth
					double x1 = this.posX + this.getLookVec().xCoord*0.7 + x*i/2 + rand.nextFloat()*0.2 - 0.1f;
					double y1 = this.posY + 1.75 + y*i/2 + rand.nextFloat()/5 - 0.1f;
					double z1 = this.posZ + this.getLookVec().zCoord*0.7 + z*i/2 + rand.nextFloat()*0.2 - 0.1f;
					Wizardry.proxy.spawnParticle(EnumParticleType.MAGIC_FIRE, this.worldObj, x1, y1, z1, x, y, z, (int)(2.0D / (Math.random() * 0.8D + 0.2D)), 2, 0, 0);
					Wizardry.proxy.spawnParticle(EnumParticleType.MAGIC_FIRE, this.worldObj, x1, y1, z1, x, y, z, (int)(2.0D / (Math.random() * 0.8D + 0.2D)), 2, 0, 0);
				}
			
			}
		}
		
		// 3 second cooldown after target is killed; otherwise it instantly shoots the next one which looks a bit odd.
		if(this.getAttackTarget() != null && this.getAttackTarget().deathTime > 0){
			this.attackTime = 60;
		}

        // This is set to false after each update so that it only becomes true if attackEntity is called.
    	if(this.getIsAttacking()) this.setAttacking(false);
    	
    	// Adding this allows the phoenix to attack despite being in the air. However, for some strange reason
    	// it will only attack when within about 3 blocks of the ground. Any higher and it just sits there, not even
    	// attempting to find targets. Even weirder, it seems to be aiming a) in a different place to the particles
    	// and b) not always at the target. Edit: that last bit is fixed for now.
    	
    	this.onGround = true;

        super.onLivingUpdate();
    }

    /**
     * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
     */
    protected void attackEntity(Entity target, float par2)
    {
    	this.setAttacking(true);
    	this.setTargetId(target.getEntityId());
    	
    	MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(this.worldObj, this, 10);
		
    	if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase entity = (EntityLivingBase) rayTrace.entityHit;
			
			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entity)){
				entity.setFire(10);
				// This motion stuff removes knockback, which is desirable for continuous spells.
				double motionX = entity.motionX;
				double motionY = entity.motionY;
				double motionZ = entity.motionZ;
				
				entity.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getCaster(), DamageType.FIRE), 3.0f);
				
				entity.motionX = motionX;
				entity.motionY = motionY;
				entity.motionZ = motionZ;
			}
		}
		if(this.ticksExisted % 16 == 0){
			this.worldObj.playSoundAtEntity(this, "wizardry:flameray", 0.5F, 1.0f);
		}
    }
    
	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entitylivingbase, float f) {
		
		// Can attack for 7 seconds, then must cool down for 3.
		if(this.attackTime > 0 && this.attackTime <= 60) return;
		
		// I can't get the raytracing method to work at the moment so it auto-aims instead. Not ideal since there is
		// essentially no escape, but it is the best I can do.
		
		// Edit: Then again, it is quite cool! And hey, it's a master spell.
		
		this.setAttacking(true);
		this.attackTime = 200;
		this.setTargetId(entitylivingbase.getEntityId());
		
		EntityLivingBase entity = entitylivingbase;
		
		if(!MagicDamage.isEntityImmune(DamageType.FIRE, entity)){
			
			entity.setFire(10);
			
			// This motion stuff removes knockback, which is desirable for continuous spells.
			double motionX = entity.motionX;
			double motionY = entity.motionY;
			double motionZ = entity.motionZ;
			
			entity.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getCaster(), DamageType.FIRE), 3.0f);
			
			entity.motionX = motionX;
			entity.motionY = motionY;
			entity.motionZ = motionZ;
		}

		if(this.ticksExisted % 16 == 0){
			this.worldObj.playSoundAtEntity(this, "wizardry:flameray", 0.5F, 1.0f);
		}
	}

    /**
     * Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking
     * (Animals, Spiders at day, peaceful PigZombies).
     */
    protected Entity findPlayerToAttack()
    {
    	List entities = WizardryUtilities.getEntitiesWithinRadius(16, this.posX, this.posY, this.posZ, this.worldObj);
    	Entity entity = null;
    	
    	for(int i=0;i<entities.size();i++){
    		// Decides if current entity should be replaced.
    		if(entity == null || this.getDistanceToEntity(entity) > this.getDistanceToEntity((Entity)entities.get(i))){
    			// Decides if new entity is a valid target.
    			if(entities.get(i) instanceof EntityMob && this.canEntityBeSeen((Entity)entities.get(i))){
    				entity = (Entity)entities.get(i);
    			}
    		}
    	}
    	
        return entity;
    }
    
    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float par1) {}

    /**
     * Returns the item ID for the item the mob drops on death.
     */
    protected int getDropItemId()
    {
        return -1;
    }

    /**
     * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    public boolean isBurning()
    {
        return false;
    }

    /**
     * Drop 0-2 items of this living's type. @param par1 - Whether this entity has recently been hit by a player. @param
     * par2 - Level of Looting used to kill this mob.
     */
    protected void dropFewItems(boolean par1, int par2)
    {
        
    }

    public boolean func_70845_n()
    {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    public void func_70844_e(boolean par1)
    {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);

        if (par1)
        {
            b0 = (byte)(b0 | 1);
        }
        else
        {
            b0 &= -2;
        }

        this.dataWatcher.updateObject(16, Byte.valueOf(b0));
    }

    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
    protected boolean isValidLightLevel()
    {
        return true;
    }
}
