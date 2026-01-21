package com.zeno.wanderlustswaytosurvive.registries;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import net.minecraft.world.item.BlockItem;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WanderlustsWayToSurvive.MOD_ID);

        public static final DeferredItem<BlockItem> COPPER_RAIL = ITEMS.registerSimpleBlockItem("copper_rail",
                        ModBlocks.COPPER_RAIL);
        public static final DeferredItem<BlockItem> EXPOSED_COPPER_RAIL = ITEMS
                        .registerSimpleBlockItem("exposed_copper_rail", ModBlocks.EXPOSED_COPPER_RAIL);
        public static final DeferredItem<BlockItem> WEATHERED_COPPER_RAIL = ITEMS
                        .registerSimpleBlockItem("weathered_copper_rail", ModBlocks.WEATHERED_COPPER_RAIL);
        public static final DeferredItem<BlockItem> OXIDIZED_COPPER_RAIL = ITEMS
                        .registerSimpleBlockItem("oxidized_copper_rail", ModBlocks.OXIDIZED_COPPER_RAIL);

        public static final DeferredItem<BlockItem> WAXED_COPPER_RAIL = ITEMS.registerSimpleBlockItem(
                        "waxed_copper_rail",
                        ModBlocks.WAXED_COPPER_RAIL);
        public static final DeferredItem<BlockItem> WAXED_EXPOSED_COPPER_RAIL = ITEMS
                        .registerSimpleBlockItem("waxed_exposed_copper_rail", ModBlocks.WAXED_EXPOSED_COPPER_RAIL);
        public static final DeferredItem<BlockItem> WAXED_WEATHERED_COPPER_RAIL = ITEMS
                        .registerSimpleBlockItem("waxed_weathered_copper_rail", ModBlocks.WAXED_WEATHERED_COPPER_RAIL);
        public static final DeferredItem<BlockItem> WAXED_OXIDIZED_COPPER_RAIL = ITEMS
                        .registerSimpleBlockItem("waxed_oxidized_copper_rail", ModBlocks.WAXED_OXIDIZED_COPPER_RAIL);

        public static void register(IEventBus eventBus) {
                ITEMS.register(eventBus);
        }
}
