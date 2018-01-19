package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.UUID;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntitySkeletonMinion extends EntitySkeleton implements ISummonedCreature {

	// Field implementations
	private int lifetime = 600;
	private WeakReference<EntityLivingBase> casterReference;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public WeakReference<EntityLivingBase> getCasterReference(){ return casterReference; }
	@Override public void setCasterReference(WeakReference<EntityLivingBase> reference){ casterReference = reference; }
	@Override public UUID getCasterUUID() { return casterUUID; }
	@Override public void setCasterUUID(UUID uuid) { this.casterUUID = uuid; }

	/**
	 * Default shell constructor, only used by client. Lifetime defaults arbitrarily to 600, but this doesn't
	 * matter because the client side entity immediately gets the lifetime value copied over to it by this class
	 * anyway. When extending this class, you must override this constructor or Minecraft won't like it, but there's
	 * no need to do anything inside it other than call super().
	 */
	public EntitySkeletonMinion(World world){
		super(world);
		this.experienceValue = 0;
	}

	/**
	 * Set lifetime to -1 to allow this creature to last forever. This constructor should be overridden when
	 * extending this class (be sure to call super()) so that AI and other things can be added.
	 */
	public EntitySkeletonMinion(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
		super(world);
		this.setPosition(x, y, z);
		this.casterReference = new WeakReference<EntityLivingBase>(caster);
		this.experienceValue = 0;
		this.lifetime = lifetime;
	}
	
	// EntitySkeleton overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the ADS into account.
	@Override
	protected void initEntityAI()
	{
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));
	}
	
	// Shouldn't have randomised armour, but does still need a bow!
	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
	}
	
	// Where the skeleton minion is summoned does not affect its type.
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
		// Can't call super, so the code from the next level up (EntityLiving) had to be copied as well.
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextGaussian() * 0.05D, 1));

        if (this.rand.nextFloat() < 0.05F)
        {
            this.setLeftHanded(true);
        }
        else
        {
            this.setLeftHanded(false);
        }

        // Halloween pumpkin heads! Why not?
        if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) == null)
        {
            Calendar calendar = this.worldObj.getCurrentDate();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F)
            {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

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
		if(this.worldObj.isRemote){
			for(int i=0;i<15;i++){
				this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + this.rand.nextFloat(), this.posY + 1 + this.rand.nextFloat(), this.posZ + this.rand.nextFloat(), 0, 0, 0);
			}
		}
	}

	@Override
	public boolean hasParticleEffect() {
		return true;
	}
	
	@Override
	public void onSuccessfulAttack(EntityLivingBase target) {
		if(this.getSkeletonType() == SkeletonType.WITHER){
            target.addPotionEffect(new PotionEffect(MobEffects.WITHER, 200));
        }
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack) {
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's 'stop as soon as you find true' method of evaluating OR statements.
		return this.interactDelegate(player, hand, stack) || super.processInteract(player, hand, stack);
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

	@Override protected int getExperiencePoints(EntityPlayer player){ return 0; }
	@Override protected boolean canDropLoot(){ return false; }
	@Override protected Item getDropItem(){ return null; }
	@Override protected ResourceLocation getLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }
	// This vanilla method has nothing to do with the custom despawn() method.
	@Override protected boolean canDespawn(){ return false; }

	@Override
	public boolean canAttackClass(Class<? extends EntityLivingBase> entityType){
		return true;
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
