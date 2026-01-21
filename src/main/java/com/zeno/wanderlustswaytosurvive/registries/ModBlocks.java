package com.zeno.wanderlustswaytosurvive.registries;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.block.custom.CopperRailBlock;
import com.zeno.wanderlustswaytosurvive.block.custom.RailCrossingBlock;
import com.zeno.wanderlustswaytosurvive.block.custom.SlopedRailBlock;
import com.zeno.wanderlustswaytosurvive.block.custom.WaxedCopperRailBlock;
import com.zeno.wanderlustswaytosurvive.block.custom.WeatheringRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister
                        .createBlocks(WanderlustsWayToSurvive.MOD_ID);

        // Unwaxed variants
        public static final DeferredBlock<Block> COPPER_RAIL = BLOCKS.register("copper_rail",
                        () -> new CopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WeatheringRailBlock.WeatherState.UNAFFECTED));

        public static final DeferredBlock<Block> EXPOSED_COPPER_RAIL = BLOCKS.register("exposed_copper_rail",
                        () -> new CopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WeatheringRailBlock.WeatherState.EXPOSED));

        public static final DeferredBlock<Block> WEATHERED_COPPER_RAIL = BLOCKS.register("weathered_copper_rail",
                        () -> new CopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WeatheringRailBlock.WeatherState.WEATHERED));

        public static final DeferredBlock<Block> OXIDIZED_COPPER_RAIL = BLOCKS.register("oxidized_copper_rail",
                        () -> new CopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WeatheringRailBlock.WeatherState.OXIDIZED));

        // Waxed variants
        public static final DeferredBlock<Block> WAXED_COPPER_RAIL = BLOCKS.register("waxed_copper_rail",
                        () -> new WaxedCopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED));

        public static final DeferredBlock<Block> WAXED_EXPOSED_COPPER_RAIL = BLOCKS.register(
                        "waxed_exposed_copper_rail",
                        () -> new WaxedCopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED));

        public static final DeferredBlock<Block> WAXED_WEATHERED_COPPER_RAIL = BLOCKS.register(
                        "waxed_weathered_copper_rail",
                        () -> new WaxedCopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED));

        public static final DeferredBlock<Block> WAXED_OXIDIZED_COPPER_RAIL = BLOCKS.register(
                        "waxed_oxidized_copper_rail",
                        () -> new WaxedCopperRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL).noCollission(),
                                        WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED));

        // Sloped Rail - 坡道铁轨
        public static final DeferredBlock<Block> SLOPED_RAIL = BLOCKS.register("sloped_rail",
                        () -> new SlopedRailBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).noCollission()));

        // Rail Crossing - 交叉铁轨
        public static final DeferredBlock<Block> RAIL_CROSSING = BLOCKS.register("rail_crossing",
                        () -> new RailCrossingBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL).noCollission()));

        public static void register(IEventBus eventBus) {
                BLOCKS.register(eventBus);
        }
}
