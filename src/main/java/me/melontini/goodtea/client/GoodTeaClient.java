package me.melontini.goodtea.client;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.client.screens.KettleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class GoodTeaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), GoodTea.KETTLE_BLOCK);
        HandledScreens.register(GoodTea.KETTLE_SCREEN_HANDLER, KettleScreen::new);
    }
}
