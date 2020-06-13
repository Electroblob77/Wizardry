package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.UUID;

public class EntitySpiderMinion extends EntityCaveSpider implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new spider minion in the given world. */
	public EntitySpiderMinion(World world){
		super(world);
		this.experienceValue = 0;
	}

	// EntitySpider overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		// Spiders use a custom AI type specific to spiders which I can't access, but it's just an extension of
		// EntityAINearestAttackableTarget which takes daylight into account. Since I want spider minions to attack
		// regardless of daylight, I can just use EntityAINearestAttackableTarget.
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));
	}

	// No spider jockeys!
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata){

		// Can't call super, so the code from the next level up (EntityLiving) had to be copied as well.
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
				.applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, EntityUtils.Operations.MULTIPLY_FLAT));

		if(this.rand.nextFloat() < 0.05F){
			this.setLeftHanded(true);
		}else{
			this.setLeftHanded(false);
		}

		// Don't need anything from EntitySpider, since neither spider jockeys nor group data is relevant.
		return livingdata;
	}

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
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				ParticleBuilder.create(Type.DARK_MAGIC)
				.pos(this.posX + this.rand.nextFloat(), this.posY + this.rand.nextFloat(), this.posZ + this.rand.nextFloat())
				.clr(0.1f, 0.2f, 0.0f)
				.spawn(world);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
	}

	@Override
	public void onSuccessfulAttack(EntityLivingBase target){

		int seconds = 0;

		if(this.world.getDifficulty() == EnumDifficulty.NORMAL){
			seconds = 7;
		}else if(this.world.getDifficulty() == EnumDifficulty.HARD){
			seconds = 15;
		}

		if(seconds > 0){
			target.addPotionEffect(new PotionEffect(MobEffects.POISON, seconds * 20, 0));
		}
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
		// Returns true unless the given entity type is a flying entity.
		return !EntityFlying.class.isAssignableFrom(entityType);
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
