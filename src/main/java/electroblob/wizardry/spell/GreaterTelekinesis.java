package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class GreaterTelekinesis extends SpellRay {

	public static final String HOLD_RANGE = "hold_range";
	public static final String THROW_VELOCITY = "throw_velocity";

	/** Makes things a bit smoother-looking / 'realistic'. */
	private static final float UNDERSHOOT = 0.2f;

	public GreaterTelekinesis(){
		super("greater_telekinesis", true, EnumAction.NONE);
		this.aimAssist(0.4f);
		this.particleSpacing(1);
		this.particleJitter(0.05);
		this.particleVelocity(0.3);
		addProperties(HOLD_RANGE, THROW_VELOCITY, DAMAGE);
		this.soundValues(0.8f, 1, 0.2f);
	}

	@Override public boolean canBeCastBy(EntityLiving npc, boolean override) { return false; }
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return false; }

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		// Can't be cast by dispensers so we know caster isn't null, but just in case...
		if(caster != null && (target instanceof EntityLivingBase || target instanceof EntityLevitatingBlock || target instanceof EntityTNTPrimed)){

			if(target instanceof EntityPlayer && ((caster instanceof EntityPlayer && !Wizardry.settings.playersMoveEachOther)
					|| ItemArtefact.isArtefactActive((EntityPlayer)target, WizardryItems.amulet_anchoring))){

				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
				return false;
			}

			if(target instanceof EntityLevitatingBlock){
				((EntityLevitatingBlock)target).suspend();
				((EntityLevitatingBlock)target).setCaster(caster); // Yep, you can steal other players' blocks in mid-air!
			}
			
			Vec3d targetPos = target.getPositionVector().add(0, target.height/2, 0);
			
			if(caster.isSneaking()){
				
				Vec3d look = caster.getLookVec().scale(getProperty(THROW_VELOCITY).floatValue() * modifiers.get(WizardryItems.range_upgrade));
				target.addVelocity(look.x, look.y, look.z);
				// No IntelliJ, it's not always false, that's not how polymorphism works
				if(caster instanceof EntityPlayer) caster.swingArm(caster.getActiveHand() == null ? EnumHand.MAIN_HAND : caster.getActiveHand());
				
			}else{
			
				WizardryUtilities.undoGravity(target);
				
				// The following code extrapolates the entity's current velocity to determine whether it will pass the
				// target position in the next tick, and adds or subtracts velocity accordingly.
				
				Vec3d vec = origin.add(caster.getLookVec().scale(getProperty(HOLD_RANGE).floatValue()));
				
				Vec3d velocity = vec.subtract(targetPos).subtract(target.motionX, target.motionY, target.motionZ)
						.scale(1 - UNDERSHOOT);
				
				target.addVelocity(velocity.x, velocity.y, velocity.z);
			}
			
			// Player motion is handled on that player's client so needs packets
			if(target instanceof EntityPlayerMP){
				((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
			}
			
			if(world.isRemote){

				ParticleBuilder.create(Type.BEAM).entity(caster).clr(0.2f, 0.6f + 0.3f * world.rand.nextFloat(), 1)
				.pos(origin.subtract(caster.getPositionVector())).target(target).time(0)
				.scale(MathHelper.sin(ticksInUse * 0.3f) * 0.1f + 0.9f).spawn(world);
				
				if(ticksInUse % 18 == 1) ParticleBuilder.create(Type.FLASH).entity(target).pos(0, target.height/2, 0)
				.scale(2.5f).time(30).clr(0.2f, 0.8f, 1).fade(1f, 1f, 1f).spawn(world);
				
				ParticleBuilder.create(Type.SPARKLE, target).vel(0, 0.05, 0).time(15).scale(0.6f).clr(0.2f, 0.6f, 1)
				.fade(1f, 1f, 1f).spawn(world);
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.canDamageBlocks(caster, world) && !WizardryUtilities.isBlockUnbreakable(world, pos)
				&& world.getBlockState(pos).getMaterial().isSolid()
				&& (world.getTileEntity(pos) == null || !world.getTileEntity(pos).getTileData().hasUniqueId(ArcaneLock.NBT_KEY))){
			
			if(!world.isRemote){

				EntityLevitatingBlock block = new EntityLevitatingBlock(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						world.getBlockState(pos));

				block.fallTime = 1;
				block.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				block.setCaster(caster);

				world.spawnEntity(block);
				world.setBlockToAir(pos);
			}
				
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
