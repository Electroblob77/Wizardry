package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class is for all inanimate magical constructs which are not projectiles. It was made from scratch to provide a
 * unifying superclass for black hole, blizzard, tornado and a few others which all share some characteristics. The
 * caster UUID, lifetime and damage multiplier are stored here, and lifetime is also synced here.
 * <p></p>
 * When extending this class, override both constructors. Generally speaking, subclasses of this class are areas of
 * effect which deal damage or apply effects over time.
 * 
 * @since Wizardry 1.0
 */
public abstract class EntityMagicConstruct extends Entity implements IEntityOwnable, IEntityAdditionalSpawnData {

	/** The UUID of the caster. As of Wizardry 4.3, this <b>is</b> synced, and rather than storing the caster
	 * instance via a weak reference, it is fetched from the UUID each time it is needed in
	 * {@link EntityMagicConstruct#getCaster()}. */
	private UUID casterUUID;

	/**
	 * The time in ticks this magical construct lasts for; defaults to 600 (30 seconds). If this is -1 the construct
	 * doesn't despawn.
	 */
	public int lifetime = 600;

	/** The damage multiplier for this construct, determined by the wand with which it was cast. */
	public float damageMultiplier = 1.0f;

	public EntityMagicConstruct(World world){
		super(world);
		this.height = 1.0f;
		this.width = 1.0f;
		this.noClip = true;
	}

	// Overrides the original to stop the entity moving when it intersects stuff. The default arrow does this to allow
	// it to stick in blocks.
	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport){
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	public void onUpdate(){

		if(this.ticksExisted > lifetime && lifetime != -1){
			this.despawn();
		}

		super.onUpdate();

	}

	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand){

		if(lifetime == -1 && getCaster() == player && player.isSneaking() && player.getHeldItem(hand).getItem() instanceof ISpellCastingItem){
			this.despawn();
			return EnumActionResult.SUCCESS;
		}

		return super.applyPlayerInteraction(player, vec, hand);
	}

	/**
	 * Defaults to just setDead() in EntityMagicConstruct, but is provided to allow subclasses to override this e.g.
	 * bubble uses it to dismount the entity inside it and play the 'pop' sound before calling super(). You should
	 * always call super() when overriding this method, in case it changes. There is no need, therefore, to call
	 * setDead() when overriding.
	 */
	public void despawn(){
		this.setDead();
	}

	@Override
	protected void entityInit(){

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){
		if(nbttagcompound.hasUniqueId("casterUUID")) casterUUID = nbttagcompound.getUniqueId("casterUUID");
		lifetime = nbttagcompound.getInteger("lifetime");
		damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
		if(casterUUID != null){
			nbttagcompound.setUniqueId("casterUUID", casterUUID);
		}
		nbttagcompound.setInteger("lifetime", lifetime);
		nbttagcompound.setFloat("damageMultiplier", damageMultiplier);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(lifetime);
		data.writeInt(getCaster() == null ? -1 : getCaster().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf data){

		lifetime = data.readInt();

		int id = data.readInt();

		if(id == -1){
			setCaster(null);
		}else{
			Entity entity = world.getEntityByID(id);
			if(entity instanceof EntityLivingBase){
				setCaster((EntityLivingBase)entity);
			}else{
				Wizardry.logger.warn("Construct caster with ID in spawn data not found");
			}
		}
	}

	@Nullable
	@Override
	public UUID getOwnerId(){
		return casterUUID;
	}

	@Nullable
	@Override
	public Entity getOwner(){
		return getCaster(); // Delegate to getCaster
	}

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	@Nullable
	public EntityLivingBase getCaster(){ // Kept despite the above method because it returns an EntityLivingBase

		Entity entity = EntityUtils.getEntityByUUID(world, getOwnerId());

		if(entity != null && !(entity instanceof EntityLivingBase)){ // Should never happen
			Wizardry.logger.warn("{} has a non-living owner!", this);
			entity = null;
		}

		return (EntityLivingBase)entity;
	}
	
	public void setCaster(@Nullable EntityLivingBase caster){
		this.casterUUID = caster == null ? null : caster.getUniqueID();
	}

	/**
	 * Shorthand for {@link AllyDesignationSystem#isValidTarget(Entity, Entity)}, with the owner of this construct as the
	 * attacker. Also allows subclasses to override it if they wish to do so.
	 */
	public boolean isValidTarget(Entity target){
		return AllyDesignationSystem.isValidTarget(this.getCaster(), target);
	}
	
	@Override
	public SoundCategory getSoundCategory(){
		return WizardrySounds.SPELLS;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

	@Override
	public boolean isPushedByWater(){
		return false;
	}
}
