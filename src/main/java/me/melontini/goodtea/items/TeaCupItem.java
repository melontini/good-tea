package me.melontini.goodtea.items;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.behaviors.TeaCupBehavior;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeaCupItem extends Item {
    public TeaCupItem(Settings settings) {
        super(settings);
    }

    private static ItemStack getStackFromNbt(NbtCompound nbt) {
        if (nbt != null) if (nbt.contains("GT-TeaItem")) {
            return ItemStack.fromNbt(nbt.getCompound("GT-TeaItem"));
        }
        return null;
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) playerEntity, stack);
        }

        if (!world.isClient()) {
            NbtCompound nbt = stack.getNbt();
            ItemStack stack1 = getStackFromNbt(nbt);
            if (stack1 != null) {
                TeaCupBehavior.INSTANCE.getBehavior(stack1).run(playerEntity, stack1);
            }
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!playerEntity.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (playerEntity == null || !playerEntity.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(GoodTea.TEA_CUP);
            }

            if (playerEntity != null) {
                playerEntity.getInventory().insertStack(new ItemStack(GoodTea.TEA_CUP));
            }
        }

        world.emitGameEvent(user, GameEvent.DRINKING_FINISH, user.getCameraBlockPos());
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        ItemStack stack1 = getStackFromNbt(nbt);
        if (stack1 != null) {
            Item item = stack1.getItem();
            if (item != null) {
                tooltip.add(new TranslatableText("tooltip.good-tea.filled_cup", item.getName()).formatted(item.getRarity(item.getDefaultStack()).formatting));
                if (item.equals(Items.POTION) || item.equals(Items.SPLASH_POTION) || item.equals(Items.LINGERING_POTION)) {
                    PotionUtil.buildTooltip(stack1, tooltip, 1.6F);
                    return;
                }
                if (TeaCupBehavior.INSTANCE.hasTooltip(item)) {
                    tooltip.add(TeaCupBehavior.INSTANCE.getTooltip(item));
                } else {
                    if (!TeaCupBehavior.INSTANCE.hasBehavior(item)) {
                        tooltip.add(new TranslatableText("tooltip.good-tea.filled_cup.nothing").formatted(Formatting.ITALIC, Formatting.GRAY));
                    } else
                        tooltip.add(new TranslatableText("tooltip.good-tea.filled_cup.something").formatted(Formatting.ITALIC, Formatting.GRAY));
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
