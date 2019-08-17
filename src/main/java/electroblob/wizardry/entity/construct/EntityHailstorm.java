package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityIceShard;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityHailstorm extends EntityMagicConstruct {

	public EntityHailstorm(World world){
		super(world);
		this.height = 3.0f;
		this.width = 5.0f;
	}

	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){
			// System.out.println(this.rotationYaw);
			EntityIceShard iceshard = new EntityIceShard(world);
			iceshard.setPosition(this.posX + rand.nextDouble() * 6 - 3, this.posY + rand.nextDouble() * 4 - 2,
					this.posZ + rand.nextDouble() * 6 - 3);
			iceshard.motionX = MathHelper.cos((float)Math.toRadians(this.rotationYaw + 90));
			iceshard.motionY = -0.6;
			iceshard.motionZ = MathHelper.sin((float)Math.toRadians(this.rotationYaw + 90));
			iceshard.setCaster(this.getCaster());
			iceshard.damageMultiplier = this.damageMultiplier;
			this.world.spawnEntity(iceshard);
		}
	}

}
