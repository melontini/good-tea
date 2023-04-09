package me.melontini.goodtea.blocks;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

@SuppressWarnings("deprecation")
public class TeaMugBlock extends Block implements Waterloggable {
    public static final IntProperty COUNT = IntProperty.of("count", 1, 3);

    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private final VoxelShape COUNT_1 = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 5.0, 10.0);
    private final VoxelShape COUNT_2 = VoxelShapes.union(Block.createCuboidShape(9.0, 0.0, 6.0, 13.0, 5.0, 10.0), Block.createCuboidShape(3.0, 0.0, 6.0, 7.0, 5.0, 10.0));
    private final VoxelShape COUNT_3 = VoxelShapes.union(Block.createCuboidShape(9.0, 0.0, 9.0, 13.0, 5.0, 13.0), Block.createCuboidShape(3.0, 0.0, 9.0, 7.0, 5.0, 13.0), Block.createCuboidShape(6.0, 0.0, 3.0, 10.0, 5.0, 7.0));

    public TeaMugBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(COUNT, 1).with(WATERLOGGED, false));
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return !context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(COUNT) < 3
                ? true
                : super.canReplace(state, context);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockState.isOf(this)) {
            return blockState.cycle(COUNT);
        } else {
            FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
            boolean bl = fluidState.getFluid() == Fluids.WATER;
            return super.getPlacementState(ctx).with(WATERLOGGED, bl);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(
            BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!state.get(WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            BlockState blockState = state.with(WATERLOGGED, true);
            world.setBlockState(pos, blockState, 3);

            world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
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

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }
}
