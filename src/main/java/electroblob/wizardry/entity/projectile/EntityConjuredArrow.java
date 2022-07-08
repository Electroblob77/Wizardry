package electroblob.wizardry.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityConjuredArrow extends EntityArrow {

	public EntityConjuredArrow(World worldIn) {
		super(worldIn);
	}

	public EntityConjuredArrow(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	public EntityConjuredArrow(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.timeInGround > 400) {
			this.setDead();
		}
	}

	@Override
	protected ItemStack getArrowStack() {
		return ItemStack.EMPTY;
	}
}
