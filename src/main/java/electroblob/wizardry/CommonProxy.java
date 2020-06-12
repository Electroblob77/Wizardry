package electroblob.wizardry;

import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.packet.*;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * The common proxy for wizardry, serving the usual purpose of dealing with all things that need to be handled
 * differently on the client and the server. A lot of the methods here appear to do absolutely nothing; this is because
 * they do client-only things which are only handled in the client proxy.
 * 
 * @see electroblob.wizardry.client.ClientProxy ClientProxy
 * @author Electroblob
 * @since Wizardry 1.0
 */
@SuppressWarnings("deprecation")
public class CommonProxy {

	// SECTION Registry
	// ===============================================================================================================

	public void registerRenderers(){}

	public void initialiseLayers(){}

	public void initialiseAnimations(){}

	public void registerKeyBindings(){}

	public net.minecraft.client.model.ModelBiped getWizardArmourModel(){
		return null;
	}

	public void initGuiBits(){}
	
	public void registerResourceReloadListeners(){}

	public void registerSoundEventListener(){}

	public void registerAtlasMarkers(){}

	// SECTION Particles
	// ===============================================================================================================

	/** Called from init() in the main mod class to initialise the particle factories. */
	public void registerParticles(){} // Does nothing since particles are client-side only

	/** Creates a new particle of the specified type from the appropriate particle factory. <i>Does not actually spawn the
	 * particle; use {@link electroblob.wizardry.util.ParticleBuilder ParticleBuilder} to spawn particles.</i> */
	public electroblob.wizardry.client.particle.ParticleWizardry createParticle(ResourceLocation type, World world, double x, double y, double z){
		return null;
	}

	public void spawnTornadoParticle(World world, double x, double y, double z, double velX, double velZ, double radius,
			int maxAge, IBlockState block, BlockPos pos){
	}

	// SECTION Items
	// ===============================================================================================================

	public boolean shouldDisplayDiscovered(Spell spell, @Nullable ItemStack stack){
		return false;
	}

	public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack){
		return null;
	}

	/**
	 * Returns the translated name of the scroll, taking spell discovery into account. If, for some reason, this gets
	 * called server-side, it uses the deprecated server version of I18n. Since any likely server-side
	 * use of this method will be for text-based logic purposes (like Bibliocraft's book checking system), and since
	 * no particular player instance can be accessed, spell discovery is ignored.
	 */
	public String getScrollDisplayName(ItemStack scroll){

		Spell spell = Spell.byMetadata(scroll.getItemDamage());

		return I18n.translateToLocalFormatted("item." + Wizardry.MODID + ":scroll.name",
				I18n.translateToLocal("spell." + spell.getUnlocalisedName())).trim();
	}

	public double getConjuredBowDurability(ItemStack stack){
		return ((ItemSpectralBow)WizardryItems.spectral_bow).getDefaultDurabilityForDisplay(stack);
	}

	/**
	 * Translates the given key with no specified style. Client-side only; on the server this simply returns
	 * the given translation key. Useful whenever translation from common classes is required, e.g. item tooltips.
	 * @param key The unlocalised name to be translated.
	 * @param args The format arguments to pass into the translation, if any.
	 * @return The resulting translated text.
	 */
	public String translate(String key, Object... args){
		return translate(key, new Style(), args);
	}

	/**
	 * Translates the given key and formats it with the given style. Client-side only; on the server this simply returns
	 * the given translation key. Useful whenever translation from common classes is required, e.g. item tooltips.
	 * @param key The unlocalised name to be translated.
	 * @param style The {@link Style} to use for the displayed text.
	 * @param args The format arguments to pass into the translation, if any.
	 * @return The resulting translated text.
	 */
	public String translate(String key, Style style, Object... args){
		return key;
	}

	/** Like {@link CommonProxy#addMultiLineDescription(List, String, Style, Object...)}, but style defaults to light grey. */
	public void addMultiLineDescription(List<String> tooltip, String key, Object... args){
		this.addMultiLineDescription(tooltip, key, new Style().setColor(TextFormatting.GRAY), args);
	}

	/**
	 * Adds a multi-line description to the given tooltip list. The description is first translated using the given
	 * translation key, then the formatting code for the given style is appended, and finally the string is word-wrapped
	 * to the standard width (100).
	 * @param tooltip The tooltip list to add to
	 * @param key The translation key for the description
	 * @param style A style to apply
	 */
	public void addMultiLineDescription(List<String> tooltip, String key, Style style, Object... args){}

	// SECTION Packet Handlers
	// ===============================================================================================================

	public void handlePlayerSyncPacket(PacketPlayerSync.Message message){}

	public void handleGlyphDataPacket(PacketGlyphData.Message message){}

	public void handleEmitterDataPacket(PacketEmitterData.Message message){}

	public void handleCastSpellPacket(PacketCastSpell.Message message){}

	public void handleCastContinuousSpellPacket(PacketCastContinuousSpell.Message message){}

	public void handleNPCCastSpellPacket(PacketNPCCastSpell.Message message){}

	public void handleDispenserCastSpellPacket(PacketDispenserCastSpell.Message message){}

	public void handleCastSpellAtPosPacket(PacketCastSpellAtPos.Message message){}

	public void handleTransportationPacket(PacketTransportation.Message message){}

	public void handleClairvoyancePacket(PacketClairvoyance.Message message){}

	public void handleAdvancementSyncPacket(PacketSyncAdvancements.Message message){}

	public void handleResurrectionPacket(PacketResurrection.Message message){}

	public void handlePossessionPacket(PacketPossession.Message message){}

	public void handleConquerShrinePacket(PacketConquerShrine.Message message){}

	// SECTION Misc
	// ===============================================================================================================

	public void setToNumberSliderEntry(Property property){}
	
	public void setToHUDChooserEntry(Property property){}

	public void setToNamedBooleanEntry(Property property){}
	
	// public void setToEntityNameEntry(Property property){}

	/**
	 * Plays a sound which moves with the given entity.
	 * 
	 * @param entity The source of the sound
	 * @param sound The SoundEvent to play
	 * @param category The SoundCategory to use
	 * @param volume Volume relative to 1
	 * @param pitch Pitch relative to 1
	 * @param repeat Whether to repeat the sound for as long as the entity is alive (or until stopped manually)
	 */
	public void playMovingSound(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat){}

	/**
	 * Plays the spell charge-up sound at the given entity.
	 * @param entity The source of the sound
	 */
	public void playChargeupSound(EntityLivingBase entity){}

	/**
	 * Plays a continuous spell sound which moves with the given entity.
	 *
	 * @param entity The source of the sound
	 * @param spell The spell this sound is associated with
	 * @param start The starting SoundEvent to play
	 * @param loop The main looped SoundEvent to play
	 * @param end The ending SoundEvent to play
	 * @param category The SoundCategory to use
	 * @param volume Volume relative to 1
	 * @param pitch Pitch relative to 1
	 */
	public void playSpellSoundLoop(EntityLivingBase entity, Spell spell, SoundEvent start, SoundEvent loop, SoundEvent end, SoundCategory category, float volume, float pitch){}

	/**
	 * Plays a continuous spell sound which moves with the given entity.
	 *
	 * @param entity The source of the sound
	 * @param spell The spell this sound is associated with
	 * @param sounds An array of the sound events to play, which must have three elements in the order: start, loop, end
	 * @param category The SoundCategory to use
	 * @param volume Volume relative to 1
	 * @param pitch Pitch relative to 1
	 * @throws IllegalArgumentException if the given array contains less than 3 sound events
	 */
	public void playSpellSoundLoop(EntityLivingBase entity, Spell spell, SoundEvent[] sounds, SoundCategory category, float volume, float pitch){
		if(sounds.length < 3) throw new IllegalArgumentException("Tried to play a continuous spell sound using an array "
				+ "of sound events, but the given array contained less than 3 sound events!");
		playSpellSoundLoop(entity, spell, sounds[0], sounds[1], sounds[2], category, volume, pitch);
	}

	/**
	 * Plays a continuous spell sound at the given position.
	 *  @param world The world in which to play the sound
	 * @param x The x coordinate to play the sound at
	 * @param y The y coordinate to play the sound at
	 * @param z The z coordinate to play the sound at
	 * @param spell The spell this sound is associated with
	 * @param start The starting SoundEvent to play
	 * @param loop The main looped SoundEvent to play
	 * @param end The ending SoundEvent to play
	 * @param category The SoundCategory to use
	 * @param volume Volume relative to 1
	 * @param pitch Pitch relative to 1
	 * @param duration The duration of the sound, or -1 to link it to a dispenser at the given coordinates
	 */
	public void playSpellSoundLoop(World world, double x, double y, double z, Spell spell, SoundEvent start, SoundEvent loop, SoundEvent end, SoundCategory category, float volume, float pitch, int duration){}

	/**
	 * Plays a continuous spell sound at the given position.
	 *
	 * @param world The world in which to play the sound
	 * @param x The x coordinate to play the sound at
	 * @param y The y coordinate to play the sound at
	 * @param z The z coordinate to play the sound at
	 * @param spell The spell this sound is associated with
	 * @param sounds An array of the sound events to play, which must have three elements in the order: start, loop, end
	 * @param category The SoundCategory to use
	 * @param volume Volume relative to 1
	 * @param pitch Pitch relative to 1
	 * @param duration The duration of the sound, or -1 to link it to a dispenser at the given coordinates
	 * @throws IllegalArgumentException if the given array contains less than 3 sound events
	 */
	public void playSpellSoundLoop(World world, double x, double y, double z, Spell spell, SoundEvent[] sounds, SoundCategory category, float volume, float pitch, int duration){
		if(sounds.length < 3) throw new IllegalArgumentException("Tried to play a continuous spell sound using an array "
				+ "of sound events, but the given array contained less than 3 sound events!");
		playSpellSoundLoop(world, x, y, z, spell, sounds[0], sounds[1], sounds[2], category, volume, pitch, duration);
	}

	/** Starts the first-person blink overlay effect for the specified player. */
	public void playBlinkEffect(EntityPlayer player){}

	/**
	 * Starts the client-side screen shake effect for the specified player.
	 * @param player The player whose screen is to be shaken
	 * @param intensity The amplitude of the shaking (around 10 looks about right)
	 */
	public void shakeScreen(EntityPlayer player, float intensity){}

	/**
	 * Loads the given shader for the given player, if they have shaders enabled.
	 * @param player The player to load the shader for
	 * @param shader The location of the shader to load
	 */
	public void loadShader(EntityPlayer player, ResourceLocation shader){}
	
	/**
	 * Gets the client-side world using {@code Minecraft.getMinecraft().world}. <b>Only to be called client side!</b>
	 * Returns null on the server side.
	 */
	public World getTheWorld(){
		return null;
	}

	/**
	 * Gets the client-side player using Minecraft.getMinecraft().player. <b>Only to be called client side!</b> Returns
	 * null on the server side.
	 */
	public EntityPlayer getThePlayer(){
		return null;
	}

	/**
	 * Returns true if the game is being viewed from the perspective of the given entity and is set to first-person
	 * view. Always returns false on the server side.
	 */
	public boolean isFirstPerson(Entity entity){
		return false;
	}

	/** Returns an unmodifiable set of the string keys for all of the loaded spell HUD skins. */
	public Set<String> getSpellHUDSkins(){
		return null;
	}

	/** Notifies nearby players of a bookshelf change, causing any lectern or arcane workbench GUI (client-side) or
	 * container (both sides) to refresh its linked bookshelves (does not send packets). */
	public void notifyBookshelfChange(World world, BlockPos pos){
		for(EntityPlayer player : world.playerEntities){
			if(player.getDistanceSq(pos) < BlockBookshelf.PLAYER_NOTIFY_RANGE * BlockBookshelf.PLAYER_NOTIFY_RANGE){
				if(player.openContainer instanceof ContainerArcaneWorkbench){
					((ContainerArcaneWorkbench)player.openContainer).refreshBookshelfSlots();
				}
			}
		}
	}

}