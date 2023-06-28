package me.melontini.goodtea.mixin.totem_tea;

import me.melontini.dark_matter.minecraft.util.TextUtil;
import me.melontini.goodtea.ducks.DivineAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements DivineAccess {
    @Unique
    private boolean good_tea$divine = false;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "tryUseTotem", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private ItemStack good_tea$setFakeTotem(ItemStack value) {
        if (this.good_tea$isDivine()) {
            this.good_tea$setDivine(false);
            if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
                player.sendMessage(TextUtil.translatable("text.good-tea.used_divine"), true);
            }
            return new ItemStack(Items.TOTEM_OF_UNDYING);
        }
        return value;
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void good_tea$readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("GT-Divine")) this.good_tea$divine = nbt.getBoolean("GT-Divine");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void good_tea$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.good_tea$divine) nbt.putBoolean("GT-Divine", true);
    }

    @Unique
    public void good_tea$setDivine(boolean divine) {
        this.good_tea$divine = divine;
    }

    @Unique
    public boolean good_tea$isDivine() {
        return good_tea$divine;
    }
}
