package me.melontini.goodtea.mixin.totem_tea;

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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "tryUseTotem", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private ItemStack good_tea$setFakeTotem(ItemStack value) {
        if (this.getAttachedOrElse(Attachments.IS_DIVINE, false)) {
            this.setAttached(Attachments.IS_DIVINE, null);
            if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
                player.sendMessage(TextUtil.translatable("text.good-tea.used_divine"), true);
            }
            return new ItemStack(Items.TOTEM_OF_UNDYING);
        }
        return value;
    }
}
