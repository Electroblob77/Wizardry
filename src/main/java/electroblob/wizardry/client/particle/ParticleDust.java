package electroblob.wizardry.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDust extends Particle {

	private final boolean shaded;

	public ParticleDust(World par1World, double x, double y, double z, double par8, double par10, double par12, float r,
			float g, float b, boolean shaded){
		super(par1World, x, y, z, par8, par10, par12);
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.setParticleTextureIndex(0);
		this.setSize(0.01F, 0.01F);
		this.particleScale *= this.rand.nextFloat() + 0.2F;
		this.motionX = par8;
		this.motionY = par10;
		this.motionZ = par12;
		this.particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
		this.shaded = shaded;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		// this.moveEntity(this.motionX, this.motionY, this.motionZ);

		if(this.particleMaxAge-- <= 0){
			this.setExpired();
		}
	}

	@Override
	public int getBrightnessForRender(float par1){
		return shaded ? super.getBrightnessForRender(par1) : 15728880;
	}
	/* @Override public float getBrightness(float par1) { return shaded ? super.getBrightness(par1) : 1.0F; } */
}
