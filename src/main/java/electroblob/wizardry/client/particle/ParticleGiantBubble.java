package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleGiantBubble extends Particle {
	/**
	 * The name used to identify this particle. Uses the mod id to avoid any possible conflicts (Not that there would be
	 * any, but I may as well.)
	 */
	public static final String NAME = Wizardry.MODID + "magicbubble";

	public ParticleGiantBubble(World par1World, double par2, double par4, double par6, double par8, double par10,
			double par12){
		super(par1World, par2, par4, par6, par8, par10, par12);
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.setParticleTextureIndex(32);
		this.setSize(0.02F, 0.02F);
		this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
		this.motionX = par8 * 0.20000000298023224D + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
		this.motionY = par10 * 0.20000000298023224D + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
		this.motionZ = par12 * 0.20000000298023224D + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY += 0.002D;
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.8500000238418579D;
		this.motionY *= 0.8500000238418579D;
		this.motionZ *= 0.8500000238418579D;

		if(this.particleMaxAge-- <= 0){
			this.setExpired();
		}
	}
}
