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

@EventBusSubscriber
public class MomentumHandler {
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

        // 1. Check Enchantment
        var momentumHolderInfo = player.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ModEnchantments.MOMENTUM);

        if (momentumHolderInfo.isEmpty())
            return;

        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                momentumHolderInfo.get(),
                player.getItemBySlot(EquipmentSlot.FEET));

        if (enchantmentLevel <= 0) {
            if (data.getCurrentSpeedBonus() > 0) {
                resetMomentum(player, data);
            }
            return;
        }

        // 2. Check Movement State (Sprinting + On Ground)
        // We only build momentum if on ground and sprinting.
        // If in air, we maintain it (or decay slowly? Plan says seamless transition via
        // jumping).
        // For now: Maintain if jumping, Reset if stopped or distinctive block change.

        BlockState stateBelow = player.getBlockStateOn();
        Block blockBelow = stateBelow.getBlock();

        boolean isSprinting = player.isSprinting();
        boolean isOnGround = player.onGround();

        // If stopped moving or sneaking, reset
        if (!isSprinting && isOnGround
                && (Math.abs(player.getDeltaMovement().x) < 0.01 && Math.abs(player.getDeltaMovement().z) < 0.01)) {
            // Reset if completely stopped? Or just decay?
            // "Accelerate continuously on same material" implies stopping resets it.
            resetMomentum(player, data);
            return;
        }

        if (isOnGround) {
            // Check Material match
            if (blockBelow != data.getLastBlock()
                    && data.getLastBlock() != net.minecraft.world.level.block.Blocks.AIR) {
                // Material Changed!
                // Reset momentum
                resetMomentum(player, data);
                data.setLastBlock(blockBelow);
            } else {
                // Same material or started fresh
                data.setLastBlock(blockBelow);

                // Accumulate Speed
                accumulateMomentum(player, data, blockBelow, enchantmentLevel);
            }
        } else {
            // In Air
            // Maintain momentum? Or decay?
            // "Seamless switching" implies maintaining.
            // Do nothing, just apply current speed.
        }

        // Apply Attribute
        applySpeedModifier(player, data.getCurrentSpeedBonus());
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
}
