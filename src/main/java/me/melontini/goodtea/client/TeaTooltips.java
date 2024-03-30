package me.melontini.goodtea.client;

import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.GoodTea;
import net.minecraft.block.BedBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static me.melontini.goodtea.util.GoodTeaStuff.KETTLE_BLOCK_ITEM;
import static me.melontini.goodtea.util.GoodTeaStuff.TEA_MUG;

public class TeaTooltips {

    public static TeaTooltips INSTANCE = new TeaTooltips();

    private final Map<Item, Tooltip> tooltips = new LinkedHashMap<>();

    public Tooltip getTooltip(Item item) {
        return GoodTeaClient.hasBehavior(item) ? tooltips.get(item) : null;
    }

    public void addTooltip(Tooltip tooltip, Item... items) {
        for (Item item : items) addTooltip(item, tooltip);
    }

    public void addTooltip(Item item, Tooltip tooltip) {
        MakeSure.notNull(tooltip);
        if (!tooltips.containsKey(item)) tooltips.putIfAbsent(item, tooltip);
        else GoodTea.LOGGER.error("Tried to add a tooltip for the same item twice! {}", item);
    }

    public void initTooltips() {
        addTooltip((stack, teaStack, world, tooltip, context) -> tooltip.add(TextUtil.translatable("tea-tooltip.good-tea.tea-mug-tea").formatted(Formatting.GRAY, Formatting.ITALIC)), TEA_MUG, KETTLE_BLOCK_ITEM);
        addTooltip(Items.AXOLOTL_BUCKET, (stack, teaStack, world, tooltip, context) -> tooltip.add(TextUtil.translatable("tea-tooltip.good-tea.axolotl_tea").formatted(Formatting.GRAY, Formatting.ITALIC)));
        addTooltip((stack, teaStack, world, tooltip, context) -> tooltip.add(TextUtil.translatable("tea-tooltip.good-tea.wheat_tea").formatted(Formatting.GRAY, Formatting.ITALIC)), Items.HAY_BLOCK, Items.WHEAT);
        addTooltip((stack, teaStack, world, tooltip, context) -> PotionUtil.buildTooltip(teaStack, tooltip, 1.2F), Items.POTION, Items.SPLASH_POTION);
        addTooltip(Items.LINGERING_POTION, (stack, teaStack, world, tooltip, context) -> PotionUtil.buildTooltip(teaStack, tooltip, 0.3125F));
    }

    public void initAutoTooltips(Item item) {
        if (item instanceof MusicDiscItem discItem) {
            addTooltip(item, (stack, teaStack, world, tooltip, context) -> tooltip.add(discItem.getDescription().formatted(Formatting.GRAY)));
        }
        if (item instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof BedBlock) {
                addTooltip(item, (stack, teaStack, world, tooltip, context) -> tooltip.add(TextUtil.translatable("tea-tooltip.good-tea.bed-tea").formatted(Formatting.GRAY, Formatting.ITALIC)));
            }
        }
    }

    @FunctionalInterface
    public interface Tooltip {
        void append(ItemStack stack, ItemStack teaStack, @Nullable World world, List<Text> tooltip, TooltipContext context);
    }
}
