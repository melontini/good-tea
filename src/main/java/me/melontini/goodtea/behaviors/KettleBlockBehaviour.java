package me.melontini.goodtea.behaviors;

import me.melontini.goodtea.util.LogUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.state.property.Property;

import java.util.*;

public class KettleBlockBehaviour {
    public static KettleBlockBehaviour INSTANCE = new KettleBlockBehaviour();

    Map<Block, List<Property<?>>> HOT_BLOCKS_WITH_STATE = new HashMap<>();

    public void addDefaultBlocks() {
        addBlockWithProperties(Blocks.CAMPFIRE, List.of(CampfireBlock.LIT));
        addBlockWithProperties(Blocks.SOUL_CAMPFIRE, List.of(CampfireBlock.LIT));
    }

    public void addBlockWithProperties(Block block, List<Property<?>> properties) {
        if (!HOT_BLOCKS_WITH_STATE.containsKey(block)) {
            HOT_BLOCKS_WITH_STATE.putIfAbsent(block, properties);
        } else {
            LogUtil.error("Tried to add behaviour for the same item twice! {}", block);
        }
    }

    public Optional<List<Property<?>>> getProperties(Block block) {
        return Optional.ofNullable(HOT_BLOCKS_WITH_STATE.getOrDefault(block, null));
    }
}
