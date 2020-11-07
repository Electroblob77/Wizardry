package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Detonate extends SpellRay {

	// More descriptive/accurate than just "damage"
	public static final String MAX_DAMAGE = "max_damage";

	public Detonate(){
		super("detonate", SpellActions.POINT, false);
		this.soundValues(4, 0.7f, 0.14f);
		this.ignoreLivingEntities(true);
		addProperties(MAX_DAMAGE, BLAST_RADIUS);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(!world.isRemote){
			
			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(getProperty(BLAST_RADIUS).doubleValue()
					* modifiers.get(WizardryItems.blast_upgrade), pos.getX(), pos.getY(), pos.getZ(), world);
			
			for(EntityLivingBase target : targets){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
						// Damage decreases with distance but cannot be less than 0, naturally.
						Math.max(getProperty(MAX_DAMAGE).floatValue() - (float)target.getDistance(pos.getX() + 0.5,
								pos.getY() + 0.5, pos.getZ() + 0.5) * 4, 0) * modifiers.get(SpellModifiers.POTENCY));
			}
			
		}else{
			world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
		}
		
		return true;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
	}

}
