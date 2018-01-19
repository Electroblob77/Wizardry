package electroblob.wizardry.client.renderer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntityDecoy;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: Backport the rewrite of this entire class.
@SideOnly(Side.CLIENT)
public class RenderDecoy extends RenderBiped<EntityDecoy> {
	
	private static final ResourceLocation steveTextures = new ResourceLocation("textures/entity/steve.png");
	
	private static final Method getEntityTexture = ReflectionHelper.findMethod(Render.class, null,
			new String[]{"getEntityTexture", "func_110775_a"}, Entity.class); // Generic parameter T is erased to Entity at runtime.
	
	public RenderDecoy(RenderManager manager){
		super(manager, new ModelBiped(0.0f), 0.5f);
	}
	
	@Override
	public void doRender(EntityDecoy entity, double x, double y, double z, float entityYaw, float partialTicks) {
		
		if(entity.getCaster() != null){
			
			this.renderName(entity, x, y, z);
			
			// Save relevant animation fields from the caster to local variables
			float pitch = entity.getCaster().rotationPitch;
			float prevPitch = entity.getCaster().prevRotationPitch;
			float swing = entity.getCaster().swingProgress;
			float prevSwing = entity.getCaster().prevSwingProgress;
			float yawOffset = entity.getCaster().renderYawOffset;
			float prevYawOffset = entity.getCaster().prevRenderYawOffset;
			float yaw = entity.getCaster().rotationYawHead;
			float prevYaw = entity.getCaster().prevRotationYawHead;
			float limbSwing = entity.getCaster().limbSwing;
			float limbSwingAmount = entity.getCaster().limbSwingAmount;
			float prevLimbSwingAmount = entity.getCaster().prevLimbSwingAmount;
			int hurtTime = entity.getCaster().hurtTime;
			boolean sneak = entity.getCaster().isSneaking();
			Entity mount = entity.getCaster().getRidingEntity();
			
			// Assign decoy's animation fields to the caster
			entity.getCaster().rotationPitch = entity.rotationPitch;
			entity.getCaster().prevRotationPitch = entity.prevRotationPitch;
			entity.getCaster().swingProgress = entity.swingProgress;
			entity.getCaster().prevSwingProgress = entity.prevSwingProgress;
			entity.getCaster().renderYawOffset = entity.renderYawOffset;
			entity.getCaster().prevRenderYawOffset = entity.prevRenderYawOffset;
			entity.getCaster().rotationYawHead = entity.rotationYawHead;
			entity.getCaster().prevRotationYawHead = entity.prevRotationYawHead;
			entity.getCaster().limbSwing = entity.limbSwing;
			entity.getCaster().limbSwingAmount = entity.limbSwingAmount;
			entity.getCaster().prevLimbSwingAmount = entity.prevLimbSwingAmount;
			entity.getCaster().hurtTime = entity.hurtTime;
			entity.getCaster().setSneaking(false); // Decoys can't sneak FIXME Not working!
			entity.getCaster().dismountRidingEntity(); // Decoys can't ride anything
			
			// Do the rendering
			renderManager.getEntityRenderObject(entity.getCaster()).doRender(entity.getCaster(), x, y, z, entityYaw, partialTicks);
			
			// Reset caster's animation fields to their original values
			entity.getCaster().rotationPitch = pitch;
			entity.getCaster().prevRotationPitch = prevPitch;
			entity.getCaster().swingProgress = swing;
			entity.getCaster().prevSwingProgress = prevSwing;
			entity.getCaster().renderYawOffset = yawOffset;
			entity.getCaster().prevRenderYawOffset = prevYawOffset;
			entity.getCaster().rotationYawHead = yaw;
			entity.getCaster().prevRotationYawHead = prevYaw;
			entity.getCaster().limbSwing = limbSwing;
			entity.getCaster().limbSwingAmount = limbSwingAmount;
			entity.getCaster().prevLimbSwingAmount = prevLimbSwingAmount;
			entity.getCaster().hurtTime = hurtTime;
			entity.getCaster().setSneaking(sneak);
			if(mount != null) entity.getCaster().startRiding(mount);
			
		}else{
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityDecoy entity){
		if(entity.getCaster() != null){
			try {
				return (ResourceLocation)getEntityTexture.invoke(renderManager.getEntityRenderObject(entity.getCaster()));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e){
				Wizardry.logger.error("Error while reflectively calling Render#getEntityTexture as part of decoy rendering");
				e.printStackTrace();
			}
		}
		// Fallback to steve textures
		return steveTextures;
	}

}