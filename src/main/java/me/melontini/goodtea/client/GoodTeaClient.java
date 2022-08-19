package me.melontini.goodtea.client;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.client.screens.KettleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

import static me.melontini.goodtea.GoodTea.MODID;

@Environment(EnvType.CLIENT)
public class GoodTeaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), GoodTea.KETTLE_BLOCK);
        HandledScreens.register(GoodTea.KETTLE_SCREEN_HANDLER, KettleScreen::new);
    }
}
