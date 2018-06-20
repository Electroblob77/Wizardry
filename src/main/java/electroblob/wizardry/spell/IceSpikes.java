package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class IceSpikes extends SpellConstructRanged<EntityIceSpike> {
	
	private static final int BASE_SPAWN_COUNT = 17; // Was 18, reduced to 17 to account for the extra one in the middle.

	public IceSpikes(){
		// Base duration is set to -1 so that the superclass doesn't waste time retrieving the duration multiplier
		super("ice_spikes", Tier.ADVANCED, Element.ICE, SpellType.ATTACK, 30, 75, EntityIceSpike::new, -1, 20, WizardrySounds.SPELL_ICE);
	}
	
	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(world.getBlockState(new BlockPos(x, y, z)).isNormalCube()) return false;
		
		// Now always spawns a spike exactly at the position aimed at
		super.spawnConstruct(world, x, y-1, z, caster, modifiers);
		
		int quantity = (int)(BASE_SPAWN_COUNT * modifiers.get(WizardryItems.blast_upgrade));

		for(int i=0; i<quantity; i++){
			
			float angle = (float)(world.rand.nextFloat() * Math.PI * 2);
			double radius = 0.5 + world.rand.nextDouble() * 2 * modifiers.get(WizardryItems.blast_upgrade);

			double x1 = x + radius * MathHelper.sin(angle);
			double z1 = z + radius * MathHelper.cos(angle);
			double y1 = WizardryUtilities.getNearestFloorLevel(world,
					new BlockPos(MathHelper.floor(x1), (int)y, MathHelper.floor(z1)), 2) - 1;

			if(y1 > -1){
				super.spawnConstruct(world, x1, y1, z1, caster, modifiers);
			}
		}
		
		return true;
	}
	
	@Override
	protected void addConstructExtras(EntityIceSpike construct, EntityLivingBase caster, SpellModifiers modifiers){
		// In this particular case, lifetime is implemented as a delay instead so is treated differently.
		construct.lifetime = 30 + construct.world.rand.nextInt(15);
	}

}
