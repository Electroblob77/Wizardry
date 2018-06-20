package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class InvigoratingPresence extends Spell {
	
	private static final double BASE_RANGE = 5;
	private static final int BASE_DURATION = 900;

	public InvigoratingPresence(){
		super("invigorating_presence", Tier.APPRENTICE, Element.HEALING, SpellType.UTILITY, 30, 60, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(
				BASE_RANGE * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world,
				EntityPlayer.class);

		for(EntityPlayer target : targets){
			if(WizardryUtilities.isPlayerAlly(caster, target) || target == caster){
				// Strength 2 for 45 seconds.
				target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,
						(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)), 1, false, false));
			}
		}

		if(world.isRemote){
			
			for(int i = 0; i < 50 * modifiers.get(WizardryItems.blast_upgrade); i++){
				
				double radius = (1 + world.rand.nextDouble() * 4) * modifiers.get(WizardryItems.blast_upgrade);
				double angle = world.rand.nextDouble() * Math.PI * 2;
				
				double x = caster.posX + radius * Math.cos(angle);
				double y = caster.getEntityBoundingBox().minY;
				double z = caster.posZ + radius * Math.sin(angle);
				
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).lifetime(50).colour(1, 0.2f, 0.2f).spawn(world);

			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1, 1 + 0.2f * world.rand.nextFloat());
		return true;
	}

}
