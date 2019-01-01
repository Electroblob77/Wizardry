package electroblob.wizardry.client.particle;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleScorch extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("scorch", 8);

	public ParticleScorch(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.rand.nextInt(TEXTURES.length)]);
		
		this.particleGravity = 0;
		this.setMaxAge(100 + rand.nextInt(40));
		this.particleScale *= 2;
		// Defaults to black (which looks like a 'normal' scorch mark)
		this.setRBGColorF(0, 0, 0);
		this.shaded = false;
	}
	
	@Override
	public void setRBGColorF(float r, float g, float b){
		super.setRBGColorF(r, g, b);
		this.setFadeColour(0, 0, 0); // Scorch particles fade to black by default
	}
	
	@Override
	public void onUpdate(){

		super.onUpdate();

		// Colour fading (scorch particles do this slightly differently)
		float ageFraction = Math.min((float)this.particleAge / ((float)this.particleMaxAge * 0.5f), 1);
		// No longer uses setRBGColorF because that method now also sets the initial values
		this.particleRed   = this.initialRed   + (this.fadeRed   - this.initialRed)   * ageFraction;
		this.particleGreen = this.initialGreen + (this.fadeGreen - this.initialGreen) * ageFraction;
		this.particleBlue  = this.initialBlue  + (this.fadeBlue  - this.initialBlue)  * ageFraction;
		
		// Fading
		if(this.particleAge > this.particleMaxAge/2){
			this.setAlphaF(1 - ((float)this.particleAge - (float)(this.particleMaxAge/2)) / (float)this.particleMaxAge);
		}
		
		EnumFacing facing = EnumFacing.fromAngle(yaw);
		if(pitch == 90) facing = EnumFacing.UP;
		if(pitch == -90) facing = EnumFacing.DOWN;
		
		// Disappears if there is no block behind it (this is the same check used to spawn it)
		if(!world.getBlockState(new BlockPos(posX, posY, posZ).offset(facing.getOpposite())).getMaterial().isSolid()){
			this.setExpired();
		}
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}
}
