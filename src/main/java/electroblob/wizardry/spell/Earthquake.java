 package electroblob.wizardry.spell;

 import electroblob.wizardry.entity.construct.EntityEarthquake;
 import electroblob.wizardry.registry.WizardryItems;
 import electroblob.wizardry.util.SpellModifiers;
 import electroblob.wizardry.util.WizardryUtilities;
 import net.minecraft.block.Block;
 import net.minecraft.block.state.IBlockState;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.item.EnumAction;
 import net.minecraft.util.EnumFacing;
 import net.minecraft.util.EnumParticleTypes;
 import net.minecraft.world.World;

 public class Earthquake extends SpellConstruct<EntityEarthquake> {

	public static final String SPREAD_SPEED = "spread_speed";

	public Earthquake(){
		super("earthquake", EnumAction.NONE, EntityEarthquake::new, true);
		this.soundValues(2, 1, 0);
		this.overlap(true);
		this.floor(true);
		addProperties(EFFECT_RADIUS, SPREAD_SPEED);
	}
	
	// This one spawns particles
	@Override public boolean requiresPacket(){ return true; }
	
	@Override
	protected void addConstructExtras(EntityEarthquake construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		// Calculates the lifetime based on the base radius and spread speed
		// Also overwrites the -1 lifetime set due to permanent being true
		construct.lifetime = (int)(getProperty(EFFECT_RADIUS).floatValue()/getProperty(SPREAD_SPEED).floatValue()
				* modifiers.get(WizardryItems.blast_upgrade));
	}
	
	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(world.isRemote){

			world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, caster.posX,
					caster.getEntityBoundingBox().minY + 0.1, caster.posZ, 0, 0, 0);

			double particleX, particleZ;

			for(int i=0; i<40; i++){

				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();

				IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY,
						particleZ, particleX - caster.posX, 0, particleZ - caster.posZ,
						Block.getStateId(block));
			}
		}
		
		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

}
