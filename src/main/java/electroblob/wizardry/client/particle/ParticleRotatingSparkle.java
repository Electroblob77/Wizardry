package electroblob.wizardry.client.particle;

import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleRotatingSparkle extends ParticleSparkle {

	private double angle;
	private double radius;
	private double speed;

	public ParticleRotatingSparkle(World world, double ox, double y, double oz){
		super(world, ox, y, oz);
		this.angle = this.rand.nextDouble() * Math.PI * 2;
		double x = ox - Math.cos(angle) * radius;
		double z = oz + radius * Math.sin(angle);
		this.radius = 1; // TODO: Find a suitable default value
		this.setPosition(x, y, z);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		if(rand.nextBoolean()){
			speed = rand.nextDouble() * 2 + 1;
		}else{
			speed = rand.nextDouble() * -2 - 1;
		}
		this.multipleParticleScaleBy(1.5f);
	}

	@Override
	public void onUpdate(){

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge){
			this.setExpired();
		}

		// This is in radians per tick...
		double omega = Math.signum(speed) * ((Math.PI * 2) / 20 - speed / (20 * radius));

		// v = r times omega; therefore the normalised velocity vector needs to be r times the angle increment / 2 pi.
		this.angle += omega;

		this.motionY -= 0.04D * (double)this.particleGravity;
		this.motionZ = radius * omega * Math.cos(angle);
		this.motionX = radius * omega * Math.sin(angle);
		this.move(motionX, motionY, motionZ);

		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(
					1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
		}

	}
}
