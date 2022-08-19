package me.melontini.goodtea.blocks.entity;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.behaviors.KettleBlockBehaviour;
import me.melontini.goodtea.blocks.KettleBlock;
import me.melontini.goodtea.screens.KettleScreenHandler;
import me.melontini.goodtea.util.TextUtil;
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
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Rarity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class KettleBlockEntity extends BlockEntity implements SidedInventory, NamedScreenHandlerFactory {

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
                case 1 -> Math.toIntExact(KettleBlockEntity.this.waterStorage.amount); //I don't think 81000 > 2147483647, soooooo
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
        super(GoodTea.KETTLE_BLOCK_ENTITY, pos, state);
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, KettleBlockEntity kettleBlockEntity) {
        kettleBlockEntity.tick();
    }

    private void tick() {
        assert world != null;
        ItemStack input = this.inventory.get(0);
        ItemStack cup = this.inventory.get(1);
        ItemStack output = this.inventory.get(2);

        if (this.time > 0) {
            BlockState state = world.getBlockState(this.pos.down());
            Optional<List<Property<?>>> optional = KettleBlockBehaviour.INSTANCE.getProperties(state.getBlock());
            if (optional.isPresent()) {
                if (state.getProperties().containsAll(optional.get())) {
                    tickTime();
                }
            } else if (state.isIn(GoodTea.HOT_BLOCKS)) {
                tickTime();
            }
        }
        if (!world.isClient) {
            ItemStack stack = new ItemStack(GoodTea.TEA_CUP_FILLED);
            ItemStack teaStack = input.copy();
            teaStack.setCount(1);
            NbtCompound nbt = new NbtCompound();
            NbtCompound nbt2 = new NbtCompound();
            teaStack.writeNbt(nbt2);
            nbt.put("GT-TeaItem", nbt2);
            stack.setNbt(nbt);
            if (this.time == -1) {
                if ((!input.isEmpty() && !input.isOf(GoodTea.TEA_CUP_FILLED)) && !cup.isEmpty() && canCombine(stack, output) && this.waterStorage.amount >= FluidConstants.BOTTLE) {
                    if (input.getItem().getRarity(input) == Rarity.COMMON) {
                        this.time = 600;
                    } else if (input.getItem().getRarity(input) == Rarity.UNCOMMON) {
                        this.time = 750;
                    } else if (input.getItem().getRarity(input) == Rarity.RARE) {
                        this.time = 800;
                    } else if (input.getItem().getRarity(input) == Rarity.EPIC) {
                        this.time = 850;
                    } else {
                        this.time = 650;
                    }
                    update();
                    //LogUtil.info("Started Processing");
                }
            }

            if (this.time != -1) {
                if (input.isEmpty() || input.isOf(GoodTea.TEA_CUP_FILLED)) {
                    //LogUtil.info("Quit Processing, input is empty");
                    this.time = -1;
                    update();
                } else if (cup.isEmpty()) {
                    //LogUtil.info("Quit Processing, cup is empty");
                    this.time = -1;
                    update();
                } else if (!canCombine(stack, output)) {
                    //LogUtil.info("Quit Processing, input & output can't combine");
                    this.time = -1;
                    update();
                } else if (this.waterStorage.amount < FluidConstants.BOTTLE) {
                    //LogUtil.info("Quit Processing, water is 0");
                    this.time = -1;
                    update();
                }
            }
            if (this.time == 0) {
                //LogUtil.info("Finishing Processing");
                if (output.isEmpty()) {
                    if (input.getItem().hasRecipeRemainder()) {
                        ItemScatterer.spawn(world, getPos().getX(), getPos().up().getY(), getPos().getZ(), new ItemStack(input.getItem().getRecipeRemainder()));
                    }
                    input.decrement(1);
                    cup.decrement(1);
                    this.inventory.set(2, stack);
                    //LogUtil.info("Output is empty, safe to insert");
                } else if (canCombine(stack, output)) {
                    int a = stack.getCount();
                    int b = output.getCount();
                    output.setCount(a + b);
                    stack.setCount(0);
                    if (input.getItem().hasRecipeRemainder()) {
                        ItemScatterer.spawn(world, getPos().getX(), getPos().up().getY(), getPos().getZ(), new ItemStack(input.getItem().getRecipeRemainder()));
                    }
                    input.decrement(1);
                    cup.decrement(1);
                    //LogUtil.info("stacks can combine, safe to insert");
                } else {
                    //LogUtil.info("stacks can't combine, error");
                }
                try (Transaction transaction = Transaction.openOuter()) {
                    long amount = this.waterStorage.extract(FluidVariant.of(Fluids.WATER), FluidConstants.BOTTLE, transaction);
                    if (amount == FluidConstants.BOTTLE) {
                        transaction.commit();
                    }
                }
                this.time = -1;
                update();
                //LogUtil.info("Finished Processing");
            }
        }
    }

    private void tickTime() {
        if (!world.isClient()) --this.time;
        if (world.isClient()) {
            BlockState state2 = world.getBlockState(this.pos);
            Direction direction = state2.get(KettleBlock.FACING);
            if (world.random.nextInt(16) == 0) {
                world.addParticle(ParticleTypes.BUBBLE_POP, (pos.offset(direction).getX() + 0.5) - (direction.getOffsetX() * 0.45), pos.getY() + 0.35, (pos.offset(direction).getZ() + 0.5) - (direction.getOffsetZ() * 0.45), 0F, 0.03F, 0F);
            }
        }
    }

    public void update() {
        markDirty();
        assert world != null;
        var state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        //LogUtil.info("Kettle Update");
    }

    private boolean canCombine(ItemStack input, ItemStack output) {
        if (input.isOf(output.getItem()) && ItemStack.areNbtEqual(input, output) && !output.isEmpty() && input.getCount() + output.getCount() <= output.getMaxCount()) {
            return true;
        } else return output.isEmpty();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.time = nbt.getInt("Time");
        waterStorage.variant = FluidVariant.fromNbt(nbt.getCompound("fluidVariant"));
        waterStorage.amount = nbt.getLong("amount");
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Time", this.time);
        nbt.put("fluidVariant", waterStorage.variant.toNbt());
        nbt.putLong("amount", waterStorage.amount);
        Inventories.writeNbt(nbt, this.inventory);
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
        if (slot == 2) {
            return false;
        } else if (slot == 1 && stack.isOf(GoodTea.TEA_CUP)) {
            return true;
        } else return slot == 0;
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
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }

        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
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
    public Text getDisplayName() {
        return TextUtil.createTranslatable("gui.good-tea.kettle");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new KettleScreenHandler(syncId, inv, this, this.propertyDelegate);
    }
}
