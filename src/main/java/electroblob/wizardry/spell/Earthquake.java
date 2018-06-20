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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Earthquake extends SpellConstruct<EntityEarthquake> {

	public Earthquake(){
		super("earthquake", Tier.MASTER, Element.EARTH, SpellType.ATTACK, 75, 250, EnumAction.NONE, EntityEarthquake::new, -1, WizardrySounds.SPELL_EARTHQUAKE);
		this.soundValues(2, 1, 0);
		this.overlap(true);
		this.floor(true);
	}
	
	// This one spawns particles
	@Override public boolean doesSpellRequirePacket(){ return true; }
	
	@Override
	protected void addConstructExtras(EntityEarthquake construct, EntityLivingBase caster, SpellModifiers modifiers){
		construct.lifetime = (int)(20 * modifiers.get(WizardryItems.blast_upgrade));
	}
	
	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EntityLivingBase caster, SpellModifiers modifiers){
		
		// TODO: Couldn't this be moved to EntityEarthquake?
		if(world.isRemote){

			world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, caster.posX,
					caster.getEntityBoundingBox().minY + 0.1, caster.posZ, 0, 0, 0);

			double particleX, particleZ;

			for(int i=0; i<40; i++){

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
		
		return super.spawnConstruct(world, x, y, z, caster, modifiers);
	}

}
