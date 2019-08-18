package electroblob.wizardry.entity.construct;

import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityArrowRain extends EntityMagicConstruct {

	public EntityArrowRain(World world){
		super(world);
		this.height = 3.0f;
		this.width = 5.0f;
	}

	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){
			EntityTippedArrow arrow = new EntityTippedArrow(world, this.posX + rand.nextDouble() * 6 - 3,
					this.posY + rand.nextDouble() * 4 - 2, this.posZ + rand.nextDouble() * 6 - 3);
			arrow.motionX = MathHelper.cos((float)Math.toRadians(this.rotationYaw + 90));
			arrow.motionY = -0.6;
			arrow.motionZ = MathHelper.sin((float)Math.toRadians(this.rotationYaw + 90));
			arrow.shootingEntity = this.getCaster();
			arrow.setDamage(7.0d * damageMultiplier);
			arrow.setPotionEffect(new ItemStack(Items.ARROW));
			this.world.spawnEntity(arrow);
		}
	}

}
