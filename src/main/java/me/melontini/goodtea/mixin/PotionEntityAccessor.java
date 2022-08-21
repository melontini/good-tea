package me.melontini.goodtea.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(PotionEntity.class)
public interface PotionEntityAccessor {
    @Invoker("applyLingeringPotion")
    void applyLingeringPotion(ItemStack stack, Potion potion);

    @Invoker("applySplashPotion")
    void applySplashPotion(List<StatusEffectInstance> statusEffects, @Nullable Entity entity);
}
