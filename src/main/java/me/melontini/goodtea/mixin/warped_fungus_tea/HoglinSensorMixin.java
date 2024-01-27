package me.melontini.goodtea.mixin.warped_fungus_tea;

import me.melontini.goodtea.util.Attachments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.HoglinSpecificSensor;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mixin(HoglinSpecificSensor.class)
public class HoglinSensorMixin {
    @Inject(at = @At("HEAD"), method = "findNearestWarpedFungus", cancellable = true)
    private void good_tea$blockPosLie(ServerWorld world, HoglinEntity hoglin, CallbackInfoReturnable<Optional<BlockPos>> cir) {
        List<LivingEntity> entities = hoglin.world.getEntitiesByClass(LivingEntity.class, new Box(hoglin.getBlockPos()).expand(6), LivingEntity::isAlive);

        if (!entities.isEmpty()) {
            Optional<LivingEntity> playerEntityOptional = entities.stream().filter(entity -> entity.getAttachedOrElse(Attachments.HOGLIN_REPELLENT, 0) > 0).min(Comparator.comparingDouble(player -> player.squaredDistanceTo(hoglin)));
            playerEntityOptional.ifPresent(entity -> cir.setReturnValue(Optional.ofNullable(entity.getBlockPos())));
        }
    }
}
