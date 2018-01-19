package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpiritHorse extends RenderHorse {
	
    private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/entity/spirit_horse.png");

    public RenderSpiritHorse(RenderManager renderManager, float par2){
        super(renderManager, new ModelHorse(), par2);
    }
    
    @Override
    protected ResourceLocation getEntityTexture(EntityHorse entity) {
    	return texture;
    }

    @Override
    protected void preRenderCallback(EntityHorse entitylivingbaseIn, float partialTickTime){
    	super.preRenderCallback(entitylivingbaseIn, partialTickTime);
    	GlStateManager.enableBlend();
    	GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

}
