package electroblob.wizardry.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDust extends ParticleWizardry {

	public ParticleDust(World world, double x, double y, double z){
		super(world, x, y, z);
		
		this.setParticleTextureIndex(0);
		this.setSize(0.01F, 0.01F);

		// Defaults
		this.particleScale *= this.rand.nextFloat() + 0.2F;
		this.particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
		this.setRBGColorF(1, 1, 1);
	}

	@Override
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		// this.moveEntity(this.motionX, this.motionY, this.motionZ);

		if(this.particleMaxAge-- <= 0){
			this.setExpired();
		}
	}
}
