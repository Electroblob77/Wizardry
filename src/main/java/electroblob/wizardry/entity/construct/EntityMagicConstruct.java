package electroblob.wizardry.entity.construct;

import java.lang.ref.WeakReference;
import java.util.UUID;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntitySummonedCreature;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * This class is for all inanimate magical constructs which are not projectiles. It was made from scratch
 * to provide a unifying superclass for black hole, blizzard, tornado and a few others which all share some
 * characteristics. The EntityPlayer instance of the caster, the lifetime and the damage multiplier are stored
 * and synced here.
 * <p>
 * When extending this class, override both constructors. Generally speaking, subclasses of this class are areas
 * of effect which deal damage or apply effects over time.
 * @since Wizardry 1.0
 */
public abstract class EntityMagicConstruct extends Entity implements IEntityAdditionalSpawnData {
	
	/** The entity that created this construct */
	private WeakReference<EntityLivingBase> caster;
	
	/** The UUID of the caster. Note that this is only for loading purposes; during normal updates
	 * the actual entity instance is stored (so that getEntityByUUID is not called constantly),
	 * so this will not always be synced (this is why it is private). */
	private UUID casterUUID;
	
	/** The time in ticks this magical construct lasts for; defaults to 600 (30 seconds). If this is -1 the construct
	 * doesn't despawn. */
	public int lifetime = 600;
	
	/** The damage multiplier for this construct, determined by the wand with which it was cast. */
	public float damageMultiplier = 1.0f;
	
	public EntityMagicConstruct(World par1World) {
		super(par1World);
		this.height = 1.0f;
		this.width = 1.0f;
		this.noClip = true;
	}
	
	public EntityMagicConstruct(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world);
		this.height = 1.0f;
		this.width = 1.0f;
		this.setPosition(x, y, z);
		this.caster = new WeakReference<EntityLivingBase>(caster);
		this.noClip = true;
		this.lifetime = lifetime;
		this.damageMultiplier = damageMultiplier;
	}
	
	// Overrides the original to stop the entity moving when it intersects stuff. The default arrow does this to allow
    // it to stick in blocks.
	@Override
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
    {
        this.setPosition(par1, par3, par5);
        this.setRotation(par7, par8);
    }
	
	public void onUpdate(){
		
		if(this.getCaster() == null && this.casterUUID != null){
			Entity entity = WizardryUtilities.getEntityByUUID(worldObj, casterUUID);
			if(entity instanceof EntityLivingBase){
				this.caster = new WeakReference<EntityLivingBase>((EntityLivingBase)entity);
			}
		}
		
		if(this.ticksExisted > lifetime && lifetime != -1){
			this.despawn();
		}
		
		super.onUpdate();
		
	}
	
	/**
	 * Defaults to just setDead() in EntityMagicConstruct, but is provided to allow subclasses to override this
	 * e.g. bubble uses it to dismount the entity inside it and play the 'pop' sound before calling super(). You
	 * should always call super() when overriding this method, in case it changes. There is no need, therefore, to
	 * call setDead() when overriding.
	 */
	public void despawn(){
		this.setDead();
	}

	@Override
	protected void entityInit() {
		
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){
		String string = nbttagcompound.getString("casterUUID");
		if(WizardryUtilities.verifyUUIDString(string)) casterUUID = UUID.fromString(string);
        lifetime = nbttagcompound.getInteger("lifetime");
        damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
		if(this.getCaster() != null){
        	nbttagcompound.setString("casterUUID", this.getCaster().getUniqueID().toString());
        }
		nbttagcompound.setInteger("lifetime", lifetime);
		nbttagcompound.setFloat("damageMultiplier", damageMultiplier);
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
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the
	 * entity may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported
	 * to another dimension, or this construct simply had no caster in the first place.
	 */
	public EntityLivingBase getCaster() {
		return caster == null ? null : caster.get();
	}
	
	/**
	 * Shorthand for {@link WizardryUtilities#isValidTarget(Entity, Entity)}, with the owner of this construct as the
	 * attacker. Also allows subclasses to override it if they wish to do so.
	 */
	public boolean isValidTarget(Entity target){
		return WizardryUtilities.isValidTarget(this.getCaster(), target);
	}
	
	@Override
    public boolean canRenderOnFire()
    {
        return false;
    }
	
	@Override
	public boolean isPushedByWater() {
		return false;
	}
}
