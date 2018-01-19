package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class GroupHeal extends Spell {

	public GroupHeal() {
		super(Tier.ADVANCED, 35, Element.HEALING, "group_heal", SpellType.DEFENCE, 150, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		boolean flag = false;

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(5*modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){

			if(target instanceof EntityPlayer){

				if(WizardryUtilities.isPlayerAlly(caster, (EntityPlayer)target) || target == caster){

					if(((EntityPlayer)target).shouldHeal()){

						target.heal((int)(6*modifiers.get(SpellModifiers.DAMAGE)));

						if(world.isRemote){
							for(int i=0; i<10; i++){
								double d0 = (double)((float)target.posX + world.rand.nextFloat()*2 - 1.0F);
								double d1 = (double)((float)WizardryUtilities.getPlayerEyesPos((EntityPlayer)target) - 0.5F + world.rand.nextFloat());
								double d2 = (double)((float)target.posZ + world.rand.nextFloat()*2 - 1.0F);
								Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
							}
						}

						WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
						flag = true;
					}
				}
				
			// Now also works on summoned creatures
			}else if(target instanceof ISummonedCreature){

				EntityLivingBase summoner = ((ISummonedCreature)target).getCaster();

				if(summoner == caster || (summoner instanceof EntityPlayer && WizardryUtilities.isPlayerAlly(caster, (EntityPlayer)summoner))){

					if(target.getHealth() < target.getMaxHealth()){
						
						target.heal((int)(6*modifiers.get(SpellModifiers.DAMAGE)));

						if(world.isRemote){
							for(int i=0; i<10; i++){
								double d0 = (double)((float)target.posX + world.rand.nextFloat()*2 - 1.0F);
								double d1 = (double)((float)WizardryUtilities.getPlayerEyesPos((EntityPlayer)target) - 0.5F + world.rand.nextFloat());
								double d2 = (double)((float)target.posZ + world.rand.nextFloat()*2 - 1.0F);
								Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
							}
						}

						WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
						flag = true;
					}
				}
			}
		}

		return flag;
	}


}
