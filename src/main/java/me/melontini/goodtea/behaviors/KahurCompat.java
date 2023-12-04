package me.melontini.goodtea.behaviors;

import com.unascribed.kahur.api.KahurImpactBehavior;
import me.melontini.goodtea.items.TeaMugItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static me.melontini.goodtea.util.GoodTeaStuff.TEA_MUG_FILLED;

public class KahurCompat {
    public static void register() {
        KahurImpactBehavior.register((kahurShotEntity, itemStack, hitResult) -> {
            NbtCompound nbt = itemStack.getNbt();
            ItemStack stack = TeaMugItem.getStackFromNbt(nbt);
            if (stack == null) {
                return KahurImpactBehavior.ImpactResult.destroy(true);
            }

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                if (((EntityHitResult) hitResult).getEntity() instanceof LivingEntity livingEntity) {
                    TeaBehavior.INSTANCE.getBehavior(stack).run(livingEntity, stack);
                }
            }
            return KahurImpactBehavior.ImpactResult.destroy(true);
        }, TEA_MUG_FILLED);
    }
}
