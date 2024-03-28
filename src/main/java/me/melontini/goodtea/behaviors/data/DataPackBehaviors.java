package me.melontini.goodtea.behaviors.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.melontini.goodtea.behaviors.TeaBehaviorProvider;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;

import java.util.*;
import java.util.function.Function;

public class DataPackBehaviors implements TeaBehaviorProvider {

    public static final DataPackBehaviors INSTANCE = new DataPackBehaviors();

    final Map<Item, Holder> behaviors = new Object2ObjectLinkedOpenHashMap<>();
    final Set<Item> disabled = new ObjectOpenHashSet<>();

    public TeaBehaviorProvider.Behavior getBehavior(Item item) {
        return Optional.ofNullable(behaviors.get(item)).map(Holder::getBehavior).orElseGet(this::defaultBehavior);
    }

    public void addBehavior(Item item, TeaBehaviorProvider.Behavior behavior, boolean complement) {
        if (disabled.contains(item)) return;

        Holder holder = behaviors.computeIfAbsent(item, Holder::new);
        holder.addBehavior(behavior, complement);
    }

    public void disable(Item item) {
        disabled.add(item);
        behaviors.remove(item);
    }

    public boolean disabled(Item item) {
        return this.disabled.contains(item);
    }

    public Set<Item> disabled() {
        return Collections.unmodifiableSet(this.disabled);
    }

    public void clear() {
        behaviors.clear();
        disabled.clear();
    }

    public Set<Item> itemsWithBehaviors() {
        return Collections.unmodifiableSet(behaviors.keySet());
    }

    public void acceptDummy(Item item) {
        if (disabled.contains(item)) return;

        behaviors.computeIfAbsent(item, Holder::new);
    }

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DataLoader());

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> INSTANCE.clear());
    }

    private static class Holder {
        private final List<TeaBehaviorProvider.Behavior> behaviors = new ObjectArrayList<>();
        private final TeaBehaviorProvider.Behavior behavior = (entity, stack) -> behaviors.forEach(behavior1 -> behavior1.run(entity, stack));
        private final Item item;
        private boolean locked;

        public Holder(Item item) {
            this.item = item;
        }

        public void addBehavior(TeaBehaviorProvider.Behavior behavior, boolean complement) {
            if (!this.locked) {
                if (!complement) this.behaviors.clear();
                this.behaviors.add(behavior);
                if (!complement) this.locked = true;
            }
        }

        public Item getItem() {
            return item;
        }

        public TeaBehaviorProvider.Behavior getBehavior() {
            return behavior;
        }
    }

    public record Data(List<Item> items, boolean disabled, boolean complement, List<String> user_commands, List<String> server_commands) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(data -> data.group(
                Codec.either(Registries.ITEM.getCodec(), Codec.list(Registries.ITEM.getCodec()))
                        .fieldOf("item_id").xmap(e -> e.map(ImmutableList::of, Function.identity()), Either::right).forGetter(Data::items),

                Codec.BOOL.optionalFieldOf("disabled", false).forGetter(Data::disabled),
                Codec.BOOL.optionalFieldOf("complement", true).forGetter(Data::disabled),

                Codec.list(Codec.STRING).optionalFieldOf("user_commands", Collections.emptyList()).forGetter(Data::user_commands),
                Codec.list(Codec.STRING).optionalFieldOf("server_commands", Collections.emptyList()).forGetter(Data::server_commands)
        ).apply(data, Data::new));
    }
}
