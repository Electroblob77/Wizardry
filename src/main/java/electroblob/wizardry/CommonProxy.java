package electroblob.wizardry;

import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.PacketClairvoyance;
import electroblob.wizardry.packet.PacketGlyphData;
import electroblob.wizardry.packet.PacketNPCCastSpell.Message;
import electroblob.wizardry.packet.PacketPlayerSync;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;

/**
 * The common proxy for wizardry, serving the usual purpose of dealing with all things that need to be handled
 * differently on the client and the server. A lot of the methods here appear to do absolutely nothing; this is because
 * they do client-only things which are only handled in the client proxy.
 * 
 * @see {@link electroblob.wizardry.client.ClientProxy}
 * @author Electroblob
 * @since Wizardry 1.0
 */
@SuppressWarnings("deprecation")
public class CommonProxy {

	// SECTION Registry
	// ===============================================================================================================

	public void registerRenderers(){
	}

	public void initialiseLayers(){
	}

	public void registerKeyBindings(){
	}

	public void registerSpellHUD(){
	}

	public net.minecraft.client.model.ModelBiped getWizardArmourModel(){
		return null;
	}

	public void initMixedFontRenderer(){
	}

	// SECTION Particles
	// ===============================================================================================================

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
	 * @param r Red component of particle colour; will be clamped to between 0 and 1
	 * @param g Red component of particle colour; will be clamped to between 0 and 1
	 * @param b Red component of particle colour; will be clamped to between 0 and 1
	 * @param doGravity Whether the particle is affected by gravity (only affects SPARKLE at the moment)
	 * @param radius The radius of the particle's motion, for cirular motion particles
	 */
	public void spawnParticle(WizardryParticleType type, World world, double x, double y, double z, double velX,
			double velY, double velZ, int maxAge, float r, float g, float b, boolean doGravity, double radius){
		// Does nothing since particles are client-side only
	}

	/**
	 * Spawns a custom particle of the specified type. doGravity defaults to false and radius defaults to 0. Note that
	 * some of these settings may not affect the particle; some particles are always affected by gravity, for instance.
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
	 * @param r Red component of particle colour; will be clamped to between 0 and 1 (unless this is a MAGIC_FIRE
	 *        particle, in which case this is the scale)
	 * @param g Red component of particle colour; will be clamped to between 0 and 1
	 * @param b Red component of particle colour; will be clamped to between 0 and 1
	 */
	public void spawnParticle(WizardryParticleType type, World world, double x, double y, double z, double velX,
			double velY, double velZ, int maxAge, float r, float g, float b){
		this.spawnParticle(type, world, x, y, z, velX, velY, velZ, maxAge, r, g, b, false, 0);
	}

	/**
	 * Spawns a custom particle of the specified type. Colour defaults to white, doGravity defaults to false and radius
	 * defaults to 0. Note that some of these settings may not affect the particle; some particles are always affected
	 * by gravity, for instance.
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
	public void spawnParticle(WizardryParticleType type, World world, double x, double y, double z, double velX,
			double velY, double velZ, int maxAge){
		this.spawnParticle(type, world, x, y, z, velX, velY, velZ, maxAge, 1, 1, 1, false, 0);
	}

	public void spawnTornadoParticle(World world, double x, double y, double z, double velX, double velZ, double radius,
			int maxAge, IBlockState block, BlockPos pos){
	}

	// SECTION Items
	// ===============================================================================================================

	public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack){
		return null;
	}

	/**
	 * Returns the translated name of the scroll, taking spell discovery into account. If, for some reason, this gets
	 * called server-side, it uses the deprecated server version of I18n, with a warning. Since any likely server-side
	 * use of this method will be for text-based logic purposes (like Bibliocraft's book checking system), and since no
	 * particular player instance can be accessed, spell discovery is ignored.
	 */
	public String getScrollDisplayName(ItemStack scroll){

		// I have now learnt that the server side I18n always translates to the default en_US, so I could just return
		// a hardcoded name in English instead.
		Wizardry.logger.info("A mod has called ItemScroll#getItemStackDisplayName from the server side. Using the"
				+ "deprecated server-side translation methods as a fallback.");

		// Displays [Empty slot] if spell is continuous.
		Spell spell = Spell.get(scroll.getItemDamage());
		if(spell.isContinuous) spell = Spells.none;

		return I18n.translateToLocalFormatted("item.wizardry:wizardry:scroll.name",
				I18n.translateToLocal("spell." + spell.getUnlocalisedName())).trim();
	}

	public double getConjuredBowDurability(ItemStack stack){
		return ((ItemSpectralBow)WizardryItems.spectral_bow).getDefaultDurabilityForDisplay(stack);
	}

	// SECTION Packet Handlers
	// ===============================================================================================================

	public void handlePlayerSyncPacket(PacketPlayerSync.Message message){
	}

	public void handleGlyphDataPacket(PacketGlyphData.Message message){
	}

	public void handleCastSpellPacket(PacketCastSpell.Message message){
	}

	public void handleCastContinuousSpellPacket(PacketCastContinuousSpell.Message message){
	}

	public void handleNPCCastSpellPacket(Message message){
	}

	public void handleTransportationPacket(PacketTransportation.Message message){
	}

	public void handleClairvoyancePacket(PacketClairvoyance.Message message){
	}

	// SECTION Misc
	// ===============================================================================================================

	public void setToNumberSliderEntry(Property property){
	}
	// public void setToEntityNameEntry(Property property){}

	/**
	 * Plays a sound which moves with the given entity.
	 * 
	 * @param entity The source of the sound
	 * @param sound The SoundEvent to play
	 * @param volume Volume relative to 1
	 * @param pitch Pitch relative to 1
	 * @param repeat Whether to repeat the sound for as long as the entity is alive (or until stopped manually)
	 */
	public void playMovingSound(Entity entity, SoundEvent sound, float volume, float pitch, boolean repeat){
	}

	/**
	 * Gets the client side world using Minecraft.getMinecraft().world. <b>Only to be called client side!</b> Returns
	 * null on the server side.
	 */
	public World getTheWorld(){
		return null;
	}

}