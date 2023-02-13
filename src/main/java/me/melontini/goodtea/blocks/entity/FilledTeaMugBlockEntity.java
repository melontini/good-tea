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

    private ItemStack SLOT_0 = ItemStack.EMPTY;
    private ItemStack SLOT_1 = ItemStack.EMPTY;
    private ItemStack SLOT_2 = ItemStack.EMPTY;

    public FilledTeaMugBlockEntity(BlockPos pos, BlockState state) {
        super(GoodTeaStuff.FILLED_TEA_MUG_BLOCK_ENTITY, pos, state);
    }

    public boolean interact(ItemStack stack, BlockState blockState) {
        if (blockState.get(FilledTeaMugBlock.COUNT) > 3) return false;

        ItemStack newStack = stack.copy();
        newStack.setCount(1);
        switch (blockState.get(FilledTeaMugBlock.COUNT)) {
            case 1 -> SLOT_0 = newStack;
            case 2 -> SLOT_1 = newStack;
            case 3 -> SLOT_2 = newStack;
            default -> {
                return false;
            }
        }
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
        var slot0 = nbt.getCompound("slot0");
        if (slot0 != null) SLOT_0 = ItemStack.fromNbt(slot0);

        var slot1 = nbt.getCompound("slot1");
        if (slot1 != null) SLOT_1 = ItemStack.fromNbt(slot1);

        var slot2 = nbt.getCompound("slot2");
        if (slot2 != null) SLOT_2 = ItemStack.fromNbt(slot2);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (SLOT_0 != null) nbt.put("slot0", SLOT_0.writeNbt(new NbtCompound()));
        if (SLOT_1 != null) nbt.put("slot1", SLOT_1.writeNbt(new NbtCompound()));
        if (SLOT_2 != null) nbt.put("slot2", SLOT_2.writeNbt(new NbtCompound()));
    }

    public ItemStack getSLOT_0() {
        return SLOT_0;
    }

    public ItemStack getSLOT_1() {
        return SLOT_1;
    }

    public ItemStack getSLOT_2() {
        return SLOT_2;
    }

    public void update() {
        markDirty();
        assert world != null;
        var state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }
}
