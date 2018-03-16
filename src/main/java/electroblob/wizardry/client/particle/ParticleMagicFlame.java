package electroblob.wizardry.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleMagicFlame extends Particle {

	/** The scale of the flame particle */
	private float flameScale;

	public ParticleMagicFlame(World par1World, double par2, double par4, double par6, double par8, double par10,
			double par12, int maxAge, float scale){
		super(par1World, par2, par4, par6, par8, par10, par12);
		this.motionX = this.motionX * 0.009999999776482582D + par8;
		this.motionY = this.motionY * 0.009999999776482582D + par10;
		this.motionZ = this.motionZ * 0.009999999776482582D + par12;
		this.flameScale = scale;
		this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
		if(maxAge == 0){
			this.particleMaxAge = (int)(2.0D / (Math.random() * 0.8D + 0.2D));
		}else{
			this.particleMaxAge = maxAge;
		}
		// IDEA: Make the particles for ray spells collide properly and not spawn on the other side of stuff.
		this.canCollide = false;
		this.setParticleTextureIndex(48);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ){
		float f6 = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
		this.particleScale = this.flameScale * (1.0F - f6 * f6 * 0.5F);
		super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public int getBrightnessForRender(float par1){
		return 256;
	}
}
