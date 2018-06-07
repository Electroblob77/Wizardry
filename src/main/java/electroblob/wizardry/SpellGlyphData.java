package electroblob.wizardry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import electroblob.wizardry.packet.PacketGlyphData;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

/** Class responsible for generating and storing the randomised spell names and descriptions for each world, which are
 * displayed as glyphs using the SGA font renderer.
 * @since Wizardry 1.1 */
public class SpellGlyphData extends WorldSavedData {

	private static final String NAME = Wizardry.MODID + "_glyphData";
	
	public Map<Spell, String> randomNames = new HashMap<Spell, String>(Spell.getTotalSpellCount());
	public Map<Spell, String> randomDescriptions = new HashMap<Spell, String>(Spell.getTotalSpellCount());

	// Required constructors
	public SpellGlyphData() {
		this(NAME);
	}

	public SpellGlyphData(String name){
		super(name);
	}
	
	/** Generates random names and descriptions for any spells which don't already have them. */
	public void generateGlyphNames(World world){

		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			if(!randomNames.containsKey(spell)) randomNames.put(spell, generateRandomName(world.rand));
		}
		
		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			if(!randomDescriptions.containsKey(spell)) randomDescriptions.put(spell, generateRandomDescription(world.rand));
		}
		
		this.markDirty();
	}
	
	private String generateRandomName(Random random){
		
		String name = "";
		
		for(int i=0; i<random.nextInt(2)+2; i++){
			name = name + RandomStringUtils.random(3 + random.nextInt(5), "abcdefghijklmnopqrstuvwxyz") + " ";
		}
		
		return name.trim();
	}
	
	private String generateRandomDescription(Random random){
		
		String name = "";
		
		for(int i=0; i<random.nextInt(16)+8; i++){
			name = name + RandomStringUtils.random(2 + random.nextInt(7), "abcdefghijklmnopqrstuvwxyz") + " ";
		}
		
		return name.trim();
	}

	/** Returns the spell glyph data for this world, or creates a new instance if it doesn't exist yet. Also checks
	 * for any spells that are missing glyph data and adds it accordingly. */
	public static SpellGlyphData get(World world) {

		SpellGlyphData instance = (SpellGlyphData) world.loadItemData(SpellGlyphData.class, NAME);

		if(instance == null){
			instance = new SpellGlyphData();
		}
		
		// These two conditions are a bit of backwards compatibility from when I added the descriptions to the
		// glyph data. Shouldn't be needed in normal operation, but I might as well leave it here.
		// Edit: More backwards compatibility, this time for the future - should any new spells be added, this now ensures
		// existing worlds will generate random names and descriptions for any new spells whilst keeping the old ones.
		if(instance.randomNames.size() < Spell.getTotalSpellCount() || instance.randomDescriptions.size() < Spell.getTotalSpellCount()){
			instance.generateGlyphNames(world);
			world.setItemData(NAME, instance);
		}
		
		return instance;
	}
	
	/** Sends the random spell names for this world to the specified player's client. */
	public void sync(EntityPlayerMP player){
		
		List<String> names = new ArrayList<String>();
		List<String> descriptions = new ArrayList<String>();
		
		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			names.add(this.randomNames.get(spell));
			descriptions.add(this.randomDescriptions.get(spell));
		}
		
		PacketGlyphData.Message msg = new PacketGlyphData.Message(names, descriptions);
		
		WizardryPacketHandler.net.sendTo(msg, player);
		
	}
	
	/** Helper method to retrieve the random glyph name for the given spell from the map stored in the given world. */
	public static String getGlyphName(Spell spell, World world){
		Map<Spell, String> names = SpellGlyphData.get(world).randomNames;
		return names == null ? "" : names.get(spell);
	}
	
	/** Helper method to retrieve the random glyph description for the given spell from the map stored in the given world. */
	public static String getGlyphDescription(Spell spell, World world){
		Map<Spell, String> descriptions = SpellGlyphData.get(world).randomDescriptions;
		return descriptions == null ? "" : descriptions.get(spell);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		
		this.randomNames = new HashMap<Spell, String>();
		this.randomDescriptions = new HashMap<Spell, String>();
		
		NBTTagList tagList = nbt.getTagList("spellGlyphData", NBT.TAG_COMPOUND);
		
		for(int i=0; i<tagList.tagCount(); i++){
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			randomNames.put(Spell.get(tag.getInteger("spell")), tag.getString("name"));
			randomDescriptions.put(Spell.get(tag.getInteger("spell")), tag.getString("description"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt){

		NBTTagList tagList = new NBTTagList();
		
		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			// Much like the enchantments tag for items, this stores a list of spell-id-to-name tag pairs
			// The description is now also included; there's no point in making a second compound tag!
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("spell", spell.id());
			tag.setString("name", this.randomNames.get(spell));
			tag.setString("description", this.randomDescriptions.get(spell));
			tagList.appendTag(tag);
		}
		
		nbt.setTag("spellGlyphData", tagList);
	}
}