package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import java.util.List;

public class PlagueOfDarkness extends Spell {

	public PlagueOfDarkness(){
		super("plague_of_darkness", EnumAction.BOW, false);
		addProperties(EFFECT_RADIUS, DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(radius, caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){
			if(AllyDesignationSystem.isValidTarget(caster, target)
					&& !MagicDamage.isEntityImmune(DamageType.WITHER, target)){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
				target.addPotionEffect(new PotionEffect(MobEffects.WITHER,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
			}
		}

		if(world.isRemote){
			
			double particleX, particleZ;
			
			for(int i = 0; i < 40 * modifiers.get(WizardryItems.blast_upgrade); i++){
				
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				ParticleBuilder.create(Type.DARK_MAGIC).pos(particleX, caster.getEntityBoundingBox().minY, particleZ)
				.vel(particleX - caster.posX, 0, particleZ - caster.posZ).clr(0.1f, 0, 0).spawn(world);
				
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				ParticleBuilder.create(Type.SPARKLE).pos(particleX, caster.getEntityBoundingBox().minY, particleZ)
				.vel(particleX - caster.posX, 0, particleZ - caster.posZ).time(30).clr(0.1f, 0, 0.05f).spawn(world);
				
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);

				if(block != null){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY,
							particleZ, particleX - caster.posX, 0, particleZ - caster.posZ, Block.getStateId(block));
				}
			}

			ParticleBuilder.create(Type.SPHERE)
					.pos(caster.posX, caster.getEntityBoundingBox().minY + 0.1, caster.posZ)
					.scale((float)radius * 0.8f)
					.clr(0.8f, 0, 0.05f)
					.spawn(world);
		}
		
		caster.swingArm(hand);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
