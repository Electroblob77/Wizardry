package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.item.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.command.IEntitySelector;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/**
 * This class (formerly EntityMobMod) was originally copied from EntityMob to allow AI to distinguish between
 * summoned creatures and mobs, whilst still letting summoned creatures have access to the EntityMob code. It
 * was later renamed and generalised to better reflect its purpose, and now all 'sentient' magical entities
 * extend this class. The lifetime variable was also factored in so it can be synced automatically, rather than
 * bothering with this for every single subclass. In fact, this class now bears little resemblance to EntityMob at all.
 * <p>
 * As of Wizardry 1.1, this class has a despawn method and an onSpawn method which can be overridden to add particles
 * and such like. It also now stores the creature's owner.
 * @since Wizardry 1.0
 */
public abstract class EntitySummonedCreature extends EntityCreature implements IEntityAdditionalSpawnData {

	/** The lifetime of the summoned creature in ticks. Allows primarily for duration multiplier support, but also
	 * for example the skeleton legion spell which lasts for 60 seconds instead of the usual 30. Syncing and saving
	 * is done automatically. As of Wizardry 1.3, despawning is handled in EntitySummonedCreature; see
	 * {@link EntitySummonedCreature#despawn()} for details. */
	public int lifetime = 600;

	/** The entity that summoned this creature */
	private WeakReference<EntityLivingBase> caster;

	/** The UUID of the caster. Note that this is only for loading purposes; during normal updates
	 * the actual entity instance is stored (so that getEntityByUUID is not called constantly),
	 * so this will not always be synced (this is why it is private). */
	private UUID casterUUID;

	/** The entity selector passed into the new AI methods, if used. */
	protected IEntitySelector targetSelector;

	/**
	 * Default shell constructor, only used by client. Lifetime defaults arbitrarily to 600, but this doesn't
	 * matter because the client side entity immediately gets the lifetime value copied over to it by this class
	 * anyway. When extending this class, you must override this constructor or Minecraft won't like it, but there's
	 * no need to do anything inside it other than call super().
	 */
	public EntitySummonedCreature(World world){
		super(world);
		this.experienceValue = 0;
	}

	/**
	 * Set lifetime to -1 to allow this creature to last forever. This constructor should be overridden when
	 * extending this class (be sure to call super()) so that AI and other things can be added.
	 */
	public EntitySummonedCreature(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){

		super(world);
		this.setPosition(x, y, z);
		this.caster = new WeakReference<EntityLivingBase>(caster);
		this.experienceValue = 0;
		this.lifetime = lifetime;

		this.targetSelector = new IEntitySelector(){

			public boolean isEntityApplicable(Entity entity){

				// If the target is valid...
				if(isValidTarget(entity)){

					//... and is a player, they can be attacked, since players can't be in the whitelist or the blacklist.
					if(entity instanceof EntityPlayer) return true;

					//... and is a mob, a summoned creature, a wizard ...
					if((entity instanceof IMob || entity instanceof EntitySummonedCreature || (entity instanceof EntityWizard && !(getCaster() instanceof EntityWizard))
							// ... or in the whitelist ...
							|| Arrays.asList(Wizardry.summonedCreatureTargetsWhitelist).contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT)))
							// ... and isn't in the blacklist ...
							&& !Arrays.asList(Wizardry.summonedCreatureTargetsBlacklist).contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT))){
						// ... it can be attacked.
						return true;
					}
				}

				return false;
			}
		};
	}
	
	@Override
    protected int getExperiencePoints(EntityPlayer player){
        return 0;
    }

	@Override
	public boolean canAttackClass(Class entityType){
		// Returns true unless the given entity type is a flying entity and this entity only has melee attacks.
		// Overridden mainly to allow the zombie to attack creepers if the config settings allow it.
		return !EntityFlying.class.isAssignableFrom(entityType) || this.hasRangedAttack();
	}
	
	@Override
	public void setRevengeTarget(EntityLivingBase entity){
		// Allows the config to prevent minions from revenge-targeting their owners.
		if(entity != this.getCaster() || Wizardry.minionRevengeTargeting) super.setRevengeTarget(entity);
	}

	/** Whether this summoned creature has a ranged attack. Used to test whether it should attack flying creatures. */
	public abstract boolean hasRangedAttack();

	/**
	 * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	 * use this to react to sunlight and start to burn. - I still have no idea what the difference between this and
	 * the plain onUpdate() method is. From what I can gather, onUpdate() always calls this one anyway.
	 */
	public void onLivingUpdate(){

		this.updateArmSwingProgress();
		float f = this.getBrightness(1.0F);

		if (f > 0.5F)
		{
			this.entityAge += 2;
		}

		super.onLivingUpdate();
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate(){

		super.onUpdate();

		if(this.getCaster() == null && this.casterUUID != null){
			Entity entity = WizardryUtilities.getEntityByUUID(worldObj, casterUUID);
			if(entity instanceof EntityLivingBase){
				this.caster = new WeakReference<EntityLivingBase>((EntityLivingBase)entity);
			}
		}

		if(this.ticksExisted == 1){
			this.onSpawn();
		}

		if(this.ticksExisted > this.lifetime && this.lifetime != -1){
			this.despawn();
		}
	}

	/**
	 * Called when this creature has existed for 1 tick, effectively when it has just been spawned. Override
	 * to add particles, sounds, etc.
	 */
	public void onSpawn(){}

	/**
	 * Defaults to just setDead() in EntitySummonedCreature, but is provided to allow subclasses to override this,
	 * for example to spawn particles. You should always call super() when overriding this method, in case it changes.
	 * There is no need, therefore, to call setDead() when overriding.
	 */
	public void despawn(){
		this.setDead();
	}
	
	// This vanilla method has nothing to do with the custom despawn() method.
	@Override
	protected boolean canDespawn(){
		return false;
	}

	/**
	 * Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking
	 * (Animals, Spiders at day, peaceful PigZombies).
	 */
	// This was the same in all subclasses that use the old AI, so it might as well be here instead.
	protected Entity findPlayerToAttack(){

		// Thought I may as well use the new AI's attribute system to get the follow range, rather than make a new method.
		List<EntityLivingBase> entities = WizardryUtilities.getEntitiesWithinRadius(this.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue(), this.posX, this.posY, this.posZ, this.worldObj);
		Entity entity = null;

		for(Entity possibleTarget : entities){
			// Decides if current entity should be replaced.
			if(entity == null || this.getDistanceToEntity(entity) > this.getDistanceToEntity(possibleTarget)){
				// Decides if new entity is a valid target.
				if(this.isValidTarget(possibleTarget) && this.canEntityBeSeen(possibleTarget)){
					entity = possibleTarget;
				}
			}
		}

		return entity;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float par2)
	{
		if (this.isEntityInvulnerable())
		{
			return false;
		}
		else if (super.attackEntityFrom(source, par2))
		{
			Entity entity = source.getEntity();

			if (this.riddenByEntity != entity && this.ridingEntity != entity)
			{
				if (entity != this)
				{
					this.entityToAttack = entity;
				}

				return true;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean attackEntityAsMob(Entity target)
	{
		float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int i = 0;

		if (target instanceof EntityLivingBase)
		{
			f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)target);
			i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)target);
		}

		boolean flag = target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getCaster(), DamageType.MAGIC), f);

		if (flag)
		{
			if (i > 0)
			{
				target.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;
			}

			int j = EnchantmentHelper.getFireAspectModifier(this);

			if (j > 0)
			{
				target.setFire(j * 4);
			}

			if (target instanceof EntityLivingBase)
			{
				EnchantmentHelper.func_151384_a((EntityLivingBase)target, this);
			}

			EnchantmentHelper.func_151385_b(this, target);
		}

		return flag;
	}

	/**
	 * Basic mob attack. Defaults to touch of death in EntityCreature. Overridden by each mob to define their attack.
	 */
	protected void attackEntity(Entity par1Entity, float par2)
	{
		if (this.attackTime <= 0 && par2 < 2.0F && par1Entity.boundingBox.maxY > this.boundingBox.minY && par1Entity.boundingBox.minY < this.boundingBox.maxY)
		{
			this.attackTime = 20;
			this.attackEntityAsMob(par1Entity);
		}
	}

	/**
	 * Takes a coordinate in and returns a weight to determine how likely this creature will try to path to the block.
	 * Args: x, y, z
	 */
	public float getBlockPathWeight(int par1, int par2, int par3)
	{
		return 0.5F - this.worldObj.getLightBrightness(par1, par2, par3);
	}

	/**
	 * Checks to make sure the light is not too bright where the mob is spawning
	 */
	protected boolean isValidLightLevel()
	{
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.boundingBox.minY);
		int k = MathHelper.floor_double(this.posZ);

		if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > this.rand.nextInt(32))
		{
			return false;
		}
		else
		{
			int l = this.worldObj.getBlockLightValue(i, j, k);

			if (this.worldObj.isThundering())
			{
				int i1 = this.worldObj.skylightSubtracted;
				this.worldObj.skylightSubtracted = 10;
				l = this.worldObj.getBlockLightValue(i, j, k);
				this.worldObj.skylightSubtracted = i1;
			}

			return l <= this.rand.nextInt(8);
		}
	}

	/**
	 * Checks if the entity's current position is a valid location to spawn this entity.
	 */
	public boolean getCanSpawnHere()
	{
		return this.worldObj.difficultySetting != EnumDifficulty.PEACEFUL && this.isValidLightLevel() && super.getCanSpawnHere();
	}
	
	@Override
	protected boolean interact(EntityPlayer player) {
		
		ExtendedPlayer properties = ExtendedPlayer.get(player);
		// Selects one of the player's minions.
		if(player.isSneaking() && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemWand){
			
			if(!player.worldObj.isRemote && properties != null && this.getCaster() == player){
				
				if(properties.selectedMinion != null && properties.selectedMinion.get() == this){
					// Deselects the selected minion if right-clicked again
					properties.selectedMinion = null;
				}else{
					// Selects this minion
					properties.selectedMinion = new WeakReference(this);
				}
				properties.sync();
			}
			return true;
		}
		
		return super.interact(player);
	}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		if(this.getCaster() != null){
			nbttagcompound.setString("casterUUID", this.getCaster().getUniqueID().toString());
		}
		nbttagcompound.setInteger("lifetime", lifetime);
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		String string = nbttagcompound.getString("casterUUID");
		if(WizardryUtilities.verifyUUIDString(string)) casterUUID = UUID.fromString(string);
		this.lifetime = nbttagcompound.getInteger("lifetime");
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(lifetime);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		lifetime = data.readInt();
	}

	/**
	 * Returns the EntityLivingBase that summoned this creature, or null if it no longer exists. Cases where the
	 * entity may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported
	 * to another dimension, or this creature simply had no caster in the first place.
	 */
	public EntityLivingBase getCaster(){
		return caster == null ? null : caster.get();
	}
	
	/**
	 * Sets the EntityLivingBase that summoned this creature to the given caster. This should only be used by subclasses
	 * wishing to synchronise the caster for use on the client side; this is why it is protected.
	 */
	protected void setCaster(EntityLivingBase caster){
		this.caster = new WeakReference<EntityLivingBase>(caster);
	}

	/**
	 * Shorthand for {@link WizardryUtilities#isValidTarget(Entity, Entity)}, with the owner of this creature as the
	 * attacker. Also allows subclasses to override it if they wish to do so.
	 */
	public boolean isValidTarget(Entity target){
		return WizardryUtilities.isValidTarget(this.getCaster(), target);
	}
}
