package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Decay extends SpellConstructRanged<EntityDecay> {
	
	private static final int BASE_SPAWN_COUNT = 5;

	public Decay(){
		super("decay", Tier.ADVANCED, Element.NECROMANCY, SpellType.ATTACK, 50, 200, EntityDecay::new, 400, 12, SoundEvents.ENTITY_WITHER_SHOOT);
		this.soundValues(1, 1.1f, 0.1f);
		this.floor(true);
		this.overlap(true);
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(world.getBlockState(new BlockPos(x, y, z)).isNormalCube()) return false;
		
		super.spawnConstruct(world, x, y, z, caster, modifiers);
		
		int quantity = (int)(BASE_SPAWN_COUNT * modifiers.get(WizardryItems.blast_upgrade));
		int horizontalRange = (int)(2 * modifiers.get(WizardryItems.blast_upgrade));
		int verticalRange = (int)(6 * modifiers.get(WizardryItems.blast_upgrade));

		for(int i=0; i<quantity; i++){
			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, horizontalRange, verticalRange);
			if(pos == null) break;
			super.spawnConstruct(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, caster, modifiers);
		}
		
		return true;
	}

}
