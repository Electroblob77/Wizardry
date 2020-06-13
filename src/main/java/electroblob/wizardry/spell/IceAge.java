package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import electroblob.wizardry.util.WizardryUtilities.SurfaceCriteria;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class IceAge extends Spell {

	public static final String FREEZE_DURATION = "freeze_duration";

	public IceAge(){
		super("ice_age", SpellActions.POINT_DOWN, false);
		this.soundValues(1.5f, 1.0f, 0);
		addProperties(EFFECT_RADIUS, FREEZE_DURATION, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		float radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(radius, caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){
			if(AllyDesignationSystem.isValidTarget(caster, target)){
				if(!world.isRemote){

					if(target instanceof EntityLiving){
						if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((EntityLiving)target,
								(int)(getProperty(FREEZE_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){
							target.playSound(WizardrySounds.MISC_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
					}else if(target instanceof EntityPlayer){
						target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
								(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
								getProperty(EFFECT_STRENGTH).intValue()));
					}
				}
			}
		}

		if(!world.isRemote && WizardryUtilities.canDamageBlocks(caster, world)){
			for(int i = -(int)radius; i <= (int)radius; i++){
				for(int j = -(int)radius; j <= (int)radius; j++){

					BlockPos pos = new BlockPos(caster).add(i, 0, j);

					Integer y = WizardryUtilities.getNearestSurface(world, new BlockPos(pos), EnumFacing.UP, (int)radius, true, SurfaceCriteria.SOLID_LIQUID_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = caster.getDistance(caster.posX + i, y, caster.posZ + j);

						// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
						if(y != -1 && world.rand.nextInt((int)(dist * 2) + 1) < radius && dist < radius){
							WizardryUtilities.freeze(world, pos.down(), true);
						}
					}
				}
			}
		}

		if(world.isRemote){

			for(int i=0; i<100; i++){
				double speed = (world.rand.nextBoolean() ? 1 : -1) * (0.1 + 0.05 * world.rand.nextDouble());
				ParticleBuilder.create(Type.SNOW)
						.pos(caster.posX, caster.getEntityBoundingBox().minY + world.rand.nextDouble() * 3, caster.posZ)
						.vel(0, 0, 0)
						.scale(2)
						.spin(world.rand.nextDouble() * (radius - 0.5) + 0.5, speed)
						.spawn(world);
			}

			for(int i=0; i<60; i++){
				double speed = (world.rand.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.rand.nextDouble());
				ParticleBuilder.create(Type.CLOUD)
						.pos(caster.posX, caster.getEntityBoundingBox().minY + world.rand.nextDouble() * 2.5, caster.posZ)
						.clr(0xffffff)
						.spin(world.rand.nextDouble() * (radius - 1) + 0.5, speed)
						.spawn(world);
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
