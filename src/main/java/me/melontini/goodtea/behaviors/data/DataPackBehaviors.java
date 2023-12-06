package me.melontini.goodtea.behaviors.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.melontini.goodtea.behaviors.TeaBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;

import java.util.*;

public class DataPackBehaviors {

    public static final DataPackBehaviors INSTANCE = new DataPackBehaviors();

    final Map<Item, Holder> behaviors = new Object2ObjectLinkedOpenHashMap<>();
    final Set<Item> disabled = new ObjectOpenHashSet<>();

    public TeaBehavior.Behavior getBehavior(ItemStack stack) {
        return getBehavior(stack.getItem());
    }

    public TeaBehavior.Behavior getBehavior(Item item) {
        return Optional.ofNullable(behaviors.get(item)).map(Holder::getBehavior)
                .orElseGet(() -> (entity, stack) -> stack.getItem().finishUsing(stack, entity.world, entity));
    }

    public void addBehavior(Item item, TeaBehavior.Behavior behavior, boolean complement) {
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
        private final List<TeaBehavior.Behavior> behaviors = new ObjectArrayList<>();
        private final TeaBehavior.Behavior behavior = (entity, stack) -> behaviors.forEach(behavior1 -> behavior1.run(entity, stack));
        ;
        private final Item item;
        private boolean locked;

        public Holder(Item item) {
            this.item = item;
        }

        public void addBehavior(TeaBehavior.Behavior behavior, boolean complement) {
            if (!this.locked) {
                if (!complement) this.behaviors.clear();
                this.behaviors.add(behavior);
                if (!complement) this.locked = true;
            }
        }

        public Item getItem() {
            return item;
        }

        public TeaBehavior.Behavior getBehavior() {
            return behavior;
        }
    }

    public static class Data {

        public boolean disabled = false;
        public boolean complement = true;

        public CommandHolder commands;

        public record CommandHolder(List<String> user_commands, List<String> server_commands) {
        }
    }
}
