package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSnow extends ParticleCustomTexture {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/particle/snow_particles.png");

	public ParticleSnow(World world, double x, double y, double z){
		super(world, x, y, z);
		this.setParticleTextureIndex(rand.nextInt(8));
		this.setVelocity(0, -0.02, 0);
		this.particleScale *= 0.6f;
		this.particleGravity = 0;
		this.canCollide = true;
		this.setLifetime(40 + rand.nextInt(10));
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
}
