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
public class ParticleSnow extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("snow", 4);

	public ParticleSnow(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.rand.nextInt(TEXTURES.length)]);
		
		this.setVelocity(0, -0.02, 0);
		this.particleScale *= 0.6f;
		this.particleGravity = 0;
		this.canCollide = true;
		this.setMaxAge(40 + rand.nextInt(10));
		// Produces a variety of light blues and whites
		this.setRBGColorF(0.9f + 0.1f * world.rand.nextFloat(), 0.95f + 0.05f * world.rand.nextFloat(), 1);
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}
}
