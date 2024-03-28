package me.melontini.goodtea.behaviors;

import me.melontini.commander.data.DynamicEventManager;
import me.melontini.dark_matter.api.base.util.Support;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import java.util.Set;
import java.util.function.Function;

public interface TeaBehaviorProvider {

    Function<MinecraftServer, TeaBehaviorProvider> PROVIDER = Support.fallback("commander",
            () -> server -> DynamicEventManager.getData(server, CommanderBehaviors.EVENT_TYPE),
            () -> server -> TeaBehavior.INSTANCE);

    default Behavior getBehavior(ItemStack stack) {
        return getBehavior(stack.getItem());
    }
    Behavior getBehavior(Item item);

    default Behavior defaultBehavior() {
        return (entity, stack) -> stack.getItem().finishUsing(stack, entity.world, entity);
    }

    Set<Item> itemsWithBehaviors();

    @FunctionalInterface
    interface Behavior {
        void run(LivingEntity entity, ItemStack stack);
    }
}
