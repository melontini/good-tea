package me.melontini.goodtea;

import me.melontini.goodtea.behaviors.KettleBlockBehaviour;
import me.melontini.goodtea.behaviors.TeaCupBehavior;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import me.melontini.goodtea.items.TeaCupItem;
import me.melontini.goodtea.screens.KettleScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.MinecraftVersion;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class GoodTea implements ModInitializer {
    public static final int MCVERSION = Integer.parseInt(MinecraftVersion.CURRENT.getName().replace(".", ""));
    public static final String MODID = "good-tea";
    public static final EntityAttributeModifier OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);
    public static final EntityAttributeModifier RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);
    public static final TagKey<Block> SHOW_SUPPORT = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_kettle_show_support"));
    public static final TagKey<Block> HOT_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_hot_blocks"));
    public static final TeaCupItem TEA_CUP_FILLED = new TeaCupItem(new FabricItemSettings().maxCount(16).rarity(Rarity.RARE));
    public static final Item TEA_CUP = new Item(new FabricItemSettings().group(ItemGroup.MISC).maxCount(16));
    public static final KettleBlock KETTLE_BLOCK = new KettleBlock(FabricBlockSettings.of(Material.METAL));
    public static final BlockItem KETTLE_BLOCK_ITEM = new BlockItem(KETTLE_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS));

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MODID, "cup"), TEA_CUP);
        Registry.register(Registry.ITEM, new Identifier(MODID, "filled_cup"), TEA_CUP_FILLED);
        Registry.register(Registry.BLOCK, new Identifier(MODID, "kettle"), KETTLE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MODID, "kettle"), KETTLE_BLOCK_ITEM);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MODID, "kettle_block_entity"), KETTLE_BLOCK_ENTITY);
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "kettle"), KETTLE_SCREEN_HANDLER);

        TeaCupBehavior.INSTANCE.addDefaultBehaviours();
        TeaCupBehavior.INSTANCE.addDefaultTooltips();
        KettleBlockBehaviour.INSTANCE.addDefaultBlocks();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);
    }

    public static final BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(KettleBlockEntity::new, KETTLE_BLOCK).build();

    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = new ScreenHandlerType<>(KettleScreenHandler::new);


}
