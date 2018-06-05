package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleIce extends ParticleCustomTexture {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/particle/ice_particles.png");

	public ParticleIce(World world, double x, double y, double z){
		super(world, x, y, z);
		
		this.setParticleTextureIndex(rand.nextInt(8));
		this.canCollide = true;
		
		// Defaults
		this.particleScale *= 0.75f;
		this.setGravity(true);
		this.shaded = false;
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
