package electroblob.wizardry.client.particle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

//@SideOnly(Side.CLIENT)
public class ParticleTornado extends ParticleDigging {

	private float angle;
	private double radius;
	private double speed;
	/** Velocity of the tornado itself; in other words the velocity of the point the particle circles around. */
	private double velX, velZ;
	private boolean fullBrightness = false;

	public ParticleTornado(World world, int maxAge, double originX, double originZ, double radius, double yPos,
			double velX, double velZ, IBlockState block){
		super(world, 0, 0, 0, 0, 0, 0, block);
		this.angle = this.rand.nextFloat() * (float)Math.PI * 2;
		double x = originX - MathHelper.cos(angle) * radius;
		double z = originZ + radius * MathHelper.sin(angle);
		this.radius = radius;
		this.setPosition(x, yPos, z);
		this.prevPosX = x;
		this.prevPosY = yPos;
		this.prevPosZ = z;
		// this.particleScale *= 0.75F;
		this.particleMaxAge = maxAge;
		this.canCollide = false;
		// Grass has special treatment, since it has a colourised top but the rest is normal.
		// Commented out for now since vanilla does something about this now, but I'm not sure what exactly
		// if(block.getBlock() != Blocks.GRASS || side == 1) this.setColour(block.getRenderColor(side));

		// Blocks that emit light are rendered with full brightness.
		if(block.getLightValue(world, new BlockPos(this.posX, this.posY, this.posZ)) == 0){
			this.particleRed *= 0.75;
			this.particleGreen *= 0.75;
			this.particleBlue *= 0.75;
		}else{
			this.fullBrightness = true;
		}

		speed = rand.nextDouble() * 2 + 1;
		this.velX = velX;
		this.velZ = velZ;
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

		this.motionZ = radius * omega * MathHelper.cos(angle);
		this.motionX = radius * omega * MathHelper.sin(angle);
		this.move(motionX + velX, 0, motionZ + velZ);

		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(
					1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
		}

	}

	@Override
	public int getBrightnessForRender(float partialTicks){
		return fullBrightness ? 15728880 : super.getBrightnessForRender(partialTicks);
	}

}
