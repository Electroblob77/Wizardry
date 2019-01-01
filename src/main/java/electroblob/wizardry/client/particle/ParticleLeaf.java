package electroblob.wizardry.client.particle;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleLeaf extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("leaf", 16);

	public ParticleLeaf(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.rand.nextInt(TEXTURES.length)]);
		
		this.setVelocity(0, -0.03, 0);
		this.setMaxAge(10 + rand.nextInt(5));
		this.particleScale *= 1.4f;
		this.particleGravity = 0;
		this.canCollide = true;
		// Produces a variety of browns and greens
		this.setRBGColorF(0.1f + 0.3f * world.rand.nextFloat(), 0.5f + 0.3f * world.rand.nextFloat(), 0.1f);
	}
	
	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// Fading
		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(1 - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
		}
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}
}
