package me.melontini.goodtea;

import me.melontini.dark_matter.api.base.util.PrependingLogger;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.data.loading.ServerReloadersEvent;
import me.melontini.goodtea.behaviors.CommanderBehaviors;
import me.melontini.goodtea.behaviors.KettleBlockStates;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.behaviors.TeaBehaviorProvider;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.Attachments;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;

import static me.melontini.goodtea.util.GoodTeaStuff.KETTLE_BLOCK_ENTITY;

public class GoodTea implements ModInitializer {

    public static final PrependingLogger LOGGER = new PrependingLogger(LogManager.getLogger("GoodTea"), PrependingLogger.NAME_METHOD_MIX);
    public static final String MODID = "good-tea";
    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MODID, "kettle"), new ScreenHandlerType<>(KettleScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    public static final Identifier ITEMS_WITH_BEHAVIORS = new Identifier(MODID, "items_with_behaviors");

    @Override
    public void onInitialize() {
        GoodTeaStuff.init();
        TeaBehavior.INSTANCE.init();

        ServerReloadersEvent.EVENT.register(context -> context.register(new KettleBlockStates()));

        Runnable commander = Support.fallback("commander", () -> CommanderBehaviors::init, () -> () -> {});
        commander.run();

        Attachments.init();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);

        RegistryEntryAddedCallback.event(Registries.ITEM).register((rawId, id, object) -> TeaBehavior.INSTANCE.initAuto(object));
        for (Item item : Registries.ITEM) {
            TeaBehavior.INSTANCE.initAuto(item);
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var packet = sendItemsS2CPacket(server);
            sender.sendPacket(ITEMS_WITH_BEHAVIORS, packet);
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            var packet = sendItemsS2CPacket(server);
            for (ServerPlayerEntity player : PlayerLookup.all(server)) {
                ServerPlayNetworking.send(player, ITEMS_WITH_BEHAVIORS, packet);
            }
        });
    }

    private static PacketByteBuf sendItemsS2CPacket(MinecraftServer server) {
        var packet = PacketByteBufs.create();
        packet.writeVarInt(0);

        var items = TeaBehaviorProvider.PROVIDER.apply(server).itemsWithBehaviors();
        packet.writeVarInt(items.size());
        for (Item item : items) {
            packet.writeIdentifier(Registries.ITEM.getId(item));
        }
        return packet;
    }
}
