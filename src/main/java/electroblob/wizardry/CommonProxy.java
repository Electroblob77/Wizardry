package electroblob.wizardry;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import cpw.mods.fml.common.FMLCommonHandler;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.PacketClairvoyance;
import electroblob.wizardry.packet.PacketGlyphData;
import electroblob.wizardry.packet.PacketPlayerSync;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;

/** The common proxy for wizardry, serving the usual purpose of dealing with all things that need to be handled
 * differently on the client and the server. A lot of the methods here appear to do absolutely nothing; this is
 * because they do client-only things which are only handled in the client proxy.
 * @see {@link electroblob.wizardry.client.ClientProxy} */
public class CommonProxy {

	/** Used to store IExtendedEntityProperties data temporarily between player death and respawn */
	private static final Map<String, NBTTagCompound> extendedEntityData = new HashMap<String, NBTTagCompound>();
	
    public void registerRenderers(){}
    
    public void registerKeyBindings(){}
    
	public void registerSpellHUD(){}
    
	public ModelBiped getWizardArmourModel(){
		return null;
	}
	
    /**
    * Adds an entity's custom data to the map for temporary storage
    * @param compound An NBT Tag Compound that stores the IExtendedEntityProperties data only
    */
    public static void storeEntityData(String name, NBTTagCompound compound){
    	extendedEntityData.put(name, compound);
    }

    /**
    * Removes the compound from the map and returns the NBT tag stored for name or null if none exists
    */
    public static NBTTagCompound getEntityData(String name){
    	return extendedEntityData.remove(name);
    }
    
    public double getPlayerEyesPos(EntityPlayer player){
		return player.posY + player.eyeHeight;
    }
    
    /** Gets the absolute coordinates of the tip of the wand belonging to the given entity. If this is a client in
     * first person view, the coordinates will instead be the entity's position + 1.2. */
    public Vec3 getWandTipPosition(EntityLivingBase entity){
		
		// 0.7 forwards, 0.4 to the right, and 1.35 upwards.
		
		double x = entity.posX - 0.7*Math.sin(Math.toRadians(entity.renderYawOffset)) - 0.4*Math.cos(Math.toRadians(entity.renderYawOffset));
		double y = entity.boundingBox.minY + 1.35;
		double z = entity.posZ + 0.7*Math.cos(Math.toRadians(entity.renderYawOffset)) - 0.4*Math.sin(Math.toRadians(entity.renderYawOffset));
		
		return Vec3.createVectorHelper(x, y, z);
	}
    
    /**
     * Spawns a custom particle of the specified type.
     * 
     * @param type EnumParticleType of the particle
     * @param world Reference to the World object
     * @param x Particle x position
     * @param y Particle y position
     * @param z Particle z position
     * @param velX Particle x velocity
     * @param velY Particle y velocity
     * @param velZ Particle z velocity
     * @param maxAge Lifetime of the particle in ticks
     * @param r Red component of particle colour
     * @param g Red component of particle colour
     * @param b Red component of particle colour
     * @param doGravity Whether the particle is affected by gravity (only affects SPARKLE at the moment)
     * @param radius The radius of the particle's motion, for cirular motion particles
     */
    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double velX, double velY, double velZ, int maxAge, float r, float g, float b, boolean doGravity, double radius){
    	// Does nothing since particles are client-side only
    }
    
    /** Spawns a custom particle of the specified type. doGravity defaults to false and radius defaults to 0. 
     * Note that some of these settings may not affect the particle; some particles are always affected by
     * gravity, for instance.
     * 
     * @param type EnumParticleType of the particle
     * @param world Reference to the World object
     * @param x Particle x position
     * @param y Particle y position
     * @param z Particle z position
     * @param velX Particle x velocity
     * @param velY Particle y velocity
     * @param velZ Particle z velocity
     * @param maxAge Lifetime of the particle in ticks
     * @param r Red component of particle colour (unless this is a MAGIC_FIRE particle, in which case this is the scale)
     * @param g Red component of particle colour
     * @param b Red component of particle colour
     */
    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double velX, double velY, double velZ, int maxAge, float r, float g, float b){
    	this.spawnParticle(type, world, x, y, z, velX, velY, velZ, maxAge, r, g, b, false, 0);
    }
    
    /** Spawns a custom particle of the specified type. Colour defaults to white, doGravity defaults to false and radius
     * defaults to 0. Note that some of these settings may not affect the particle; some particles are always affected by
     * gravity, for instance.
     * 
     * @param type EnumParticleType of the particle
     * @param world Reference to the World object
     * @param x Particle x position
     * @param y Particle y position
     * @param z Particle z position
     * @param velX Particle x velocity
     * @param velY Particle y velocity
     * @param velZ Particle z velocity
     * @param maxAge Lifetime of the particle in ticks
     */
    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double velX, double velY, double velZ, int maxAge){
    	this.spawnParticle(type, world, x, y, z, velX, velY, velZ, maxAge, 1, 1, 1, false, 0);
    }
    
    public void spawnDigParticle(World world, double x, double y, double z, double velX, double velY, double velZ, Block block){}

	public void spawnTornadoParticle(World world, double x, double y, double z, double velX, double velZ, double radius, int maxAge, Block block, int metadata){}
    
    /** Plays a sound which moves with the given entity.
     * 
     * @param entity The source of the sound
     * @param soundName String resource path of the sound
     * @param volume Volume relative to 1
     * @param pitch Pitch relative to 1
     * @param repeat Whether to repeat the sound for as long as the entity is alive (or until stopped manually)
     */
	public void playMovingSound(Entity entity, String soundName, float volume, float pitch, boolean repeat){}
    
    public void handleCastSpellPacket(PacketCastSpell.Message message){}
    
    public double getWandDisplayDamage(ItemStack stack){
    	return 1.0d;
    }
    
	public int getConjuredItemDisplayDamage(ItemStack stack){
		return 0;
	}

	public void registerEventHandlers(){
		MinecraftForge.EVENT_BUS.register(new WizardryEventHandler());
	}

	public void handleTransportationPacket(PacketTransportation.Message message){}

	public FontRenderer getFontRenderer(ItemStack stack) {
		return null;
	}
	
	/** Returns the translated name of the scroll, taking spell discovery into account. If, for some reason, this gets
	 * called server-side, it uses the deprecated server version of I18n, with a warning. Since any likely server-side
	 * use of this method will be for text-based logic purposes (like Bibliocraft's book checking system), and since no
	 * particular player instance can be accessed, spell discovery is ignored. */
	public String getScrollDisplayName(ItemStack scroll){
		
		// I have now learnt that the server side I18n always translates to the default en_US, so I could just return
		// a hardcoded name in English instead.
		//Wizardry.logger.info("A mod has called ItemScroll#getItemStackDisplayName from the server side. Using the"
		//		+ "deprecated server-side translation methods as a fallback.");
		
		// Displays [Empty slot] if spell is continuous.
		Spell spell = Spell.get(scroll.getItemDamage());
		if(spell.isContinuous) spell = WizardryRegistry.none;
		
		return StatCollector.translateToLocalFormatted("item.scroll.name", StatCollector.translateToLocal("spell." + spell.getUnlocalisedName())).trim();
	}

	public void handlePlayerSyncPacket(PacketPlayerSync.Message message){}

	public void handleGlyphDataPacket(PacketGlyphData.Message message){}

	public void handleCastContinuousSpellPacket(PacketCastContinuousSpell.Message message){}

	public void setToNumberSliderEntry(Property property){}

	public void handleClairvoyancePacket(PacketClairvoyance.Message message){}

}