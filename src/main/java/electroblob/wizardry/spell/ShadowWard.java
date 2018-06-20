package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ShadowWard extends Spell {

	public ShadowWard(){
		super("shadow_ward", Tier.ADVANCED, Element.NECROMANCY, SpellType.DEFENCE, 10, 0, EnumAction.BLOCK, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(world.isRemote){
			double dx = -1 + 2 * world.rand.nextFloat();
			double dy = -1 + world.rand.nextFloat();
			double dz = -1 + 2 * world.rand.nextFloat();
			world.spawnParticle(EnumParticleTypes.PORTAL, caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight(), caster.posZ, dx, dy, dz);
		}

		if(ticksInUse % 50 == 0){
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_PORTAL_AMBIENT, 0.6f, 1.5f);
		}

		return true;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof EntityLivingBase){
			// There used to be a check that the target was a player here, but I don't see any reason for it.
			ItemStack wand = event.getEntityLiving().getActiveItemStack();

			if(wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
					&& WandHelper.getCurrentSpell(wand) instanceof ShadowWard && !event.getSource().isUnblockable()
					&& !(event.getSource() instanceof IElementalDamage && ((IElementalDamage)event.getSource()).isRetaliatory())){

				event.setCanceled(true);
				// Now we can preserve the original damage source (sort of) as long as we make it retaliatory.
				// For some reason this isn't working, so I've reverted to plain old magic damage for now.
				//event.getEntityLiving().attackEntityFrom(
				//		MagicDamage.causeDirectMagicDamage(event.getSource().getTrueSource(), DamageType.MAGIC, true), event.getAmount() * 0.5f);
				event.getEntityLiving().attackEntityFrom(DamageSource.MAGIC, event.getAmount() * 0.5f);
				((EntityLivingBase)event.getSource().getTrueSource()).attackEntityFrom(
						MagicDamage.causeDirectMagicDamage(event.getEntityLiving(), DamageType.MAGIC, true), event.getAmount() * 0.5f);
			}
		}
	}

}
