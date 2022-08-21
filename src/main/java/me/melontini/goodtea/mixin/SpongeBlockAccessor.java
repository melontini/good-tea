package me.melontini.goodtea.mixin;

import net.minecraft.block.SpongeBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpongeBlock.class)
public interface SpongeBlockAccessor {
    @Invoker("absorbWater")
    boolean absorbWater(World world, BlockPos pos);
}
