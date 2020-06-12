package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIceSpike extends EntityMagicConstruct {

	private EnumFacing facing;

	public EntityIceSpike(World world){
		super(world);
		this.setSize(0.5f, 1.0f);
	}

	public void setFacing(EnumFacing facing){
		this.facing = facing;
		this.setRotation(-facing.getHorizontalAngle(), WizardryUtilities.getPitch(facing));
		float yaw = (-facing.getHorizontalAngle()) * (float)Math.PI/180;
		float pitch = (WizardryUtilities.getPitch(facing) - 90) * (float)Math.PI/180;
		Vec3d min = this.getPositionVector().add(new Vec3d(-width/2, 0, -width/2).rotatePitch(pitch).rotateYaw(yaw));
		Vec3d max = this.getPositionVector().add(new Vec3d(width/2, height, width/2).rotatePitch(pitch).rotateYaw(yaw));
		this.setEntityBoundingBox(new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z));
	}

	public EnumFacing getFacing(){
		return facing;
	}

	@Override
	public void onUpdate(){

		double extensionSpeed = 0;

		if(lifetime - this.ticksExisted < 15){
			extensionSpeed = -0.01 * (this.ticksExisted - (lifetime - 15));
		}else if(lifetime - this.ticksExisted < 25){
			extensionSpeed = 0;
		}else if(lifetime - this.ticksExisted < 28){
			extensionSpeed = 0.25;
		}

		if(facing != null){ // Will probably be null on the client side, but should never be on the server side
			this.move(MoverType.SELF, this.facing.getXOffset() * extensionSpeed, this.facing.getYOffset() * extensionSpeed,
					this.facing.getZOffset() * extensionSpeed);
		}

		if(lifetime - this.ticksExisted == 30) this.playSound(WizardrySounds.ENTITY_ICE_SPIKE_EXTEND, 1, 2.5f);

		if(!this.world.isRemote){
			for(Object entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox())){
				if(entity instanceof EntityLivingBase && this.isValidTarget((EntityLivingBase)entity)){
					DamageSource source = this.getCaster() == null ? DamageSource.MAGIC : MagicDamage.causeDirectMagicDamage(this.getCaster(), DamageType.FROST);
					// Potion effect only gets added if the damage succeeded.
					if(((EntityLivingBase)entity).attackEntityFrom(source, Spells.ice_spikes.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier))
						((EntityLivingBase)entity).addPotionEffect(new PotionEffect(WizardryPotions.frost,
								Spells.ice_spikes.getProperty(Spell.EFFECT_DURATION).intValue(),
								Spells.ice_spikes.getProperty(Spell.EFFECT_STRENGTH).intValue()));
				}
			}
		}

		super.onUpdate();
	}

	@Override
	public int getBrightnessForRender(){
		return 15728880;
	}
}
