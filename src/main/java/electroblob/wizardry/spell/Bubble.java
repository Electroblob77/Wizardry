package electroblob.wizardry.spell;

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
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Bubble extends SpellRay {

	public Bubble(){
		super("bubble", false, EnumAction.NONE);
		this.soundValues(0.5f, 1.1f, 0.2f);
		addProperties(DURATION);
	}
	
	@Override
	protected SoundEvent[] createSounds(){
		return this.createSoundsWithSuffixes("shoot", "splash");
	}
 
	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			if(!world.isRemote){
				// Deals a small amount damage so the target counts as being hit by the caster
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1);
				
				EntityBubble bubble = new EntityBubble(world);
				bubble.setPosition(target.posX, target.posY, target.posZ);
				bubble.setCaster(caster);
				bubble.lifetime = ((int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				bubble.isDarkOrb = false;
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
		world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, 0, 0, 0);
		ParticleBuilder.create(Type.MAGIC_BUBBLE).pos(x, y, z).spawn(world);
	}

}
