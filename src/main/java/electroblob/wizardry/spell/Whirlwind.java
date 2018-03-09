package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Whirlwind extends Spell {

	public Whirlwind(){
		super(Tier.APPRENTICE, 10, Element.EARTH, "whirlwind", SpellType.DEFENCE, 15, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		// Left as EntityLivingBase because why not be able to move armour stands around?
		if(rayTrace != null && rayTrace.entityHit instanceof EntityLivingBase){
			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(!world.isRemote){

				target.motionX = caster.getLookVec().x * 2;
				target.motionY = caster.getLookVec().y * 2 + 1;
				target.motionZ = caster.getLookVec().z * 2;

				// Player motion is handled on that player's client so needs packets
				if(target instanceof EntityPlayerMP){
					((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
				}
			}

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x2 = (double)(caster.posX + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().x * caster.getDistance(target) * 0.5);
					double y2 = (double)(WizardryUtilities.getPlayerEyesPos(caster) + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().y * caster.getDistance(target) * 0.5);
					double z2 = (double)(caster.posZ + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().z * caster.getDistance(target) * 0.5);
					world.spawnParticle(EnumParticleTypes.CLOUD, x2, y2, z2, caster.getLookVec().x,
							caster.getLookVec().y, caster.getLookVec().z);
					// Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, x2, y2, z2,
					// entityplayer.getLookVec().xCoord, entityplayer.getLookVec().yCoord,
					// entityplayer.getLookVec().zCoord, null, 1.0f, 1.0f, 0.8f, 10));
				}
			}
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.8F,
					world.rand.nextFloat() * 0.2F + 0.6F);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				target.motionX = caster.getLookVec().x * 2;
				target.motionY = caster.getLookVec().y * 2 + 1;
				target.motionZ = caster.getLookVec().z * 2;

				// Player motion is handled on that player's client so needs packets
				if(target instanceof EntityPlayerMP){
					((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
				}
			}
			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x2 = (double)(caster.posX + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().x * caster.getDistance(target) * 0.5);
					double y2 = (double)(caster.posY + caster.getEyeHeight() + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().y * caster.getDistance(target) * 0.5);
					double z2 = (double)(caster.posZ + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().z * caster.getDistance(target) * 0.5);
					world.spawnParticle(EnumParticleTypes.CLOUD, x2, y2, z2, caster.getLookVec().x,
							caster.getLookVec().y, caster.getLookVec().z);
				}
			}
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 0.8F, world.rand.nextFloat() * 0.2F + 0.6F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
