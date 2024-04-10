package me.melontini.goodtea.mixin.totem_tea;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.util.Attachments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"))
    private void good_tea$setFakeTotem(CallbackInfoReturnable<Boolean> cir, @Local LocalRef<ItemStack> totem) {
        if (this.getAttachedOrElse(Attachments.IS_DIVINE, false)) {
            this.setAttached(Attachments.IS_DIVINE, null);
            if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
                player.sendMessage(TextUtil.translatable("text.good-tea.used_divine"), true);
            }
            totem.set(new ItemStack(Items.TOTEM_OF_UNDYING));
        }
    }
}
