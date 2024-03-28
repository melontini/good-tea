package me.melontini.goodtea.util;

import me.melontini.dark_matter.api.data.nbt.NbtBuilder;
import me.melontini.dark_matter.api.item_group.DarkMatterEntries;
import me.melontini.dark_matter.api.item_group.ItemGroupBuilder;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.behaviors.data.DataPackBehaviors;
import me.melontini.goodtea.blocks.FilledTeaMugBlock;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.blocks.TeaMugBlock;
import me.melontini.goodtea.blocks.entity.FilledTeaMugBlockEntity;
import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import me.melontini.goodtea.items.TeaMugItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.*;

import static me.melontini.goodtea.GoodTea.MODID;

public class GoodTeaStuff {

    public static EntityAttributeModifier OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);
    public static EntityAttributeModifier RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);

    public static TagKey<Block> SHOW_SUPPORT = TagKey.of(Registries.BLOCK.getKey(), id("gt_kettle_show_support"));
    public static TagKey<Block> HOT_BLOCKS = TagKey.of(Registries.BLOCK.getKey(), id("gt_hot_blocks"));
    public static TeaMugBlock TEA_MUG_BLOCK = RegistryUtil.register(Registries.BLOCK, id("mug"), () -> new TeaMugBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).nonOpaque().strength(0.1F).sounds(BlockSoundGroup.CANDLE)));
    public static BlockItem TEA_MUG = RegistryUtil.register(Registries.ITEM, id("mug"), () -> new BlockItem(TEA_MUG_BLOCK, new Item.Settings().maxCount(16)));
    public static KettleBlock KETTLE_BLOCK = RegistryUtil.register(Registries.BLOCK, id("kettle"), () -> new KettleBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).requiresTool().strength(2f).nonOpaque()));
    public static BlockItem KETTLE_BLOCK_ITEM = RegistryUtil.register(Registries.ITEM, id("kettle"), () -> new BlockItem(KETTLE_BLOCK, new Item.Settings()));
    public static BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY = RegistryUtil.register(Registries.BLOCK_ENTITY_TYPE, id("kettle"), RegistryUtil.blockEntityType(KettleBlockEntity::new, KETTLE_BLOCK));
    public static FilledTeaMugBlock FILLED_TEA_MUG_BLOCK = RegistryUtil.register(Registries.BLOCK, id("filled_mug"), () -> new FilledTeaMugBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).sounds(BlockSoundGroup.CANDLE).strength(0.1f).nonOpaque()));
    public static TeaMugItem TEA_MUG_FILLED = RegistryUtil.register(Registries.ITEM, id("filled_mug"), () -> new TeaMugItem(FILLED_TEA_MUG_BLOCK, new Item.Settings().maxCount(16).rarity(Rarity.RARE).recipeRemainder(TEA_MUG)));
    public static BlockEntityType<FilledTeaMugBlockEntity> FILLED_TEA_MUG_BLOCK_ENTITY = RegistryUtil.register(Registries.BLOCK_ENTITY_TYPE, id("filled_mug"), RegistryUtil.blockEntityType(FilledTeaMugBlockEntity::new, FILLED_TEA_MUG_BLOCK));
    public static final ItemStack KETTLE = KETTLE_BLOCK_ITEM.getDefaultStack();
    public static final ItemStack MUG = TEA_MUG.getDefaultStack();

    public static ItemGroup GROUP = ItemGroupBuilder.create(id("item_group"))
            .entries(stacks -> {
                List<ItemStack> teaStarterPack = new ArrayList<>();
                teaStarterPack.add(KETTLE);
                teaStarterPack.add(MUG);

                teaStarterPack.add(ItemStack.EMPTY);

                teaStarterPack.add(Items.CAMPFIRE.getDefaultStack());
                teaStarterPack.add(Items.SOUL_CAMPFIRE.getDefaultStack());
                teaStarterPack.add(Items.LAVA_BUCKET.getDefaultStack());
                stacks.appendStacks(teaStarterPack, true);

                var list = DataPackBehaviors.INSTANCE.itemsWithBehaviors().stream().sorted(Comparator.comparingInt(Registries.ITEM::getRawId)).toList();

                Set<ItemStack> set = ItemStackSet.create();

                for(ItemGroup itemGroup : ItemGroups.getGroups()) {
                    if (itemGroup.getType() != ItemGroup.Type.SEARCH) {
                        set.addAll(itemGroup.getSearchTabStacks());
                    }
                }

                set.removeIf(itemStack -> !list.contains(itemStack.getItem()));

                for (ItemStack stack : set) {
                    var mug = TEA_MUG_FILLED.getDefaultStack();
                    mug.setNbt(NbtBuilder.create().put("GT-TeaItem", stack.writeNbt(new NbtCompound())).build());
                    stacks.add(mug, DarkMatterEntries.Visibility.TAB);
                    stacks.add(stack, DarkMatterEntries.Visibility.TAB);
                    stacks.add(ItemStack.EMPTY, DarkMatterEntries.Visibility.TAB);
                }
            }).icon(KETTLE).displayName(TextUtil.translatable("itemGroup.good-tea.item_group")).build();

    public static Identifier id(String s) {
        return new Identifier(MODID, s);
    }

    public static void init() {
    }
}
