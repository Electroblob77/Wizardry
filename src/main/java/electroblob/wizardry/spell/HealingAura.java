package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class HealingAura extends Spell {

	public HealingAura(){
		super(Tier.ADVANCED, 35, Element.HEALING, "healing_aura", SpellType.DEFENCE, 150, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.onGround){
			if(!world.isRemote){
				EntityHealAura healaura = new EntityHealAura(world, caster.posX, caster.posY, caster.posZ, caster,
						(int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(healaura);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			if(caster.onGround
					&& world.getEntitiesWithinAABB(EntityHealAura.class, caster.getEntityBoundingBox()).isEmpty()){
				if(!world.isRemote){
					EntityHealAura healaura = new EntityHealAura(world, caster.posX, caster.posY, caster.posZ, caster,
							(int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
							modifiers.get(SpellModifiers.DAMAGE));
					world.spawnEntity(healaura);
				}
				return true;
			}
			return false;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
