package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
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

import javax.annotation.Nullable;

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
		if(!spawnFangs(world, new Vec3d(caster.posX, caster.getEntityBoundingBox().minY, caster.posZ),
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

		if(direction.lengthSquared() == 0) return false; // Prevent casting directly down/up

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

			int count = (int)(getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade));
			float yaw = (float)MathHelper.atan2(direction.z, direction.x); // Yes, this is the right way round!

			for(int i = 0; i < count; i++){

				Vec3d vec = origin.add(direction.scale((i + 1) * FANG_SPACING));
				// Not exactly the same as evokers but it's how constructs work so it kinda fits
				Integer y = BlockUtils.getNearestFloor(world, new BlockPos(vec), 5);

				if(y != null){
					EntityEvokerFangs fangs = new EntityEvokerFangs(world, vec.x, y, vec.z, yaw, i, caster); // null is fine here
					fangs.getEntityData().setFloat(SpellThrowable.DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));
					world.spawnEntity(fangs);
					flag = true;
				}
			}
		}

		return flag;
	}

	// TODO: Events to handle damage modifiers, ADS, etc. for fang entities (probably could do with this for other vanilla-entity spells)

}
