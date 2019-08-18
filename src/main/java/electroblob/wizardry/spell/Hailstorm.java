package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class Hailstorm extends SpellConstructRanged<EntityHailstorm> {

	public Hailstorm(){
		super("hailstorm", EntityHailstorm::new, false);
		this.floor(true);
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){

		// Moves the entity back towards the caster a bit, so the area of effect is better centred on the position.
		// 3 is the distance to move the entity back towards the caster.
		double dx = caster.posX - x;
		double dz = caster.posZ - z;
		double distRatio = 3 / Math.sqrt(dx * dx + dz * dz);
		x += dx * distRatio;
		z += dz * distRatio;
		// Moves the entity up 5 blocks so that it is above mobs' heads.
		y += 5;

		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityHailstorm construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		// Makes the arrows shoot in the direction the caster was looking when they cast the spell.
		if(caster != null) construct.rotationYaw = caster.rotationYawHead;
	}

}
