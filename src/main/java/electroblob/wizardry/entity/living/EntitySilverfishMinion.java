package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class EntitySilverfishMinion extends EntitySilverfish implements ISummonedCreature {

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
	public EntitySilverfishMinion(World world){
		super(world);
		this.experienceValue = 0;
	}

	/**
	 * Set lifetime to -1 to allow this creature to last forever. This constructor should be overridden when extending
	 * this class (be sure to call super()) so that AI and other things can be added.
	 */
	public EntitySilverfishMinion(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
		super(world);
		this.setPosition(x, y, z);
		this.casterReference = new WeakReference<EntityLivingBase>(caster);
		this.experienceValue = 0;
		this.lifetime = lifetime;
	}

	// EntitySilverfish overrides
	@Override
	protected void initEntityAI(){
		// Super not called because we don't want AISummonSilverfish or AIHideInStone
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, false));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));
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
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, this.posX + this.rand.nextFloat(),
						this.posY + this.rand.nextFloat(), this.posZ + this.rand.nextFloat(), 0.0d, 0.0d, 0.0d, 0, 0.3f,
						0.3f, 0.3f);
			}
		}
	}

	@Override
	public void onSuccessfulAttack(EntityLivingBase target){
		if(!target.isEntityAlive()){
			this.onKillEntity(target);
		}
	}

	@Override
	public void onKillEntity(EntityLivingBase victim){
		// If the silverfish has a summoner, this is actually called from Wizardry's event handler rather than by
		// Minecraft itself, because the damagesource being changed causes it not to get called.
		if(!this.world.isRemote){
			// Summons 1-4 more silverfish
			int alliesToSummon = rand.nextInt(4) + 1;

			for(int i = 0; i < alliesToSummon; i++){
				EntitySilverfishMinion silverfish = new EntitySilverfishMinion(this.world, victim.posX, victim.posY,
						victim.posZ, this.getCaster(), this.lifetime);
				this.world.spawnEntity(silverfish);
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

	// This vanilla method has nothing to do with the custom despawn() method.
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