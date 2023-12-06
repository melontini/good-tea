package me.melontini.goodtea;

import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.goodtea.behaviors.KahurCompat;
import me.melontini.goodtea.behaviors.KettleBehaviour;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.behaviors.TeaTooltips;
import me.melontini.goodtea.behaviors.data.DataPackBehaviors;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;

import static me.melontini.goodtea.util.GoodTeaStuff.KETTLE_BLOCK_ENTITY;

@SuppressWarnings("UnstableApiUsage")
public class GoodTea implements ModInitializer {

    public static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("GoodTea"), PrependingLogger.NAME_METHOD_MIX);
    public static final String MODID = "good-tea";
    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MODID, "kettle"), new ScreenHandlerType<>(KettleScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    public static final Identifier ITEMS_WITH_BEHAVIORS = new Identifier(MODID, "items_with_behaviors");

    @Override
    public void onInitialize() {
        GoodTeaStuff.init();
        TeaBehavior.INSTANCE.init();
        TeaTooltips.INSTANCE.initTooltips();
        KettleBehaviour.INSTANCE.init();

        DataPackBehaviors.register();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);

        if (FabricLoader.getInstance().isModLoaded("kahur")) {
            KahurCompat.register();
        }

        RegistryEntryAddedCallback.event(Registries.ITEM).register((rawId, id, object) -> {
            TeaBehavior.INSTANCE.initAuto(object);
            TeaTooltips.INSTANCE.initAutoTooltips(object);
        });
        for (Item item : Registries.ITEM) {
            TeaBehavior.INSTANCE.initAuto(item);
            TeaTooltips.INSTANCE.initAutoTooltips(item);
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var packet = sendItemsS2CPacket();
            sender.sendPacket(ITEMS_WITH_BEHAVIORS, packet);
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            var packet = sendItemsS2CPacket();
            for (ServerPlayerEntity player : PlayerLookup.all(server)) {
                ServerPlayNetworking.send(player, ITEMS_WITH_BEHAVIORS, packet);
            }
        });
    }

    private static PacketByteBuf sendItemsS2CPacket() {
        var packet = PacketByteBufs.create();

        var disabled = DataPackBehaviors.INSTANCE.disabled();
        packet.writeVarInt(disabled.size());
        for (Item item : disabled) {
            packet.writeIdentifier(Registries.ITEM.getId(item));
        }

        var items = DataPackBehaviors.INSTANCE.itemsWithBehaviors();
        packet.writeVarInt(items.size());
        for (Item item : items) {
            packet.writeIdentifier(Registries.ITEM.getId(item));
        }
        return packet;
    }
}
