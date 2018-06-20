package electroblob.wizardry.entity.construct;

import net.minecraft.world.World;

@Deprecated // This needs changing into a particle
public class EntityLightningPulse extends EntityMagicConstruct {

	public EntityLightningPulse(World world){
		super(world);
		this.setSize(6, 0.2f);
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
