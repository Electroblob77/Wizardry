package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityIceGiant extends EntityIronGolem implements ISummonedCreature {

	// Field implementations
	private int lifetime = 600;
	private WeakReference<EntityLivingBase> casterReference;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override
	public int getLifetime(){
		return lifetime;
	}

	@Override
	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@Override
	public WeakReference<EntityLivingBase> getCasterReference(){
		return casterReference;
	}

	@Override
	public void setCasterReference(WeakReference<EntityLivingBase> reference){
		casterReference = reference;
	}

	@Override
	public UUID getCasterUUID(){
		return casterUUID;
	}

	@Override
	public void setCasterUUID(UUID uuid){
		this.casterUUID = uuid;
	}

	/**
	 * Default shell constructor, only used by client. Lifetime defaults arbitrarily to 600, but this doesn't matter
	 * because the client side entity immediately gets the lifetime value copied over to it by this class anyway. When
	 * extending this class, you must override this constructor or Minecraft won't like it, but there's no need to do
	 * anything inside it other than call super().
	 */
	public EntityIceGiant(World world){
		super(world);
		this.setSize(1.4F, 2.9F);
		this.experienceValue = 0;
	}

	/**
	 * Set lifetime to -1 to allow this creature to last forever. This constructor should be overridden when extending
	 * this class (be sure to call super()) so that AI and other things can be added.
	 */
	public EntityIceGiant(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
		super(world);
		this.setSize(1.4F, 2.9F);
		this.setPosition(x, y, z);
		this.casterReference = new WeakReference<EntityLivingBase>(caster);
		this.experienceValue = 0;
		this.lifetime = lifetime;
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
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));
	}

	// EntityIronGolem overrides

	@Override
	protected void updateAITasks(){
	} // Disables home-checking

	@Override
	public Village getVillage(){
		return null;
	}

	@Override
	public int getHoldRoseTick(){
		return 0;
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
	}

	@Override
	public void onDespawn(){
		this.playSound(WizardrySounds.SPELL_FREEZE, 1.0f, 1.0f);
		if(this.world.isRemote){
			for(int i = 0; i < 30; i++){
				float brightness = 0.5f + (rand.nextFloat() / 2);
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, this.world,
						this.posX - 1 + rand.nextDouble() * 2, this.posY + rand.nextDouble() * 3,
						this.posZ - 1 + rand.nextDouble() * 2, 0, -0.02, 0, 12 + rand.nextInt(8), brightness,
						brightness + 0.1f, 1.0f);
			}
		}
	}

	@Override
	public void onLivingUpdate(){

		super.onLivingUpdate();

		if(this.world.isRemote){
			Wizardry.proxy.spawnParticle(WizardryParticleType.SNOW, this.world, this.posX - 1 + rand.nextDouble() * 2,
					this.posY + rand.nextDouble() * 3, this.posZ - 1 + rand.nextDouble() * 2, 0, -0.02, 0,
					40 + rand.nextInt(10));
		}
	}

	@Override
	public void onSuccessfulAttack(EntityLivingBase target){

		target.motionY += 0.2;
		target.motionX += this.getLookVec().xCoord * 0.2;
		target.motionZ += this.getLookVec().xCoord * 0.2;

		target.addPotionEffect(new PotionEffect(WizardryPotions.frost, 300, 0));

		this.applyEnchantments(this, target);

		this.playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 1.0F);
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
		this.writeNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override
	protected int getExperiencePoints(EntityPlayer player){
		return 0;
	}

	@Override
	protected boolean canDropLoot(){
		return false;
	}

	@Override
	protected Item getDropItem(){
		return null;
	}

	@Override
	protected ResourceLocation getLootTable(){
		return null;
	}

	@Override
	public boolean canPickUpLoot(){
		return false;
	}

	// This vanilla method has nothing to do with the custom onDespawn() method.
	@Override
	protected boolean canDespawn(){
		return false;
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
		return Wizardry.settings.showSummonedCreatureNames && getCaster() != null;
	}

}
