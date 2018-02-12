package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Petrify extends Spell {

	private static final int baseDuration = 900;

	/**
	 * The NBT tag name for storing the petrified flag in the target's tag compound. Defined here in case it changes.
	 */
	public static final String NBT_KEY = "petrified";

	public Petrify(){
		super(Tier.ADVANCED, 40, Element.SORCERY, "petrify", SpellType.ATTACK, 100, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLiving && !world.isRemote){

			EntityLiving target = (EntityLiving)rayTrace.entityHit;

			if(target.deathTime > 0) return false;

			BlockPos pos = new BlockPos(target);

			target.extinguish();

			// Short mobs such as spiders and pigs
			if((target.height < 1.2 || target.isChild()) && WizardryUtilities.canBlockBeReplaced(world, pos)){
				world.setBlockState(pos, WizardryBlocks.petrified_stone.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 1);
					((TileEntityStatue)world.getTileEntity(pos))
							.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				}
				target.getEntityData().setBoolean(NBT_KEY, true);
				target.setDead();
			}
			// Normal sized mobs like zombies and skeletons
			else if(target.height < 2.5 && WizardryUtilities.canBlockBeReplaced(world, pos)
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){
				world.setBlockState(pos, WizardryBlocks.petrified_stone.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 2);
					((TileEntityStatue)world.getTileEntity(pos))
							.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				}

				world.setBlockState(pos.up(), WizardryBlocks.petrified_stone.getDefaultState());
				if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos.up())).setCreatureAndPart(target, 2, 2);
				}
				target.getEntityData().setBoolean(NBT_KEY, true);
				target.setDead();
			}
			// Tall mobs like endermen
			else if(WizardryUtilities.canBlockBeReplaced(world, pos)
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up())
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up(2))){
				world.setBlockState(pos, WizardryBlocks.petrified_stone.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 3);
					((TileEntityStatue)world.getTileEntity(pos))
							.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				}

				world.setBlockState(pos.up(), WizardryBlocks.petrified_stone.getDefaultState());
				if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos.up())).setCreatureAndPart(target, 2, 3);
				}

				world.setBlockState(pos.up(2), WizardryBlocks.petrified_stone.getDefaultState());
				if(world.getTileEntity(pos.up(2)) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(pos.up(2))).setCreatureAndPart(target, 3, 3);
				}
				target.getEntityData().setBoolean(NBT_KEY, true);
				target.setDead();
			}
		}
		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				// world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0,
						0.1f, 0.1f, 0.1f);
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
						12 + world.rand.nextInt(8), 0.2f, 0.2f, 0.2f);
			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

}
