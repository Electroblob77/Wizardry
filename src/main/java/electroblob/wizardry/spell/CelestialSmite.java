package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class CelestialSmite extends SpellRay {

	public CelestialSmite(){
		super("celestial_smite", SpellActions.POINT, false);
		addProperties(EFFECT_RADIUS, DAMAGE, BURN_DURATION);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, @Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, @Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(radius, hit.x, hit.y, hit.z, world);

		DamageSource source = caster == null ? DamageSource.MAGIC : MagicDamage.causeDirectMagicDamage(caster, DamageType.RADIANT);
		float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);

		for(EntityLivingBase target : targets){
			target.attackEntityFrom(source, damage);
			target.setFire(getProperty(BURN_DURATION).intValue());
		}

		if(world.isRemote){

			ParticleBuilder.create(Type.BEAM).pos(hit.x, world.getActualHeight(), hit.z).target(hit).scale(8)
			.clr(0xffbf00).time(10).spawn(world);
			ParticleBuilder.create(Type.SPHERE).pos(hit).scale(4).clr(0xfff098).spawn(world);

			if(side == EnumFacing.UP){
				Vec3d vec = hit.add(new Vec3d(side.getDirectionVec()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
				ParticleBuilder.create(Type.SCORCH).pos(vec).face(side).scale(3).spawn(world);
			}
		}

		return true;
	}

	@Override
	protected boolean onMiss(World world, @Nullable EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
}
