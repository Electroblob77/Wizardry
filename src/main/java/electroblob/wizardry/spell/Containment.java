package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Containment extends SpellRay {

	public Containment(){
		super("containment", false, SpellActions.POINT);
		this.soundValues(1, 1, 0.2f);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){
			((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.containment,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
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
		ParticleBuilder.create(Type.PATH).pos(x, y, z).clr(0x7988cc).time(20 + world.rand.nextInt(8)).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(1f, 1f, 1f).spawn(world);
	}

}
