package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LightningHammer extends SpellConstructRanged<EntityHammer> {

	public static final String ATTACK_INTERVAL = "attack_interval";
	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public LightningHammer(){
		super("lightning_hammer", EntityHammer::new, false);
		this.soundValues(3, 1, 0);
		this.floor(true);
		this.overlap(true);
		addProperties(EFFECT_RADIUS, SECONDARY_MAX_TARGETS, ATTACK_INTERVAL, DIRECT_DAMAGE, SPLASH_DAMAGE);
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		if(!world.canBlockSeeSky(new BlockPos(x, y, z))) return false;
		return super.spawnConstruct(world, x, y + 50, z, side, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityHammer construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		construct.motionY = -2;
	}

}
