package me.melontini.goodtea;

import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.data.NbtBuilder;
import me.melontini.crackerutil.interfaces.AnimatedItemGroup;
import me.melontini.crackerutil.util.MathStuff;
import me.melontini.goodtea.behaviors.KahurCompat;
import me.melontini.goodtea.behaviors.KettleBlockBehaviour;
import me.melontini.goodtea.behaviors.TeaCupBehavior;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static me.melontini.goodtea.util.GoodTeaStuff.*;

@SuppressWarnings("UnstableApiUsage")
public class GoodTea implements ModInitializer {
    public static final String MODID = "good-tea";
    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "kettle"), new ScreenHandlerType<>(KettleScreenHandler::new));

    @Override
    public void onInitialize() {
        GoodTeaStuff.init();
        TeaCupBehavior.INSTANCE.addDefaultBehaviours();
        TeaCupBehavior.INSTANCE.addDefaultTooltips();
        KettleBlockBehaviour.INSTANCE.addDefaultBlocks();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);

        if (FabricLoader.getInstance().isModLoaded("kahur")) {
            KahurCompat.register();
        }
    }

    public static class GoodTeaGroup extends ItemGroup implements AnimatedItemGroup {

        public final ItemStack KETTLE = KETTLE_BLOCK_ITEM.getDefaultStack();
        public final ItemStack CUP = TEA_CUP.getDefaultStack();
        private final DefaultedList<ItemStack> EMPTY_LIST = DefaultedList.ofSize(9, ItemStack.EMPTY);
        float angle = 45f, lerpPoint = 0;

        public GoodTeaGroup(int index, String id) {
            super(index, id);
        }

        @Override
        public void animateIcon(MatrixStack matrixStack, CreativeInventoryScreen creativeInventoryScreen, int itemX, int itemY) {
            MinecraftClient client = MinecraftClient.getInstance();

            BakedModel model1 = client.getItemRenderer().getModel(CUP, null, null, 0);
            matrixStack.push();
            matrixStack.translate(itemX - 3.5, itemY + 4, 100.0F + client.getItemRenderer().zOffset);
            matrixStack.translate(8.0, 8.0, 0.0);
            matrixStack.scale(1.0F, -1.0F, 1.0F);
            matrixStack.scale(15.0F, 15.0F, 15.0F);
            DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, CUP, model1);
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
            List<ItemStack> teaStartedPack = new ArrayList<>();
            teaStartedPack.add(KETTLE);
            teaStartedPack.add(CUP);

            teaStartedPack.add(ItemStack.EMPTY);

            teaStartedPack.add(Items.CAMPFIRE.getDefaultStack());
            teaStartedPack.add(Items.SOUL_CAMPFIRE.getDefaultStack());
            teaStartedPack.add(Items.LAVA_BUCKET.getDefaultStack());
            appendStacks(stacks, teaStartedPack);

            var help = DefaultedList.<ItemStack>of();
            var list = TeaCupBehavior.INSTANCE.TEA_CUP_BEHAVIOR.keySet().stream().sorted(Comparator.comparing(Registry.ITEM::getId)).toList();
            for (Item item : list) {
                item.appendStacks(SEARCH, help);

                for (ItemStack stack : help) {
                    var cup = new ItemStack(TEA_CUP_FILLED);
                    cup.setNbt(NbtBuilder.create().put("GT-TeaItem", stack.writeNbt(new NbtCompound())).build());
                    stacks.add(cup);
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
