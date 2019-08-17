package electroblob.wizardry.spell;

import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class FontOfMana extends Spell {

	public FontOfMana(){
		super("font_of_mana", EnumAction.BOW, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(EFFECT_RADIUS, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double maxRadius = getProperty(EFFECT_RADIUS).doubleValue();

		List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(
				maxRadius * modifiers.get(WizardryItems.blast_upgrade),
				caster.posX, caster.posY, caster.posZ, world, EntityPlayer.class);

		for(EntityPlayer target : targets){
			if(AllyDesignationSystem.isPlayerAlly(caster, target) || target == caster){
				target.addPotionEffect(new PotionEffect(WizardryPotions.font_of_mana,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						(int)(getProperty(EFFECT_STRENGTH).intValue() + (modifiers.get(SpellModifiers.POTENCY) - 1) * 2)));
			}
		}

		if(world.isRemote){
			for(int i = 0; i < 100 * modifiers.get(WizardryItems.blast_upgrade); i++){

				double radius = (1 + world.rand.nextDouble() * (maxRadius - 1)) * modifiers.get(WizardryItems.blast_upgrade);
				float angle = world.rand.nextFloat() * (float)Math.PI * 2;
				;
				float hue = world.rand.nextFloat() * 0.4f;

				double x = caster.posX + radius * MathHelper.cos(angle);
				double y = caster.getEntityBoundingBox().minY;
				double z = caster.posZ + radius * MathHelper.sin(angle);

				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).time(50)
						.clr(1, 1 - hue, 0.6f + hue).spawn(world);
			}
		}

		playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

	@SubscribeEvent(priority = EventPriority.LOW) // Doesn't really matter but there's no point processing it if casting is blocked
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Moved from ItemWand (quite why this wasn't done with modifiers before I don't know!)
		if(event.getCaster() != null && event.getCaster().isPotionActive(WizardryPotions.font_of_mana)){
			// Dividing by this rather than setting it takes upgrades and font of mana into account simultaneously
			event.getModifiers().set(WizardryItems.cooldown_upgrade, event.getModifiers().get(WizardryItems.cooldown_upgrade)
					/ (2 + event.getCaster().getActivePotionEffect(WizardryPotions.font_of_mana).getAmplifier()), false);
		}
	}
}