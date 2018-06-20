package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class PlagueOfDarkness extends Spell {
	
	private static final double BASE_RADIUS = 5;
	private static final float BASE_DAMAGE = 8;
	private static final int BASE_DURATION = 140;

	public PlagueOfDarkness(){
		super("plague_of_darkness", Tier.MASTER, Element.NECROMANCY, SpellType.ATTACK, 75, 200, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
				BASE_RADIUS * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target)
					&& !MagicDamage.isEntityImmune(DamageType.WITHER, target)){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER),
						BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY));
				target.addPotionEffect(new PotionEffect(MobEffects.WITHER,
						(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)), 2));
			}
		}
		if(world.isRemote){
			
			double particleX, particleZ;
			
			for(int i = 0; i < 40 * modifiers.get(WizardryItems.blast_upgrade); i++){
				
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				ParticleBuilder.create(Type.DARK_MAGIC).pos(particleX, caster.getEntityBoundingBox().minY, particleZ)
				.vel(particleX - caster.posX, 0, particleZ - caster.posZ).colour(0.1f, 0, 0).spawn(world);
				
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				ParticleBuilder.create(Type.SPARKLE).pos(particleX, caster.getEntityBoundingBox().minY, particleZ)
				.vel(particleX - caster.posX, 0, particleZ - caster.posZ).lifetime(30).colour(0.1f, 0, 0.05f).spawn(world);
				
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);

				if(block != null){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY,
							particleZ, particleX - caster.posX, 0, particleZ - caster.posZ, Block.getStateId(block));
				}
			}
		}
		
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_DEATH, 1, 1 + 0.2f * world.rand.nextFloat());
		return true;
	}

}
