package electroblob.wizardry.spell;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEvokerFangs;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@EventBusSubscriber
public class Fangs extends Spell {

	private static final double FANG_SPACING = 1.25;

	public Fangs(){
		super("fangs", SpellActions.SUMMON, false);
		addProperties(RANGE);
		this.npcSelector((e, o) -> true);
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return true;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		if(!spawnFangs(world, caster.getPositionVector(),
				GeometryUtils.replaceComponent(caster.getLookVec(), Axis.Y, 0).normalize(), caster, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		if(!spawnFangs(world, caster.getPositionVector(), target.getPositionVector().subtract(caster.getPositionVector()).normalize(), caster, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		if(!spawnFangs(world, new Vec3d(x, y, z), new Vec3d(direction.getDirectionVec()), null, modifiers)) return false;
		this.playSound(world, x, y, z, ticksInUse, -1, modifiers);
		return true;
	}

	protected boolean spawnFangs(World world, Vec3d origin, Vec3d direction, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		boolean defensiveCircle = caster instanceof EntityPlayer && caster.isSneaking()
				&& ItemArtefact.isArtefactActive((EntityPlayer)caster, WizardryItems.ring_evoker);

		if(!defensiveCircle && direction.lengthSquared() == 0) return false; // Prevent casting directly down/up

		boolean flag = false;

		if(world.isRemote){

			double x = origin.x;
			double y = caster == null ? origin.y : origin.y + caster.getEyeHeight();
			double z = origin.z;

			for(int i = 0; i < 12; i++){
				ParticleBuilder.create(Type.DARK_MAGIC, world.rand, x, y, z, 0.5, false)
						.clr(0.4f, 0.3f, 0.35f).spawn(world); // Colour from EntitySpellcasterIllager
			}

		}else{

			if(defensiveCircle){

				for(int i = 0; i < 5; i++){
					float yaw = i * (float)Math.PI * 0.4f;
					flag |= this.spawnFangsAt(world, caster, modifiers, yaw, 0, origin.add(MathHelper.cos(yaw) * 1.5, 0, MathHelper.sin(yaw) * 1.5));
				}

				for(int k = 0; k < 8; k++){
					float yaw = k * (float)Math.PI * 2f / 8f + ((float)Math.PI * 2f / 5f);
					flag |= this.spawnFangsAt(world, caster, modifiers, yaw, 3, origin.add(MathHelper.cos(yaw) * 2.5, 0, MathHelper.sin(yaw) * 2.5));
				}

			}else{

				int count = (int)(getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade));
				float yaw = (float)MathHelper.atan2(direction.z, direction.x); // Yes, this is the right way round!

				for(int i = 0; i < count; i++){
					Vec3d vec = origin.add(direction.scale((i + 1) * FANG_SPACING));
					flag |= spawnFangsAt(world, caster, modifiers, yaw, i, vec);
				}
			}
		}

		return flag;
	}

	private boolean spawnFangsAt(World world, @Nullable EntityLivingBase caster, SpellModifiers modifiers, float yaw, int delay, Vec3d vec){

		// Not exactly the same as evokers but it's how constructs work so it kinda fits
		Integer y = BlockUtils.getNearestFloor(world, new BlockPos(vec), 5);

		if(y != null){
			EntityEvokerFangs fangs = new EntityEvokerFangs(world, vec.x, y, vec.z, yaw, delay, caster); // null is fine here
			fangs.getEntityData().setFloat(SpellThrowable.DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));
			world.spawnEntity(fangs);
			return true;
		}

		return false;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource().getImmediateSource() instanceof EntityEvokerFangs){
			if(!AllyDesignationSystem.isValidTarget(event.getSource().getTrueSource(), event.getEntityLiving())){
				event.setCanceled(true); // Don't attack allies
			}
		}
	}

}
