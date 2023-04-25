package me.melontini.goodtea.behaviors;

import me.melontini.crackerutil.util.MakeSure;
import me.melontini.goodtea.GoodTea;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.state.property.Property;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class KettleBehaviour {
    public static KettleBehaviour INSTANCE = new KettleBehaviour();

    public final Map<Block, Map<Property<?>, ?>> HOT_BLOCKS_WITH_STATE = new LinkedHashMap<>();

    public void init() {
        addBlockWithProperties(Blocks.CAMPFIRE, Map.of(CampfireBlock.LIT, true));
        addBlockWithProperties(Blocks.SOUL_CAMPFIRE, Map.of(CampfireBlock.LIT, true));
    }

    public void addBlockWithProperties(Block block, Map<Property<?>, ?> propertyMap) {
        MakeSure.notNulls(block, propertyMap);
        if (!HOT_BLOCKS_WITH_STATE.containsKey(block)) {
            HOT_BLOCKS_WITH_STATE.putIfAbsent(block, propertyMap);
        } else {
            GoodTea.LOGGER.error("Tried to add blockstates for the same block twice! {}", block);
        }
    }

    public Optional<Map<Property<?>, ?>> getProperties(Block block) {
        return Optional.ofNullable(HOT_BLOCKS_WITH_STATE.getOrDefault(block, null));
    }
}
