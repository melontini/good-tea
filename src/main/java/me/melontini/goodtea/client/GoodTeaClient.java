package me.melontini.goodtea.client;

import me.melontini.dark_matter.api.item_group.ItemGroupAnimaton;
import me.melontini.dark_matter.api.minecraft.client.util.DrawUtil;
import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.behaviors.data.DataPackBehaviors;
import me.melontini.goodtea.client.screens.KettleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.HashSet;
import java.util.Set;

import static me.melontini.goodtea.util.GoodTeaStuff.*;

@Environment(EnvType.CLIENT)
public class GoodTeaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), KETTLE_BLOCK);
        HandledScreens.register(GoodTea.KETTLE_SCREEN_HANDLER, KettleScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(GoodTea.ITEMS_WITH_BEHAVIORS, (client, handler, buf, responseSender) -> {
            Set<Identifier> disabled = new HashSet<>();
            int disLength = buf.readVarInt();
            for (int i = 0; i < disLength; i++) disabled.add(buf.readIdentifier());

            Set<Identifier> ids = new HashSet<>();
            int length = buf.readVarInt();
            for (int i = 0; i < length; i++) ids.add(buf.readIdentifier());
            client.execute(() -> {
                for (Identifier id : disabled) DataPackBehaviors.INSTANCE
                        .disable(Registries.ITEM.get(id));

                for (Identifier id : ids) DataPackBehaviors.INSTANCE
                        .acceptDummy(Registries.ITEM.get(id));
            });
        });

        ItemGroupAnimaton.setIconAnimation(GROUP, new ItemGroupAnimaton() {
            float angle = 45f, lerpPoint = 0;
            @Override
            public void animateIcon(ItemGroup group, DrawContext context, int itemX, int itemY, boolean selected, boolean isTopRow) {
                MinecraftClient client = MinecraftClient.getInstance();

                MatrixStack matrixStack = context.getMatrices();
                BakedModel model1 = client.getItemRenderer().getModel(MUG, null, null, 0);
                matrixStack.push();
                matrixStack.translate(itemX - 3.5, itemY + 4, 100.0F);
                matrixStack.translate(8.0, 8.0, 0.0);
                matrixStack.scale(1.0F, -1.0F, 1.0F);
                matrixStack.scale(15.0F, 15.0F, 15.0F);
                DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, MUG, model1);
                matrixStack.pop();


                BakedModel model = client.getItemRenderer().getModel(KETTLE, null, null, 0);
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
                DrawUtil.renderGuiItemModelCustomMatrixNoTransform(matrixStack, KETTLE, model);
                matrixStack.pop();
            }
        });
    }
}
