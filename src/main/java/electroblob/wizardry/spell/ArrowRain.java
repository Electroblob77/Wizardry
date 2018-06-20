package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityArrowRain;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class ArrowRain extends SpellConstructRanged<EntityArrowRain> {

	public ArrowRain(){
		super("arrow_rain", Tier.MASTER, Element.SORCERY, SpellType.ATTACK, 75, 300, EntityArrowRain::new, 120, 20, WizardrySounds.SPELL_SUMMONING);
		this.floor(true);
	}
	
	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EntityLivingBase caster, SpellModifiers modifiers){
		
		// Moves the entity back towards the caster a bit, so the area of effect is better centred on the position.
		// 3 is the distance to move the entity back towards the caster.
		double dx = caster.posX - x;
		double dz = caster.posZ - z;
		double distRatio = 3 / Math.sqrt(dx * dx + dz * dz);
		x += dx * distRatio;
		z += dz * distRatio;
		// Moves the entity up 5 blocks so that it is above mobs' heads.
		y += 5;
		
		return super.spawnConstruct(world, x, y, z, caster, modifiers);
	}
	
	@Override
	protected void addConstructExtras(EntityArrowRain construct, EntityLivingBase caster, SpellModifiers modifiers){
		// Makes the arrows shoot in the direction the caster was looking when they cast the spell.
		construct.rotationYaw = caster.rotationYawHead;
	}

}
