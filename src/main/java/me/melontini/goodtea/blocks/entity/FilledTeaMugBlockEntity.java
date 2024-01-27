package me.melontini.goodtea.blocks.entity;

import me.melontini.goodtea.blocks.FilledTeaMugBlock;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FilledTeaMugBlockEntity extends BlockEntity {

    private final ItemStack[] stacks = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    public FilledTeaMugBlockEntity(BlockPos pos, BlockState state) {
        super(GoodTeaStuff.FILLED_TEA_MUG_BLOCK_ENTITY, pos, state);
    }

    public boolean interact(ItemStack stack, BlockState blockState) {
        if (blockState.get(FilledTeaMugBlock.COUNT) > 3) return false;

        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        stacks[blockState.get(FilledTeaMugBlock.COUNT) - 1] = newStack;
        update();
        return true;
    }

    @Nullable
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        for (int i = 0; i < stacks.length; i++) {
            var slot = nbt.getCompound("slot" + i);
            if (slot != null) stacks[i] = ItemStack.fromNbt(slot);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        for (int i = 0; i < stacks.length; i++) {
            var stack = stacks[i];
            nbt.put("slot" + i, stack.writeNbt(new NbtCompound()));
        }
    }

    public ItemStack getSlot(int slot) {
        return slot >= 0 && slot < stacks.length ? stacks[slot] : ItemStack.EMPTY;
    }

    public void update() {
        markDirty();
        assert world != null;
        var state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }
}
