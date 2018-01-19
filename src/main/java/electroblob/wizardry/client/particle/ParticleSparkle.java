package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSparkle extends ParticleCustomTexture {
	
	/* I have now figured out what particle factories are for: they separate out the individual uses of the varargs
	 * parameter in spawnParticle so they are kept with the particle class. For my purposes, it would be easier to do
	 * that in the particle spawning method itself. */
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/particle/sparkle_particles.png");

	// NOTE: Uncomment once 2.1.0 is released
	//private final float initialRed;
	//private final float initialGreen;
	//private final float initialBlue;
	
	// TODO: Assign these via the constructors, as part of the refactoring for particle parameters.
	// NOTE: Uncomment once 2.1.0 is released
//	private final float fadeRed = 1;
//	private final float fadeGreen = 1;
//	private final float fadeBlue = 0;
	
    public ParticleSparkle(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b){
		super(world, x, y, z, vx, vy, vz);
		this.setRBGColorF(r, g, b);
		// NOTE: Uncomment once 2.1.0 is released
		//initialRed = r;
		//initialGreen = g;
		//initialBlue = b;
        this.particleMaxAge = 48 + this.rand.nextInt(12);
    }

    public ParticleSparkle(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, int maxAge){
		super(world, x, y, z, vx, vy, vz, maxAge);
		this.setRBGColorF(r, g, b);
		// NOTE: Uncomment once 2.1.0 is released
		//initialRed = r;
		//initialGreen = g;
		//initialBlue = b;
    }
    
    public ParticleSparkle(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, boolean doGravity){
    	this(world, x, y, z, vx, vy, vz, r, g, b);
    	this.particleGravity = doGravity ? 1 : 0;
    }
    
    public ParticleSparkle(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, int maxAge, boolean doGravity){
    	this(world, x, y, z, vx, vy, vz, r, g, b, maxAge);
    	this.particleGravity = doGravity ? 1 : 0;
    }
    
    @Override
    public void init(){
    	this.setParticleTextureIndex(rand.nextInt(16));
        this.particleScale *= 0.75f;
    	this.particleGravity = 0;
    	this.canCollide = false;
    	this.fullBrightness = true;
    }
    
	@Override public ResourceLocation getTexture(){ return TEXTURE; }
	@Override protected int getXFrames(){ return 4; }
	@Override protected int getYFrames(){ return 4; }

    @Override
    public void onUpdate(){
    
        super.onUpdate();
        // Fading
        if(this.particleAge > this.particleMaxAge / 2){
            this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
        }
        // Colour fading TODO Uncomment once 2.1.0 is released
//        float ageFraction = (float)this.particleAge / (float)this.particleMaxAge;
//        this.setRBGColorF(this.initialRed + (this.fadeRed - this.initialRed)*ageFraction,
//        		this.initialGreen + (this.fadeGreen - this.initialGreen)*ageFraction,
//        		this.initialBlue + (this.fadeBlue - this.initialBlue)*ageFraction);
//
//        this.setParticleTextureIndex((this.particleAge * 11)/this.particleMaxAge);
    }
    
    /*
     * As a side note, I see a lot of magic mods with fancy-looking particle effects that really seem to 'glow'. It's
     * actually not that hard - you simply create a reasonably high-res texture with translucency and then set the
     * OpenGL blend function to something like SRC_ALPHA, SRC_ALPHA or ONE, ONE. The thing is... they're not very
     * Minecraft-y. I still maintain that part of wizardry's appeal is that it stays true to the game's pixelated charm,
     * rather than trying to make it something it's not. Still, the newer textures are much better than the defaults I
     * used to use.
     */
}
