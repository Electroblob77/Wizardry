package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class PlagueOfDarkness extends Spell {

	public PlagueOfDarkness() {
		super(EnumTier.MASTER, 75, EnumElement.NECROMANCY, "plague_of_darkness", EnumSpellType.ATTACK, 200, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(5.0d*blastMultiplier, caster.posX, caster.posY, caster.posZ, world);
		
		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target) && !MagicDamage.isEntityImmune(DamageType.WITHER, target)){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER), 8.0f * damageMultiplier);
				target.addPotionEffect(new PotionEffect(Potion.wither.id, (int)(140*durationMultiplier), 2));
			}
		}
		if(world.isRemote){
			double particleX, particleZ;
			for(int i=0;i<40*blastMultiplier;i++){
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ,
						particleX - caster.posX, 0, particleZ - caster.posZ, 0, 0.1f, 0.0f, 0.0f);
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ,
						particleX - caster.posX, 0, particleZ - caster.posZ, 30, 0.1f, 0.0f, 0.05f);
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				
				Block block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
				// Player actual eye height is 1.62, client is 1.5 too high, hence the -1.5.
				if(block != null){
					Wizardry.proxy.spawnDigParticle(world, particleX, caster.posY - 1.5, particleZ, particleX - caster.posX, 0, particleZ - caster.posZ,
							block);
				}
			}
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "mob.wither.death", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
