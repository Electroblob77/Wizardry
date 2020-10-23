package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 3.5.4
public class ModelIceBarrier extends ModelBase {

	private final ModelRenderer mainGroup;

	public ModelIceBarrier(){

		textureWidth = 64;
		textureHeight = 64;

		mainGroup = new ModelRenderer(this);
		mainGroup.setRotationPoint(0.0F, 24.0F, 0.0F);
		setRotationAngle(mainGroup, 0.0F, 0.0F, 0.7854F);
		mainGroup.cubeList.add(new ModelBox(mainGroup, 0, 0, -14.5F, -14.5F, -1.5F, 29, 29, 3, 0.0F, false));
		mainGroup.cubeList.add(new ModelBox(mainGroup, 0, 32, -9.0F, -9.0F, -4.0F, 18, 18, 8, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		mainGroup.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}