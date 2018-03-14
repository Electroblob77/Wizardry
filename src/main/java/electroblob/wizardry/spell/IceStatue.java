package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.advancement.AdvancementHelper;
import electroblob.wizardry.advancement.AdvancementHelper.EnumAdvancement;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IceStatue extends Spell {

	private static final int baseDuration = 400;

	public IceStatue(){
		super(Tier.APPRENTICE, 15, Element.ICE, "ice_statue", SpellType.ATTACK, 40, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLiving && !world.isRemote){

			EntityLiving target = (EntityLiving)rayTrace.entityHit;

			BlockPos pos = new BlockPos(target);

			if(target.isBurning()){
				target.extinguish();
			}

			// Stops the entity looking red while frozen and the resulting z-fighting
			target.hurtTime = 0;

			if(target instanceof EntityBlaze) WizardryAdvancementTriggers.freeze_blaze.triggerFor(caster);

			// Short mobs such as spiders and pigs
			if((target.height < 1.2 || target.isChild()) && WizardryUtilities.canBlockBeReplaced(world, pos)){
				world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 1);
					((TileEntityStatue)world.getTileEntity(pos))
							.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				}
				target.setDead();
				target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
			// Normal sized mobs like zombies and skeletons
			else if(target.height < 2.5 && WizardryUtilities.canBlockBeReplaced(world, pos)
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){
				world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 2);
					((TileEntityStatue)world.getTileEntity(pos))
							.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				}

				world.setBlockState(pos.up(), WizardryBlocks.ice_statue.getDefaultState());
				if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos.up())).setCreatureAndPart(target, 2, 2);
				}
				target.setDead();
				target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
			// Tall mobs like endermen and iron golems
			else if(WizardryUtilities.canBlockBeReplaced(world, pos)
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up())
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up(2))){
				world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 3);
					((TileEntityStatue)world.getTileEntity(pos))
							.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				}

				world.setBlockState(pos.up(), WizardryBlocks.ice_statue.getDefaultState());
				if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos.up())).setCreatureAndPart(target, 2, 3);
				}

				world.setBlockState(pos.up(2), WizardryBlocks.ice_statue.getDefaultState());
				if(world.getTileEntity(pos.up(2)) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos.up(2))).setCreatureAndPart(target, 3, 3);
				}
				target.setDead();
				target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
		}
		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				float brightness = 0.5f + (world.rand.nextFloat() / 2);

				double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
						12 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
				Wizardry.proxy.spawnParticle(WizardryParticleType.SNOW, world, x1, y1, z1, 0.0d, -0.02d, 0.0d,
						20 + world.rand.nextInt(10));

			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F,
				world.rand.nextFloat() * 0.4F + 1.2F);
		return true;
	}

}
