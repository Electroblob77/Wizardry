package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.UUID;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * Abstract base implementation of {@link ISummonedCreature} which is the superclass to all custom summoned entities
 * (i.e. entities that don't extend vanilla/mod creatures). Also serves as an example of how to correctly implement the
 * above interface, and includes some non-critical method overrides which should be used for best results (xp, drops,
 * and such like). <i>Not to be confused with the old version of EntitySummonedCreature; that system has been
 * replaced.</i>
 * 
 * @since Wizardry 1.2
 * @author Electroblob
 */
public abstract class EntitySummonedCreature extends EntityCreature implements ISummonedCreature {

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
	public EntitySummonedCreature(World world){
		super(world);
		this.experienceValue = 0;
	}

	/**
	 * Set lifetime to -1 to allow this creature to last forever. This constructor should be overridden when extending
	 * this class (be sure to call super()) so that AI and other things can be added.
	 */
	public EntitySummonedCreature(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
		super(world);
		this.setPosition(x, y, z);
		this.casterReference = new WeakReference<EntityLivingBase>(caster);
		this.experienceValue = 0;
		this.lifetime = lifetime;
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
		// Returns true unless the given entity type is a flying entity and this entity only has melee attacks.
		return !EntityFlying.class.isAssignableFrom(entityType) || this.hasRangedAttack();
	}

	// TODO: Backport the following two methods to 1.7.10.
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

	// Specific to EntitySummonedCreature, remove if copying

	/** Whether this summoned creature has a ranged attack. Used to test whether it should attack flying creatures. */
	public abstract boolean hasRangedAttack();
}