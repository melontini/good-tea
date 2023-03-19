package me.melontini.goodtea.util;

import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.content.ContentBuilder;
import me.melontini.crackerutil.content.RegistryUtil;
import me.melontini.crackerutil.data.NbtBuilder;
import me.melontini.crackerutil.interfaces.AnimatedItemGroup;
import me.melontini.crackerutil.util.Utilities;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.blocks.FilledTeaMugBlock;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.blocks.TeaMugBlock;
import me.melontini.goodtea.blocks.entity.FilledTeaMugBlockEntity;
import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import me.melontini.goodtea.items.TeaMugItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static me.melontini.goodtea.GoodTea.MODID;

public class GoodTeaStuff {
    public static EntityAttributeModifier OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);

    public static EntityAttributeModifier RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);

    public static TagKey<Block> SHOW_SUPPORT = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_kettle_show_support"));
    public static TagKey<Block> HOT_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_hot_blocks"));
    public static TeaMugBlock TEA_MUG_BLOCK = ContentBuilder.BlockBuilder.create(TeaMugBlock.class, new Identifier(MODID, "mug"), AbstractBlock.Settings.of(Material.DECORATION, MapColor.PALE_YELLOW))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(BlockItem.class, id, block, new Item.Settings()).group(ItemGroup.MISC).maxCount(16))
            .nonOpaque().strength(0.1F).sounds(BlockSoundGroup.CANDLE).build();
    public static BlockItem TEA_MUG = RegistryUtil.asItem(TEA_MUG_BLOCK);
    public static KettleBlock KETTLE_BLOCK = ContentBuilder.BlockBuilder.create(KettleBlock.class, new Identifier(MODID, "kettle"), AbstractBlock.Settings.of(Material.METAL, MapColor.STONE_GRAY))
            .blockEntity((block, id) -> ContentBuilder.BlockEntityBuilder.create(id, KettleBlockEntity::new, block))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(BlockItem.class, id, block, new Item.Settings()).group(ItemGroup.DECORATIONS))
            .requiresTool().strength(2.0f).nonOpaque().build();
    public static BlockItem KETTLE_BLOCK_ITEM = RegistryUtil.asItem(KETTLE_BLOCK);
    public static BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY = RegistryUtil.getBlockEntityFromBlock(KETTLE_BLOCK);
    public static FilledTeaMugBlock FILLED_TEA_MUG_BLOCK = ContentBuilder.BlockBuilder.create(FilledTeaMugBlock.class, new Identifier(MODID, "filled_mug"), AbstractBlock.Settings.of(Material.DECORATION, MapColor.PALE_YELLOW))
            .blockEntity((block, id) -> ContentBuilder.BlockEntityBuilder.create(id, FilledTeaMugBlockEntity::new, block))
            .item((block, id) -> ContentBuilder.ItemBuilder.create(TeaMugItem.class, id, block, new Item.Settings()).maxCount(16).rarity(Rarity.RARE).recipeRemainder(TEA_MUG))
            .sounds(BlockSoundGroup.CANDLE).strength(0.1f).nonOpaque().build();
    public static TeaMugItem TEA_MUG_FILLED = RegistryUtil.asItem(FILLED_TEA_MUG_BLOCK);
    public static BlockEntityType<FilledTeaMugBlockEntity> FILLED_TEA_MUG_BLOCK_ENTITY = RegistryUtil.getBlockEntityFromBlock(FILLED_TEA_MUG_BLOCK);
    public static ItemGroup GROUP = Util.make(() -> {
        ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
        return new GoodTeaGroup(ItemGroup.GROUPS.length - 1, "good_tea_item_group");
    });

    public static void init() {
    }

    public static class GoodTeaGroup extends ItemGroup implements AnimatedItemGroup {

        public final ItemStack KETTLE = KETTLE_BLOCK_ITEM.getDefaultStack();
        public final ItemStack MUG = TEA_MUG.getDefaultStack();
        float angle = 45f, lerpPoint = 0;

        public GoodTeaGroup(int index, String id) {
            super(index, id);
            this.setIconAnimation(this);
        }

        @Override
        public void animateIcon(MatrixStack matrixStack, int itemX, int itemY, boolean selected, boolean isTopRow) {
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
            matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle));
            DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, KETTLE, model);
            matrixStack.pop();
        }

        @Override
        public ItemStack createIcon() {
            return KETTLE_BLOCK_ITEM.getDefaultStack();
        }

        @Override
        public void appendStacks(DefaultedList<ItemStack> stacks) {
            List<ItemStack> teaStarterPack = new ArrayList<>();
            teaStarterPack.add(KETTLE);
            teaStarterPack.add(MUG);

            teaStarterPack.add(ItemStack.EMPTY);

            teaStarterPack.add(Items.CAMPFIRE.getDefaultStack());
            teaStarterPack.add(Items.SOUL_CAMPFIRE.getDefaultStack());
            teaStarterPack.add(Items.LAVA_BUCKET.getDefaultStack());
            Utilities.appendStacks(stacks, teaStarterPack);

            var help = DefaultedList.<ItemStack>of();
            var list = TeaBehavior.INSTANCE.TEA_BEHAVIOR.keySet().stream().sorted(Comparator.comparing(Registry.ITEM::getId)).toList();
            for (Item item : list) {
                item.appendStacks(SEARCH, help);

                for (ItemStack stack : help) {
                    var mug = TEA_MUG_FILLED.getDefaultStack();
                    mug.setNbt(NbtBuilder.create().put("GT-TeaItem", stack.writeNbt(new NbtCompound())).build());
                    stacks.add(mug);
                    stacks.add(stack);
                    stacks.add(ItemStack.EMPTY);
                }
                help.clear();
            }
        }
    }
}
