package com.zeno.wanderlustswaytosurvive.block.custom;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.zeno.wanderlustswaytosurvive.registries.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Supplier;

public interface WeatheringRailBlock extends ChangeOverTimeBlock<WeatheringRailBlock.WeatherState> {

    Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> {
        return ImmutableBiMap.<Block, Block>builder()
                .put(ModBlocks.COPPER_RAIL.get(), ModBlocks.EXPOSED_COPPER_RAIL.get())
                .put(ModBlocks.EXPOSED_COPPER_RAIL.get(), ModBlocks.WEATHERED_COPPER_RAIL.get())
                .put(ModBlocks.WEATHERED_COPPER_RAIL.get(), ModBlocks.OXIDIZED_COPPER_RAIL.get())
                .build();
    });

    Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> {
        return NEXT_BY_BLOCK.get().inverse();
    });

    static Optional<Block> getPrevious(Block block) {
        return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(block));
    }

    static Block getFirst(Block block) {
        Block first = block;
        for (Block prev = PREVIOUS_BY_BLOCK.get().get(block); prev != null; prev = PREVIOUS_BY_BLOCK.get().get(prev)) {
            first = prev;
        }
        return first;
    }

    static Optional<BlockState> getPrevious(BlockState state) {
        return getPrevious(state.getBlock()).map((block) -> {
            return block.withPropertiesOf(state);
        });
    }

    static Optional<Block> getNext(Block block) {
        return Optional.ofNullable(NEXT_BY_BLOCK.get().get(block));
    }

    static BlockState getFirst(BlockState state) {
        return getFirst(state.getBlock()).withPropertiesOf(state);
    }

    @Override
    default Optional<BlockState> getNext(BlockState state) {
        return getNext(state.getBlock()).map((block) -> {
            return block.withPropertiesOf(state);
        });
    }

    @Override
    default float getChanceModifier() {
        return this.getAge() == WeatheringRailBlock.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
    }

    public enum WeatherState {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED;
    }
}
