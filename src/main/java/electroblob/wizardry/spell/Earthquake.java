 package electroblob.wizardry.spell;

 import electroblob.wizardry.Wizardry;
 import electroblob.wizardry.entity.construct.EntityEarthquake;
 import electroblob.wizardry.item.SpellActions;
 import electroblob.wizardry.registry.WizardryItems;
 import electroblob.wizardry.util.SpellModifiers;
 import electroblob.wizardry.util.WizardryUtilities;
 import net.minecraft.block.Block;
 import net.minecraft.block.state.IBlockState;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.EnumFacing;
 import net.minecraft.util.EnumParticleTypes;
 import net.minecraft.world.World;

 public class Earthquake extends SpellConstruct<EntityEarthquake> {

	public static final String SPREAD_SPEED = "spread_speed";

	public Earthquake(){
		super("earthquake", SpellActions.POINT_DOWN, EntityEarthquake::new, true);
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

			world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, x, y + 0.1, z, 0, 0, 0);

			double particleX, particleZ;

			for(int i=0; i<40; i++){

				particleX = x - 1.0d + 2 * world.rand.nextDouble();
				particleZ = z - 1.0d + 2 * world.rand.nextDouble();

				IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, y,
						particleZ, particleX - x, 0, particleZ - z, Block.getStateId(block));
			}

			WizardryUtilities.getEntitiesWithinRadius(15, x, y, z, world, EntityPlayer.class)
					.forEach(p -> Wizardry.proxy.shakeScreen(p, 12));

		}
		
		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

}
