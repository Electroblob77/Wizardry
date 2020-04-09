package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;

//@SideOnly(Side.CLIENT)
public class RenderWizard extends RenderBiped<EntityWizard> {

	static final ResourceLocation[] TEXTURES = new ResourceLocation[6];

	public RenderWizard(RenderManager renderManager){

		super(renderManager, new ModelWizard(), 0.5F);

		for(int i = 0; i < 6; i++){
			TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/wizard/wizard_" + i + ".png");
		}
		// Just using the default without overriding models, since the armour sets its own model anyway.
		this.addLayer(new LayerBipedArmor(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWizard wizard){
		return TEXTURES[wizard.textureIndex];
	}

}
