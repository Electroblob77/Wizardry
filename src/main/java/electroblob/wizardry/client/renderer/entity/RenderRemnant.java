package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelRemnant;
import electroblob.wizardry.entity.living.EntityRemnant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class RenderRemnant extends Render<EntityRemnant> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/remnant.png");

	private final ModelRemnant model;

	public RenderRemnant(RenderManager manager){
		super(manager);
		model = new ModelRemnant();
	}

	@Override
	public void doRender(EntityRemnant entity, double x, double y, double z, float entityYaw, float partialTicks){

		float age = (float)entity.ticksExisted + partialTicks;
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x, (float)y, (float)z);
		this.bindTexture(TEXTURE);
//		float whatIsThis = MathHelper.sin(age * 0.2F) / 2.0F + 0.5F;
//		whatIsThis = whatIsThis * whatIsThis + whatIsThis;

		if(this.renderOutlines){
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(this.getTeamColor(entity));
		}

		float expansion = 0.9f;

		if(entity.deathTime > 0){
			float f = (entity.deathTime + partialTicks) * 0.25f;
			f = 1 - (1 / (f + 1));
			expansion -= f * 0.75f;

		}else if(entity.hurtTime > 0){
			float f = (entity.hurtTime - partialTicks) / entity.maxHurtTime;
			// Neat bit of maths borrowed from the camera tilt effect when hurt
			f = MathHelper.sin(f * f * f * f * (float)Math.PI);
			expansion += f * 0.2f;
		}

//		if(entity.isAttacking()) s -= 0.4f;
		float rotationSpeed = entity.isAttacking() ? 10 : 3;

		this.model.render(entity, rotationSpeed, expansion, age, 0, 0, 0.0625f);

		if(this.renderOutlines){
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		GlStateManager.popMatrix();

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityRemnant entity){
		return TEXTURE;
	}

}
