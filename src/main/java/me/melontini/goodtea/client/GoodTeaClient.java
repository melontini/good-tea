package me.melontini.goodtea.client;

import com.google.common.collect.ImmutableList;
import me.melontini.dark_matter.api.item_group.ItemGroupAnimaton;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.client.screens.KettleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static me.melontini.goodtea.util.GoodTeaStuff.*;

@Environment(EnvType.CLIENT)
public class GoodTeaClient implements ClientModInitializer {

    private static final Set<Item> ITEMS_WITH_BEHAVIORS = new LinkedHashSet<>();

    public static List<Item> getItemsWithBehaviors() {
        return ImmutableList.copyOf(ITEMS_WITH_BEHAVIORS);
    }

    public static boolean hasBehavior(Item item) {
        return ITEMS_WITH_BEHAVIORS.contains(item);
    }

    @Override
    public void onInitializeClient() {
        TeaTooltips.INSTANCE.initTooltips();
        for (Item item : Registries.ITEM) {
            TeaTooltips.INSTANCE.initAutoTooltips(item);
        }

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), KETTLE_BLOCK);
        HandledScreens.register(GoodTea.KETTLE_SCREEN_HANDLER, KettleScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(GoodTea.ITEMS_WITH_BEHAVIORS, (client, handler, buf, responseSender) -> {
            ITEMS_WITH_BEHAVIORS.clear();
            buf.readVarInt();

            Set<Identifier> ids = new HashSet<>();
            int length = buf.readVarInt();
            for (int i = 0; i < length; i++) ids.add(buf.readIdentifier());
            client.execute(() -> {
                for (Identifier id : ids) ITEMS_WITH_BEHAVIORS.add(Registries.ITEM.get(id));
            });
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ITEMS_WITH_BEHAVIORS.clear());

        ItemGroupAnimaton.setIconAnimation(GROUP, new ItemGroupAnimaton() {
            private float angle = 45f, lerpPoint = 0;
            private final ItemStack kettle = KETTLE_BLOCK_ITEM.getDefaultStack(), mug = TEA_MUG.getDefaultStack();
            @Override
            public void animateIcon(ItemGroup group, DrawContext context, int itemX, int itemY, boolean selected, boolean isTopRow) {
                MinecraftClient client = MinecraftClient.getInstance();

                MatrixStack matrixStack = context.getMatrices();
                BakedModel model1 = client.getItemRenderer().getModel(mug, null, null, 0);
                matrixStack.push();
                matrixStack.translate(itemX - 3.5, itemY + 4, 100.0F);
                matrixStack.translate(8.0, 8.0, 0.0);
                matrixStack.scale(1.0F, -1.0F, 1.0F);
                matrixStack.scale(15.0F, 15.0F, 15.0F);
                DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, mug, model1);
                matrixStack.pop();


                BakedModel model = client.getItemRenderer().getModel(kettle, null, null, 0);
                //itemX + 5, itemY - 5
                matrixStack.push();
                matrixStack.translate(itemX + 2.5, itemY - 5, 100.0F);
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
                DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, kettle, model);
                matrixStack.pop();
            }
        });
    }
}
