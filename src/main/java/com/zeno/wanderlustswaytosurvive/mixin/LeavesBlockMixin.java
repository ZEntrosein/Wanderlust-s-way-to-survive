package com.zeno.wanderlustswaytosurvive.mixin;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 骑马穿叶 Mixin
 * 当骑马且马踩着非树叶方块时，可以穿过树叶
 * 离开地面后有短暂宽限期，仍可穿过树叶
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LeavesBlockMixin {

    // 记录每匹马最后一次站在非树叶方块上的时间
    @Unique
    private static final Map<UUID, Long> lastOnSolidGroundTime = new ConcurrentHashMap<>();

    // 宽限期（毫秒）- 从配置读取
    @Unique
    private static long getGracePeriodMs() {
        try {
            if (MomentumConfig.INSTANCE != null) {
                return MomentumConfig.INSTANCE.horseLeafGracePeriod.get();
            }
        } catch (Exception ignored) {
        }
        return 500; // 默认值
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void modifyCollisionForHorse(BlockGetter world, BlockPos pos,
            CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {

        // 获取当前 BlockState
        BlockState self = (BlockState) (Object) this;

        // 只处理树叶方块
        if (!(self.getBlock() instanceof LeavesBlock)) {
            return;
        }

        // 检查配置是否已加载且启用
        try {
            if (MomentumConfig.INSTANCE == null || !MomentumConfig.INSTANCE.enableHorseLeafPassthrough.get()) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();

            // 检查是否是被骑乘的马类实体
            if (entity instanceof AbstractHorse horse && horse.isVehicle()) {
                UUID horseId = horse.getUUID();
                long currentTime = System.currentTimeMillis();

                // 获取马脚下的方块
                BlockPos belowPos = horse.blockPosition().below();
                BlockState belowState = world.getBlockState(belowPos);

                boolean onNonLeafGround = !(belowState.getBlock() instanceof LeavesBlock)
                        && !belowState.isAir();

                if (onNonLeafGround) {
                    // 马站在非树叶实体方块上，更新时间戳
                    lastOnSolidGroundTime.put(horseId, currentTime);
                    cir.setReturnValue(Shapes.empty());
                } else {
                    // 马不在非树叶地面上，检查宽限期
                    Long lastTime = lastOnSolidGroundTime.get(horseId);
                    if (lastTime != null && (currentTime - lastTime) < getGracePeriodMs()) {
                        // 在宽限期内，仍可穿过树叶
                        cir.setReturnValue(Shapes.empty());
                    }
                    // 超出宽限期，保持原版碰撞
                }
            }
        }
    }
}
