package com.zeno.wanderlustswaytosurvive.block.custom;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import com.mojang.serialization.MapCodec;

/**
 * 交叉铁轨 - 两条轨道交叉，矿车根据移动方向自动选择路径
 * 用于铁轨十字路口，避免转弯
 */
public class RailCrossingBlock extends BaseRailBlock {
    public static final MapCodec<RailCrossingBlock> CODEC = simpleCodec(RailCrossingBlock::new);
    // 只允许直线形状（南北/东西），不允许转弯或斜坡
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    @Override
    protected MapCodec<? extends BaseRailBlock> codec() {
        return CODEC;
    }

    public RailCrossingBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, RailShape.NORTH_SOUTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return false; // 交叉轨不能形成斜坡
    }

    /**
     * 根据矿车的移动方向动态选择轨道方向
     * 如果矿车主要沿 Z 轴移动，选择南北轨道
     * 如果矿车主要沿 X 轴移动，选择东西轨道
     */
    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world,
            BlockPos pos, AbstractMinecart cart) {
        if (cart == null) {
            return RailShape.NORTH_SOUTH;
        }

        Vec3 deltaMovement = cart.getDeltaMovement();
        if (Math.abs(deltaMovement.z) > Math.abs(deltaMovement.x)) {
            return RailShape.NORTH_SOUTH;
        } else {
            return RailShape.EAST_WEST;
        }
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        // 使用铜轨速度
        return (float) MomentumConfig.INSTANCE.copperRailSpeed.get().doubleValue();
    }
}
