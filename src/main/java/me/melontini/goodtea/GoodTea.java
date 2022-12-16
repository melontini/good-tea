package me.melontini.goodtea;

import me.melontini.crackerutil.client.util.DrawUtil;
import me.melontini.crackerutil.data.NbtBuilder;
import me.melontini.crackerutil.interfaces.AnimatedItemGroup;
import me.melontini.crackerutil.util.MathStuff;
import me.melontini.goodtea.behaviors.KahurCompat;
import me.melontini.goodtea.behaviors.KettleBlockBehaviour;
import me.melontini.goodtea.behaviors.TeaBehavior;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
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
        TeaBehavior.INSTANCE.addDefaultBehaviours();
        TeaBehavior.INSTANCE.addDefaultTooltips();
        KettleBlockBehaviour.INSTANCE.addDefaultBlocks();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);

        if (FabricLoader.getInstance().isModLoaded("kahur")) {
            KahurCompat.register();
        }
    }
}
