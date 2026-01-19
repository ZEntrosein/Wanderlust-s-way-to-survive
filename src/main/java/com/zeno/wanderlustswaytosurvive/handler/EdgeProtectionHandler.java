package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModEnchantments;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID, value = Dist.CLIENT)
public class EdgeProtectionHandler {

    @SubscribeEvent
    public static void onInputUpdate(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player))
            return;

        // 1. Check Enchantment
        // Note: EnchantmentHelper on client should work if tags are synced.
        var momentumHolderInfo = player.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ModEnchantments.MOMENTUM);

        if (momentumHolderInfo.isEmpty())
            return;

        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                momentumHolderInfo.get(),
                player.getItemBySlot(EquipmentSlot.FEET));

        if (enchantmentLevel <= 0)
            return;

        // 2. Check Conditions: On Ground, Not Jumping, Sprinting
        // Input.jumping is true if space is held.
        if (!player.onGround() || event.getInput().jumping || !player.isSprinting()) {
            return;
        }

        // 3. Check Speed Threshold
        // Configurable threshold? Let's say if speed > base speed + something.
        double currentSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        // Default player speed is 0.1. Sprinting adds. Momentum adds more.
        // Let's assume a threshold of 0.2 (twice base walk speed).
        // Using config if possible, or hardcoded for now.
        double threshold = MomentumConfig.INSTANCE.baseSpeedCap.get() * 0.5 + 0.1; // Rough heuristic

        if (currentSpeed < threshold)
            return;

        // 4. Predict movement
        // We look at Input forward/left/right
        float forward = event.getInput().forwardImpulse;
        float strafe = event.getInput().leftImpulse; // check naming

        if (forward == 0 && strafe == 0)
            return;

        // Simple check: Look 1 block ahead in look direction
        // Or accurate check: player velocity direction
        Vec3 lookAngle = player.getLookAngle();
        // Flatten look vector
        lookAngle = new Vec3(lookAngle.x, 0, lookAngle.z).normalize();

        // Calculate predicted position relative to feet
        // If moving forward:
        double checkDist = 0.6; // slightly outside hitbox
        Vec3 checkPos = player.position().add(lookAngle.scale(checkDist));

        BlockPos currentPos = player.blockPosition();
        BlockPos nextPos = BlockPos.containing(checkPos);

        if (currentPos.equals(nextPos))
            return; // Still in same block

        BlockState currentState = player.level().getBlockState(currentPos);
        BlockState nextState = player.level().getBlockState(nextPos);

        // If blocks are different (and next is not Air? No, we don't want to run into
        // air either ideally?
        // Wait, "Edge Protection" usually means "Don't fall off".
        // But the requirement says "Entering different material".
        // So checking block type equality is correct.

        if (currentState.getBlock() != nextState.getBlock()) {
            // Block type mismatch! Stop input.
            event.getInput().forwardImpulse = 0;
            event.getInput().leftImpulse = 0;

            // Maybe tiny backward push or visual shake?
            // For now just stop input.
        }
    }
}
