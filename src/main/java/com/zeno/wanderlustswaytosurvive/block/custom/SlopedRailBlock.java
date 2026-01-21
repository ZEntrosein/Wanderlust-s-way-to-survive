package com.zeno.wanderlustswaytosurvive.block.custom;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import com.mojang.serialization.MapCodec;

/**
 * 坡道铁轨 - 永远保持上坡状态，不会被相邻铁轨改变方向
 * 用于在没有支撑方块的情况下创建斜坡（如桥梁入口）
 */
public class SlopedRailBlock extends BaseRailBlock {
    public static final MapCodec<SlopedRailBlock> CODEC = simpleCodec(SlopedRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;

    @Override
    protected MapCodec<? extends BaseRailBlock> codec() {
        return CODEC;
    }

    public SlopedRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, RailShape.ASCENDING_NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        BlockState state = super.defaultBlockState();
        Direction direction = context.getHorizontalDirection();

        // 根据玩家朝向设置上坡方向
        switch (direction) {
            case EAST -> state = state.setValue(SHAPE, RailShape.ASCENDING_EAST);
            case WEST -> state = state.setValue(SHAPE, RailShape.ASCENDING_WEST);
            case NORTH -> state = state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
            case SOUTH -> state = state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
        }

        return state.setValue(WATERLOGGED, waterlogged);
    }

    @Override
    protected BlockState updateDir(Level level, BlockPos pos, BlockState state, boolean placing) {
        // 不更新方向，保持放置时的状态
        return state;
    }

    @Override
    protected BlockState updateState(BlockState state, Level level, BlockPos pos, boolean movedByPiston) {
        return state;
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // 不受邻居影响，保持原有形状
        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.getBlockState(pos).is(this)) {
            // 检查下方是否有支撑
            if (!canSupportRigidBlock(level, pos.below())) {
                dropResources(state, level, pos);
                level.removeBlock(pos, isMoving);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            RailShape shape = state.getValue(SHAPE);
            // 如果当前的形状不再是坡道（被拉平了），则尝试恢复
            if (!shape.isAscending()) {
                if (oldState.is(this) && oldState.getValue(SHAPE).isAscending()) {
                    // 恢复之前的坡道形状
                    level.setBlock(pos, state.setValue(SHAPE, oldState.getValue(SHAPE)), 3);
                }
            }
        }
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        // 检查前方是否有空气（用于判断是否在坡道顶端）
        boolean airInFront = false;
        RailShape shape = state.getValue(SHAPE);

        switch (shape) {
            case ASCENDING_NORTH -> airInFront = level.getBlockState(pos.north()).is(Blocks.AIR);
            case ASCENDING_EAST -> airInFront = level.getBlockState(pos.east()).is(Blocks.AIR);
            case ASCENDING_SOUTH -> airInFront = level.getBlockState(pos.south()).is(Blocks.AIR);
            case ASCENDING_WEST -> airInFront = level.getBlockState(pos.west()).is(Blocks.AIR);
            default -> {}
        }

        // 前方有空气时使用铜轨速度（可以加速冲出去），否则使用爬坡速度
        if (airInFront) {
            return (float) MomentumConfig.INSTANCE.copperRailSpeed.get().doubleValue();
        } else {
            return (float) MomentumConfig.INSTANCE.maxAscendingSpeed.get().doubleValue();
        }
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos,
            AbstractMinecart cart) {
        return state.getValue(SHAPE);
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        // 坡道轨本身就是斜坡，不需要额外形成斜坡
        return false;
    }


}
