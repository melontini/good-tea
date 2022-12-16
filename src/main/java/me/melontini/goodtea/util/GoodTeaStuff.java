package me.melontini.goodtea.util;

import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.content.RegistryUtil;
import me.melontini.crackerutil.data.NbtBuilder;
import me.melontini.crackerutil.interfaces.AnimatedItemGroup;
import me.melontini.crackerutil.util.MathStuff;
import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import me.melontini.goodtea.items.TeaMugItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
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
    public static EntityAttributeModifier OBSIDIAN_TOUGHNESS;

    public static EntityAttributeModifier RABBITS_LUCK;

    public static TagKey<Block> SHOW_SUPPORT;
    public static TagKey<Block> HOT_BLOCKS;
    public static Item TEA_MUG;

    public static TeaMugItem TEA_MUG_FILLED;
    public static KettleBlock KETTLE_BLOCK;

    public static BlockItem KETTLE_BLOCK_ITEM;
    public static BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY;
    public static ItemGroup GROUP;

    public static void init() {
        TEA_MUG = RegistryUtil.createItem(Item.class, new Identifier(MODID, "mug"), new FabricItemSettings().group(ItemGroup.MISC).maxCount(16));
        KETTLE_BLOCK = (KettleBlock) RegistryUtil.createBlock(KettleBlock.class, new Identifier(MODID, "kettle"), FabricBlockSettings.of(Material.METAL));
        KETTLE_BLOCK_ENTITY = RegistryUtil.createBlockEntity(new Identifier(MODID, "kettle_block_entity"), BlockEntityType.Builder.create(KettleBlockEntity::new, KETTLE_BLOCK));
        KETTLE_BLOCK_ITEM = (BlockItem) RegistryUtil.createItem(BlockItem.class, new Identifier(MODID, "kettle"), KETTLE_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS));
        TEA_MUG_FILLED = (TeaMugItem) RegistryUtil.createItem(TeaMugItem.class, new Identifier(MODID, "filled_mug"), new FabricItemSettings().maxCount(16).rarity(Rarity.RARE).recipeRemainder(TEA_MUG));
        HOT_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_hot_blocks"));
        SHOW_SUPPORT = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_kettle_show_support"));
        OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);
        RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);
        GROUP = Util.make(() -> {
            ((ItemGroupExtensions) ItemGroup.BUILDING_BLOCKS).fabric_expandArray();
            return new GoodTeaGroup(ItemGroup.GROUPS.length - 1, "good_tea_item_group");
        });
    }

    public static class GoodTeaGroup extends ItemGroup implements AnimatedItemGroup {

        public final ItemStack KETTLE = KETTLE_BLOCK_ITEM.getDefaultStack();
        public final ItemStack MUG = TEA_MUG.getDefaultStack();
        private final DefaultedList<ItemStack> EMPTY_LIST = DefaultedList.ofSize(9, ItemStack.EMPTY);
        float angle = 45f, lerpPoint = 0;

        public GoodTeaGroup(int index, String id) {
            super(index, id);
        }

        @Override
        public void animateIcon(MatrixStack matrixStack, int itemX, int itemY) {
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
            appendStacks(stacks, teaStarterPack);

            var help = DefaultedList.<ItemStack>of();
            var list = TeaBehavior.INSTANCE.TEA_BEHAVIOR.keySet().stream().sorted(Comparator.comparing(Registry.ITEM::getId)).toList();
            for (Item item : list) {
                item.appendStacks(SEARCH, help);

                for (ItemStack stack : help) {
                    var mug = new ItemStack(TEA_MUG_FILLED);
                    mug.setNbt(NbtBuilder.create().put("GT-TeaItem", stack.writeNbt(new NbtCompound())).build());
                    stacks.add(mug);
                    stacks.add(stack);
                    stacks.add(ItemStack.EMPTY);
                }
                help.clear();
            }
        }

        private void appendStacks(DefaultedList<ItemStack> stacks, List<ItemStack> list) {
            if (list.isEmpty()) return; //we shouldn't add line breaks if there are no items.

            int rows = MathStuff.fastCeil(list.size() / 9d);
            stacks.addAll(list);
            int left = (rows * 9) - list.size();
            for (int i = 0; i < left; i++) {
                stacks.add(ItemStack.EMPTY); //fill the gaps
            }
            stacks.addAll(EMPTY_LIST); //line break
        }
    }
}
