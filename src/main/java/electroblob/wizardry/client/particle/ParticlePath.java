package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Clairvoyance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticlePath extends ParticleWizardry {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "particle/path");

	private final double originX, originY, originZ;

	public ParticlePath(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURE); // This particle only has 1 texture
		
		this.originX = x;
		this.originY = y;
		this.originZ = z;
		
		// Set to a constant to remove the randomness from Particle.
		this.particleScale = 1.25f;
		this.particleGravity = 0;
		this.shaded = false;
		this.canCollide = false;
		this.setRBGColorF(1, 1, 1);
	}

	@Override
	public void onUpdate(){
		
		super.onUpdate();

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
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(TEXTURE);
	}
	
}
