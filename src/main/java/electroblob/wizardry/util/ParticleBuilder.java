package electroblob.wizardry.util;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * <i>"Don't waste time spawning particles manually - let {@code ParticleBuilder} do the work for you!"</i>
 * <p>
 * Singleton class that builds wizardry particles. This is an alternative (and neater, I think) solution to using varargs.
 * All building methods are chainable, so particles can be created using only one line of code, similar to the
 * {@code BufferBuilder} system. The number of different combinations of parameters now required for the various particle
 * types in wizardry made the method overloads in the proxies very cumbersome and inevitably resulted in redundant
 * parameters, which made the code messy and hard to read. Those methods have now been removed.
 * <p>
 * {@link ParticleBuilder#instance} retrieves the static instance of the particle builder. Use
 * {@link ParticleBuilder#particle(Type)} to start building a particle, or alternatively use the static
 * convenience version {@link ParticleBuilder#create(Type)}. Use {@link ParticleBuilder#spawn(World)}
 * to finish building and spawn the particle. Between these two, a variety of parameters can be set using the various
 * setter methods (see individual method descriptions for more details). These, along with {@code ParticleBuilder.particle(...)},
 * return the particle builder instance, allowing them to be chained together to spawn particles using a single line of code.
 * If any parameters are unspecified these will default to certain values, which may or may not depend on the particle type.
 * Not all parameters affect all particles. Again, see individual method descriptions for more details.
 * <p>
 * For example, a typical call to the particle builder might look something like this:
 * <p>
 * <code>ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).clr(r, g, b).spawn(world);</code>
 * <p>
 * It also goes without saying that <b>this class should only ever be used client-side</b>. Attempting to spawn particles
 * on the server side will not work and will print a warning to the console.
 * @author Electroblob
 * @since Wizardry 4.2
 */
/* This isn't strictly a builder class in the traditional sense, because rather than returning the built object at the
 * end, it sends it to be processed instead and returns nothing. It's also lazy, see the comment about builder variables
 * below. */
public final class ParticleBuilder {

	/** The static instance of the particle builder. */
	public static final ParticleBuilder instance = new ParticleBuilder();
	
	/** Whether the particle builder is currently building or not. */
	private boolean building = false;
	
	// Builder variables
	// We can't just store a particle and set its parameters in the builder methods, because the server won't like having
	// a field of a client-only type
	private Type type;
	private double x, y, z;
	private double vx, vy, vz;
	private float r, g, b;
	private float fr, fg, fb;
	private double radius;
	private double rpt;
	private int lifetime;
	private boolean gravity;
	private boolean shaded;
	private boolean collide;
	private float scale;
	private Entity entity;
	private float yaw, pitch;
	private double tx, ty, tz;
	private Entity target;
	
	/** Enum constants representing the different types of particle added by wizardry. As of 4.2.0, this has been moved
	 * from its own file {@code WizardryParticleType} to inside {@link ParticleBuilder}. This allowed its name to be
	 * shortened to simply {@code Type}, making most references more concise. References in classes where another
	 * {@code Type} is also used can simply refer to the full name, {@code ParticleBuilder.Type}, which is no more verbose
	 * than before.
	 * <p>
	 * Individual constants have comments detailing their corresponding default parameters. A range of values indicates
	 * randomness. */
	public enum Type {
		/** 3D-rendered light-beam particle.<p><b>Defaults:</b><p>Lifetime: 1 tick<br> Colour: white */		BEAM,
		/** Helical animated 'buffing' particle.<p><b>Defaults:</b><p>Lifetime: 15 ticks
		 * <br>Velocity: (0, 0.27, 0)<br>Colour: white */ 													BUFF,
		/** Spiral particle, like potions.<p><b>Defaults:</b><p>Lifetime: 8-40 ticks<br>Colour: white */ 	DARK_MAGIC,
		/** Single pixel particle.<p><b>Defaults:</b><p>Lifetime: 16-80 ticks<br>Colour: white */ 			DUST,
		/** Rapid flash, like fireworks.<p><b>Defaults:</b><p>Lifetime: 6 ticks<br>Colour: white */ 		FLASH,
		/** Small shard of ice.<p><b>Defaults:</b><p>Lifetime: 8-40 ticks<br>Gravity: true */ 				ICE,
		/** Single leaf.<p><b>Defaults:</b><p>Lifetime: 10-15 ticks<br>Velocity: (0, -0.03, 0)
		 * <br>Colour: green/brown */ 																		LEAF,
		/** 3D-rendered lightning particle.<p><b>Defaults:</b><p>Lifetime: 3 ticks<br> Colour: blue */		LIGHTNING,
		/** 2D lightning effect, normally on the ground.<p><b>Defaults:</b><p>Lifetime: 7 ticks
		 * <br>Facing: up */																				LIGHTNING_PULSE,
		/** Bubble that doesn't burst in air.<p><b>Defaults:</b><p>Lifetime: 8-40 ticks	*/					MAGIC_BUBBLE,
		/** Scaleable, moving flame.<p><b>Defaults:</b><p>Lifetime: 8-40 ticks<br> */ 						MAGIC_FIRE,
		/** Soft-edged round particle.<p><b>Defaults:</b><p>Lifetime: 8-40 ticks<br>Colour: white */ 		PATH,
		/** Scorch mark.<p><b>Defaults:</b><p>Lifetime: 100-140 ticks<br>Colour: black<br>Fade: black */	SCORCH,
		/** Snowflake particle.<p><b>Defaults:</b><p>Lifetime: 40-50 ticks<br>Velocity: (0, -0.02, 0) */	SNOW,
		/** Animated lightning particle.<p><b>Defaults:</b><p>Lifetime: 3 ticks */							SPARK,
		/** Animated sparkle particle.<p><b>Defaults:</b><p>Lifetime: 48-60 ticks<br>Colour: white */		SPARKLE
	}
	
	private ParticleBuilder(){
		reset();
	}
	
	// ============================================= Core builder methods =============================================
	
	/**
	 * Starts building a particle of the given type. Static convenience version of
	 * {@link ParticleBuilder#particle(Type)}; makes code more concise.
	 * @param type The type of particle to build
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is already building.
	 */
	public static ParticleBuilder create(Type type){
		return ParticleBuilder.instance.particle(type);
	}
	
	/**
	 * Starts building a particle of the given type.
	 * @param type The type of particle to build
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is already building.
	 */
	public ParticleBuilder particle(Type type){
		if(building) throw new IllegalStateException("Already Building! Particle being built: " + getCurrentParticleString());
		this.type = type;
		this.building = true;
		return this;
	}
	
	/** Gets a readable string representation of the current builder parameters; used in error messages. */
	private String getCurrentParticleString(){
		return String.format("[ Type: %s, Position: (%s, %s, %s), Velocity: (%s, %s, %s), Colour: (%s, %s, %s), "
				+ "Fade Colour: (%s, %s, %s), Radius: %s, Revs/tick: %s, Lifetime: %s, Gravity: %s, Shaded: %s,"
				+ "Scale: %s, Entity: %s ]",
				type, x, y, z, vx, vy, vz, r, g, b, fr, fg, fb, radius, rpt, lifetime, gravity, shaded, scale, entity);
	}
	
	/**
	 * Sets the position of the particle being built. If unspecified, this defaults to the origin (0, 0, 0). If an entity
	 * is specified using {@link ParticleBuilder#entity(Entity)}, this will be <i>relative to</i> that entity's position.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param x The x coordinate to set
	 * @param y The y coordinate to set
	 * @param z The z coordinate to set
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder pos(double x, double y, double z){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	/**
	 * Sets the position of the particle being built. This is a vector-based alternative to {@link ParticleBuilder#pos(
	 * double, double, double)}, allowing for even more concise code when a vector is available.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param pos A vector representing the coordinates of the particle to be built.
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder pos(Vec3d pos){
		return pos(pos.x, pos.y, pos.z);
	}
	
	/**
	 * Sets the velocity of the particle being built. If unspecified, this defaults to the particle's default velocity,
	 * specified within its constructor.
	 * <p>
	 * <b>Affects:</b> All particle types except {@link Type#DUST DUST}
	 * @param vx The x velocity to set
	 * @param vy The y velocity to set
	 * @param vz The z velocity to set
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder vel(double vx, double vy, double vz){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
		return this;
	}
	
	/**
	 * Sets the velocity of the particle being built. This is a vector-based alternative to {@link ParticleBuilder#vel(
	 * double, double, double)}, allowing for even more concise code when a vector is available.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param vel A vector representing the velocity of the particle to be built.
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder vel(Vec3d vel){
		return vel(vel.x, vel.y, vel.z);
	}
	
	/**
	 * Sets the colour of the particle being built. If unspecified, this defaults to the particle's default colour,
	 * specified within its constructor.
	 * <p>
	 * <b>Affects:</b> {@link Type#DARK_MAGIC DARK_MAGIC}, {@link Type#DUST DUST}, {@link Type#FLASH FLASH},
	 * {@link Type#LEAF LEAF}, {@link Type#PATH PATH}, {@link Type#SPARKLE SPARKLE}
	 * @param r The red colour component to set; will be clamped to between 0 and 1
	 * @param g The green colour component to set; will be clamped to between 0 and 1
	 * @param b The blue colour component to set; will be clamped to between 0 and 1
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder clr(float r, float g, float b){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.r = MathHelper.clamp(r, 0, 1);
		this.g = MathHelper.clamp(g, 0, 1);
		this.b = MathHelper.clamp(b, 0, 1);
		return this;
	}
	
	/**
	 * Sets the fade colour of the particle being built. If unspecified, this defaults to the whatever the particle's base
	 * colour is.
	 * <p>
	 * <b>Affects:</b> {@link Type#DARK_MAGIC DARK_MAGIC}, {@link Type#DUST DUST}, {@link Type#FLASH FLASH},
	 * {@link Type#LEAF LEAF}, {@link Type#PATH PATH}, {@link Type#SPARKLE SPARKLE}
	 * @param r The red colour component to set; will be clamped to between 0 and 1
	 * @param g The green colour component to set; will be clamped to between 0 and 1
	 * @param b The blue colour component to set; will be clamped to between 0 and 1
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder fade(float r, float g, float b){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.fr = MathHelper.clamp(r, 0, 1);
		this.fg = MathHelper.clamp(g, 0, 1);
		this.fb = MathHelper.clamp(b, 0, 1);
		return this;
	}
	
	/**
	 * Sets the scale of the particle being built. If unspecified, this defaults to 1.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param scale The scale to set, as a multiple of the particle's default scale
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder scale(float scale){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.scale = scale;
		return this;
	}
	
	/**
	 * Sets the lifetime of the particle being built. If unspecified, this defaults to the particle's default lifetime,
	 * specified within its constructor.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param lifetime The lifetime to set in ticks
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder time(int lifetime){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.lifetime = lifetime;
		return this;
	}
	
	/**
	 * Sets the spin parameters of the particle being built. If unspecified, these both default to 0.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param radius The rotation radius to set
	 * @param speed The rotation speed to set, in revolutions per tick
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder spin(double radius, double speed){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.radius = radius;
		this.rpt = speed;
		return this;
	}
	
	/**
	 * Sets the gravity of the particle being built. If unspecified, this defaults to false.
	 * <p>
	 * <b>Affects:</b> {@link Type#ICE ICE}, {@link Type#SPARKLE SPARKLE}
	 * @param gravity True to enable gravity for the particle, false to disable
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder gravity(boolean gravity){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.gravity = gravity;
		return this;
	}
	
	/**
	 * Sets the shading of the particle being built. If unspecified, this defaults to false.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param shaded True to enable shading for the particle, false for full brightness
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder shaded(boolean shaded){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.shaded = shaded;
		return this;
	}
	
	/**
	 * Sets the collisions of the particle being built. If unspecified, this defaults to false.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param collide True to enable block collisions for the particle, false to disable
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder collide(boolean collide){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.collide = collide;
		return this;
	}
	
	/**
	 * Sets the entity of the particle being built. This will cause the particle to move with the given entity, and will
	 * make the position specified using {@link ParticleBuilder#pos(double, double, double)} <i>relative to</i> that
	 * entity's position.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param entity The entity to set (passing in null will do nothing but will not cause any problems, so for the sake
	 * of conciseness it is not necessary to perform a null check on the passed-in argument)
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder entity(Entity entity){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.entity = entity;
		return this;
	}
	
	/**
	 * Sets the rotation of the particle being built. If unspecified, the particle will use the default behaviour and
	 * rotate to face the viewer.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param yaw The yaw angle to set in degrees, where 0 is south.
	 * @param pitch The pitch angle to set in degrees, where 0 is horizontal.
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder face(float yaw, float pitch){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.yaw = yaw;
		this.pitch = pitch;
		return this;
	}
	
	/**
	 * Sets the rotation of the particle being built. This is an {@code EnumFacing}-based alternative to {@link
	 * ParticleBuilder#face(float, float)} which sets the yaw and pitch to the appropriate angles for the given facing.
	 * For example, if the given facing is {@code NORTH}, the particle will render parallel to the north face of blocks.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param direction The {@code EnumFacing} direction to set.
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder face(EnumFacing direction){
		return face(direction.getHorizontalAngle(), direction.getAxis().isVertical() ? direction.getAxisDirection().getOffset() * 90 : 0);
	}
	
	/**
	 * Sets the target of the particle being built. This will cause the particle to stretch to touch the given position.
	 * <p>
	 * <b>Affects:</b> 
	 * @param x The target x-coordinate to set
	 * @param y The target y-coordinate to set
	 * @param z The target z-coordinate to set
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder target(double x, double y, double z){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.tx = x;
		this.ty = y;
		this.tz = z;
		return this;
	}
	
	/**
	 * Sets the target of the particle being built. This is a vector-based alternative to {@link ParticleBuilder#
	 * target(double, double, double)}, allowing for even more concise code when a vector is available.
	 * <p>
	 * <b>Affects:</b> All particle types
	 * @param pos A vector representing the target position of the particle to be built.
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder target(Vec3d pos){
		return target(pos.x, pos.y, pos.z);
	}
	
	/**
	 * Sets the target of the particle being built. This will cause the particle to stretch to touch the given entity.
	 * <p>
	 * <b>Affects:</b> 
	 * @param target The entity to set
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public ParticleBuilder target(Entity target){
		if(!building) throw new IllegalStateException("Not building yet!");
		this.target = target;
		return this;
	}
	
	/**
	 * Spawns the particle that has been built and resets the particle builder.
	 * @param world The world in which to spawn the particle
	 * @throws IllegalStateException if the particle builder is not yet building.
	 */
	public void spawn(World world){
		
		if(!building) throw new IllegalStateException("Not building yet!");
		
		if(y < 0 && entity == null) Wizardry.logger.warn("Spawning particle below y = 0 - are you sure the position/entity"
				+ "has been set correctly?");
		
		if(!world.isRemote){
			Wizardry.logger.warn("ParticleBuilder.spawn(...) called on the server side! ParticleBuilder has prevented a "
					+ "server crash, but calling it on the server will do nothing. Consider adding a world.isRemote check.");
			// Must stop here because the line after this if statement would crash the server!
			reset();
			return;
		}
		
		electroblob.wizardry.client.particle.ParticleWizardry particle = Wizardry.proxy.createParticle(type, world, x, y, z);
		
		if(particle == null){
			reset();
			return;
		}
		
		// Anything with an if statement here allows default values to be set in particle constructors
		particle.multipleParticleScaleBy(scale);
		if(!Double.isNaN(vx) && !Double.isNaN(vy) && !Double.isNaN(vz)) particle.setVelocity(vx, vy, vz);
		if(r >= 0 && g >= 0 && b >= 0) particle.setRBGColorF(r, g, b);
		if(fr >= 0 && fg >= 0 && fb >= 0)particle.setFadeColour(fr, fg, fb);
		if(lifetime >= 0) particle.setMaxAge(lifetime);
		particle.setGravity(gravity);
		particle.setShaded(shaded);
		particle.setCollisions(collide);
		if(radius > 0) particle.setSpin(radius, rpt);
		particle.setEntity(entity);
		if(!Float.isNaN(yaw) && !Float.isNaN(pitch)) particle.setFacing(yaw, pitch);
		particle.setTargetPosition(tx, ty, tz);
		particle.setTargetEntity(target);
		
		net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		
		reset();
	}
	
	/** Resets the state of the particle builder and resets all the builder variables to their default values. */
	private void reset(){
		building = false;
		type = null;
		x = 0;
		y = 0;
		z = 0;
		// NaN indicates the velocity was not set (can't use -1 since it could very reasonably be -1)
		// For all other values -1 indicates the value was not set
		vx = Double.NaN;
		vy = Double.NaN;
		vz = Double.NaN;
		r = -1;
		g = -1;
		b = -1;
		fr = -1;
		fg = -1;
		fb = -1;
		radius = 0;
		rpt = 0;
		lifetime = -1;
		gravity = false;
		shaded = false;
		collide = false;
		scale = 1;
		entity = null;
		yaw = Float.NaN;
		pitch = Float.NaN;
		tx = Double.NaN;
		ty = Double.NaN;
		tz = Double.NaN;
		target = null;
	}
	
	// ============================================== Convenience methods ==============================================
	
	// These may seem to go against the whole point of this class, but of course they return the ParticleBuilder instance
	// so anything else can still be chained onto them - centralising commonly-used particle spawning patterns without
	// losing any of the flexibility of the particle builder. In addition, callers of these methods are still free to
	// change any of the parameters that were set within them afterwards.
	
	/**
	 * Starts building a particle of the given type and positions it randomly within the given entity's bounding box.
	 * Equivalent to calling {@code ParticleBuilder.create(type).pos(...)}; users should chain any additional builder
	 * methods onto this one and finish with {@code .spawn(world)} as normal.
	 * Used extensively with summoned creatures; makes code much neater and more concise.
	 * <p>
	 * <i>N.B. this does <b>not</b> cause the particle to move with the given entity.</i>
	 * @param type The type of particle to build
	 * @param entity The entity to position the particle at
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is already building.
	 */
	public static ParticleBuilder create(Type type, Entity entity){
		
		double x = entity.posX + (entity.world.rand.nextDouble() - 0.5D) * (double)entity.width;
		double y = entity.posY + entity.world.rand.nextDouble() * (double)entity.height;
		double z = entity.posZ + (entity.world.rand.nextDouble() - 0.5D) * (double)entity.width;
		
		return ParticleBuilder.instance.particle(type).pos(x, y, z);
	}
	
	/**
	 * Starts building a particle of the given type and positions it randomly within the given radius of the given position,
	 * with velocity proportional to distance from the given position if move is true. Good for making explosion-type effects.
	 * Equivalent to calling {@code ParticleBuilder.create(type).pos(...).vel(...)}; users should chain any additional builder
	 * methods onto this one and finish with {@code .spawn(world)} as normal.
	 * @param type The type of particle to build
	 * @param random An RNG instance
	 * @param x The x coordinate of the centre of the region in which to position the particle
	 * @param y The y coordinate of the centre of the region in which to position the particle
	 * @param z The z coordinate of the centre of the region in which to position the particle
	 * @param radius The radius of the region in which to position the particle
	 * @param move Whether the particle should move outwards from the centre (note that if this is false, the particle's
	 * default velocity will apply)
	 * @return The particle builder instance, allowing other methods to be chained onto this one
	 * @throws IllegalStateException if the particle builder is already building.
	 */
	public static ParticleBuilder create(Type type, Random random, double x, double y, double z, double radius, boolean move){
		
		double px = x + (random.nextDouble()*2 - 1) * radius;
		double py = y + (random.nextDouble()*2 - 1) * radius;
		double pz = z + (random.nextDouble()*2 - 1) * radius;
		
		if(move) return ParticleBuilder.instance.particle(type).pos(px, py, pz).vel(px-x, py-y, pz-z);
		
		return ParticleBuilder.instance.particle(type).pos(px, py, pz);
	}
	
	// Methods for spawning specific effects (similar to the FX playing methods with the ids in RenderGlobal)
	
	/** Spawns spark and large smoke particles (8 of each) within a 1x1x1 volume centred on the given position. */
	public static void spawnShockParticles(World world, double x, double y, double z) {
		double px, py, pz;
		for(int i=0; i<8; i++){
			px = x + world.rand.nextDouble() - 0.5;
			py = y + world.rand.nextDouble() - 0.5;
			pz = z + world.rand.nextDouble() - 0.5;
			ParticleBuilder.create(Type.SPARK).pos(px, py, pz).spawn(world);
			px = x + world.rand.nextDouble() - 0.5;
			py = y + world.rand.nextDouble() - 0.5;
			pz = z + world.rand.nextDouble() - 0.5;
			world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, px, py, pz, 0, 0, 0);
		}
	}
	
}
