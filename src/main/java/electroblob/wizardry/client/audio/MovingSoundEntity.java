package electroblob.wizardry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

// Copied from MovingSoundMinecart; if it ever breaks between updates take a look at that.
//@SideOnly(Side.CLIENT)
public class MovingSoundEntity<T extends Entity> extends MovingSound {
	
	protected final T source;
	protected float distance = 0.0F;

	public MovingSoundEntity(T entity, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat){
		super(sound, category);
		this.source = entity;
		this.repeat = repeat;
		this.volume = volume;
		this.pitch = pitch;
		this.repeatDelay = 0;
	}

	@Override
	public void update(){
		
		if(this.source.isDead){
			this.donePlaying = true;
		}else{
			this.xPosF = (float)this.source.posX;
			this.yPosF = (float)this.source.posY;
			this.zPosF = (float)this.source.posZ;
		}
	}
}