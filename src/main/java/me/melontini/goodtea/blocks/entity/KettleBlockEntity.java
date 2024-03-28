package me.melontini.goodtea.blocks.entity;

import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.data.nbt.NbtBuilder;
import me.melontini.dark_matter.api.data.nbt.NbtUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.behaviors.KettleBehaviour;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.screens.KettleScreenHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static me.melontini.goodtea.util.GoodTeaStuff.*;

@SuppressWarnings("UnstableApiUsage")
public class KettleBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {
    private static final MutableText KETTLE_GUI_KEY = TextUtil.translatable("gui.good-tea.kettle");

    public final SingleVariantStorage<FluidVariant> waterStorage = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET;
        }

        @Override
        protected boolean canInsert(FluidVariant variant) {
            return variant.isOf(Fluids.WATER);
        }
    };
    public int time = 0;

    public final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        public int get(int index) {
            return switch (index) {
                case 0 -> KettleBlockEntity.this.time;
                case 1 -> (int) KettleBlockEntity.this.waterStorage.amount; //I don't think 81000 > 2147483647, soooooo
                default -> 0;
            };
        }

        public void set(int index, int value) {
            switch (index) {
                case 0 -> KettleBlockEntity.this.time = value;
                case 1 -> KettleBlockEntity.this.waterStorage.amount = value;
            }
        }

        public int size() {
            return 2;
        }
    };
    protected DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    public KettleBlockEntity(BlockPos pos, BlockState state) {
        super(KETTLE_BLOCK_ENTITY, pos, state);
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, KettleBlockEntity kettleBlockEntity) {
        kettleBlockEntity.tick();
    }

    private ItemStack teaMugStack = ItemStack.EMPTY;
    private ItemStack lastInputStack = ItemStack.EMPTY;

    private void tick() {
        assert world != null;
        ItemStack input = this.inventory.get(0);
        ItemStack mug = this.inventory.get(1);
        ItemStack output = this.inventory.get(2);

        if (this.time > 0) {
            BlockState state = world.getBlockState(this.pos.down());
            if (state.isIn(HOT_BLOCKS)) {
                this.tickTime();
            } else {
                KettleBehaviour.INSTANCE.getProperties(state.getBlock()).ifPresent(propertyMap -> {
                    if (state.getProperties().containsAll(propertyMap.keySet())) {
                        if (state.getProperties().stream().filter(propertyMap::containsKey).allMatch(property -> state.get(property).equals(propertyMap.get(property)))) {
                            tickTime();
                        }
                    }
                });
            }
        }
        if (!world.isClient) {
            if (!ItemStack.areEqual(input, lastInputStack)) {
                lastInputStack = input.copy();

                teaMugStack = new ItemStack(TEA_MUG_FILLED);
                ItemStack teaStack = input.copy();
                teaStack.setCount(1);
                teaMugStack.setNbt(NbtBuilder.create()
                        .put("GT-TeaItem", teaStack.writeNbt(new NbtCompound()))
                        .build());
            }

            if (this.time == -1) {
                if ((!input.isEmpty() && !input.isOf(TEA_MUG_FILLED)) && !mug.isEmpty() && canCombine(teaMugStack, output) && this.waterStorage.amount >= FluidConstants.BOTTLE) {
                    switch (input.getItem().getRarity(input)) {
                        case COMMON -> this.time = 400;
                        case UNCOMMON -> this.time = 550;
                        case RARE -> this.time = 600;
                        case EPIC -> this.time = 650;
                        default -> this.time = 500;
                    }
                    update();
                }
            }

            if (this.time != -1) {
                if (input.isEmpty() || input.isOf(TEA_MUG_FILLED)) {
                    this.time = -1;
                    update();
                } else if (mug.isEmpty()) {
                    this.time = -1;
                    update();
                } else if (!canCombine(teaMugStack, output)) {
                    this.time = -1;
                    update();
                } else if (this.waterStorage.amount < FluidConstants.BOTTLE) {
                    this.time = -1;
                    update();
                }
            }
            if (this.time == 0) {
                if (output.isEmpty()) {
                    if (input.getItem().hasRecipeRemainder()) {
                        ItemScatterer.spawn(world, getPos().getX(), getPos().up().getY(), getPos().getZ(), new ItemStack(input.getItem().getRecipeRemainder()));
                    }
                    input.decrement(1);
                    mug.decrement(1);
                    this.inventory.set(2, teaMugStack.copy());
                } else if (canCombine(teaMugStack, output)) {
                    int a = teaMugStack.getCount();
                    int b = output.getCount();
                    output.setCount(a + b);
                    if (input.getItem().hasRecipeRemainder()) {
                        ItemScatterer.spawn(world, getPos().getX(), getPos().up().getY(), getPos().getZ(), new ItemStack(input.getItem().getRecipeRemainder()));
                    }
                    input.decrement(1);
                    mug.decrement(1);
                }
                try (Transaction transaction = Transaction.openOuter()) {
                    long amount = this.waterStorage.extract(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE, transaction);
                    if (amount == FluidConstants.BOTTLE) transaction.commit();
                }
                this.time = -1;
                update();
            }
        }
    }

    private void tickTime() {
        if (!world.isClient()) {
            --this.time;
            markDirty();
        }
        if (world.isClient()) {
            BlockState state2 = world.getBlockState(this.pos);
            Direction direction = state2.get(KettleBlock.FACING);
            if (MathUtil.threadRandom().nextInt(120) == 0)
                world.playSound(getPos().getX() + .5, getPos().getY() + .5, getPos().getZ() + .5, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
            if (MathUtil.threadRandom().nextInt(16) == 0)
                world.addParticle(ParticleTypes.BUBBLE_POP, (pos.offset(direction).getX() + 0.5) - (direction.getOffsetX() * 0.45), pos.getY() + 0.35, (pos.offset(direction).getZ() + 0.5) - (direction.getOffsetZ() * 0.45), 0F, 0.03F, 0F);
        }
    }

    public void update() {
        markDirty();
        assert world != null;
        var state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
    }

    private boolean canCombine(ItemStack input, ItemStack output) {
        if (input.isOf(output.getItem()) && Objects.equals(input.getNbt(), output.getNbt()) && !output.isEmpty() && input.getCount() + output.getCount() <= output.getMaxCount()) {
            return true;
        } else return output.isEmpty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.time = nbt.getInt("Time");
        waterStorage.variant = FluidVariant.fromNbt(nbt.getCompound("fluidVariant"));
        waterStorage.amount = nbt.getLong("amount");
        NbtUtil.readInventoryFromNbt(nbt, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Time", this.time);
        nbt.put("fluidVariant", waterStorage.variant.toNbt());
        nbt.putLong("amount", waterStorage.amount);
        NbtUtil.writeInventoryToNbt(nbt, this);
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
    public int[] getAvailableSlots(Direction side) {
        return new int[]{0, 1, 2};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 2) return false;
        else if (slot == 1 && stack.isOf(TEA_MUG)) return true;
        return slot == 0 && !stack.isOf(TEA_MUG);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == 2;
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.inventory, slot, amount);
        if (!itemStack.isEmpty()) this.markDirty();

        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) stack.setCount(this.getMaxCountPerStack());
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public MutableText getDisplayName() {
        return KETTLE_GUI_KEY;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new KettleScreenHandler(syncId, inv, this, this.propertyDelegate);
    }
}
