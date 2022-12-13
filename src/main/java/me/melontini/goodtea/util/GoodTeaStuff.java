package me.melontini.goodtea.util;

import me.melontini.crackerutil.content.RegistryUtil;
import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import me.melontini.goodtea.items.TeaCupItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

import static me.melontini.goodtea.GoodTea.MODID;

public class GoodTeaStuff {
    public static EntityAttributeModifier OBSIDIAN_TOUGHNESS;

    public static EntityAttributeModifier RABBITS_LUCK;

    public static TagKey<Block> SHOW_SUPPORT;
    public static TagKey<Block> HOT_BLOCKS;
    public static Item TEA_CUP;

    public static TeaCupItem TEA_CUP_FILLED;
    public static KettleBlock KETTLE_BLOCK;

    public static BlockItem KETTLE_BLOCK_ITEM;
    public static BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY;
    public static ItemGroup GROUP;

    public static void init() {
        TEA_CUP = RegistryUtil.createItem(Item.class, new Identifier(MODID, "cup"), new FabricItemSettings().group(ItemGroup.MISC).maxCount(16));
        KETTLE_BLOCK = (KettleBlock) RegistryUtil.createBlock(KettleBlock.class, new Identifier(MODID, "kettle"), FabricBlockSettings.of(Material.METAL));
        KETTLE_BLOCK_ENTITY = RegistryUtil.createBlockEntity(new Identifier(MODID, "kettle_block_entity"), BlockEntityType.Builder.create(KettleBlockEntity::new, KETTLE_BLOCK));
        KETTLE_BLOCK_ITEM = (BlockItem) RegistryUtil.createItem(BlockItem.class, new Identifier(MODID, "kettle"), KETTLE_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS));
        TEA_CUP_FILLED = (TeaCupItem) RegistryUtil.createItem(TeaCupItem.class, new Identifier(MODID, "filled_cup"), new FabricItemSettings().maxCount(16).rarity(Rarity.RARE).recipeRemainder(TEA_CUP));
        HOT_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_hot_blocks"));
        SHOW_SUPPORT = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_kettle_show_support"));
        OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);
        RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);
        GROUP = Util.make(() -> {
            ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
            return new GoodTea.GoodTeaGroup(ItemGroup.GROUPS.length - 1, "good_tea_item_group");
        });
    }
}
