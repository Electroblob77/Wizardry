package electroblob.wizardry.entity;

import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Shield;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

public class EntityShield extends Entity {

	public WeakReference<EntityPlayer> player;

	public EntityShield(World world){
		super(world);
		this.noClip = true;
		this.width = 1.2f;
		this.height = 1.4f;
	}

	public EntityShield(World par1World, EntityPlayer player){
		super(par1World);
		this.width = 1.2f;
		this.height = 1.4f;
		this.player = new WeakReference<EntityPlayer>(player);
		this.noClip = true;
		this.setPositionAndRotation(player.posX + player.getLookVec().x,
				player.posY + 1 + player.getLookVec().y, player.posZ + player.getLookVec().z,
				player.rotationYawHead, player.rotationPitch);
		this.setEntityBoundingBox(new AxisAlignedBB(this.posX - 0.6f, this.posY - 0.7f, this.posZ - 0.6f,
				this.posX + 0.6f, this.posY + 0.7f, this.posZ + 0.6f));
	}

	@Override
	public void onUpdate(){
		// System.out.println("Shield exists, ID: " + this.getUniqueID().toString());
		EntityPlayer entityplayer = player != null ? player.get() : null;
		if(entityplayer != null){
			this.setPositionAndRotation(entityplayer.posX + entityplayer.getLookVec().x * 0.3,
					entityplayer.posY + 1 + entityplayer.getLookVec().y * 0.3,
					entityplayer.posZ + entityplayer.getLookVec().z * 0.3, entityplayer.rotationYawHead,
					entityplayer.rotationPitch);
			if(!entityplayer.isHandActive() || !(entityplayer.getHeldItem(entityplayer.getActiveHand()).getItem() instanceof ISpellCastingItem)){
				WizardData.get(entityplayer).setVariable(Shield.SHIELD_KEY, null);
				this.setDead();
			}
		}else if(!world.isRemote){
			this.setDead();
		}
	}

	// Overrides the original to stop the entity moving when it intersects stuff. The default arrow does this to allow
	// it to stick in blocks.
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9){
		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);
	}

	public boolean attackEntityFrom(DamageSource source, float damage){
		if(source != null && source.getImmediateSource() instanceof IProjectile){
			world.playSound(null, source.getImmediateSource().posX, source.getImmediateSource().posY,
					source.getImmediateSource().posZ, WizardrySounds.ENTITY_SHIELD_DEFLECT, WizardrySounds.SPELLS, 0.3f, 1.3f);
		}
		super.attackEntityFrom(source, damage);
		return false;
	}
	
	@Override
	public SoundCategory getSoundCategory(){
		return WizardrySounds.SPELLS;
	}

	public boolean canBeCollidedWith(){
		return !this.isDead;
	}

	public AxisAlignedBB getCollisionBox(Entity par1Entity){
		return par1Entity.getEntityBoundingBox();
	}

	@Override
	protected void entityInit(){

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){

	}

}
