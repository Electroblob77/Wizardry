package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GreaterHeal extends Spell {

	public GreaterHeal(){
		super(Tier.ADVANCED, 15, Element.HEALING, "greater_heal", SpellType.DEFENCE, 40, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.shouldHeal()){
			caster.heal((int)(8 * modifiers.get(SpellModifiers.DAMAGE)));
			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double dx = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double dy = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F
							+ world.rand.nextFloat());
					double dz = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world, dx, dy, dz, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
				}
			}
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
					world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(caster.getHealth() < caster.getMaxHealth()){
			caster.heal((int)(8 * modifiers.get(SpellModifiers.DAMAGE)));
			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double dx = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double dy = (double)((float)caster.posY + caster.getEyeHeight() - 0.5F + world.rand.nextFloat());
					double dz = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world, dx, dy, dz, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
				}
			}
			caster.playSound(WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}

		return false;
	}

}
