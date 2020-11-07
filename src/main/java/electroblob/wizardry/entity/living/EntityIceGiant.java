package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.Village;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityIceGiant extends EntityIronGolem implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new ice giant in the given world. */
	public EntityIceGiant(World world){
		super(world);
		this.setSize(1.4F, 2.9F);
		this.experienceValue = 0;
	}

	@Override
	protected void initEntityAI(){
		this.getNavigator().getNodeProcessor().setCanSwim(false);
		this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.0D, true));
		this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 32.0F));
		// this.tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
		// this.tasks.addTask(5, new EntityAIWander(this, 0.6D));
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));
	}

	// EntityIronGolem overrides

	@Override protected void updateAITasks(){} // Disables home-checking
	@Override public Village getVillage(){ return null; }
	@Override public int getHoldRoseTick(){ return 0; }

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
		this.playSound(WizardrySounds.ENTITY_ICE_GIANT_DESPAWN, 1.0f, 1.0f);
		this.spawnParticleEffect();
	}
	
	private void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 30; i++){
				float brightness = 0.5f + (rand.nextFloat() / 2);
				ParticleBuilder.create(Type.SPARKLE, this).vel(0, -0.02, 0).time(12 + rand.nextInt(8))
				.clr(brightness, brightness + 0.1f, 1.0f).spawn(world);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return DrawingUtils.mix(0xffffff, 0x73e1ff, animationProgress);
	}

	@Override
	public void onLivingUpdate(){

		super.onLivingUpdate();

		if(this.world.isRemote){
			ParticleBuilder.create(Type.SNOW, this).spawn(world);
		}
	}

	@Override
	public void onSuccessfulAttack(EntityLivingBase target){

		target.motionY += 0.2;
		target.motionX += this.getLookVec().x * 0.2;
		target.motionZ += this.getLookVec().z * 0.2;

		target.addPotionEffect(new PotionEffect(WizardryPotions.frost, 300, 0));

		this.applyEnchantments(this, target);

		this.playSound(WizardrySounds.ENTITY_ICE_GIANT_ATTACK, 1.0F, 1.0F);
	}

	@Override
	public boolean hasParticleEffect(){
		return false;
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
