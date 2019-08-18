package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Decay extends SpellConstructRanged<EntityDecay> {

	public static final String DECAY_PATCHES_SPAWNED = "decay_patches_spawned";

	public Decay(){
		super("decay", EntityDecay::new, false);
		this.soundValues(1, 1.1f, 0.1f);
		this.floor(true);
		this.overlap(true);
		addProperties(DECAY_PATCHES_SPAWNED, EFFECT_DURATION);
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(world.getBlockState(new BlockPos(x, y, z)).isNormalCube()) return false;
		
		super.spawnConstruct(world, x, y, z, side, caster, modifiers);

		float decayCount = getProperty(DECAY_PATCHES_SPAWNED).floatValue();
		int quantity = (int)(decayCount * modifiers.get(WizardryItems.blast_upgrade));
		// If there are more decay patches, they need more space to spawn in
		int horizontalRange = (int)(0.4 * decayCount * modifiers.get(WizardryItems.blast_upgrade));
		int verticalRange = (int)(6 * modifiers.get(WizardryItems.blast_upgrade));

		for(int i=0; i<quantity; i++){
			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, horizontalRange, verticalRange);
			if(pos == null) break;
			super.spawnConstruct(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, side, caster, modifiers);
		}
		
		return true;
	}

}
