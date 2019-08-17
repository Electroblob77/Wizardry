package electroblob.wizardry.client.particle;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleLightningPulse extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("lightning_pulse", 8);

	public ParticleLightningPulse(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES);
		
		this.particleScale = 32f;
		this.setRBGColorF(1, 1, 1);
		this.shaded = false;
		this.canCollide = false;
		this.setMaxAge(7); // Lifetime defaults to 7 (and is very unlikely to be changed)
		// Faces up by default
		this.pitch = 90;
		this.yaw = 0;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}

}
