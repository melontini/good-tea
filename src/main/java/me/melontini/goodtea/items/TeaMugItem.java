package me.melontini.goodtea.items;

import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.behaviors.TeaTooltips;
import me.melontini.goodtea.behaviors.data.DataPackBehaviors;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static me.melontini.goodtea.util.GoodTeaStuff.TEA_MUG;

public class TeaMugItem extends BlockItem {

    public TeaMugItem(Block block, Settings settings) {
        super(block, settings);
    }

    public static ItemStack getStackFromNbt(NbtCompound nbt) {
        if (nbt != null) if (nbt.contains("GT-TeaItem")) return ItemStack.fromNbt(nbt.getCompound("GT-TeaItem"));
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
                DataPackBehaviors.INSTANCE.getBehavior(stack1).run(user, stack1);
            }
        }

        if (player != null) {
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (player == null || !player.getAbilities().creativeMode) {
            if (stack.isEmpty()) return new ItemStack(TEA_MUG);
            if (player != null) player.getInventory().offerOrDrop(new ItemStack(TEA_MUG));
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
}
