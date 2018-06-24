package electroblob.wizardry;

import electroblob.wizardry.client.particle.ParticleWizardry;
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
import electroblob.wizardry.util.ParticleBuilder.Type;
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

	public void registerSpellHUD(){}

	public net.minecraft.client.model.ModelBiped getWizardArmourModel(){
		return null;
	}

	public void initGuiBits(){}

	// SECTION Particles
	// ===============================================================================================================

	/** Called from init() in the main mod class to initialise the particle factories. */
	public void initParticleFactories(){} // Does nothing since particles are client-side only

	/** Creates a new particle of the specified type from the appropriate particle factory. <i>Does not actually spawn the
	 * particle; use {@link electroblob.wizardry.util.ParticleBuilder ParticleBuilder} to spawn particles.</i> */
	public ParticleWizardry createParticle(Type type, World world, double x, double y, double z){
		return null;
	}

	public void spawnTornadoParticle(World world, double x, double y, double z, double velX, double velZ, double radius,
			int maxAge, IBlockState block, BlockPos pos){
	}
	
	public void spawnEntityParticle(World world, Entity entity, int maxAge, float r, float g, float b){}

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

		Spell spell = Spell.get(scroll.getItemDamage());

		return I18n.translateToLocalFormatted("item." + Wizardry.MODID + ":scroll.name",
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