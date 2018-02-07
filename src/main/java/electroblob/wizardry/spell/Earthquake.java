package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityEarthquake;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Earthquake extends Spell {

	public Earthquake(){
		super(Tier.MASTER, 75, Element.EARTH, "earthquake", SpellType.ATTACK, 250, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.onGround){

			if(!world.isRemote){
				world.spawnEntity(new EntityEarthquake(world, caster.posX, caster.getEntityBoundingBox().minY,
						caster.posZ, caster, (int)(20 * modifiers.get(WizardryItems.blast_upgrade)),
						modifiers.get(SpellModifiers.DAMAGE)));
			}else{

				world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, caster.posX,
						caster.getEntityBoundingBox().minY + 0.1, caster.posZ, 0, 0, 0);

				double particleX, particleZ;

				for(int i = 0; i < 40; i++){

					particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
					particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();

					IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
					if(block != null){
						world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY,
								particleZ, particleX - caster.posX, 0, particleZ - caster.posZ,
								Block.getStateId(block));
					}
				}
			}

			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_EARTHQUAKE, 2, 1);
			caster.swingArm(hand);

			return true;
		}
		return false;
	}

}
