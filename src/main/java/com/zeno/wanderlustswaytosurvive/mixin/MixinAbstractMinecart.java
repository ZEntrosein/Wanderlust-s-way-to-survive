package com.zeno.wanderlustswaytosurvive.mixin;

import com.zeno.wanderlustswaytosurvive.block.custom.CopperRailBlock;
import com.zeno.wanderlustswaytosurvive.block.custom.WaxedCopperRailBlock;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// 确保铜铁轨在原版矿车物理中被正确识别为动力铁轨
@Mixin(AbstractMinecart.class)
public abstract class MixinAbstractMinecart {

    @ModifyVariable(method = "moveAlongTrack", at = @At("HEAD"), argsOnly = true)
    private BlockState wanderlusts_way_to_survive$masqueradeCopperRail(BlockState state) {
        if (state.getBlock() instanceof CopperRailBlock || state.getBlock() instanceof WaxedCopperRailBlock) {
            // 伪装成动力铁轨以触发原版加速逻辑
            return Blocks.POWERED_RAIL.defaultBlockState()
                    .setValue(PoweredRailBlock.SHAPE, state.getValue(PoweredRailBlock.SHAPE))
                    .setValue(PoweredRailBlock.POWERED, state.getValue(PoweredRailBlock.POWERED));
        }
        return state;
    }
}
