package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class PotionContainment extends PotionMagicEffect {

	public static final String ENTITY_TAG = "containmentPos";

	public PotionContainment(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/containment.png"));
		this.setPotionName("potion." + Wizardry.MODID + ":containment");
	}

	@Override
	public boolean isReady(int duration, int amplifier){
		return true; // Execute the effect every tick
	}

	public static float getContainmentDistance(int effectStrength){
		return 15 - effectStrength * 4;
	}

	@Override
	public void performEffect(EntityLivingBase target, int strength){
		float maxDistance = getContainmentDistance(strength);

		// Initialise the containment position to the entity's position if it wasn't set already
		if(!target.getEntityData().hasKey(ENTITY_TAG)){
			NBTExtras.storeTagSafely(target.getEntityData(), ENTITY_TAG, NBTUtil.createPosTag(new BlockPos(target.getPositionVector().subtract(0.5, 0.5, 0.5))));
		}

		Vec3d origin = GeometryUtils.getCentre(NBTUtil.getPosFromTag(target.getEntityData().getCompoundTag(ENTITY_TAG)));

		double x = target.posX, y = target.posY, z = target.posZ;

		// Containment fields are cubes so we're dealing with each axis separately
		if(target.getEntityBoundingBox().maxX > origin.x + maxDistance) x = origin.x + maxDistance - target.width/2;
		if(target.getEntityBoundingBox().minX < origin.x - maxDistance) x = origin.x - maxDistance + target.width/2;

		if(target.getEntityBoundingBox().maxY > origin.y + maxDistance) y = origin.y + maxDistance - target.height;
		if(target.getEntityBoundingBox().minY < origin.y - maxDistance) y = origin.y - maxDistance;

		if(target.getEntityBoundingBox().maxZ > origin.z + maxDistance) z = origin.z + maxDistance - target.width/2;
		if(target.getEntityBoundingBox().minZ < origin.z - maxDistance) z = origin.z - maxDistance + target.width/2;

		if(x != target.posX || y != target.posY || z != target.posZ)
		{
			target.addVelocity(0.15 * Math.signum(x - target.posX), 0.15 * Math.signum(y - target.posY), 0.15 * Math.signum(z - target.posZ));
			EntityUtils.undoGravity(target);
			if(target.world.isRemote){
				target.world.playSound(target.posX, target.posY, target.posZ, WizardrySounds.ENTITY_FORCEFIELD_DEFLECT,
						WizardrySounds.SPELLS, 0.3f, 1f, false);
			}
		}

		// Need to do this here because it's the only way to hook into potion ending both client- and server-side
		if(target.getActivePotionEffect(this).getDuration() <= 1) target.getEntityData().removeTag(ENTITY_TAG);

	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		// This is LAST-RESORT CLEANUP. It does NOT need checking every tick! We always check for the actual potion anyway.
		if(event.getEntity().ticksExisted % 20 == 0 && event.getEntityLiving().getEntityData().hasKey(ENTITY_TAG)
				&& !event.getEntityLiving().isPotionActive(WizardryPotions.containment)){
			event.getEntityLiving().getEntityData().removeTag(ENTITY_TAG);
		}
	}

}
