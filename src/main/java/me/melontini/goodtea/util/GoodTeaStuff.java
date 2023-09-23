package me.melontini.goodtea.util;

import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import me.melontini.dark_matter.api.content.interfaces.AnimatedItemGroup;
import me.melontini.dark_matter.api.content.interfaces.DarkMatterEntries;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import me.melontini.dark_matter.api.minecraft.data.NbtBuilder;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.blocks.FilledTeaMugBlock;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.blocks.TeaMugBlock;
import me.melontini.goodtea.blocks.entity.FilledTeaMugBlockEntity;
import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import me.melontini.goodtea.items.TeaMugItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.*;

import static me.melontini.goodtea.GoodTea.MODID;

public class GoodTeaStuff {
    public static EntityAttributeModifier OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);
    public static EntityAttributeModifier RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);
    public static TagKey<Block> SHOW_SUPPORT = TagKey.of(Registries.BLOCK.getKey(), new Identifier(MODID, "gt_kettle_show_support"));
    public static TagKey<Block> HOT_BLOCKS = TagKey.of(Registries.BLOCK.getKey(), new Identifier(MODID, "gt_hot_blocks"));
    public static TeaMugBlock TEA_MUG_BLOCK = ContentBuilder.BlockBuilder.create(new Identifier(MODID, "mug"), () -> new TeaMugBlock(AbstractBlock.Settings.of(Material.DECORATION, MapColor.PALE_YELLOW).nonOpaque().strength(0.1F).sounds(BlockSoundGroup.CANDLE)))
            .item((block, identifier) -> ContentBuilder.ItemBuilder.create(identifier, () -> new BlockItem(block, new Item.Settings().maxCount(16))).itemGroup(ItemGroups.FOOD_AND_DRINK)).build();
    public static BlockItem TEA_MUG = RegistryUtil.asItem(TEA_MUG_BLOCK);
    public static KettleBlock KETTLE_BLOCK = ContentBuilder.BlockBuilder.create(new Identifier(MODID, "kettle"), () -> new KettleBlock(AbstractBlock.Settings.of(Material.METAL, MapColor.STONE_GRAY).requiresTool().strength(2f).nonOpaque()))
            .item((block, identifier) -> ContentBuilder.ItemBuilder.create(identifier, () -> new BlockItem(block, new Item.Settings())).itemGroup(ItemGroups.FUNCTIONAL))
            .blockEntity((block, identifier) -> ContentBuilder.BlockEntityBuilder.create(identifier, KettleBlockEntity::new, block)).build();
    public static BlockItem KETTLE_BLOCK_ITEM = RegistryUtil.asItem(KETTLE_BLOCK);
    public static BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY = RegistryUtil.getBlockEntityFromBlock(KETTLE_BLOCK);
    public static FilledTeaMugBlock FILLED_TEA_MUG_BLOCK = ContentBuilder.BlockBuilder.create(new Identifier(MODID, "filled_mug"), () -> new FilledTeaMugBlock(AbstractBlock.Settings.of(Material.DECORATION, MapColor.PALE_YELLOW).sounds(BlockSoundGroup.CANDLE).strength(0.1f).nonOpaque()))
            .item((block, identifier) -> ContentBuilder.ItemBuilder.create(identifier, () -> new TeaMugItem(block, new Item.Settings().maxCount(16).rarity(Rarity.RARE).recipeRemainder(TEA_MUG))))
            .blockEntity((block, identifier) -> ContentBuilder.BlockEntityBuilder.create(identifier, FilledTeaMugBlockEntity::new, block)).build();
    public static TeaMugItem TEA_MUG_FILLED = RegistryUtil.asItem(FILLED_TEA_MUG_BLOCK);
    public static BlockEntityType<FilledTeaMugBlockEntity> FILLED_TEA_MUG_BLOCK_ENTITY = RegistryUtil.getBlockEntityFromBlock(FILLED_TEA_MUG_BLOCK);
    public static final ItemStack KETTLE = KETTLE_BLOCK_ITEM.getDefaultStack();
    public static final ItemStack MUG = TEA_MUG.getDefaultStack();
    public static ItemGroup GROUP = ContentBuilder.ItemGroupBuilder.create(new Identifier(MODID, "item_group"))
            .animatedIcon(() -> new AnimatedItemGroup() {
                float angle = 45f, lerpPoint = 0;
                @Override
                public void animateIcon(ItemGroup group, MatrixStack matrixStack, int itemX, int itemY, boolean selected, boolean isTopRow) {
                    MinecraftClient client = MinecraftClient.getInstance();

                    BakedModel model1 = client.getItemRenderer().getModel(MUG, null, null, 0);
                    matrixStack.push();
                    matrixStack.translate(itemX - 3.5, itemY + 4, 100.0F + client.getItemRenderer().zOffset);
                    matrixStack.translate(8.0, 8.0, 0.0);
                    matrixStack.scale(1.0F, -1.0F, 1.0F);
                    matrixStack.scale(15.0F, 15.0F, 15.0F);
                    DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, MUG, model1);
                    matrixStack.pop();


                    BakedModel model = client.getItemRenderer().getModel(KETTLE, null, null, 0);
                    //itemX + 5, itemY - 5
                    matrixStack.push();
                    matrixStack.translate(itemX + 2.5, itemY - 5, 100.0F + client.getItemRenderer().zOffset);
                    matrixStack.translate(8.0, 8.0, 0.0);
                    matrixStack.scale(1.0F, -1.0F, 1.0F);
                    matrixStack.scale(16.0F, 16.0F, 16.0F);


                    angle = MathHelper.lerp(0.1f * client.getLastFrameDuration(), angle, lerpPoint);
                    if (angle < 0.1f && lerpPoint == 0f) {
                        lerpPoint = 45f;
                    }
                    if (angle > 44.9f && lerpPoint == 45f) {
                        lerpPoint = 0f;
                    }
                    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
                    DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, KETTLE, model);
                    matrixStack.pop();
                }
            }).entries(stacks -> {
                List<ItemStack> teaStarterPack = new ArrayList<>();
                teaStarterPack.add(KETTLE);
                teaStarterPack.add(MUG);

                teaStarterPack.add(ItemStack.EMPTY);

                teaStarterPack.add(Items.CAMPFIRE.getDefaultStack());
                teaStarterPack.add(Items.SOUL_CAMPFIRE.getDefaultStack());
                teaStarterPack.add(Items.LAVA_BUCKET.getDefaultStack());
                appendStacks(stacks, teaStarterPack, true);

                var list = TeaBehavior.INSTANCE.TEA_BEHAVIOR.keySet();

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
            }).icon(KETTLE).build();

    public static void init() {
    }

    private static void appendStacks(DarkMatterEntries entries, Collection<ItemStack> list, boolean lineBreak) {
        if (list == null || list.isEmpty()) return; //we shouldn't add line breaks if there are no items.

        int rows = MathStuff.fastCeil(list.size() / 9d);
        entries.addAll(list, DarkMatterEntries.Visibility.TAB);
        int left = (rows * 9) - list.size();
        for (int i = 0; i < left; i++) {
            entries.add(ItemStack.EMPTY, DarkMatterEntries.Visibility.TAB); //fill the gaps
        }
        if (lineBreak) entries.addAll(DefaultedList.ofSize(9, ItemStack.EMPTY), DarkMatterEntries.Visibility.TAB); //line break
    }

}
