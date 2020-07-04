package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityBoulder;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.Vec3d;

public class Boulder extends SpellConstruct<EntityBoulder> {

	public static final String SPEED = "speed";
	public static final String KNOCKBACK_STRENGTH = "knockback_strength";

	public Boulder(){
		super("boulder", SpellActions.SUMMON, EntityBoulder::new, false);
		addProperties(SPEED, DAMAGE, KNOCKBACK_STRENGTH);
	}

	@Override
	protected void addConstructExtras(EntityBoulder construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		float speed = getProperty(SPEED).floatValue();
		// Unlike tornado, boulder always has the same speed
		Vec3d direction = caster == null ? new Vec3d(side.getDirectionVec())
				: GeometryUtils.replaceComponent(caster.getLookVec(), Axis.Y, 0).normalize();
		construct.setHorizontalVelocity(direction.x * speed, direction.z * speed);
		construct.rotationYaw = caster == null ? side.getHorizontalAngle() : caster.rotationYaw;
		double yOffset = caster == null ? 0 : 1.6;
		construct.setPosition(construct.posX + direction.x, construct.posY + yOffset, construct.posZ + direction.z);
	}

}
