package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Clairvoyance;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticlePath extends ParticleCustomTexture {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/particle/path_particles.png");

	private final double originX, originY, originZ;

	public ParticlePath(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g,
			float b){
		super(world, x, y, z, vx, vy, vz);
		this.setRBGColorF(r, g, b);
		this.originX = x;
		this.originY = y;
		this.originZ = z;
	}

	public ParticlePath(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g,
			float b, int maxAge){
		super(world, x, y, z, vx, vy, vz, maxAge);
		this.setRBGColorF(r, g, b);
		this.originX = x;
		this.originY = y;
		this.originZ = z;
	}

	@Override
	public void init(){
		this.setParticleTextureIndex(0);
		// Set to a constant to remove the randomness from Particle.
		this.particleScale = 1.25f;
		this.particleGravity = 0;
		this.fullBrightness = true;
		this.canCollide = false;
	}

	@Override
	public ResourceLocation getTexture(){
		return TEXTURE;
	}

	@Override
	protected int getXFrames(){
		return 1;
	}

	@Override
	protected int getYFrames(){
		return 1;
	}

	@Override
	public void onUpdate(){

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge){
			this.setExpired();
		}

		this.move(this.motionX, this.motionY, this.motionZ);

		// Fading
		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(1.0F
					- 2 * (((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge));
		}

		if(this.particleAge % Clairvoyance.PARTICLE_MOVEMENT_INTERVAL == 0){
			this.setPosition(this.originX, this.originY, this.originZ);
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
		}

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
