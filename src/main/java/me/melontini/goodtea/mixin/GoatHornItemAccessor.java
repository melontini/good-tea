package me.melontini.goodtea.mixin;

import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(GoatHornItem.class)
public interface GoatHornItemAccessor {
    @Invoker("getInstrument")
    Optional<? extends RegistryEntry<Instrument>> gt$getInstrument(ItemStack stack);
}
