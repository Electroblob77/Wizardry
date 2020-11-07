package electroblob.wizardry.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Copied from ParticleFirework.Overlay; for some reason that class has no public constructors, plus I want to change the
 * scale and a few other things
 * @author Electroblob
 * @since Wizardry 4.2.0
 */
public class ParticleFlash extends ParticleWizardry {

	public ParticleFlash(World world, double x, double y, double z){
		super(world, x, y, z);
		this.setRBGColorF(1, 1, 1);
		this.particleScale = 0.6f; // 7.1f is the value used in fireworks
		this.particleMaxAge = 6;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true; // Well this fixes everything... let's hope it doesn't cause any side-effects!
	}

	@Override
    public void drawParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		float f4 = particleScale * MathHelper.sin(((float)this.particleAge + partialTicks - 1.0F)/particleMaxAge * (float)Math.PI);
        this.setAlphaF(0.6F - ((float)this.particleAge + partialTicks - 1.0F)/particleMaxAge * 0.5F);
        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        int i = this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        buffer.pos(f5 - rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 - rotationYZ * f4 - rotationXZ * f4).tex(0.5D, 0.375D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 - rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 - rotationYZ * f4 + rotationXZ * f4).tex(0.5D, 0.125D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + rotationX * f4 + rotationXY * f4, f6 + rotationZ * f4, f7 + rotationYZ * f4 + rotationXZ * f4).tex(0.25D, 0.125D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f5 + rotationX * f4 - rotationXY * f4, f6 - rotationZ * f4, f7 + rotationYZ * f4 - rotationXZ * f4).tex(0.25D, 0.375D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
	}

}
