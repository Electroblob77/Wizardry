package electroblob.wizardry.client.particle;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleMagicFlame extends ParticleWizardry {

	// 4 different animation strips, 8 frames in each strip
	private static final ResourceLocation[][] TEXTURES = generateTextures("flame", 4, 8);

	public ParticleMagicFlame(World world, double x, double y, double z){

		super(world, x, y, z, TEXTURES[world.rand.nextInt(TEXTURES.length)]);

		this.setRBGColorF(1, 1, 1);
		this.particleAlpha = 1;
		this.particleMaxAge = 12 + rand.nextInt(4);
		this.shaded = false;
		this.canCollide = true;
	}

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation[] array : TEXTURES){
			for(ResourceLocation texture : array){
				event.getMap().registerSprite(texture);
			}
		}
	}
}
