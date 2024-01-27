package me.melontini.goodtea.mixin.warped_fungus_tea;

import me.melontini.goodtea.util.Attachments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void good_tea$tick(CallbackInfo ci) {
        if (!world.isClient()) {
            int i = this.getAttachedOrElse(Attachments.HOGLIN_REPELLENT, 0);
            if (i > 0) {
                this.setAttached(Attachments.HOGLIN_REPELLENT, i - 1 == 0 ? null : i - 1);
            }
        }
    }
}
