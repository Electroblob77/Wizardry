package electroblob.wizardry.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Advancement trigger for things done in the arcane workbench. The majority of any
 * ICriterionTrigger class is just boilerplate, and this is no exception. */
public class ArcaneWorkbenchTrigger implements ICriterionTrigger<ArcaneWorkbenchTrigger.Instance> {

	private final ResourceLocation id;
	private final Map<PlayerAdvancements, ArcaneWorkbenchTrigger.Listeners> listeners = Maps.newHashMap();

	public ArcaneWorkbenchTrigger(ResourceLocation id){
		this.id = id;
	}

	public ResourceLocation getId(){
		return this.id;
	}

	public void addListener(PlayerAdvancements advancements, Listener<ArcaneWorkbenchTrigger.Instance> listener){

		ArcaneWorkbenchTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners == null){
			listeners = new ArcaneWorkbenchTrigger.Listeners(advancements);
			this.listeners.put(advancements, listeners);
		}

		listeners.add(listener);
	}

	public void removeListener(PlayerAdvancements advancements, Listener<ArcaneWorkbenchTrigger.Instance> listener){

		ArcaneWorkbenchTrigger.Listeners listeners = this.listeners.get(advancements);

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

	public ArcaneWorkbenchTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context){
		return new ArcaneWorkbenchTrigger.Instance(this.id, ItemPredicate.deserialize(json.get("item")));
	}

	public void trigger(EntityPlayerMP player, ItemStack stack){

		ArcaneWorkbenchTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

		if(listeners != null){
			listeners.trigger(stack);
		}
	}

	public static class Instance extends AbstractCriterionInstance {

		private final ItemPredicate item;

		public Instance(ResourceLocation criterionIn, ItemPredicate item){
			super(criterionIn);
			this.item = item;
		}

		public boolean test(ItemStack stack){
			return this.item.test(stack);
		}
	}

	static class Listeners {

		private final PlayerAdvancements playerAdvancements;
		private final Set<Listener<ArcaneWorkbenchTrigger.Instance>> listeners = Sets.newHashSet();

		public Listeners(PlayerAdvancements advancements){
			this.playerAdvancements = advancements;
		}

		public boolean isEmpty(){
			return this.listeners.isEmpty();
		}

		public void add(Listener<ArcaneWorkbenchTrigger.Instance> listener){
			this.listeners.add(listener);
		}

		public void remove(Listener<ArcaneWorkbenchTrigger.Instance> listener){
			this.listeners.remove(listener);
		}

		public void trigger(ItemStack stack){

			List<Listener<ArcaneWorkbenchTrigger.Instance>> list = null;

			for(Listener<ArcaneWorkbenchTrigger.Instance> listener : this.listeners){

				if(listener.getCriterionInstance().test(stack)){

					if(list == null){
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if(list != null){
				for(Listener<ArcaneWorkbenchTrigger.Instance> listener : list){
					listener.grantCriterion(this.playerAdvancements);
				}
			}
		}
	}
}