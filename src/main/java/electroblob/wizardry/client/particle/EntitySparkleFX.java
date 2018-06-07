package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntitySparkleFX extends EntityFX
{
	/** The name used to identify this particle, without gravity. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME = Wizardry.MODID + "sparkle";

	/** The name used to identify this particle, with gravity. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME_GRAVITY = Wizardry.MODID + "sparkleGravity";
	
	private static final ResourceLocation sparkleTextures = new ResourceLocation("wizardry:textures/particle/sparkle_particles.png");
	
    private boolean gravity = false;

    public EntitySparkleFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float red, float green, float blue, int maxAge)
    {
        super(par1World, par2, par4, par6);
        this.motionX = par8;
        this.motionY = par10;
        this.motionZ = par12;
        this.particleScale *= 0.75F;
        this.particleMaxAge = maxAge;
        this.noClip = true;
        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
    }
    
    public EntitySparkleFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float red, float green, float blue)
    {
        super(par1World, par2, par4, par6);
        this.motionX = par8;
        this.motionY = par10;
        this.motionZ = par12;
        this.particleScale *= 0.75F;
        this.particleMaxAge = 48 + this.rand.nextInt(12);
        this.noClip = true;
        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
    }
    
    public EntitySparkleFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float red, float green, float blue, boolean doGravity){
    	this(par1World, par2, par4, par6, par8, par10, par12, red, green, blue);
    	this.gravity = doGravity;
    }
    
    public EntitySparkleFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, float red, float green, float blue, int maxAge, boolean doGravity){
    	this(par1World, par2, par4, par6, par8, par10, par12, red, green, blue, maxAge);
    	this.gravity = doGravity;
    }

    /**
     * returns the bounding box for this entity
     */
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return false;
    }
    
    @Override
    public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexX = par1 % 4;
        this.particleTextureIndexY = par1 / 4;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
        
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
        if(this.gravity){
            this.motionY -= 0.05d;
        }

        if (this.particleAge > this.particleMaxAge / 2)
        {
            this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
        }

        this.setParticleTextureIndex((this.particleAge * 11)/this.particleMaxAge);
        
    }
    
    /* There are 4 layers of particles, specified as 0-3 by the method below.
     * - Layer 0 causes the normal particles.png to be bound to the render engine for normal particles.
     * - Layer 1 causes the block textures to be bound to the render engine for digging fx and falling fx.
     * - Layer 2 causes the item textures to be bound to the render engine for tool breaking fx, snowballpoofs, slime particles, etc.
     * - Layer 3 is not used in vanilla minecraft and was presumably added by forge for exactly this reason.
     * This means no texture is bound by vanilla minecraft, meaning you are free to do as you wish without possibly
     * overwriting vanilla particles. Mod particles won't be overwritten anyway since they bind their own textures.
     * It is of course important to bind the texture every time you render a custom particle, but I don't see how
     * you could do it any other way, since you don't have access to EffectRenderer.
     */
    @Override
    public int getFXLayer() {
    	// This can only be 0-3 or it will cause an ArrayIndexOutOfBoundsException in EffectRenderer.
        return 3;
    }
    
    @Override
    public void renderParticle(Tessellator tessellator, float partialTicks, float par3, float par4, float par5, float par6, float par7){
        
    	GL11.glPushMatrix();
    	
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        
        RenderHelper.disableStandardItemLighting();

    	Minecraft.getMinecraft().renderEngine.bindTexture(sparkleTextures);
    	
        tessellator.startDrawingQuads();
    	
        float f6 = (float)this.particleTextureIndexX / 4.0F;
        float f7 = f6 + 0.0624375F*4;
        float f8 = (float)this.particleTextureIndexY / 4.0F;
        float f9 = f8 + 0.0624375F*4;
        float f10 = 0.1F * this.particleScale;

        if (this.particleIcon != null)
        {
            f6 = this.particleIcon.getMinU();
            f7 = this.particleIcon.getMaxU();
            f8 = this.particleIcon.getMinV();
            f9 = this.particleIcon.getMaxV();
        }
        
        // Fix which gets proper values for interpPos so the particles don't appear to move.
        Entity player = Minecraft.getMinecraft().thePlayer;
        this.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
   	 	this.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
   	 	this.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        float f14 = 1.0F;
        
        tessellator.setColorRGBA_F(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha);
        tessellator.addVertexWithUV((double)(f11 - par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 - par5 * f10 - par7 * f10), (double)f7, (double)f9);
        tessellator.addVertexWithUV((double)(f11 - par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 - par5 * f10 + par7 * f10), (double)f7, (double)f8);
        tessellator.addVertexWithUV((double)(f11 + par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 + par5 * f10 + par7 * f10), (double)f6, (double)f8);
        tessellator.addVertexWithUV((double)(f11 + par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 + par5 * f10 - par7 * f10), (double)f6, (double)f9);
        
        tessellator.draw();

        GL11.glPopMatrix();
    
    }
    
    @Override
    public int getBrightnessForRender(float par1)
    {
        return 15728880;
    }

    @Override
    public float getBrightness(float par1)
    {
        return 1.0F;
    }
}
