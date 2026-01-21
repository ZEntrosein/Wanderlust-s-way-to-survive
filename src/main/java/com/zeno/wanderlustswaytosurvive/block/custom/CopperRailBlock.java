package com.zeno.wanderlustswaytosurvive.block.custom;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CopperRailBlock extends PoweredRailBlock implements WeatheringRailBlock {
    private final WeatheringRailBlock.WeatherState weatherState;

    public CopperRailBlock(Properties properties, WeatheringRailBlock.WeatherState weatherState) {
        // 关键修复：第二个参数 true 表示这是动力铁轨而非激活铁轨
        // Modern_Minecarts 使用这种方式避免晃动
        super(properties, true);
        this.weatherState = weatherState;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, net.minecraft.world.level.block.state.properties.RailShape.NORTH_SOUTH)
                .setValue(POWERED, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() == Items.HONEYCOMB) {
            if (level instanceof ServerLevel) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
                }

                BlockState waxedState = null;
                if (this.weatherState == WeatherState.UNAFFECTED) {
                    waxedState = ModBlocks.WAXED_COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.weatherState == WeatherState.EXPOSED) {
                    waxedState = ModBlocks.WAXED_EXPOSED_COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.weatherState == WeatherState.WEATHERED) {
                    waxedState = ModBlocks.WAXED_WEATHERED_COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.weatherState == WeatherState.OXIDIZED) {
                    waxedState = ModBlocks.WAXED_OXIDIZED_COPPER_RAIL.get().withPropertiesOf(state);
                }

                if (waxedState != null) {
                    level.setBlock(pos, waxedState, 11);
                    level.levelEvent(player, 3003, pos, 0); // Particles
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else if (stack.is(ItemTags.AXES)) {
            if (this.weatherState == WeatherState.UNAFFECTED) {
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }

            if (level instanceof ServerLevel) {
                BlockState previousState = null;
                if (this.weatherState == WeatherState.EXPOSED) {
                    previousState = ModBlocks.COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.weatherState == WeatherState.WEATHERED) {
                    previousState = ModBlocks.EXPOSED_COPPER_RAIL.get().withPropertiesOf(state);
                } else if (this.weatherState == WeatherState.OXIDIZED) {
                    previousState = ModBlocks.WEATHERED_COPPER_RAIL.get().withPropertiesOf(state);
                }

                if (previousState != null) {
                    level.setBlock(pos, previousState, 11);
                    level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(null, 3005, pos, 0); // Scrape particles
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

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        double finalSpeed = 0.4d;

        switch (this.weatherState) {
            case UNAFFECTED -> finalSpeed = MomentumConfig.INSTANCE.copperRailSpeed.get();
            case EXPOSED -> finalSpeed = MomentumConfig.INSTANCE.exposedCopperRailSpeed.get();
            case WEATHERED -> finalSpeed = MomentumConfig.INSTANCE.weatheredCopperRailSpeed.get();
            case OXIDIZED -> finalSpeed = MomentumConfig.INSTANCE.oxidizedCopperRailSpeed.get();
        }

        // Check ascending logic
        if (getRailDirection(state, level, pos, null).isAscending()
                && finalSpeed >= MomentumConfig.INSTANCE.maxAscendingSpeed.get()) {
            return (float) MomentumConfig.INSTANCE.maxAscendingSpeed.get().doubleValue();
        }

        // Debug Logging
        // if (level.getGameTime() % 20 == 0) System.out.println((level.isClientSide ?
        // "Client" : "Server") + " Speed: " + finalSpeed);

        return (float) finalSpeed;
    }

    // COPPER AGING
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float f = 0.05688889F;
        if (random.nextFloat() < f) {
            this.getNext(state).ifPresent((nextState) -> {
                level.setBlock(pos, nextState, 11);
            });
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return WeatheringRailBlock.getNext(state.getBlock()).isPresent();
    }

    @Override
    public WeatheringRailBlock.WeatherState getAge() {
        return this.weatherState;
    }
}
