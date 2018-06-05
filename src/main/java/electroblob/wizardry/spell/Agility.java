package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Agility extends Spell {

	public Agility(){
		super(Tier.APPRENTICE, 20, Element.SORCERY, "agility", SpellType.UTILITY, 40, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// 1.10 allows the particles to be completely hidden.
		caster.addPotionEffect(new PotionEffect(MobEffects.SPEED,
				(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 1, false, false));
		caster.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST,
				(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 1, false, false));

		if(world.isRemote){
			for(int i = 0; i < 10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
				ParticleBuilder.create(Type.SPARKLE).pos(x1, y1, z1).vel(0, 0.1F, 0)
				.lifetime(48 + world.rand.nextInt(12)).colour(0.4f, 1.0f, 0.8f).spawn(world);
			}
			Wizardry.proxy.spawnEntityParticle(world, caster, 15, 0.4f, 1.0f, 0.8f);
		}

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
				world.rand.nextFloat() * 0.4F + 1.0F);
		return true;
	}

}
