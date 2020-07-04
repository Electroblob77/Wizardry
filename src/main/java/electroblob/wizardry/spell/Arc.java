package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Arc extends SpellRay {

	public Arc(){
		super("arc", SpellActions.POINT, false);
		this.aimAssist(0.6f);
		this.soundValues(1, 1.7f, 0.2f);
		this.addProperties(DAMAGE);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
		
			if(world.isRemote){
				// Rather neatly, the entity can be set here and if it's null nothing will happen.
				ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
				ParticleBuilder.spawnShockParticles(world, target.posX, target.posY + target.height/2, target.posZ);
			}
	
			// This is a lot neater than it was, thanks to the damage type system.
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
