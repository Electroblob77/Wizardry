package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Invisibility extends Spell {

	public Invisibility(){
		super(Tier.ADVANCED, 35, Element.SORCERY, "invisibility", SpellType.UTILITY, 200, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY,
				(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0, false, false));

		if(world.isRemote){
			for(int i = 0; i < 10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
				Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
						48 + world.rand.nextInt(12), 0.7f, 1.0f, 1.0f);
			}
		}
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
				world.rand.nextFloat() * 0.4F + 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(!caster.isPotionActive(MobEffects.INVISIBILITY)){

			caster.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY,
					(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0, false, false));

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double y1 = (double)((float)caster.posY + caster.getEyeHeight() - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 0.7f, 1.0f, 1.0f);
				}
			}
			caster.playSound(WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;

		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
