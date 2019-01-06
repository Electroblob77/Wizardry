package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IceAge extends Spell {

	private static final int baseDuration = 1200;

	public IceAge(){
		super("ice_age", Tier.MASTER, Element.ICE, SpellType.ATTACK, 70, 250, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
				7 * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target)){
				if(!world.isRemote){
					if(target instanceof EntityBlaze || target instanceof EntityMagmaCube){
						// These have been removed for the time being because they cause the entity to sink into the
						// floor when it breaks out.
						// target.attackEntityFrom(WizardryUtilities.causePlayerMagicDamage(entityplayer), 8.0f *
						// modifiers.get(SpellModifiers.DAMAGE));
					}else{
						// target.attackEntityFrom(WizardryUtilities.causePlayerMagicDamage(entityplayer), 4.0f *
						// modifiers.get(SpellModifiers.DAMAGE));
					}
					if(target.isBurning()){
						target.extinguish();
					}

					if(target instanceof EntityBlaze) WizardryAdvancementTriggers.freeze_blaze.triggerFor(caster);

					if(target instanceof EntityLiving){

						// Stops the entity looking red while frozen and the resulting z-fighting
						target.hurtTime = 0;

						BlockPos pos = new BlockPos(target);

						// Short mobs such as spiders and pigs
						if((target.height < 1.2 || target.isChild())
								&& WizardryUtilities.canBlockBeReplaced(world, pos)){
							world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
							if(world.getTileEntity(pos) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart((EntityLiving)target, 1,
										1);
								((TileEntityStatue)world.getTileEntity(pos)).setLifetime(
										(int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
							}
							target.setDead();
							target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
						// Normal sized mobs like zombies and skeletons
						else if(target.height < 2.5 && WizardryUtilities.canBlockBeReplaced(world, pos)
								&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){
							world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
							if(world.getTileEntity(pos) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart((EntityLiving)target, 1,
										2);
								((TileEntityStatue)world.getTileEntity(pos)).setLifetime(
										(int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
							}

							world.setBlockState(pos.up(), WizardryBlocks.ice_statue.getDefaultState());
							if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(pos.up()))
										.setCreatureAndPart((EntityLiving)target, 2, 2);
							}
							target.setDead();
							target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
						// Tall mobs like endermen
						else if(WizardryUtilities.canBlockBeReplaced(world, pos)
								&& WizardryUtilities.canBlockBeReplaced(world, pos.up())
								&& WizardryUtilities.canBlockBeReplaced(world, pos.up(2))){
							world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
							if(world.getTileEntity(pos) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart((EntityLiving)target, 1,
										3);
								((TileEntityStatue)world.getTileEntity(pos)).setLifetime(
										(int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
							}

							world.setBlockState(pos.up(), WizardryBlocks.ice_statue.getDefaultState());
							if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(pos.up()))
										.setCreatureAndPart((EntityLiving)target, 2, 3);
							}

							world.setBlockState(pos.up(2), WizardryBlocks.ice_statue.getDefaultState());
							if(world.getTileEntity(pos.up(2)) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(pos.up(2)))
										.setCreatureAndPart((EntityLiving)target, 3, 3);
							}
							target.setDead();
							target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
					}
				}
			}
		}
		if(!world.isRemote){
			for(int i = -7; i < 8; i++){
				for(int j = -7; j < 8; j++){

					BlockPos pos = new BlockPos(caster).add(i, 0, j);

					int y = WizardryUtilities.getNearestFloorLevelB(world, new BlockPos(pos), 7);

					pos = new BlockPos(pos.getX(), y, pos.getZ());

					double dist = caster.getDistance((int)caster.posX + i, y, (int)caster.posZ + j);

					// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
					if(y != -1 && world.rand.nextInt((int)dist * 2 + 1) < 7 && dist < 8){
						if(world.getBlockState(pos.down()) == Blocks.WATER.getDefaultState()){
							world.setBlockState(pos.down(), Blocks.ICE.getDefaultState());
						}else if(world.getBlockState(pos.down()) == Blocks.LAVA.getDefaultState()){
							world.setBlockState(pos.down(), Blocks.OBSIDIAN.getDefaultState());
						}else if(world.getBlockState(pos.down()) == Blocks.FLOWING_LAVA.getDefaultState()){
							world.setBlockState(pos.down(), Blocks.COBBLESTONE.getDefaultState());
						}else if(Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos)){
							world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
						}
					}
				}
			}
		}
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.7F, 1.0f);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_WIND, 1.0F, 1.0f);
		return true;
	}

}
