package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class PotionContainment extends PotionMagicEffect {

	public static final String ENTITY_TAG = "containmentPos";

	private static final double RUBBERBAND_THRESHOLD = 0.24;

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

		if(x != target.posX || y != target.posY || z != target.posZ){

//			if(target.world.isRemote){
//
//				if(x != target.posX){
//					for(int i = 0; i < 20; i++){
//						ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(
//								x,
//								target.posY + target.height * target.world.rand.nextFloat(),
//								target.posZ + target.width * (target.world.rand.nextFloat() - 0.5f))
//								.face(EnumFacing.EAST).clr(0.8f, 0.9f, 1).spawn(target.world);
//					}
//				}
//
//				if(y != target.posY){
//					for(int i = 0; i < 20; i++){
//						ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(
//								target.posX + target.width * (target.world.rand.nextFloat() - 0.5f),
//								y,
//								target.posZ + target.width * (target.world.rand.nextFloat() - 0.5f))
//								.face(EnumFacing.UP).clr(0.8f, 0.9f, 1).spawn(target.world);
//					}
//				}
//
//				if(z != target.posZ){
//					for(int i = 0; i < 20; i++){
//						ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(
//								target.posX + target.width * (target.world.rand.nextFloat() - 0.5f),
//								target.posY + target.height * target.world.rand.nextFloat(),
//								z)
//								.face(EnumFacing.SOUTH).clr(0.8f, 0.9f, 1).spawn(target.world);
//					}
//				}
//			}

			// Rubberbanding prevention (the 0.25 limit is from NetHandlerPlayServer, just search "moved wrongly"
			if(target instanceof EntityPlayer){
				x = MathHelper.clamp(x, target.posX - RUBBERBAND_THRESHOLD, target.posX + RUBBERBAND_THRESHOLD);
				y = MathHelper.clamp(y, target.posY - RUBBERBAND_THRESHOLD, target.posY + RUBBERBAND_THRESHOLD);
				z = MathHelper.clamp(z, target.posZ - RUBBERBAND_THRESHOLD, target.posZ + RUBBERBAND_THRESHOLD);
			}

			EntityUtils.undoGravity(target);
			target.addVelocity(0.35 * Math.signum(x - target.posX), 0.35 * Math.signum(y - target.posY), 0.35 * Math.signum(z - target.posZ));
			target.setPositionAndUpdate(x, y, z); // FIXME: This line must be causing the server rubberbanding, not sure why
			// Player motion is handled on that player's client so needs packets
			if(target instanceof EntityPlayerMP){
				((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
			}
//
//			target.world.playSound(target.posX, target.posY, target.posZ, WizardrySounds.ENTITY_FORCEFIELD_DEFLECT,
//					WizardrySounds.SPELLS, 0.3f, 1f, false);

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
