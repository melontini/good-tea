package me.melontini.goodtea.mixin.screenhandler_tea;

import me.melontini.goodtea.ducks.CraftingScreenAllowanceAccess;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements CraftingScreenAllowanceAccess {
    @Unique
    private boolean canUseCraftingTable = false;

    @Override
    public boolean isAllowed() {
        return canUseCraftingTable;
    }

    @Override
    public void setAllowed(boolean allowed) {
        this.canUseCraftingTable = allowed;
    }
}
