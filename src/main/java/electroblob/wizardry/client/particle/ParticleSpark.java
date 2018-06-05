package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSpark extends ParticleCustomTexture {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/particle/lightning_particles.png");

	public ParticleSpark(World world, double x, double y, double z){
		super(world, x, y, z);
		// Multiplied by 4 because the index works slightly differently for spark particles.
		this.setParticleTextureIndex(rand.nextInt(8) * 4);
		this.particleScale *= 1.4f;
		this.shaded = false;
		this.canCollide = false;
		this.setLifetime(3); // Lifetime defaults to 3 (and is very unlikely to be changed)
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		// Well this is handy! Looks like vanilla uses the texture index like this too.
		this.nextTextureIndexX();
	}

	@Override
	public ResourceLocation getTexture(){
		return TEXTURE;
	}

	@Override
	protected int getXFrames(){
		return 4;
	}

	@Override
	protected int getYFrames(){
		return 8;
	}

	@Override
	public void applyGLStateChanges(){
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// TESTME: Are these two actually necessary?
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
	}

	@Override
	public void undoGLStateChanges(){
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}

}
