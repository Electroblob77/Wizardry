package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;

public class Tornado extends SpellConstruct<EntityTornado> {

	public static final String SPEED = "speed";
	public static final String UPWARD_ACCELERATION = "upward_acceleration";

	public Tornado(){
		super("tornado", EnumAction.NONE, EntityTornado::new, false);
		addProperties(EFFECT_RADIUS, SPEED, DAMAGE, UPWARD_ACCELERATION);
	}

	@Override
	protected void addConstructExtras(EntityTornado construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		float speed = getProperty(SPEED).floatValue();
		construct.setHorizontalVelocity(caster.getLookVec().x * speed, caster.getLookVec().z * speed);
	}

}
