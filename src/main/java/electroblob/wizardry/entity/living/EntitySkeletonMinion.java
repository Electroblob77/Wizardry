package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.UUID;

// Extends AbstractSkeleton because EntitySkeleton drops skulls, which we don't want
public class EntitySkeletonMinion extends AbstractSkeleton implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new skeleton minion in the given world. */
	public EntitySkeletonMinion(World world){
		super(world);
		this.experienceValue = 0;
	}

	// EntitySkeleton overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));
	}

	// Shouldn't have randomised armour, but does still need a bow!
	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty){
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
	}

	// Where the skeleton minion is summoned does not affect its type.
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata){
		// Can't call super, so the code from the next level up (EntityLiving) had to be copied as well.
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
				.applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, EntityUtils.Operations.MULTIPLY_FLAT));

		if(this.rand.nextFloat() < 0.05F){
			this.setLeftHanded(true);
		}else{
			this.setLeftHanded(false);
		} 

		// Halloween pumpkin heads! Why not?
		if(this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()){
			Calendar calendar = this.world.getCurrentDate();

			if(calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F){
				this.setItemStackToSlot(EntityEquipmentSlot.HEAD,
						new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
				this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
			}
		}

		return livingdata;
	}

	// Since we're extending AbstractSkeleton these aren't set by the superclass like normal
	@Override protected SoundEvent getAmbientSound(){ return SoundEvents.ENTITY_SKELETON_AMBIENT; }
	@Override protected SoundEvent getHurtSound(DamageSource source){ return SoundEvents.ENTITY_SKELETON_HURT; }
	@Override protected SoundEvent getDeathSound(){ return SoundEvents.ENTITY_SKELETON_DEATH; }
	@Override protected SoundEvent getStepSound(){ return SoundEvents.ENTITY_SKELETON_STEP; }

	// Implementations

	@Override
	public void setRevengeTarget(EntityLivingBase entity){
		if(this.shouldRevengeTarget(entity)) super.setRevengeTarget(entity);
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		this.updateDelegate();
	}

	@Override
	public void onSpawn(){
		this.spawnParticleEffect();
		if(getCaster() instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)getCaster(), WizardryItems.charm_undead_helmets)){
			setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + this.rand.nextFloat() - 0.5f,
						this.posY + this.rand.nextFloat() * height, this.posZ + this.rand.nextFloat() - 0.5f, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
		return this.interactDelegate(player, hand) || super.processInteract(player, hand);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		this.readNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override protected int getExperiencePoints(EntityPlayer player){ return 0; }
	@Override protected boolean canDropLoot(){ return false; }
	@Override protected Item getDropItem(){ return null; }
	@Override protected ResourceLocation getLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }

	// This vanilla method has nothing to do with the custom despawn() method.
	@Override protected boolean canDespawn(){
		return getCaster() == null && getOwnerId() == null;
	}

	@Override
	public boolean getCanSpawnHere(){
		return this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
	}

	@Override
	public boolean canAttackClass(Class<? extends EntityLivingBase> entityType){
		// Returns true unless the given entity type is a flying entity and this skeleton does not have a bow.
		return !EntityFlying.class.isAssignableFrom(entityType) || this.getHeldItemMainhand().getItem() instanceof ItemBow;
	}

	@Override
	public ITextComponent getDisplayName(){
		if(getCaster() != null){
			return new TextComponentTranslation(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					new TextComponentTranslation("entity." + this.getEntityString() + ".name"));
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		// If this returns true, the renderer will show the nameplate when looking directly at the entity
		return Wizardry.settings.summonedCreatureNames && getCaster() != null;
	}
}
