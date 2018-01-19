package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.block.BlockTransportationStone;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Transportation extends Spell {

	public Transportation(){
		super(Tier.ADVANCED, 100, Element.SORCERY, "transportation", SpellType.UTILITY, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		WizardData properties = WizardData.get(caster);
		// Fixes the sound not playing in first person.
		if(world.isRemote) WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);
		
		// Only works when the caster is in the same dimension.
		if(properties != null && properties.getTpCountdown() == 0){
			if(caster.dimension == properties.getStoneCircleDimension()){
				// Has to be y since x and z could reasonably be -1.
				if(properties.getStoneCircleLocation() != null){
					if(BlockTransportationStone.testForCircle(world, properties.getStoneCircleLocation())){
						WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_PORTAL_TRIGGER, 1.0f, 1.0f);
						caster.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 150, 0));
						properties.setTpCountdown(75);
						return true;
					}else{
						if(!world.isRemote) caster.addChatComponentMessage(new TextComponentTranslation("spell.transportation.missing"));
					}
				}else{
					if(!world.isRemote) caster.addChatComponentMessage(new TextComponentTranslation("spell.transportation.undefined"));
				}
			}else{
				if(!world.isRemote) caster.addChatComponentMessage(new TextComponentTranslation("spell.transportation.wrongdimension"));
			}
		}
		return false;
	}


}
