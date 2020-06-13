package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Entrapment extends SpellRay {

	public static final String DAMAGE_INTERVAL = "damage_interval";

	public Entrapment(){
		super("entrapment", false, SpellActions.POINT);
		this.soundValues(1, 0.85f, 0.3f);
		addProperties(EFFECT_DURATION, DAMAGE_INTERVAL);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			
			if(!world.isRemote){
				// Deals a small amount damage so the target counts as being hit by the caster
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1);
				
				EntityBubble bubble = new EntityBubble(world);
				bubble.setPosition(target.posX, target.posY, target.posZ);
				bubble.setCaster(caster);
				bubble.lifetime = ((int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				bubble.isDarkOrb = true;
				bubble.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				
				world.spawnEntity(bubble);
				target.startRiding(bubble);
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.PORTAL, x, y - 0.5, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
	}

}
