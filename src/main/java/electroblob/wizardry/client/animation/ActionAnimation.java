package electroblob.wizardry.client.animation;

import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.spell.Grapple;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * An animation that is associated with a particular {@link net.minecraft.item.EnumAction}. This animation will display
 * when a player is using an item with that action.
 * @author Electroblob
 * @since Wizardry 4.3
 * @see electroblob.wizardry.item.SpellActions
 */
public abstract class ActionAnimation extends Animation {

	private final EnumAction action;

	public ActionAnimation(EnumAction action){
		super(action.name());
		this.action = action;
	}

	@Override
	public boolean shouldDisplay(EntityPlayer player, boolean firstPerson){
		return !firstPerson && player.getItemInUseMaxCount() > 0 && player.getActiveItemStack().getItemUseAction() == action;
	}

	/** Called from the client proxy to register all of wizardry's action animations. */
	public static void register(){

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.POINT){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){
				// Something (probably some sort of race condition) causes getActiveItemStack to sometimes be wrong
				if(player.getActiveItemStack() != player.getHeldItem(player.getActiveHand())) return;

				EnumHandSide side = InventoryUtils.getSideForHand(player, player.getActiveHand());

				float pitch = (float)Math.toRadians(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks);
				float yaw = (float)Math.toRadians(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks)
						- (float)Math.toRadians(player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks);

				float x = -((float)Math.PI / 2) + pitch + 0.2f;
				float y = (side == EnumHandSide.RIGHT ? -0.25f : 0.25f) + yaw;

				ModelRendererExtended arm = (ModelRendererExtended)getArmForSide(model, side);
				arm.setRotation(x, y, 0);
			}
		});

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.POINT_UP){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){
				if(player.getActiveItemStack() != player.getHeldItem(player.getActiveHand())) return;
				EnumHandSide side = InventoryUtils.getSideForHand(player, player.getActiveHand());
				ModelRendererExtended arm = (ModelRendererExtended)getArmForSide(model, side);
				arm.addRotation(-2.2f, side == EnumHandSide.RIGHT ? 0.2f : -0.2f, 0);
			}
		});

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.POINT_DOWN){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){
				float tick = player.getItemInUseMaxCount() + partialTicks; // It's not the "max" use count at all!
				float y = Math.min(0.4f + tick * 0.05f, 0.7f);
				EnumHandSide side = InventoryUtils.getSideForHand(player, player.getActiveHand());
				ModelRendererExtended arm = (ModelRendererExtended)getArmForSide(model, side);
				arm.addRotation(-0.2f, side == EnumHandSide.RIGHT ? y : -y, 0);
			}
		});

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.SUMMON){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){
				float tick = player.getItemInUseMaxCount() + partialTicks; // It's not the "max" use count at all!
				float x = -Math.min(0.4f + tick * 0.2f, 2f);
				((ModelRendererExtended)getArmForSide(model, EnumHandSide.RIGHT)).addRotation(x, 1.2f, 0);
				((ModelRendererExtended)getArmForSide(model, EnumHandSide.LEFT)).addRotation(x, -1.2f, 0);
			}
		});

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.GRAPPLE){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){

				WizardData data = WizardData.get(player);
				RayTraceResult hit = data.getVariable(Grapple.TARGET_KEY);
				if(hit == null || hit.typeOfHit == RayTraceResult.Type.MISS) return;
				Vec3d target = hit.hitVec;

				if(hit.entityHit instanceof EntityLivingBase){
					// If the target is an entity, we need to use the entity's centre rather than the original hit position
					// because the entity will have moved!
					target = new Vec3d(hit.entityHit.posX, hit.entityHit.getEntityBoundingBox().minY + hit.entityHit.height/2, hit.entityHit.posZ);
				}

				EnumHandSide side = InventoryUtils.getSideForHand(player, player.getActiveHand());

				ModelRendererExtended arm = (ModelRendererExtended)getArmForSide(model, side);

				Vec3d direction = target.subtract(player.getPositionEyes(partialTicks));
				float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;

				float pitch = (float)MathHelper.atan2(MathHelper.sqrt(direction.x*direction.x + direction.z*direction.z), direction.y);
				float x = pitch - (float)Math.PI * 0.9f;
				float y = -(float)Math.toRadians(yaw) - (float)MathHelper.atan2(direction.x, direction.z);
				y += (side == EnumHandSide.RIGHT ? -0.25f : 0.25f);
				if(Math.abs(pitch) < 0.2f) y = arm.rotateAngleY;

				arm.setRotation(x, y, 0);
			}
		});

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.IMBUE){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){
				float tick = player.getItemInUseMaxCount() + partialTicks; // It's not the "max" use count at all!
				float z = Math.max(1.5f - tick * 0.1f, 0.8f);
				EnumHandSide side = InventoryUtils.getSideForHand(player, player.getActiveHand());
				ModelRendererExtended arm = (ModelRendererExtended)getArmForSide(model, side);
				arm.addRotation(-1.2f, side == EnumHandSide.RIGHT ? -0.2f : 0.2f, side == EnumHandSide.RIGHT ? z : -z);
				if(!player.getHeldItem(InventoryUtils.getHandForSide(player, side.opposite())).isEmpty()){
					((ModelRendererExtended)getArmForSide(model, side.opposite())).addRotation(-0.8f, side == EnumHandSide.LEFT ? 0.3f : -0.3f, 0);
				}
			}
		});

		PlayerAnimator.registerAnimation(new ActionAnimation(SpellActions.THRUST){
			@Override
			public void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson){
				if(player.getActiveItemStack() != player.getHeldItem(player.getActiveHand())) return;
				EnumHandSide side = InventoryUtils.getSideForHand(player, player.getActiveHand());
				ModelRendererExtended arm = (ModelRendererExtended)getArmForSide(model, side);
				float y = side == EnumHandSide.RIGHT ? -0.6f : 0.6f;
				arm.addRotation(-1.2f, y, 0);
				if(player.getHeldItem(InventoryUtils.getHandForSide(player, side.opposite())).isEmpty()){
					ModelRendererExtended otherArm = (ModelRendererExtended)getArmForSide(model, side.opposite());
					otherArm.setRotation(arm.rotateAngleX - 1.2f, -arm.rotateAngleY - y, otherArm.rotateAngleZ);
				}
			}
		});

	}

}
