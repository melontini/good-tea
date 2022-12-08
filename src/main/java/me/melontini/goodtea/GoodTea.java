package me.melontini.goodtea;

import com.unascribed.kahur.api.KahurImpactBehavior;
import me.melontini.crackerutil.content.RegistryUtil;
import me.melontini.goodtea.behaviors.KahurCompat;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class GoodTea implements ModInitializer {
    public static final String MODID = "good-tea";
    public static final EntityAttributeModifier OBSIDIAN_TOUGHNESS = new EntityAttributeModifier(UUID.fromString("36dae011-70d8-482a-b3b3-7bb12c871eae"), "Tea Modifier", 2, EntityAttributeModifier.Operation.ADDITION);
    public static final EntityAttributeModifier RABBITS_LUCK = new EntityAttributeModifier(UUID.fromString("57c5033e-c071-4b23-8f14-0551eb4c5b0a"), "Tea Modifier", 1, EntityAttributeModifier.Operation.ADDITION);
    public static final TagKey<Block> SHOW_SUPPORT = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_kettle_show_support"));
    public static final TagKey<Block> HOT_BLOCKS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "gt_hot_blocks"));
    public static final Item TEA_CUP = RegistryUtil.createItem(Item.class, new Identifier(MODID, "cup"), new FabricItemSettings().group(ItemGroup.MISC).maxCount(16));
    public static final TeaCupItem TEA_CUP_FILLED = (TeaCupItem) RegistryUtil.createItem(TeaCupItem.class, new Identifier(MODID, "filled_cup"), new FabricItemSettings().maxCount(16).rarity(Rarity.RARE).recipeRemainder(TEA_CUP));
    public static final KettleBlock KETTLE_BLOCK = (KettleBlock) RegistryUtil.createBlock(KettleBlock.class, new Identifier(MODID, "kettle"), FabricBlockSettings.of(Material.METAL));
    public static final BlockItem KETTLE_BLOCK_ITEM = (BlockItem) RegistryUtil.createItem(BlockItem.class, new Identifier(MODID, "kettle"), KETTLE_BLOCK, new FabricItemSettings().group(ItemGroup.DECORATIONS));
    public static final BlockEntityType<KettleBlockEntity> KETTLE_BLOCK_ENTITY = RegistryUtil.createBlockEntity(new Identifier(MODID, "kettle_block_entity"), BlockEntityType.Builder.create(KettleBlockEntity::new, KETTLE_BLOCK));
    public static ScreenHandlerType<KettleScreenHandler> KETTLE_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MODID, "kettle"), new ScreenHandlerType<>(KettleScreenHandler::new));

    @Override
    public void onInitialize() {
        TeaCupBehavior.INSTANCE.addDefaultBehaviours();
        TeaCupBehavior.INSTANCE.addDefaultTooltips();
        KettleBlockBehaviour.INSTANCE.addDefaultBlocks();

        FluidStorage.SIDED.registerForBlockEntity((kettle, direction) -> kettle.waterStorage, KETTLE_BLOCK_ENTITY);

        if (FabricLoader.getInstance().isModLoaded("kahur")) {
            KahurCompat.register();
        }
    }
}
