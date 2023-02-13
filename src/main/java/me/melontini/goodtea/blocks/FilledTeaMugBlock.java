package me.melontini.goodtea.blocks;

import me.melontini.goodtea.blocks.entity.FilledTeaMugBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FilledTeaMugBlock extends BlockWithEntity implements Waterloggable {
    public static final IntProperty COUNT = IntProperty.of("count", 1, 3);

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private final VoxelShape COUNT_1 = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 5.0, 10.0);
    private final VoxelShape COUNT_2 = VoxelShapes.union(Block.createCuboidShape(9.0, 0.0, 6.0, 13.0, 5.0, 10.0), Block.createCuboidShape(3.0, 0.0, 6.0, 7.0, 5.0, 10.0));
    private final VoxelShape COUNT_3 = VoxelShapes.union(Block.createCuboidShape(9.0, 0.0, 9.0, 13.0, 5.0, 13.0), Block.createCuboidShape(3.0, 0.0, 9.0, 7.0, 5.0, 13.0), Block.createCuboidShape(6.0, 0.0, 3.0, 10.0, 5.0, 7.0));

    public FilledTeaMugBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(COUNT, 1).with(WATERLOGGED, false));
    }

    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return !context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(COUNT) < 3
                ? true
                : super.canReplace(state, context);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockState.isOf(this)) {
            BlockState state = blockState.cycle(COUNT);
            ((FilledTeaMugBlockEntity) Objects.requireNonNull(ctx.getWorld().getBlockEntity(ctx.getBlockPos()))).interact(ctx.getStack(), state);
            return state;
        } else {
            FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
            boolean bl = fluidState.getFluid() == Fluids.WATER;
            return super.getPlacementState(ctx).with(WATERLOGGED, bl);
        }
    }

    public void onPlaced(World world, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (blockState.isOf(this)) {
            ((FilledTeaMugBlockEntity) Objects.requireNonNull(world.getBlockEntity(pos))).interact(itemStack, blockState);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FilledTeaMugBlockEntity filledTeaMugBlock) {
                if (!world.isClient) {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), filledTeaMugBlock.getSLOT_0());
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), filledTeaMugBlock.getSLOT_1());
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), filledTeaMugBlock.getSLOT_2());
                }

                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        if (state.get(WATERLOGGED)) {
            world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            BlockState blockState = state.with(WATERLOGGED, true);
            world.setBlockState(pos, blockState, 3);

            world.createAndScheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COUNT, WATERLOGGED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return switch (state.get(COUNT)) {
            case 2 -> COUNT_2;
            case 3 -> COUNT_3;
            default -> COUNT_1;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(COUNT)) {
            case 2 -> COUNT_2;
            case 3 -> COUNT_3;
            default -> COUNT_1;
        };
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FilledTeaMugBlockEntity(pos, state);
    }
}
