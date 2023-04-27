package me.melontini.goodtea.behaviors;

import com.unascribed.kahur.api.KahurImpactBehavior;
import me.melontini.goodtea.items.TeaMugItem;
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

import static me.melontini.goodtea.util.GoodTeaStuff.TEA_MUG_FILLED;

public class KahurCompat {
    public static void register() {
        KahurImpactBehavior.register((kahurShotEntity, itemStack, hitResult) -> {
            NbtCompound nbt = itemStack.getNbt();
            ItemStack stack1 = TeaMugItem.getStackFromNbt(nbt);
            if (stack1 == null) {
                return KahurImpactBehavior.ImpactResult.destroy(true);
            }

            LivingEntity target = null;
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                if (((EntityHitResult) hitResult).getEntity() instanceof LivingEntity livingEntity) {
                    target = livingEntity;
                }
            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                Vec3d pos = hitResult.getPos();
                List<LivingEntity> livingEntities = kahurShotEntity.world.getEntitiesByClass(LivingEntity.class, new Box(((BlockHitResult) hitResult).getBlockPos()).expand(0.5), LivingEntity::isAlive);
                Optional<LivingEntity> winner = livingEntities.stream().min(Comparator.comparingDouble(livingEntity -> livingEntity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())));
                if (winner.isPresent()) {
                    target = winner.get();
                }
            }

            if (target != null) {
                TeaBehavior.INSTANCE.getBehavior(stack1).run(target, stack1);
            }
            return KahurImpactBehavior.ImpactResult.destroy(true);
        }, TEA_MUG_FILLED);
    }

}
