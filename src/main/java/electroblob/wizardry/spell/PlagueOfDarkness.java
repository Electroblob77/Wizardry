package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
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

	public PlagueOfDarkness() {
		super(Tier.MASTER, 75, Element.NECROMANCY, "plague_of_darkness", SpellType.ATTACK, 200, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(5.0d*modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);
		
		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target) && !MagicDamage.isEntityImmune(DamageType.WITHER, target)){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER), 8.0f * modifiers.get(SpellModifiers.DAMAGE));
				target.addPotionEffect(new PotionEffect(MobEffects.WITHER, (int)(140*modifiers.get(WizardryItems.duration_upgrade)), 2));
			}
		}
		if(world.isRemote){
			double particleX, particleZ;
			for(int i=0;i<40*modifiers.get(WizardryItems.blast_upgrade);i++){
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ,
						particleX - caster.posX, 0, particleZ - caster.posZ, 0, 0.1f, 0.0f, 0.0f);
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ,
						particleX - caster.posX, 0, particleZ - caster.posZ, 30, 0.1f, 0.0f, 0.05f);
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				
				IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
				
				if(block != null){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY, particleZ,
							particleX - caster.posX, 0, particleZ - caster.posZ, Block.getStateId(block));
				}
			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_DEATH, 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
