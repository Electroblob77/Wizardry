package electroblob.wizardry.spell;

import electroblob.wizardry.entity.living.EntityHuskMinion;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class SummonZombie extends SpellMinion<EntityZombieMinion> {

	public SummonZombie(){
		super("summon_zombie", EntityZombieMinion::new);
		this.soundValues(7, 0.6f, 0);
	}

	@Override
	protected EntityZombieMinion createMinion(World world, EntityLivingBase caster, SpellModifiers modifiers){
		if(caster instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)caster, WizardryItems.charm_minion_variants)){
			return new EntityHuskMinion(world);
		}else{
			return super.createMinion(world, caster, modifiers);
		}
	}

}
