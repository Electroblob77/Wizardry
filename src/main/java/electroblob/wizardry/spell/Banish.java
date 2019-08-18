package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Banish extends SpellRay {

	public static final String MINIMUM_TELEPORT_DISTANCE = "minimum_teleport_distance";
	public static final String MAXIMUM_TELEPORT_DISTANCE = "maximum_teleport_distance";

	public Banish(){
		super("banish", false, EnumAction.NONE);
		this.addProperties(MINIMUM_TELEPORT_DISTANCE, MAXIMUM_TELEPORT_DISTANCE);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityLivingBase){

			EntityLivingBase entity = (EntityLivingBase)target;

			double minRadius = getProperty(MINIMUM_TELEPORT_DISTANCE).doubleValue();
			double maxRadius = getProperty(MAXIMUM_TELEPORT_DISTANCE).doubleValue();
			double radius = (minRadius + world.rand.nextDouble() * maxRadius-minRadius) * modifiers.get(WizardryItems.blast_upgrade);

			teleport(entity, world, radius);
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.PORTAL, x, y - 0.5, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.2f, 0, 0.2f).spawn(world);
	}

	// Extracted as a separate method for external use
	public boolean teleport(EntityLivingBase entity, World world, double radius){

		float angle = world.rand.nextFloat() * (float)Math.PI * 2;

		int x = MathHelper.floor(entity.posX + MathHelper.sin(angle) * radius);
		int z = MathHelper.floor(entity.posZ - MathHelper.cos(angle) * radius);
		Integer y = WizardryUtilities.getNearestFloor(world,
				new BlockPos(x, (int)entity.getEntityBoundingBox().minY, z), (int)radius);

		if(world.isRemote){

			for(int i=0; i<10; i++){
				double dx1 = entity.posX;
				double dy1 = entity.getEntityBoundingBox().minY + entity.height * world.rand.nextFloat();
				double dz1 = entity.posZ;
				world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
						world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}

			// Can't be bothered to route this through the proxies!
			if(entity == net.minecraft.client.Minecraft.getMinecraft().player)
				electroblob.wizardry.client.WizardryClientEventHandler.playBlinkEffect();
		}

		if(y != null){

			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the target does
			// not teleport 1 block above the ground.
			if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
				y--;
			}

			if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
					|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
				return false;
			}

			if(!world.isRemote){
				entity.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
			}

			this.playSound(world, entity, 0, -1, new SpellModifiers());
		}

		return true;
	}

}
