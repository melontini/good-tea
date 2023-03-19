package me.melontini.goodtea;

import me.melontini.crackerutil.CrackerLog;
import me.melontini.goodtea.behaviors.KahurCompat;
import me.melontini.goodtea.behaviors.KettleBehaviour;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static me.melontini.goodtea.util.GoodTeaStuff.*;

@SuppressWarnings("UnstableApiUsage")
public class GoodTea implements ModInitializer {
    public static final String MODID = "good-tea";
    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "kettle"), new ScreenHandlerType<>(KettleScreenHandler::new));

    @Override
    public void onInitialize() {
        GoodTeaStuff.init();
        TeaBehavior.INSTANCE.init();
        TeaBehavior.INSTANCE.initTooltips();
        KettleBehaviour.INSTANCE.init();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);

        if (FabricLoader.getInstance().isModLoaded("kahur")) {
            KahurCompat.register();
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CrackerLog.info("Found {} item behaviors", TeaBehavior.INSTANCE.TEA_BEHAVIOR.size()));
    }
}
