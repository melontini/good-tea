package me.melontini.goodtea.mixin.chorus_tea;

import me.melontini.goodtea.util.Attachments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
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
    private void tick(CallbackInfo ci) {
        if (!world.isClient()) {
            LivingEntity user = (LivingEntity) (Object) this;
            int i = this.getAttachedOrElse(Attachments.CHORUS_TELEPORT_TIME, 0);
            if (i > 0) {
                this.setAttached(Attachments.CHORUS_TELEPORT_TIME, i - 1 == 0 ? null : i - 1);

                int sinceLast = Math.min(this.getAttachedOrElse(Attachments.CHORUS_LAST_TELEPORT_TIME, 0) + 1, (i - 1) / 2);
                this.setAttached(Attachments.CHORUS_LAST_TELEPORT_TIME, sinceLast);

                if (world.random.nextInt(Math.max((int) (((i - 1) * 0.7) - sinceLast) / 2, 1)) == 0) {
                    this.setAttached(Attachments.CHORUS_LAST_TELEPORT_TIME, null);

                    double d = user.getX();
                    double e = user.getY();
                    double f = user.getZ();

                    for (int l = 0; l < 16; ++l) {
                        double g = user.getX() + (user.getRandom().nextDouble() - 0.5) * 16.0;
                        double h = MathHelper.clamp(
                                user.getY() + (double) (user.getRandom().nextInt(16) - 8),
                                world.getBottomY(),
                                world.getBottomY() + ((ServerWorld) world).getLogicalHeight() - 1
                        );
                        double j = user.getZ() + (user.getRandom().nextDouble() - 0.5) * 16.0;
                        if (user.hasVehicle()) {
                            user.stopRiding();
                        }

                        Vec3d vec3d = user.getPos();
                        if (user.teleport(g, h, j, true)) {
                            world.emitGameEvent(GameEvent.TELEPORT, vec3d, GameEvent.Emitter.of(user));
                            SoundEvent soundEvent = user instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                            world.playSound(null, d, e, f, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            user.playSound(soundEvent, 1.0F, 1.0F);
                            break;
                        }
                    }
                }
            }
        }
    }
}
