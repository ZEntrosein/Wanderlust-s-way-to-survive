package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.attachment.MomentumData;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModAttachmentTypes;
import com.zeno.wanderlustswaytosurvive.registries.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@EventBusSubscriber
public class MomentumHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int debugTickCounter = 0;

    private static final UUID MOMENTUM_MODIFIER_ID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
    // Use a fixed ResourceLocation for the attribute modifier in 1.21 if needed,
    // but UUID is still used in code often.
    // NeoForge/Vanilla 1.21 might prefer ResourceLocation for modifiers.
    private static final ResourceLocation MOMENTUM_MODIFIER_RL = ResourceLocation
            .fromNamespaceAndPath("wanderlustswaytosurvive", "momentum_bonus");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide)
            return; // Logic on server side, attributes sync to client

        MomentumData data = player.getData(ModAttachmentTypes.MOMENTUM);

        // 1. Check Enchantment using ItemEnchantments component (works for datapack
        // enchantments)
        var boots = player.getItemBySlot(EquipmentSlot.FEET);
        int enchantmentLevel = getEnchantmentLevelByKey(boots, ModEnchantments.MOMENTUM);

        if (enchantmentLevel <= 0) {
            if (data.getCurrentSpeedBonus() > 0) {
                resetMomentum(player, data);
            }
            // Debug: No enchantment on boots
            debugTickCounter++;
            if (debugTickCounter >= 20) {
                debugTickCounter = 0;
                LOGGER.info("[Traveler Debug] Player {} has no Traveler enchantment on boots (level: {})",
                        player.getName().getString(), enchantmentLevel);
            }
            return;
        }

        // 2. Check Movement State (Sprinting + On Ground)
        // We only build momentum if on ground and sprinting.
        // If in air, we maintain it (or decay slowly? Plan says seamless transition via
        // jumping).
        // For now: Maintain if jumping, Reset only on distinctive block change.
        // NOTE: We do NOT reset when player stops moving - only when block type
        // changes.
        // This allows maintaining momentum through grass and other slowdown blocks.

        // Get the actual ground block (skip non-solid blocks like grass, flowers, etc.)
        Block blockBelow = getActualGroundBlock(player);

        boolean isOnGround = player.onGround();
        boolean isSprinting = player.isSprinting();

        // Check if player is actively moving (for accumulation, not reset)
        double horizontalSpeed = Math.sqrt(
                player.getDeltaMovement().x * player.getDeltaMovement().x +
                        player.getDeltaMovement().z * player.getDeltaMovement().z);
        boolean isMoving = horizontalSpeed > 0.001;

        // Debug: Log block detection
        debugTickCounter++;
        if (debugTickCounter >= 20) {
            Block lastBlock = data.getLastBlock();
            LOGGER.info(
                    "[Traveler Debug] Ground Detection - blockBelow: {}, lastBlock: {}, onGround: {}, isMoving: {}, speed: {}",
                    blockBelow != null ? BuiltInRegistries.BLOCK.getKey(blockBelow) : "null",
                    lastBlock != null ? BuiltInRegistries.BLOCK.getKey(lastBlock) : "null",
                    isOnGround,
                    isMoving,
                    String.format("%.4f", horizontalSpeed));
        }

        if (isOnGround && blockBelow != null) {
            // Check Material match
            if (blockBelow != data.getLastBlock()
                    && data.getLastBlock() != net.minecraft.world.level.block.Blocks.AIR) {
                // Material Changed!
                // Reset momentum
                LOGGER.info("[Traveler Debug] RESET: Block changed from {} to {}",
                        data.getLastBlock() != null ? BuiltInRegistries.BLOCK.getKey(data.getLastBlock()) : "null",
                        BuiltInRegistries.BLOCK.getKey(blockBelow));
                resetMomentum(player, data);
                data.setLastBlock(blockBelow);
            } else {
                // Same material or started fresh
                data.setLastBlock(blockBelow);

                // Accumulate Speed
                accumulateMomentum(player, data, blockBelow, enchantmentLevel);
            }
        } else if (isOnGround && blockBelow == null) {
            // On something non-solid - just maintain current state, don't reset
            if (debugTickCounter >= 20) {
                LOGGER.info("[Traveler Debug] On non-solid block, maintaining momentum");
            }
        } else {
            // In Air
            // Maintain momentum? Or decay?
            // "Seamless switching" implies maintaining.
            // Do nothing, just apply current speed.
        }

        // Reset debug counter if it reached threshold
        if (debugTickCounter >= 20) {
            debugTickCounter = 0;
        }

        // Apply Attribute
        applySpeedModifier(player, data.getCurrentSpeedBonus());

        // Debug: Print speed every second (20 ticks)
        debugTickCounter++;
        if (debugTickCounter >= 20) {
            debugTickCounter = 0;
            LOGGER.info("[Traveler Debug] Player: {}, Speed Bonus: {}, Ticks on Material: {}, Block: {}",
                    player.getName().getString(),
                    String.format("%.4f", data.getCurrentSpeedBonus()),
                    data.getTicksOnMaterial(),
                    data.getLastBlock() != null ? BuiltInRegistries.BLOCK.getKey(data.getLastBlock()) : "null");
        }
    }

    private static void accumulateMomentum(Player player, MomentumData data, Block block, int enchantmentLevel) {
        data.setTicksOnMaterial(data.getTicksOnMaterial() + 1);

        // Calculate Cap
        double baseCap = MomentumConfig.INSTANCE.baseSpeedCap.get();
        double xpBonus = player.experienceLevel * MomentumConfig.INSTANCE.xpSpeedMultiplier.get();

        // Block Cap overwrite
        ResourceLocation blockRL = BuiltInRegistries.BLOCK.getKey(block);
        double blockCap = getAllConfigBlockCaps(blockRL);

        // Use the higher of base or block specific? Or just block specific overrides
        // base?
        // Let's say Map overrides Base if present. Otherwise Base.
        double capBaseValue = (blockCap >= 0) ? blockCap : baseCap;

        double totalCap = (capBaseValue + xpBonus)
                * (1 + (enchantmentLevel * MomentumConfig.INSTANCE.enchantmentLevelMultiplier.get()));

        // Acceleration rate
        // Let's say it takes 5 seconds (100 ticks) to reach cap.
        float acceleration = (float) (totalCap / 100.0f);

        if (data.getCurrentSpeedBonus() < totalCap) {
            data.setCurrentSpeedBonus(Math.min((float) totalCap, data.getCurrentSpeedBonus() + acceleration));
        }
    }

    private static void resetMomentum(Player player, MomentumData data) {
        data.setCurrentSpeedBonus(0);
        data.setTicksOnMaterial(0);
        data.setLastBlock(net.minecraft.world.level.block.Blocks.AIR);
        applySpeedModifier(player, 0);
    }

    private static void applySpeedModifier(Player player, float value) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MOMENTUM_MODIFIER_RL);
            if (value > 0) {
                attribute.addTransientModifier(new AttributeModifier(
                        MOMENTUM_MODIFIER_RL,
                        value,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static double getAllConfigBlockCaps(ResourceLocation blockRL) {
        // Implement parsing properly.
        // List<String> format: "modid:block,cap"
        List<? extends String> caps = MomentumConfig.INSTANCE.blockSpeedCaps.get();
        for (String s : caps) {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                if (parts[0].trim().equals(blockRL.toString())) {
                    try {
                        return Double.parseDouble(parts[1].trim());
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Get enchantment level by ResourceKey from an ItemStack.
     * Works with datapack-driven enchantments in 1.21+.
     */
    private static int getEnchantmentLevelByKey(net.minecraft.world.item.ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty())
            return 0;

        net.minecraft.world.item.enchantment.ItemEnchantments enchantments = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);

        // Debug: Print all enchantments on the item
        if (!enchantments.isEmpty()) {
            LOGGER.info("[Traveler Debug] Checking boots for enchantment: {}", enchantmentKey.location());
            for (var entry : enchantments.entrySet()) {
                var holder = entry.getKey();
                var keyOpt = holder.unwrapKey();
                if (keyOpt.isPresent()) {
                    LOGGER.info("[Traveler Debug]   Found enchantment: {} (level {})",
                            keyOpt.get().location(), entry.getIntValue());
                    if (keyOpt.get().equals(enchantmentKey)) {
                        return entry.getIntValue();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Get the actual solid ground block under the player, skipping non-collidable
     * blocks
     * like tall grass, flowers, etc.
     * Returns null if no solid block found within 2 blocks below player.
     */
    private static Block getActualGroundBlock(Player player) {
        net.minecraft.core.BlockPos playerPos = player.blockPosition();
        net.minecraft.world.level.Level level = player.level();

        // Check blocks from player's feet position down
        for (int y = 0; y >= -2; y--) {
            net.minecraft.core.BlockPos checkPos = playerPos.offset(0, y, 0);
            BlockState state = level.getBlockState(checkPos);
            Block block = state.getBlock();

            // Skip air
            if (state.isAir())
                continue;

            // Skip non-collidable blocks (grass, flowers, etc.)
            // These blocks don't have collision and player walks through them
            if (!state.getCollisionShape(level, checkPos).isEmpty()) {
                return block;
            }

            // Also check if the block is tagged as replaceable (like tall grass)
            // These shouldn't count for momentum tracking
        }

        return null;
    }
}
