package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityArrowRain extends EntityScaledConstruct {

	public EntityArrowRain(World world){
		super(world);
		setSize(Spells.arrow_rain.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 5);
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

			EntityTippedArrow arrow = new EntityTippedArrow(world, x, y, z);

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
