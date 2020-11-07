package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityHailstorm extends EntityScaledConstruct {

	public EntityHailstorm(World world){
		super(world);
		setSize(Spells.hailstorm.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			double x = posX + (world.rand.nextDouble() - 0.5D) * (double)width;
			double y = posY + world.rand.nextDouble() * (double)height;
			double z = posZ + (world.rand.nextDouble() - 0.5D) * (double)width;

			EntityIceShard iceshard = new EntityIceShard(world);
			iceshard.setPosition(x, y, z);

			iceshard.motionX = MathHelper.cos((float)Math.toRadians(this.rotationYaw + 90));
			iceshard.motionY = -0.6;
			iceshard.motionZ = MathHelper.sin((float)Math.toRadians(this.rotationYaw + 90));

			iceshard.setCaster(this.getCaster());
			iceshard.damageMultiplier = this.damageMultiplier;

			this.world.spawnEntity(iceshard);
		}
	}

}
