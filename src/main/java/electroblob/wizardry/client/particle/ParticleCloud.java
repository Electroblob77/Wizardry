package electroblob.wizardry.client.particle;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleCloud extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("cloud", 4);

	public ParticleCloud(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.rand.nextInt(TEXTURES.length)]);
		
		this.setRBGColorF(1, 1, 1);
		this.particleMaxAge = 48 + this.rand.nextInt(12);
		this.particleScale *= 6;
		this.setGravity(false);
		this.setAlphaF(0);
		this.canCollide = false;
		this.shaded = true;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// Fading
		float fadeTime = this.particleMaxAge * 0.3f;
		this.setAlphaF(MathHelper.clamp(Math.min(this.particleAge / fadeTime, (this.particleMaxAge - this.particleAge) / fadeTime), 0, 1));

	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}

}
