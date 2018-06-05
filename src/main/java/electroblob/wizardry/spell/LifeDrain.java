package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LifeDrain extends Spell {

	public LifeDrain(){
		super(Tier.APPRENTICE, 10, Element.NECROMANCY, "life_drain", SpellType.ATTACK, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && WizardryUtilities.isLiving(rayTrace.entityHit)){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(ticksInUse % 12 == 0){
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC),
						2.0f * modifiers.get(SpellModifiers.DAMAGE));
				caster.heal(1);
			}
		}
		if(world.isRemote){
			for(int i = 5; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				// world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				if(i % 5 == 0){
					Wizardry.proxy.spawnParticle(Type.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0, 0.1f, 0.0f, 0.0f);
				}
				Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, -0.05 * look.x * i,
						-0.05 * look.y * i, -0.05 * look.z * i, 8 + world.rand.nextInt(6), 0.5f, 0.0f, 0.0f);
			}
		}
		if(ticksInUse % 18 == 0){
			if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SUMMONING, 1.0F, 0.6f);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_CRACKLE, 2.0F, 1.0f);
		}
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		Vec3d vec = new Vec3d(target.posX - caster.posX, target.posY - caster.posY, target.posZ - caster.posZ)
				.normalize();

		if(target != null){
			if(ticksInUse % 12 == 0){
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC),
						2.0f * modifiers.get(SpellModifiers.DAMAGE));
				caster.heal(1);
			}
		}
		if(world.isRemote){
			for(int i = 5; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + vec.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = caster.posY + caster.getEyeHeight() - 0.4f + vec.y * i / 2 + world.rand.nextFloat() / 5
						- 0.1f;
				double z1 = caster.posZ + vec.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				// world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				if(i % 5 == 0){
					Wizardry.proxy.spawnParticle(Type.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0, 0.1f, 0.0f, 0.0f);
				}
				Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, -0.05 * vec.x * i,
						-0.05 * vec.y * i, -0.05 * vec.z * i, 8 + world.rand.nextInt(6), 0.5f, 0.0f, 0.0f);
			}
		}
		if(ticksInUse % 18 == 0){
			if(ticksInUse == 0) caster.playSound(WizardrySounds.SPELL_SUMMONING, 1.0F, 0.6f);
			caster.playSound(WizardrySounds.SPELL_LOOP_CRACKLE, 2.0F, 1.0f);
		}

		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
