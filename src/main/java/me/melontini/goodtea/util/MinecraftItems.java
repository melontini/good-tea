package me.melontini.goodtea.util;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class MinecraftItems {
    public static Optional<Item> OCHRE_FROGLIGHT = Registry.ITEM.getOrEmpty(new Identifier("ochre_froglight"));
    public static Optional<Item> VERDANT_FROGLIGHT = Registry.ITEM.getOrEmpty(new Identifier("verdant_froglight"));
    public static Optional<Item> PEARLESCENT_FROGLIGHT = Registry.ITEM.getOrEmpty(new Identifier("pearlescent_froglight"));
}
