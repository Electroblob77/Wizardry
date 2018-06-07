package electroblob.wizardry.client.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.ReflectionHelper;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderStatue extends TileEntitySpecialRenderer {

	private static final ResourceLocation stoneTexture32 = new ResourceLocation("wizardry:textures/entity/stone_statue_32.png");
	private static final ResourceLocation stoneTexture64 = new ResourceLocation("wizardry:textures/entity/stone_statue_64.png");

	private static final HashSet<Class<? extends EntityLiving>> entitiesUsing64Texture = new HashSet<Class<? extends EntityLiving>>();

	// Reflective method and field accesses; defined here since anything only ever needs to be reflectively accessed once.
	
	// Since the second argument is never used, it might as well be null.
	private static final Method selectZombieModel = ReflectionHelper.findMethod(RenderZombie.class, null, new String[]{"func_82427_a"}, EntityZombie.class);
	private static final Method preRenderCallback = ReflectionHelper.findMethod(RendererLivingEntity.class, null, new String[]{"func_77041_b", "preRenderCallback"}, EntityLivingBase.class, float.class);
	private static final Method shouldRenderPass = ReflectionHelper.findMethod(RendererLivingEntity.class, null, new String[]{"func_77032_a", "shouldRenderPass"}, EntityLivingBase.class, int.class, float.class);
	private static final Method renderEquippedItems = ReflectionHelper.findMethod(RendererLivingEntity.class, null, new String[]{"func_77029_c", "renderEquippedItems"}, EntityLivingBase.class, float.class);
	
	private static final Field mainModelField = ReflectionHelper.findField(RendererLivingEntity.class, "field_77045_g", "mainModel");
	private static final Field renderPassModelField = ReflectionHelper.findField(RendererLivingEntity.class, "field_77046_h", "renderPassModel");

	static {
		entitiesUsing64Texture.add(EntityZombie.class);
		entitiesUsing64Texture.add(EntityPigZombie.class);
		entitiesUsing64Texture.add(EntityVillager.class);
		entitiesUsing64Texture.add(EntityWitch.class);
		entitiesUsing64Texture.add(EntityHorse.class);
		entitiesUsing64Texture.add(EntityIronGolem.class);
		entitiesUsing64Texture.add(EntityWither.class);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float partialTickTime) {

		GL11.glPushMatrix();
		// The next line makes stuff render in the same place relative to the world wherever the player is.
		GL11.glTranslatef((float)x + 0.5F, (float)y, (float)z + 0.5F);

		if(tileentity instanceof TileEntityStatue){

			TileEntityStatue statue = (TileEntityStatue)tileentity;

			if(statue.creature != null && statue.position == 1){

				if(statue.isIce){

					float yaw = statue.creature.prevRotationYaw;
					int i = statue.creature.getBrightnessForRender(0);

					if (statue.creature.isBurning())
					{
						i = 15728880;
					}

					int j = i % 65536;
					int k = i / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					GL11.glRotatef(-yaw, 0F, 1F, 0F);

					RenderManager.instance.renderEntityWithPosYaw(statue.creature, 0, 0, 0, 0, 0);

				}else{

					GL11.glPushMatrix();
					GL11.glDisable(GL11.GL_CULL_FACE);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					float someScalingFactor = 0.0625f;

					float yaw = statue.creature.prevRotationYaw;
					int brightness = statue.creature.getBrightnessForRender(0);

					int j = brightness % 65536;
					int k = brightness / 65536;
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

					GL11.glRotatef(-yaw + 180, 0F, 1F, 0F);

					Render render = RenderManager.instance.getEntityRenderObject(statue.creature);

					if(render instanceof RendererLivingEntity){

						RendererLivingEntity renderliving = (RendererLivingEntity)render;

						// Reflection. This is... complete madness really, but it works!
						// What it effectively does is emulate the doRender method in RendererLivingEntity, but with
						// my own control over the textures.
						// Edit: Switched over to the forge-supplied ReflectionHelper methods, since these make it a
						// lot more concise and easy to read - since they're there, I might as well use them!
						// Edit 2: In order for this to work outside the development environment, you need to supply the
						// obfuscated (srg) names as well as the mcp ones, unless of course they are the same.
						
						try {
							
							// Chooses the appropriate zombie model, normal or villager
							// Fixed by moving before the model fields are accessed
							if(render instanceof RenderZombie && statue.creature instanceof EntityZombie){
								selectZombieModel.invoke(renderliving, (EntityZombie)statue.creature);
							}
							
							// Turns out that java automatically infers the type parameter T in this method from the type
							// I am assigning the returned value to. Neat!
							ModelBase mainModel = (ModelBase) mainModelField.get(renderliving);
							ModelBase renderPassModel = (ModelBase) renderPassModelField.get(renderliving);

							mainModel.isRiding = statue.creature.isRiding();
							mainModel.isChild = statue.creature.isChild();
						
							if(renderPassModel != null){
								renderPassModel.isRiding = statue.creature.isRiding();
								renderPassModel.isChild = statue.creature.isChild();
							}
								
							if(entitiesUsing64Texture.contains(statue.creature.getClass())){
								Minecraft.getMinecraft().renderEngine.bindTexture(stoneTexture64);
							}else{
								Minecraft.getMinecraft().renderEngine.bindTexture(stoneTexture32);
							}

							GL11.glEnable(GL12.GL_RESCALE_NORMAL);
							GL11.glScalef(-1.0F, -1.0F, 1.0F);

							preRenderCallback.invoke(renderliving, statue.creature, someScalingFactor);

							// Why is this -1.5f? No idea!
							GL11.glTranslatef(0, -1.5f, 0);

							GL11.glEnable(GL11.GL_ALPHA_TEST);

							mainModel.render(statue.creature, 0, 0, 0, 0, 0, someScalingFactor);

							for(int i=0; i<4; i++){
								if((Integer)shouldRenderPass.invoke(render, statue.creature, i, someScalingFactor) > 0){
									// Should never be null here since shouldRenderPass returns -1 when it is null, but there's no harm in checking.
									if(renderPassModel != null) renderPassModel.render(statue.creature, 0, 0, 0, 0, 0, someScalingFactor);
								}
							}

							GL11.glDepthMask(true);
							
							renderEquippedItems.invoke(render, statue.creature, someScalingFactor);

						// 'Pokemon' exception handling... Because why not?!
						} catch (Exception e) {
							System.err.println("Something went very wrong! Error while rendering petrified creature:");
							e.printStackTrace();
						}
					}
					
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glDisable(GL12.GL_RESCALE_NORMAL);
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glPopMatrix();

				}

			}
		}
		GL11.glPopMatrix();
	}

}
