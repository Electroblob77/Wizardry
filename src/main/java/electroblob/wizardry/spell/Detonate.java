package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Detonate extends SpellRay {
	
	private static final double BASE_RADIUS = 3;

	public Detonate(){
		super("detonate", Tier.ADVANCED, Element.FIRE, SpellType.ATTACK, 45, 50, false, 16, SoundEvents.ENTITY_GENERIC_EXPLODE);
		this.soundValues(4, 0.7f, 0.14f);
		this.ignoreEntities(true);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(!world.isRemote){
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
					BASE_RADIUS * modifiers.get(WizardryItems.blast_upgrade), pos.getX(), pos.getY(), pos.getZ(), world);
			
			for(EntityLivingBase target : targets){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
						// Damage decreases with distance but cannot be less than 0, naturally.
						Math.max(12.0f - (float)target.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
								* 4, 0) * modifiers.get(SpellModifiers.POTENCY));
			}
			
		}else{
			world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
		}
		
		return true;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
	}

}
