package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class ShulkerBullet extends Spell {

	public ShulkerBullet(){
		super("shulker_bullet", SpellActions.POINT_DOWN, false);
		this.soundValues(2, 1, 0.3f);
		addProperties(RANGE);
	}

	@Override public boolean canBeCastBy(EntityLiving npc, boolean override){ return true; }

	@Override public boolean canBeCastBy(TileEntityDispenser dispenser){ return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		if(!shoot(world, caster, caster.posX, caster.posY, caster.posZ, EnumFacing.UP, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		if(!shoot(world, caster, caster.posX, caster.posY, caster.posZ, EnumFacing.UP, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int duration, int ticksInUse, SpellModifiers modifiers){
		if(!shoot(world, null, x, y, z, direction, modifiers)) return false;
		this.playSound(world, x, y, z, ticksInUse, -1, modifiers);
		return true;
	}

	private boolean shoot(World world, @Nullable EntityLivingBase caster, double x, double y, double z, EnumFacing direction, SpellModifiers modifiers){

		if(!world.isRemote){

			double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

			List<EntityLivingBase> possibleTargets = EntityUtils.getLivingWithinRadius(range, x, y, z, world);

			possibleTargets.remove(caster);
			possibleTargets.removeIf(t -> t instanceof EntityArmorStand);

			if(possibleTargets.isEmpty()) return false;

			// getDistanceSq doesn't require square-rooting so it's faster when only comparing
			possibleTargets.sort(Comparator.comparingDouble(t -> t.getDistanceSq(x, y, z)));

			Entity target = possibleTargets.get(0);

			// Y axis because the player is always upright
			if(caster != null){
				world.spawnEntity(new EntityShulkerBullet(world, caster, target, direction.getAxis()));
			}else{
				// Can't use the normal constructor because doesn't accept null for the owner
				EntityShulkerBullet bullet = new EntityShulkerBullet(world);
				bullet.setLocationAndAngles(x, y, z, bullet.rotationYaw, bullet.rotationPitch);

				// Where there's a will there's a way...
				NBTTagCompound nbt = new NBTTagCompound();
				bullet.writeToNBT(nbt);
				nbt.setInteger("Dir", direction.getIndex());
				BlockPos pos = new BlockPos(target);
				NBTTagCompound targetTag = NBTUtil.createUUIDTag(target.getUniqueID());
				targetTag.setInteger("X", pos.getX());
				targetTag.setInteger("Y", pos.getY());
				targetTag.setInteger("Z", pos.getZ());
				NBTExtras.storeTagSafely(nbt, "Target", targetTag);
				bullet.readFromNBT(nbt); // LOL I just modified private fields without reflection

				world.spawnEntity(bullet);
			}
		}

		return true;
	}
}
