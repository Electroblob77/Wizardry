package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleLeaf extends ParticleCustomTexture {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/particle/leaf_particles.png");

	public ParticleLeaf(World world, double x, double y, double z){
		super(world, x, y, z);
		this.setVelocity(0, -0.03, 0);
		this.setLifetime(10 + rand.nextInt(5));
		this.setParticleTextureIndex(rand.nextInt(16));
		this.particleScale *= 1.4f;
		this.particleGravity = 0;
		this.canCollide = true;
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
