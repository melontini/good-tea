package me.melontini.goodtea.items;

import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.behaviors.TeaBehaviorProvider;
import me.melontini.goodtea.client.TeaTooltips;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static me.melontini.goodtea.util.GoodTeaStuff.TEA_MUG;

public class TeaMugItem extends BlockItem {

    private final List<ItemStack> stacks = new ArrayList<>();

    public void disguise(ItemStack... stacks) {
        this.stacks.addAll(Arrays.asList(stacks));
    }

    public TeaMugItem(Block block, Settings settings) {
        super(block, settings);
        disguise(Items.GOLDEN_APPLE.getDefaultStack(), Items.WARPED_FUNGUS.getDefaultStack(),
                Items.TOTEM_OF_UNDYING.getDefaultStack(), Items.RABBIT_FOOT.getDefaultStack());
    }

    public static ItemStack getStackFromNbt(NbtCompound nbt) {
        if (nbt != null) if (nbt.contains("GT-TeaItem")) return ItemStack.fromNbt(nbt.getCompound("GT-TeaItem"));
        return null;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isOf(Items.ECHO_SHARD)) {
                var nbt = stack.getOrCreateNbt();
                if (nbt.getInt("HashCode") != 0) {
                    nbt.remove("HashCode");
                } else {
                    nbt.putInt("HashCode", ThreadLocalRandom.current().nextInt(512) + 1);
                }
                otherStack.decrement(1);
                return true;
            }
        }
        return false;
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        MakeSure.notNulls(stack, world);
        PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (player instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);
        }

        if (!world.isClient()) {
            NbtCompound nbt = stack.getNbt();
            ItemStack stack1 = getStackFromNbt(nbt);
            if (stack1 != null) {
                TeaBehaviorProvider.PROVIDER.apply(world.getServer()).getBehavior(stack1).run(user, stack1);
            }
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        user.emitGameEvent(GameEvent.DRINK, user);

        if (player == null || !player.getAbilities().creativeMode) {
            if (stack.isEmpty()) return new ItemStack(TEA_MUG);
            if (player != null) player.getInventory().offerOrDrop(new ItemStack(TEA_MUG));
        }

        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (world == null || !world.isClient()) return;

        NbtCompound nbt = stack.getNbt();
        ItemStack stack1 = getTooltipStackFromNbt(nbt);
        if (stack1 != null) {
            Item item = stack1.getItem();
            tooltip.add(TextUtil.translatable("tooltip.good-tea.filled_mug", item.getName()).formatted(item.getRarity(item.getDefaultStack()).formatting));
            if (TeaTooltips.INSTANCE.hasTooltip(item)) {
                TeaTooltips.INSTANCE.getTooltip(item).append(stack, stack1, world, tooltip, context);
            }
        }
    }

    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    public ItemStack getTooltipStackFromNbt(NbtCompound nbt) {
        var stack = getStackFromNbt(nbt);
        if (stack != null && nbt.getInt("HashCode") != 0) {
            return stacks.get(Math.max(Math.abs(stackHashCode(stack) % (stacks.size() - 1)), 0));
        }
        return stack;
    }

    private static int stackHashCode(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        int i = 31 + stack.getItem().hashCode();
        return 31 * i + (nbtCompound == null ? 0 : nbtCompound.hashCode());
    }
}
