package me.melontini.goodtea.items;

import me.melontini.crackerutil.util.MakeSure;
import me.melontini.goodtea.behaviors.TeaBehavior;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static me.melontini.goodtea.util.GoodTeaStuff.TEA_MUG;

public class TeaMugItem extends Item {
    private static final Text NOTHING_TEXT = Text.translatable("tooltip.good-tea.filled_mug.nothing").formatted(Formatting.GRAY);
    private static final Text SOMETHING_TEXT = Text.translatable("tooltip.good-tea.filled_mug.something").formatted(Formatting.GRAY);
    public TeaMugItem(Settings settings) {
        super(settings);
    }

    public static ItemStack getStackFromNbt(NbtCompound nbt) {
        if (nbt != null) if (nbt.contains("GT-TeaItem")) {
            return ItemStack.fromNbt(nbt.getCompound("GT-TeaItem"));
        }
        return null;
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
                TeaBehavior.INSTANCE.getBehavior(stack1).run(user, stack1);
            }
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (player == null || !player.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(TEA_MUG);
            }

            if (player != null) {
                player.getInventory().offerOrDrop(new ItemStack(TEA_MUG));
            }
        }

        Optional<GameEvent> optional = Registry.GAME_EVENT.getOrEmpty(new Identifier("drink"));
        GameEvent event = optional.orElseGet(() -> Registry.GAME_EVENT.get(new Identifier("drinking_finish")));
        user.emitGameEvent(event, user);
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        MakeSure.notNulls(stack, world);
        NbtCompound nbt = stack.getNbt();
        ItemStack stack1 = getStackFromNbt(nbt);
        if (stack1 != null) {
            Item item = stack1.getItem();
            if (item != null) {
                tooltip.add(Text.translatable("tooltip.good-tea.filled_mug", item.getName()).formatted(item.getRarity(item.getDefaultStack()).formatting));
                if (TeaBehavior.INSTANCE.hasTooltip(item)) {
                    TeaBehavior.INSTANCE.getTooltip(item).append(stack, stack1, world, tooltip, context);
                } else {
                    if (!TeaBehavior.INSTANCE.hasBehavior(item)) {
                        tooltip.add(NOTHING_TEXT);
                    } else
                        tooltip.add(SOMETHING_TEXT);
                }
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
}
