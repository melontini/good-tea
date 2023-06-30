package me.melontini.goodtea;

import me.melontini.dark_matter.util.PrependingLogger;
import me.melontini.goodtea.behaviors.KahurCompat;
import me.melontini.goodtea.behaviors.KettleBehaviour;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;

import static me.melontini.goodtea.util.GoodTeaStuff.KETTLE_BLOCK_ENTITY;

@SuppressWarnings("UnstableApiUsage")
public class GoodTea implements ModInitializer {
    public static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("GoodTea"), PrependingLogger.NAME_METHOD_MIX);
    public static final String MODID = "good-tea";
    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MODID, "kettle"), new ScreenHandlerType<>(KettleScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

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

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TeaBehavior.INSTANCE.initAuto();
            TeaBehavior.INSTANCE.initAutoTooltips();
            LOGGER.info("Found {} item behaviors", TeaBehavior.INSTANCE.TEA_BEHAVIOR.size());
        });
    }
}
