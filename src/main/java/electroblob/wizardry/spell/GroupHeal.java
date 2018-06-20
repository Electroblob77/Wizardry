package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GroupHeal extends Spell {

	public GroupHeal(){
		super("group_heal", Tier.ADVANCED, Element.HEALING, SpellType.DEFENCE, 35, 150, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean flag = false;

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
				5 * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){

			if(target instanceof EntityPlayer){

				if(WizardryUtilities.isPlayerAlly(caster, (EntityPlayer)target) || target == caster){

					if(((EntityPlayer)target).shouldHeal()){

						target.heal(6 * modifiers.get(SpellModifiers.POTENCY));

						if(world.isRemote){
							
							for(int i = 0; i < 10; i++){
								double x = caster.posX + world.rand.nextDouble() * 2 - 1;
								double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
								double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
								ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).colour(1, 1, 0.3f).spawn(world);
							}
							 
							Wizardry.proxy.spawnEntityParticle(world, caster, 15, 1, 1, 0.3f);
						}

						WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
								world.rand.nextFloat() * 0.4F + 1.0F);
						flag = true;
					}
				}

			// Now also works on summoned creatures
			}else if(target instanceof ISummonedCreature){

				EntityLivingBase summoner = ((ISummonedCreature)target).getCaster();

				if(summoner == caster || (summoner instanceof EntityPlayer
						&& WizardryUtilities.isPlayerAlly(caster, (EntityPlayer)summoner))){

					if(target.getHealth() < target.getMaxHealth() && target.getHealth() > 0){

						target.heal(6 * modifiers.get(SpellModifiers.POTENCY));

						if(world.isRemote){
							for(int i = 0; i < 10; i++){
								double x = caster.posX + world.rand.nextDouble() * 2 - 1;
								double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
								double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
								ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).colour(1, 1, 0.3f).spawn(world);
							}
							 
							Wizardry.proxy.spawnEntityParticle(world, caster, 15, 1, 1, 0.3f);
						}

						WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F,
								world.rand.nextFloat() * 0.4F + 1.0F);
						flag = true;
					}
				}
			}
		}

		return flag;
	}

}
