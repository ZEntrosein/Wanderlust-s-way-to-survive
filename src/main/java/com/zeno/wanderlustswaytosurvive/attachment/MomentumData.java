package com.zeno.wanderlustswaytosurvive.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/**
 * 动量数据类 - 存储玩家的速度加成状态
 * 用于旅者附魔的速度累积功能
 */
public class MomentumData {
    // 当前累积的速度加成（直接加到移动速度上）
    private float currentSpeedBonus;
    // 玩家在当前方块材质上奔跑的tick数
    private int ticksOnMaterial;
    // 上一次站立的方块类型（运行时使用Block，序列化时使用ResourceLocation）
    private Block lastBlock;

    public MomentumData() {
        this.currentSpeedBonus = 0.0f;
        this.ticksOnMaterial = 0;
        this.lastBlock = Blocks.AIR;
    }

    public MomentumData(float currentSpeedBonus, int ticksOnMaterial, Block lastBlock) {
        this.currentSpeedBonus = currentSpeedBonus;
        this.ticksOnMaterial = ticksOnMaterial;
        this.lastBlock = lastBlock;
    }

    public float getCurrentSpeedBonus() {
        return currentSpeedBonus;
    }

    public void setCurrentSpeedBonus(float currentSpeedBonus) {
        this.currentSpeedBonus = currentSpeedBonus;
    }

    public int getTicksOnMaterial() {
        return ticksOnMaterial;
    }

    public void setTicksOnMaterial(int ticksOnMaterial) {
        this.ticksOnMaterial = ticksOnMaterial;
    }

    public Block getLastBlock() {
        return lastBlock;
    }

    public void setLastBlock(Block lastBlock) {
        this.lastBlock = lastBlock;
    }

    /**
     * 重置所有动量数据
     */
    public void reset() {
        this.currentSpeedBonus = 0.0f;
        this.ticksOnMaterial = 0;
        this.lastBlock = Blocks.AIR;
    }

    /**
     * 从另一个MomentumData复制数据
     */
    public void copyFrom(MomentumData other) {
        this.currentSpeedBonus = other.currentSpeedBonus;
        this.ticksOnMaterial = other.ticksOnMaterial;
        this.lastBlock = other.lastBlock;
    }

    // 序列化编解码器
    public static final Codec<MomentumData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("currentSpeedBonus").forGetter(MomentumData::getCurrentSpeedBonus),
            Codec.INT.fieldOf("ticksOnMaterial").forGetter(MomentumData::getTicksOnMaterial),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("lastBlock").forGetter(MomentumData::getLastBlock))
            .apply(instance, MomentumData::new));

    @Override
    public String toString() {
        return "MomentumData{" +
                "currentSpeedBonus=" + currentSpeedBonus +
                ", ticksOnMaterial=" + ticksOnMaterial +
                ", lastBlock=" + BuiltInRegistries.BLOCK.getKey(lastBlock) +
                '}';
    }
}
