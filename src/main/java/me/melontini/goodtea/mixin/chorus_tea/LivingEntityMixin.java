package me.melontini.goodtea.mixin.chorus_tea;

import me.melontini.goodtea.ducks.ChorusAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ChorusAccess {

    @Unique
    private int good_tea$chorusTeleport = 0;
    @Unique
    private int good_tea$ticksSinceLastTeleport = 0;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo ci) {
        if (!world.isClient()) {
            LivingEntity user = (LivingEntity) (Object) this;
            if (this.good_tea$chorusTeleport > 0) {
                this.good_tea$chorusTeleport--;
                this.good_tea$ticksSinceLastTeleport = Math.min(this.good_tea$ticksSinceLastTeleport + 1, this.good_tea$chorusTeleport / 2);

                if (world.random.nextInt(Math.max((int) ((this.good_tea$chorusTeleport * 0.7) - good_tea$ticksSinceLastTeleport) / 2, 1)) == 0) {
                    this.good_tea$ticksSinceLastTeleport = 0;

                    double d = user.getX();
                    double e = user.getY();
                    double f = user.getZ();

                    for (int i = 0; i < 16; ++i) {
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

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void good_tea$readNbt(NbtCompound nbt, CallbackInfo ci) {
        this.good_tea$chorusTeleport = nbt.getInt("GT-ChorusTeleport");
        this.good_tea$ticksSinceLastTeleport = nbt.getInt("GT-ChorusTeleportTicks");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void good_tea$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("GT-ChorusTeleport", this.good_tea$chorusTeleport);
        nbt.putInt("GT-ChorusTeleportTicks", this.good_tea$ticksSinceLastTeleport);
    }

    @Override
    public boolean good_tea$isTeleporting() {
        return this.good_tea$chorusTeleport > 0;
    }

    @Override
    public void good_tea$addTeleportingTime(int timeInTicks) {
        this.good_tea$chorusTeleport += timeInTicks;
    }
}
