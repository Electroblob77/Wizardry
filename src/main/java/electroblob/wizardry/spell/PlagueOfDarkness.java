package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class PlagueOfDarkness extends SpellAreaEffect {

	public PlagueOfDarkness(){
		super("plague_of_darkness", SpellActions.POINT_DOWN, false);
		this.alwaysSucceed(true);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(!MagicDamage.isEntityImmune(DamageType.WITHER, target)){
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER),
					getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			target.addPotionEffect(new PotionEffect(MobEffects.WITHER,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		double particleX, particleZ;

		for(int i = 0; i < 40 * modifiers.get(WizardryItems.blast_upgrade); i++){

			particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();
			ParticleBuilder.create(Type.DARK_MAGIC).pos(particleX, origin.y, particleZ)
					.vel(particleX - origin.x, 0, particleZ - origin.z).clr(0.1f, 0, 0).spawn(world);

			particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();
			ParticleBuilder.create(Type.SPARKLE).pos(particleX, origin.y, particleZ)
					.vel(particleX - origin.x, 0, particleZ - origin.z).time(30).clr(0.1f, 0, 0.05f).spawn(world);

			particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();

			IBlockState block = world.getBlockState(new BlockPos(origin.x, origin.y - 0.5, origin.z));

			if(block != null){
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, origin.y,
						particleZ, particleX - origin.x, 0, particleZ - origin.z, Block.getStateId(block));
			}
		}

		ParticleBuilder.create(Type.SPHERE).pos(origin.add(0, 0.1, 0)).scale((float)radius * 0.8f).clr(0.8f, 0, 0.05f).spawn(world);
	}

}
