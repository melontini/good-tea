package me.melontini.goodtea.behaviors;

import com.unascribed.kahur.api.KahurImpactBehavior;
import me.melontini.crackerutil.util.MakeSure;
import me.melontini.goodtea.items.TeaCupItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static me.melontini.goodtea.GoodTea.TEA_CUP_FILLED;

public class KahurCompat {
    public static void register() {
        KahurImpactBehavior.register((kahurShotEntity, itemStack, hitResult) -> {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                if (((EntityHitResult) hitResult).getEntity() instanceof LivingEntity livingEntity) {
                    NbtCompound nbt = itemStack.getNbt();
                    ItemStack stack1 = TeaCupItem.getStackFromNbt(nbt);
                    if (stack1 != null) {
                        TeaCupBehavior.INSTANCE.getBehavior(stack1).run(livingEntity, stack1);
                    }
                }
            } else if (hitResult.getType() == HitResult.Type.BLOCK){
                Vec3d pos = hitResult.getPos();
                List<LivingEntity> livingEntities = kahurShotEntity.world.getEntitiesByClass(LivingEntity.class, new Box(((BlockHitResult)hitResult).getBlockPos()).expand(0.5), LivingEntity::isAlive);
                Optional<LivingEntity> winner = livingEntities.stream().min(Comparator.comparingDouble(livingEntity -> livingEntity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())));
                if (winner.isPresent()) {
                    NbtCompound nbt = itemStack.getNbt();
                    ItemStack stack1 = TeaCupItem.getStackFromNbt(nbt);
                    if (stack1 != null) {
                        TeaCupBehavior.INSTANCE.getBehavior(stack1).run(winner.get(), stack1);
                    }
                }
            }
            return KahurImpactBehavior.ImpactResult.destroy(true);
        }, TEA_CUP_FILLED);
    }
}
