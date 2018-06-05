package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSparkle extends ParticleCustomTexture {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/particle/sparkle_particles.png");

	// Implementation of colour fading (perhaps these belong in ParticleWizardry?)
	private float initialRed;
	private float initialGreen;
	private float initialBlue;

	public ParticleSparkle(World world, double x, double y, double z){
		super(world, x, y, z);
		this.setRBGColorF(1, 1, 1);
		this.particleMaxAge = 48 + this.rand.nextInt(12);
		this.particleScale *= 0.75f;
		this.particleGravity = 0;
		this.canCollide = false;
		this.shaded = false;
	}

	// Overridden to set the initial colour values
	@Override
	public void setRBGColorF(float r, float g, float b){
		super.setRBGColorF(r, g, b);
		initialRed = r;
		initialGreen = g;
		initialBlue = b;
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
		return 4;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// Fading
		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(
					1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
		}
		
		// Colour fading
		float ageFraction = (float)this.particleAge / (float)this.particleMaxAge;
		// No longer uses setRBGColorF because that method now also sets the initial values
		this.particleRed = this.initialRed + (this.fadeRed - this.initialRed)*ageFraction;
		this.particleGreen = this.initialGreen + (this.fadeGreen - this.initialGreen)*ageFraction;
		this.particleBlue = this.initialBlue + (this.fadeBlue - this.initialBlue)*ageFraction;

		this.setParticleTextureIndex((this.particleAge * 11)/this.particleMaxAge);
	}

	/* As a side note, I see a lot of magic mods with fancy-looking particle effects that really seem to 'glow'. It's
	 * actually not that hard - you simply create a reasonably high-res texture with translucency and then set the
	 * OpenGL blend function to something like SRC_ALPHA, SRC_ALPHA or ONE, ONE. The thing is... they're not very
	 * Minecraft-y. I still maintain that part of wizardry's appeal is that it stays true to the game's pixelated charm,
	 * rather than trying to make it something it's not. Still, the newer textures are much better than the defaults I
	 * used to use. */
}
