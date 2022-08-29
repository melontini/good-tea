package me.melontini.goodtea.items;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.behaviors.TeaCupBehavior;
import me.melontini.goodtea.util.TextUtil;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
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

public class TeaCupItem extends Item {
    public TeaCupItem(Settings settings) {
        super(settings);
    }

    public static ItemStack getStackFromNbt(NbtCompound nbt) {
        if (nbt != null) if (nbt.contains("GT-TeaItem")) {
            return ItemStack.fromNbt(nbt.getCompound("GT-TeaItem"));
        }
        return null;
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity player = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (player instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) player, stack);
        }

        if (!world.isClient()) {
            NbtCompound nbt = stack.getNbt();
            ItemStack stack1 = getStackFromNbt(nbt);
            if (stack1 != null) {
                TeaCupBehavior.INSTANCE.getBehavior(stack1).run(player, stack1);
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
                return new ItemStack(GoodTea.TEA_CUP);
            }

            if (player != null) {
                player.getInventory().offerOrDrop(new ItemStack(GoodTea.TEA_CUP));
            }
        }

        Optional<GameEvent> optional = Registry.GAME_EVENT.getOrEmpty(new Identifier("drink"));
        GameEvent event = optional.orElseGet(() -> Registry.GAME_EVENT.get(new Identifier("drinking_finish")));
        user.emitGameEvent(event, user);
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        ItemStack stack1 = getStackFromNbt(nbt);
        if (stack1 != null) {
            Item item = stack1.getItem();
            if (item != null) {
                tooltip.add(TextUtil.applyFormatting(TextUtil.createTranslatable("tooltip.good-tea.filled_cup", item.getName()), item.getRarity(item.getDefaultStack()).formatting));
                if (TeaCupBehavior.INSTANCE.hasTooltip(item)) {
                    TeaCupBehavior.INSTANCE.getTooltip(item).append(stack, stack1, world, tooltip, context);
                } else {
                    if (!TeaCupBehavior.INSTANCE.hasBehavior(item)) {
                        tooltip.add(TextUtil.applyFormatting(TextUtil.createTranslatable("tooltip.good-tea.filled_cup.nothing"), Formatting.GRAY));
                    } else
                        tooltip.add(TextUtil.applyFormatting(TextUtil.createTranslatable("tooltip.good-tea.filled_cup.something"), Formatting.GRAY));
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
