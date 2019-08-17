package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class InvigoratingPresence extends Spell {

	public InvigoratingPresence(){
		super("invigorating_presence", EnumAction.BOW, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(EFFECT_RADIUS, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(
				getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade),
				caster.posX, caster.posY, caster.posZ, world, EntityPlayer.class);

		for(EntityPlayer target : targets){
			if(AllyDesignationSystem.isPlayerAlly(caster, target) || target == caster){

				int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

				target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue() + bonusAmplifier));
			}
		}

		if(world.isRemote){
			
			for(int i = 0; i < 50 * modifiers.get(WizardryItems.blast_upgrade); i++){
				
				double radius = (1 + world.rand.nextDouble() * 4) * modifiers.get(WizardryItems.blast_upgrade);
				float angle = world.rand.nextFloat() * (float)Math.PI * 2;;
				
				double x = caster.posX + radius * MathHelper.cos(angle);
				double y = caster.getEntityBoundingBox().minY;
				double z = caster.posZ + radius * MathHelper.sin(angle);
				
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).time(50).clr(1, 0.2f, 0.2f).spawn(world);

			}
		}

		playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
