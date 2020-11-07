package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/** Based on ModelEnderCrystal */
public class ModelRemnant extends ModelBase {

	private final ModelRenderer cube = new ModelRenderer(this, "cube");

	public ModelRemnant(){
		this.cube.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
	}

	@Override
	public void render(Entity entity, float rotationSpeed, float expandFraction, float age, float netHeadYaw, float headPitch, float scale){

		GlStateManager.pushMatrix();

//		GlStateManager.scale(2.0F, 2.0F, 2.0F);
//		GlStateManager.translate(0.0F, -0.5F, 0.0F);

		GlStateManager.disableCull();

		GlStateManager.translate(0.0F, entity.height/2, 0.0F);

		float s = expandFraction + MathHelper.sin(age * 0.1f) * 0.06f;

		GlStateManager.scale(s, s, s);

		GlStateManager.rotate(age * rotationSpeed/2, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
		GlStateManager.rotate(age * rotationSpeed, 0.0F, 1.0F, 0.0F);
		this.cube.render(scale);

		GlStateManager.scale(0.875F, 0.875F, 0.875F);
		GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
		GlStateManager.rotate(age * rotationSpeed, 0.0F, 1.0F, 0.0F);
		this.cube.render(scale);

		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}
}
