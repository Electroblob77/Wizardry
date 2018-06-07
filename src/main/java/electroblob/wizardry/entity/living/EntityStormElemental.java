package electroblob.wizardry.entity.living;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityStormElemental extends EntitySummonedCreature
{
	/** Random offset used in floating behaviour */
	private float heightOffset = 0.5F;

	/** ticks until heightOffset is randomized */
	private int heightOffsetUpdateTime;
	private int field_70846_g;

	public EntityStormElemental(World world){
        super(world);
    }
	
	public EntityStormElemental(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
    	
        super(world, x, y, z, caster, lifetime);
		this.isImmuneToFire = true;
		this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(2, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, true, IMob.mobSelector));
		this.experienceValue = 0;
	}

	/**
	 * Returns true if the newer Entity AI code should be run
	 */
	protected boolean isAIEnabled()
	{
		return false;
	}

	@Override
	public boolean hasRangedAttack() {
		return true;
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(3.0d);
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0d);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
	}

	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new Byte((byte)0));
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
		for(int i=0;i<15;i++){
			float brightness = rand.nextFloat()*0.4f;
			if(this.worldObj.isRemote){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX - 0.5d + rand.nextDouble(), this.posY + this.height/2 - 0.5d + rand.nextDouble(), this.posZ - 0.5d + rand.nextDouble(), 0, 0.05f, 0, 20 + rand.nextInt(10), brightness, 0.0f, brightness);
			}
		}
	}

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn.
	 */
	public void onLivingUpdate()
	{
		if (!this.worldObj.isRemote)
		{
			/*
            if (this.isWet())
            {
                this.attackEntityFrom(DamageSource.drown, 1.0F);
            }
			 */

			--this.heightOffsetUpdateTime;

			if (this.heightOffsetUpdateTime <= 0)
			{
				this.heightOffsetUpdateTime = 100;
				this.heightOffset = 0.5F + (float)this.rand.nextGaussian() * 3.0F;
			}

			if (this.getEntityToAttack() != null && this.getEntityToAttack().posY + (double)this.getEntityToAttack().getEyeHeight() > this.posY + (double)this.getEyeHeight() + (double)this.heightOffset)
			{
				this.motionY += (0.30000001192092896D - this.motionY) * 0.30000001192092896D;
			}
		}

		if(this.ticksExisted % 120 == 1){
			this.playSound("wizardry:wind", 1.0f, 1.0f);
		}

		if (this.rand.nextInt(24) == 0)
		{
			this.worldObj.playSoundEffect(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, "fire.fire", 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F);
		}

		if (!this.onGround && this.motionY < 0.0D)
		{
			this.motionY *= 0.6D;
		}

		if(worldObj.isRemote){
			for(int i=0; i<2; ++i){
				worldObj.spawnParticle("largesmoke", this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0);
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0d, 0.0d, 0.0d, 0, 0, 0, 0);
			}

			for(int i=0; i<10; i++){
				float brightness = rand.nextFloat()*0.2f;
				double dy = this.rand.nextDouble() * (double)this.height;
				Wizardry.proxy.spawnParticle(EnumParticleType.BLIZZARD, worldObj, this.posX, this.posY + dy, this.posZ, 0, 0, 0, 20 + rand.nextInt(10), 0, brightness, brightness, false, 0.2f + 0.5f*dy);
			}
		}

		super.onLivingUpdate();
	}

	/**
	 * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
	 */
	protected void attackEntity(Entity target, float distance)
	{
		// This is normally the melee attack, but here it is changed to be lightning.
		if (this.attackTime <= 0 && distance < 3.0F && target.boundingBox.maxY > this.boundingBox.minY && target.boundingBox.minY < this.boundingBox.maxY)
		{
			this.attackTime = 50;
    		
    		EntityLightningBolt lightning = new EntityLightningBolt(worldObj, this.posX, this.posY, this.posZ);
    		worldObj.addWeatherEffect(lightning);
		}
		else if (distance < 30.0F)
		{
			double dx = target.posX - this.posX;
			double dy = target.boundingBox.minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
			double dz = target.posZ - this.posZ;

			if(this.attackTime == 0){

				this.playSound("wizardry:electricitya", 1.0F, worldObj.rand.nextFloat() * 0.3F + 0.8F);
				EntityLightningDisc lightningdisc = new EntityLightningDisc(this.worldObj, this.getCaster() == null ? this : this.getCaster());
				lightningdisc.setPosition(this.posX + this.getLookVec().xCoord, this.posY, this.posZ + this.getLookVec().zCoord);

				lightningdisc.directTowards(target, 0.5f);

				lightningdisc.posY = this.posY + (double)(this.height / 2.0F) + 0.5D;
				this.worldObj.spawnEntityInWorld(lightningdisc);

				this.attackTime = 50;
			}

			this.rotationYaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
			this.hasAttacked = true;
		}
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt p_70077_1_){
		// Left blank so the storm elemental is immune to lightning.
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
