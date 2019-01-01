package electroblob.wizardry.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleMagicFlame extends ParticleWizardry {

	/** The scale of the flame particle */
	private float flameScale;

	public ParticleMagicFlame(World world, double x, double y, double z){
		super(world, x, y, z);
		this.flameScale = 1 + world.rand.nextFloat();
		this.setRBGColorF(1, 1, 1);
		this.particleAlpha = 1;
		this.particleMaxAge = (int)(2.0D / (Math.random() * 0.8D + 0.2D));
		this.shaded = false;
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
	
	@Override
	public Particle multipleParticleScaleBy(float scale){
		this.flameScale *= scale;
		return super.multipleParticleScaleBy(scale);
	}
}
