package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.client.model.ModelHammer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderHammer extends Render {
	
	private static final ResourceLocation texture = new ResourceLocation("wizardry:textures/entity/lightning_hammer.png");
	private ModelHammer model = new ModelHammer();
	
	public RenderHammer(){
		
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(x, y+1.5, z);
		GL11.glRotatef(180, 0F, 0F, 1F);
		
		this.bindTexture(texture);
		
		model.render(entity, 0, 0, 0, 0, 0, 0.0625f);
		
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}

}
