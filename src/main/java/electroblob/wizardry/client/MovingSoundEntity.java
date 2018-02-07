package electroblob.wizardry.client;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// Copied from MovingSoundMinecart; if it ever breaks between updates take a look at that.
@SideOnly(Side.CLIENT)
public class MovingSoundEntity extends MovingSound {
	private final Entity source;
	private float distance = 0.0F;

	public MovingSoundEntity(Entity entity, SoundEvent sound, float volume, float pitch, boolean repeat){
		// Uses BLOCKS because that's the closest thing to inanimate entities. Could use NEUTRAL like
		// MovingSoundMinecart.
		super(sound, SoundCategory.BLOCKS);
		this.source = entity;
		this.repeat = repeat;
		this.volume = volume;
		this.pitch = pitch;
		this.repeatDelay = 0;
	}

	/**
	 * Updates the JList with a new model.
	 */
	@Override
	public void update(){
		if(this.source.isDead && repeat){
			this.donePlaying = true;
		}else{
			this.xPosF = (float)this.source.posX;
			this.yPosF = (float)this.source.posY;
			this.zPosF = (float)this.source.posZ;
			float f = MathHelper.sqrt(this.source.motionX * this.source.motionX
					+ this.source.motionY * this.source.motionY + this.source.motionZ * this.source.motionZ);

			// Is this something to do with the Doppler effect?
			if((double)f >= 0.01D){
				this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
				this.volume = 0.0F + MathHelper.clamp(f, 0.0F, 0.5F) * 0.7F;
			}else{
				// this.pitch = 0.0F;
				// this.volume = 0.0F;
			}
		}
	}
}