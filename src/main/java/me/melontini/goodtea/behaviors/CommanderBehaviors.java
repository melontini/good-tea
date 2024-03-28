package me.melontini.goodtea.behaviors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.melontini.commander.command.ConditionedCommand;
import me.melontini.commander.data.Subscription;
import me.melontini.commander.data.types.EventTypes;
import me.melontini.commander.event.EventContext;
import me.melontini.commander.event.EventType;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

import static me.melontini.goodtea.util.GoodTeaStuff.id;
import static net.minecraft.loot.context.LootContextParameters.*;

public class CommanderBehaviors implements TeaBehaviorProvider {

    public static final EventType EVENT_TYPE = EventTypes.register(id("drank_tea"), EventType.builder()
            .extension(Data.CODEC, s -> new CommanderBehaviors(s).apply()).build());

    final Map<Item, Holder> behaviors = new Object2ObjectLinkedOpenHashMap<>();
    final Set<Item> disabled = new ObjectOpenHashSet<>();
    private final List<Subscription<Data>> subscriptions;

    public CommanderBehaviors(List<Subscription<Data>> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public TeaBehaviorProvider.Behavior getBehavior(Item item) {
        return Optional.ofNullable(behaviors.get(item)).map(Holder::getBehavior).orElseGet(this::defaultBehavior);
    }

    public void addBehavior(Item item, DoubleTrouble behavior, boolean complement) {
        if (this.disabled(item)) return;

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

    public void clear() {
        behaviors.clear();
        disabled.clear();
    }

    public Set<Item> itemsWithBehaviors() {
        return Collections.unmodifiableSet(behaviors.keySet());
    }

    private CommanderBehaviors apply() {
        this.clear();
        TeaBehavior.INSTANCE.getBehaviors().forEach((key, value) -> this.addBehavior(key, (entity, stack, context) -> value.run(entity, stack), true));

        for (Subscription<Data> subscription : this.subscriptions) {
            for (Item item : subscription.parameters().items()) {
                if (subscription.parameters().disabled()) {
                    this.disable(item);
                    continue;
                }
                addBehavior(item, (entity, stack, context) -> {
                    for (ConditionedCommand cc : subscription.list()) {
                        cc.execute(context);
                    }
                }, subscription.parameters().complement());
            }
        }
        return this;
    }

    public static void init() {}

    private static class Holder {
        private final List<DoubleTrouble> behaviors = new ObjectArrayList<>();
        private final TeaBehaviorProvider.Behavior behavior = (entity, stack) -> {
            LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder((ServerWorld) entity.getWorld())
                    .add(THIS_ENTITY, entity).add(ORIGIN, entity.getPos()).add(TOOL, stack);
            LootContext lootContext = new LootContext.Builder(builder.build(LootContextTypes.FISHING)).build(null);
            EventContext context = new EventContext(lootContext, EVENT_TYPE);

            behaviors.forEach(behavior1 -> behavior1.run(entity, stack, context));
        };
        private final Item item;
        private boolean locked;

        public Holder(Item item) {
            this.item = item;
        }

        public void addBehavior(DoubleTrouble behavior, boolean complement) {
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

    public interface DoubleTrouble {
        void run(LivingEntity entity, ItemStack stack, EventContext context);
    }

    public record Data(List<Item> items, boolean disabled, boolean complement) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(data -> data.group(
                ExtraCodecs.list(Registries.ITEM.getCodec()).fieldOf("items").forGetter(Data::items),
                ExtraCodecs.optional("disabled", Codec.BOOL, false).forGetter(Data::disabled),
                ExtraCodecs.optional("complement", Codec.BOOL, true).forGetter(Data::disabled)
        ).apply(data, Data::new));
    }
}
