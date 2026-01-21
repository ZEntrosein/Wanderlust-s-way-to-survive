package com.zeno.wanderlustswaytosurvive.block.custom;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedCopperRailBlock extends PoweredRailBlock {
    private final WaxedWeatherState waxedWeatherState;

    public WaxedCopperRailBlock(Properties properties, WaxedWeatherState waxedWeatherState) {
        // 关键修复：第二个参数 true 表示这是动力铁轨而非激活铁轨
        super(properties, true);
        this.waxedWeatherState = waxedWeatherState;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, net.minecraft.world.level.block.state.properties.RailShape.NORTH_SOUTH)
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        double finalSpeed = 0.4d;

        switch (this.waxedWeatherState) {
            case WAXED_UNAFFECTED -> finalSpeed = MomentumConfig.INSTANCE.copperRailSpeed.get();
            case WAXED_EXPOSED -> finalSpeed = MomentumConfig.INSTANCE.exposedCopperRailSpeed.get();
            case WAXED_WEATHERED -> finalSpeed = MomentumConfig.INSTANCE.weatheredCopperRailSpeed.get();
            case WAXED_OXIDIZED -> finalSpeed = MomentumConfig.INSTANCE.oxidizedCopperRailSpeed.get();
        }

        var railShape = state.getValue(SHAPE);
        if (railShape.isAscending() && finalSpeed >= MomentumConfig.INSTANCE.maxAscendingSpeed.get()) {
            return (float) MomentumConfig.INSTANCE.maxAscendingSpeed.get().doubleValue();
        }

        return (float) finalSpeed;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ItemTags.AXES)) {
            if (level instanceof ServerLevel) {
                BlockState unusedState = null;
                if (this.waxedWeatherState == WaxedWeatherState.WAXED_UNAFFECTED) {
                    unusedState = ModBlocks.COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.waxedWeatherState == WaxedWeatherState.WAXED_EXPOSED) {
                    unusedState = ModBlocks.EXPOSED_COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.waxedWeatherState == WaxedWeatherState.WAXED_WEATHERED) {
                    unusedState = ModBlocks.WEATHERED_COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.waxedWeatherState == WaxedWeatherState.WAXED_OXIDIZED) {
                    unusedState = ModBlocks.OXIDIZED_COPPER_RAIL.get().withPropertiesOf(state);
                }

                if (unusedState != null) {
                    level.setBlock(pos, unusedState, 11);
                    level.playSound(null, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(null, 3004, pos, 0); // Wax off particles
                    if (player != null) {
                        stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    public enum WaxedWeatherState {
        WAXED_UNAFFECTED,
        WAXED_EXPOSED,
        WAXED_WEATHERED,
        WAXED_OXIDIZED;
    }
}
