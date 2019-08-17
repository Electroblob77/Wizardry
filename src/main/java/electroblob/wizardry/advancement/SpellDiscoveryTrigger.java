package electroblob.wizardry.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.spell.Spell;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Advancement trigger that is triggered when a spell is discovered. The majority of any
 * ICriterionTrigger class is just boilerplate, and this is no exception. */
public class SpellDiscoveryTrigger implements ICriterionTrigger<SpellDiscoveryTrigger.Instance> {

	private final ResourceLocation id;
	private final Map<PlayerAdvancements, SpellDiscoveryTrigger.Listeners> listeners = Maps.newHashMap();

	public SpellDiscoveryTrigger(ResourceLocation id){
		this.id = id;
	}

	public ResourceLocation getId(){
		return this.id;
	}

	public void addListener(PlayerAdvancements advancements, Listener<SpellDiscoveryTrigger.Instance> listener){

		SpellDiscoveryTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners == null){
			listeners = new SpellDiscoveryTrigger.Listeners(advancements);
			this.listeners.put(advancements, listeners);
		}

		listeners.add(listener);
	}

	public void removeListener(PlayerAdvancements advancements, Listener<SpellDiscoveryTrigger.Instance> listener){

		SpellDiscoveryTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners != null){
			listeners.remove(listener);

			if(listeners.isEmpty()){
				this.listeners.remove(advancements);
			}
		}
	}

	public void removeAllListeners(PlayerAdvancements advancements){
		this.listeners.remove(advancements);
	}

	public SpellDiscoveryTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context){

		String s = JsonUtils.getString(json, "source");
		DiscoverSpellEvent.Source source = DiscoverSpellEvent.Source.byName(s);
		if(source == null) throw new JsonSyntaxException("No such spell discovery source: " + s);
		return new SpellDiscoveryTrigger.Instance(this.id, SpellPredicate.deserialize(json.get("spell")), source);
	}

	public void trigger(EntityPlayerMP player, Spell spell, DiscoverSpellEvent.Source source){

		SpellDiscoveryTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

		if(listeners != null){
			listeners.trigger(spell, source);
		}
	}

	public static class Instance extends AbstractCriterionInstance {

		private final SpellPredicate spell;
		private final DiscoverSpellEvent.Source source;

		public Instance(ResourceLocation criterion, SpellPredicate spell, DiscoverSpellEvent.Source source){
			super(criterion);
			this.spell = spell;
			this.source = source;
		}

		public boolean test(Spell spell, DiscoverSpellEvent.Source source){
			return this.spell.test(spell) && source == this.source;
		}
	}

	static class Listeners {

		private final PlayerAdvancements playerAdvancements;
		private final Set<Listener<SpellDiscoveryTrigger.Instance>> listeners = Sets.newHashSet();

		public Listeners(PlayerAdvancements advancements){
			this.playerAdvancements = advancements;
		}

		public boolean isEmpty(){
			return this.listeners.isEmpty();
		}

		public void add(Listener<SpellDiscoveryTrigger.Instance> listener){
			this.listeners.add(listener);
		}

		public void remove(Listener<SpellDiscoveryTrigger.Instance> listener){
			this.listeners.remove(listener);
		}

		public void trigger(Spell spell, DiscoverSpellEvent.Source source){

			List<Listener<SpellDiscoveryTrigger.Instance>> list = null;

			for(Listener<SpellDiscoveryTrigger.Instance> listener : this.listeners){

				if(listener.getCriterionInstance().test(spell, source)){

					if(list == null){
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if(list != null){
				for(Listener<SpellDiscoveryTrigger.Instance> listener : list){
					listener.grantCriterion(this.playerAdvancements);
				}
			}
		}
	}
}