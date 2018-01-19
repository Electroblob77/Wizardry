package electroblob.wizardry.client.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.ClientProxy;
import electroblob.wizardry.spell.Petrify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * Layer used to render the stone texture on a petrified creature. Handles dynamic tiling of the stone texture and
 * reflective access for classes that don't play nicely (looking at you, {@link RenderZombie}).
 * @author Electroblob
 * @since Wizardry 1.2
 */
public class LayerStone implements LayerRenderer<EntityLivingBase> {
	
    protected ModelBase model;
    private final RenderLivingBase<?> renderer;

	private static final ResourceLocation texture = new ResourceLocation("textures/blocks/stone.png");

	private static final Field zombieLayers = ReflectionHelper.findField(RenderZombie.class, "defaultLayers", "field_177122_o");
	private static final Field zombieVillagerLayers = ReflectionHelper.findField(RenderZombie.class, "villagerLayers", "field_177121_n");
	private static final Method swapZombieModel = ReflectionHelper.findMethod(RenderZombie.class, null,
			new String[]{"swapArmor", "func_82427_a"}, EntityZombie.class); // Second parameter (null) is unused
	
	@SuppressWarnings("unchecked") // The compiler is being annoying and I know that what I'm doing is type-safe.
	public static void initialiseLayers(){
		for(Entry<Class<? extends Entity>, Render<? extends Entity>> entry : Minecraft.getMinecraft().getRenderManager().entityRenderMap.entrySet()){
			// Zombies don't play nicely because they have their own, private lists of layers for the regular zombie
			// and the zombie villager, which are assigned within the constructor for RenderZombie and swapped out
			// as necessary. However, Mojang, in their infinite wisdom, haven't bothered to override addLayer to
			// modify those internal lists, so that method is useless.
			if(entry.getValue() instanceof RenderZombie){
				try {
					Object layers = zombieLayers.get(entry.getValue());
					if(layers instanceof List<?>){
						// Nice as the layer renderer system is, it doesn't lend itself to reflective access.
						// I KNOW that 'layers' (which was obtained using reflection) is of the type
						// List<LayerRenderer<EntityZombie>>, because that's what it's declared as. However, if I cast
						// 'layers' to List<LayerRenderer<EntityZombie>>, I can't add a LayerStone because it's only a
						// LayerRenderer<EntityLivingBase>, not a LayerRenderer<EntityZombie>.
						((List<LayerRenderer<EntityLivingBase>>)layers).add(new LayerStone((RenderLivingBase<?>)entry.getValue()));
					}
					layers = zombieVillagerLayers.get(entry.getValue());
					if(layers instanceof List<?>){
						((List<LayerRenderer<EntityLivingBase>>)layers).add(new LayerStone((RenderLivingBase<?>)entry.getValue()));
					}
				} catch (IllegalArgumentException | IllegalAccessException e){
					Wizardry.logger.error("Error while reflectively accessing zombie render layers");
					e.printStackTrace();
				}
			}else if(entry.getValue() instanceof RenderLivingBase){
				// Adds a stone layer to all the living entity renderers in the game. Whether it is actually rendered
				// is decided in doRenderLayer below on a per-entity basis.
				((RenderLivingBase<?>)entry.getValue()).addLayer(new LayerStone((RenderLivingBase<?>)entry.getValue()));
			}
			// NOTE: May have to do some special stuff for players if they are to be added; see Minecraft.getMinecraft().getRenderManager().getSkinMap()
		}
	}
	
    public LayerStone(RenderLivingBase<?> renderer){
        this.renderer = renderer;
        this.model = renderer.getMainModel();
    }

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale){
    	
    	if(entity.getEntityData().getBoolean(Petrify.NBT_KEY)){

			GlStateManager.enableLighting();
    		int i = this.getBlockBrightnessForEntity(entity, partialTicks);

			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
    		
    		ResourceLocation breakingTexture = ClientProxy.renderStatue.getBlockBreakingTexture();
    		
    		if(breakingTexture != null){
        		// Block breaking animation
    			// TODO: Spider eyes and enderman eyes (any others?) show through the stone when the block is being broken...
        		GlStateManager.enableBlend();
        		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    			this.renderer.bindTexture(breakingTexture);
    			this.renderEntityModel(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
        		GlStateManager.disableBlend();
    		}else{
    			// Stone texture
    			this.renderer.bindTexture(texture);
    			this.renderEntityModel(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
    		}
    	}
    }
    
    private int getBlockBrightnessForEntity(Entity entity, float partialTicks){
    	
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor_double(entity.posX), 0, MathHelper.floor_double(entity.posZ));

        if(entity.worldObj.isBlockLoaded(pos)){
            pos.setY(MathHelper.floor_double(entity.posY + (double)entity.getEyeHeight()));
            return entity.worldObj.getCombinedLight(pos, 0);
        }else{
            return 0;
        }
    }

	private void renderEntityModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale){
		
		GlStateManager.pushMatrix();
		// Enables tiling (Also used for guardian beam, beacon beam and ender crystal beam)
		// TODO: Backport this improvement
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		
		GlStateManager.depthMask(true); // Some entities set depth mask to false (i.e. no sorting of faces by depth)
		// In particular, LayerSpiderEyes sets it to false when the spider is invisible, for some reason.
		
		// Changes the scale at which the texture is applied to the model. See LayerCreeper for a similar example,
		// but with translation instead of scaling.
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		double scaleX = 1, scaleY = 1;
		// It's more logical to use the model's texture size, but some classes don't bother setting it properly
		// (e.g. ModelVillager), so to get the correct dimensions I'm getting them from the first box instead.
		if(model.boxList != null && model.boxList.get(0) != null){
		    scaleX = (double)model.boxList.get(0).textureWidth/16d;
		    scaleY = (double)model.boxList.get(0).textureHeight/16d;
		}else{ // Fallback to model fields; should never be needed
		    scaleX = (double)model.textureWidth/16d;
		    scaleY = (double)model.textureHeight/16d;
		}
		GlStateManager.scale(scaleX, scaleY, 1);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		// Lets RenderZombie do its (stupid and inflexible) model switching thing
		if(this.renderer instanceof RenderZombie && entity instanceof EntityZombie){
			try {
				swapZombieModel.invoke(this.renderer, entity);
		        this.model = this.renderer.getMainModel();
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Wizardry.logger.error("Error while reflectively calling RenderZombie#swapArmor");
				e.printStackTrace();
			}
		}
		// Hides the hat layer for bipeds
		if(this.model instanceof ModelBiped) ((ModelBiped) this.model).bipedHeadwear.isHidden = true;
		
		this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
		this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		
		if(this.model instanceof ModelBiped) ((ModelBiped) this.model).bipedHeadwear.isHidden = false;
		
		// Undoes the texture scaling
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
		GlStateManager.popMatrix();
	}

    @Override
    public boolean shouldCombineTextures(){
        return false;
    }
}