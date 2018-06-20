package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Entrapment extends SpellRay {

	public Entrapment(){
		super("entrapment", Tier.ADVANCED, Element.NECROMANCY, SpellType.ATTACK, 35, 75, false, 10, SoundEvents.ENTITY_WITHER_SHOOT);
		this.soundValues(1, 0.85f, 0.3f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			if(!world.isRemote){
				// Deals a small amount damage so the target counts as being hit by the caster
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1);
				
				EntityBubble bubble = new EntityBubble(world);
				bubble.setPosition(target.posX, target.posY, target.posZ);
				bubble.setCaster(caster);
				bubble.lifetime = ((int)(200 * modifiers.get(WizardryItems.duration_upgrade)));
				bubble.isDarkOrb = true;
				bubble.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				
				world.spawnEntity(bubble);
				target.startRiding(bubble);
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.PORTAL, x, y - 0.5, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).colour(0.1f, 0, 0).spawn(world);
	}

}
