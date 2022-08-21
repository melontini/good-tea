package me.melontini.goodtea.mixin.warped_fungus_tea;

import me.melontini.goodtea.ducks.HoglinRepellentAccess;
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
        List<LivingEntity> playerEntityList = hoglin.world.getEntitiesByClass(LivingEntity.class, new Box(hoglin.getBlockPos()).expand(6), LivingEntity::isAlive);

        if (!playerEntityList.isEmpty()) {
            Optional<LivingEntity> playerEntityOptional = playerEntityList.stream().filter(player -> ((HoglinRepellentAccess) player).good_tea$isHoglinRepellent()).min(Comparator.comparingDouble(player -> player.squaredDistanceTo(hoglin)));
            if (playerEntityOptional.isPresent() && ((HoglinRepellentAccess) playerEntityOptional.get()).good_tea$isHoglinRepellent()) {
                cir.setReturnValue(Optional.ofNullable(playerEntityOptional.get().getBlockPos()));
            }
        }
    }
}
