package electroblob.wizardry.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class MovingSoundEntity extends MovingSound
{
    private final Entity source;
    private float pitch = 0.0F;

    public MovingSoundEntity(Entity entity, String soundName, float volume, float pitch, boolean repeat)
    {
        super(new ResourceLocation(soundName));
        this.source = entity;
        this.repeat = repeat;
        this.volume = volume;
        this.field_147663_c = pitch;
        this.field_147665_h = 0;
    }

    /**
     * Updates the JList with a new model.
     */
    public void update()
    {
        if (this.source.isDead && repeat)
        {
            this.donePlaying = true;
        }
        else
        {
            this.xPosF = (float)this.source.posX;
            this.yPosF = (float)this.source.posY;
            this.zPosF = (float)this.source.posZ;
            float f = MathHelper.sqrt_double(this.source.motionX * this.source.motionX + this.source.motionY * this.source.motionY + this.source.motionZ * this.source.motionZ);

            // Is this something to do with the Doppler effect?
            if ((double)f >= 0.01D)
            {
                this.pitch = MathHelper.clamp_float(this.pitch + 0.0025F, 0.0F, 1.0F);
                this.volume = 0.0F + MathHelper.clamp_float(f, 0.0F, 0.5F) * 0.7F;
            }
            else
            {
                //this.pitch = 0.0F;
                //this.volume = 0.0F;
            }
        }
    }
}