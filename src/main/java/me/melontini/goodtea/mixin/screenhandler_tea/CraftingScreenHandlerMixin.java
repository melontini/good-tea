package me.melontini.goodtea.mixin.screenhandler_tea;

import me.melontini.goodtea.ducks.CraftingScreenAllowanceAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(at = @At("RETURN"), method = "canUse", cancellable = true)
    private void good_tea$canUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (((CraftingScreenAllowanceAccess) player).good_tea$isAllowed()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(at = @At("TAIL"), method = "close")
    private void good_tea$close(PlayerEntity player, CallbackInfo ci) {
        ((CraftingScreenAllowanceAccess) player).good_tea$setAllowed(false);
    }
}
