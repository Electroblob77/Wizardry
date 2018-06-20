package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LightningHammer extends SpellConstructRanged<EntityHammer> {

	public LightningHammer(){
		super("lightning_hammer", Tier.MASTER, Element.LIGHTNING, SpellType.ATTACK, 100, 300, EntityHammer::new, 600, 40, WizardrySounds.SPELL_SUMMONING);
		this.soundValues(3, 1, 0);
		this.floor(true);
		this.overlap(true);
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EntityLivingBase caster, SpellModifiers modifiers){
		// TESTME: Does this need to be one block higher?
		if(!world.canBlockSeeSky(new BlockPos(x, y, z))) return false;
		return super.spawnConstruct(world, x, y + 50, z, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityHammer construct, EntityLivingBase caster, SpellModifiers modifiers){
		construct.motionY = -2;
	}

}
