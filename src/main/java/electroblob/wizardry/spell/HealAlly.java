package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HealAlly extends SpellRay {

	public HealAlly(){
		super("heal_ally", Tier.APPRENTICE, Element.HEALING, SpellType.DEFENCE, 10, 20, false, 10, WizardrySounds.SPELL_HEAL);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			EntityLivingBase entity = (EntityLivingBase)target;
			
			if(entity.getHealth() < entity.getMaxHealth() && entity.getHealth() > 0){
				
				entity.heal((int)(5 * modifiers.get(SpellModifiers.POTENCY)));

				if(world.isRemote){
					
					float r = 1; float g = 1; float b = 0.3f;
					
					for(int i = 0; i < 10; i++){
						double x1 = (double)((float)entity.posX + world.rand.nextFloat() * 2 - 1.0f);
						double y1 = (double)((float)entity.getEntityBoundingBox().minY + entity.getEyeHeight() - 0.5f + world.rand.nextFloat());
						double z1 = (double)((float)entity.posZ + world.rand.nextFloat() * 2 - 1.0f);
						ParticleBuilder.create(Type.SPARKLE).pos(x1, y1, z1).vel(0, 0.1F, 0).clr(r, g, b).spawn(world);
					}

					ParticleBuilder.create(Type.BUFF).entity(caster).clr(r, g, b).spawn(world);
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
