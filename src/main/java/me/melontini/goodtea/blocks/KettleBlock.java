package me.melontini.goodtea.blocks;

import me.melontini.goodtea.blocks.entity.KettleBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import static me.melontini.goodtea.util.GoodTeaStuff.KETTLE_BLOCK_ENTITY;
import static me.melontini.goodtea.util.GoodTeaStuff.SHOW_SUPPORT;

@SuppressWarnings({"UnstableApiUsage", "deprecation"})
public class KettleBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty SUPPORT = BooleanProperty.of("support");
    private final VoxelShape BASE_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 7.0, 13.0);
    private final VoxelShape LID_SHAPE = Block.createCuboidShape(4.0, 7.0, 4.0, 12.0, 8.0, 12.0);

    private final VoxelShape LEG_0 = Block.createCuboidShape(0, -16, 0, 1, -1, 1);
    private final VoxelShape LEG_1 = Block.createCuboidShape(0, -16, 15, 1, -1, 16);
    private final VoxelShape LEG_2 = Block.createCuboidShape(15, -16, 0, 16, -1, 1);
    private final VoxelShape LEG_3 = Block.createCuboidShape(15, -16, 15, 16, -1, 16);
    private final VoxelShape TOP = Block.createCuboidShape(0, -1, 0, 16, 0, 16);
    private final VoxelShape SUPPORT_SHAPE = VoxelShapes.union(TOP, LEG_0, LEG_1, LEG_2, LEG_3);

    public KettleBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, KETTLE_BLOCK_ENTITY, KettleBlockEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient()) {
            if (!player.shouldCancelInteraction()) {
                if (hand == Hand.MAIN_HAND && stack.isOf(Items.WATER_BUCKET)) {
                    if (blockEntity instanceof KettleBlockEntity kettleBlockEntity) {
                        try (Transaction transaction = Transaction.openOuter()) {
                            long i = kettleBlockEntity.waterStorage.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET, transaction);
                            if (i != FluidConstants.BUCKET) {
                                transaction.abort();
                                return ActionResult.PASS;
                            }
                            if (!player.isCreative()) {
                                stack.decrement(1);
                                player.getInventory().insertStack(Items.BUCKET.getDefaultStack());
                            }
                            transaction.commit();
                            kettleBlockEntity.update();
                        }
                    }
                    return ActionResult.SUCCESS;
                } else this.openScreen(world, pos, player);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.success(world.isClient);
    }

    protected void openScreen(World world, BlockPos pos, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof KettleBlockEntity) {
            player.openHandledScreen((NamedScreenHandlerFactory) blockEntity);
            //TODO add Kettle Stat
            //player.incrementStat(Stats.INTERACT_WITH_FURNACE);
        }

    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
        if (state.isIn(SHOW_SUPPORT)) {
            return this.getDefaultState().with(FACING, ctx.getPlayerFacing()).with(SUPPORT, true);
        }
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing()).with(SUPPORT, false);
    }

    @Override //I guess?
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return state.with(SUPPORT, neighborState.isIn(SHOW_SUPPORT));
        }
        return state;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof KettleBlockEntity kettleBlockEntity) {
                if (!world.isClient) {
                    ItemScatterer.spawn(world, pos, kettleBlockEntity);
                }

                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SUPPORT);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new KettleBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return VoxelShapes.union(BASE_SHAPE, LID_SHAPE);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(SUPPORT)) {
            return VoxelShapes.union(BASE_SHAPE, LID_SHAPE, SUPPORT_SHAPE);
        }
        return VoxelShapes.union(BASE_SHAPE, LID_SHAPE);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
