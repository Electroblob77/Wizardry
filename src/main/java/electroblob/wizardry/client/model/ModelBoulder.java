package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

// Made with Blockbench 3.5.2
public class ModelBoulder extends ModelBase {

	private final ModelRenderer mainGroup;
	private final ModelRenderer box1;
	private final ModelRenderer box2;
	private final ModelRenderer box3;
	private final ModelRenderer box4;
	private final ModelRenderer box5;
	private final ModelRenderer box6;

	public ModelBoulder(){

		textureWidth = 64;
		textureHeight = 32;

		mainGroup = new ModelRenderer(this);
		mainGroup.setRotationPoint(0.0F, 24.0F, 0.0F);

		box1 = new ModelRenderer(this);
		box1.setRotationPoint(0.0F, -19.0F, 0.0F);
		mainGroup.addChild(box1);
		box1.cubeList.add(new ModelBox(box1, 0, 0, -11.0F, -11.0F, 11.0F, 22, 22, 8, 0.0F, false));

		box2 = new ModelRenderer(this);
		box2.setRotationPoint(0.0F, -19.0F, 0.0F);
		mainGroup.addChild(box2);
		setRotationAngle(box2, -1.5708F, 0.0F, 0.0F);
		box2.cubeList.add(new ModelBox(box2, 0, 0, -11.0F, -11.0F, 11.0F, 22, 22, 8, 0.0F, false));

		box3 = new ModelRenderer(this);
		box3.setRotationPoint(0.0F, -19.0F, 0.0F);
		mainGroup.addChild(box3);
		setRotationAngle(box3, 0.0F, -1.5708F, 0.0F);
		box3.cubeList.add(new ModelBox(box3, 0, 0, -11.0F, -11.0F, 11.0F, 22, 22, 8, 0.0F, false));

		box4 = new ModelRenderer(this);
		box4.setRotationPoint(0.0F, -19.0F, 0.0F);
		mainGroup.addChild(box4);
		setRotationAngle(box4, 0.0F, 1.5708F, 0.0F);
		box4.cubeList.add(new ModelBox(box4, 0, 0, -11.0F, -11.0F, 11.0F, 22, 22, 8, 0.0F, false));

		box5 = new ModelRenderer(this);
		box5.setRotationPoint(0.0F, -19.0F, 0.0F);
		mainGroup.addChild(box5);
		setRotationAngle(box5, 1.5708F, 0.0F, 0.0F);
		box5.cubeList.add(new ModelBox(box5, 0, 0, -11.0F, -11.0F, 11.0F, 22, 22, 8, 0.0F, false));

		box6 = new ModelRenderer(this);
		box6.setRotationPoint(0.0F, -19.0F, 0.0F);
		mainGroup.addChild(box6);
		setRotationAngle(box6, 3.1416F, 0.0F, 0.0F);
		box6.cubeList.add(new ModelBox(box6, 0, 0, -11.0F, -11.0F, 11.0F, 22, 22, 8, 0.0F, false));
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
		mainGroup.render(f5);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z){
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}

}