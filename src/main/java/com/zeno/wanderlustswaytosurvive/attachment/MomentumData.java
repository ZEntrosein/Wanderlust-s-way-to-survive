package com.zeno.wanderlustswaytosurvive.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class MomentumData {
    // Current accumulated speed bonus (additive to movement speed)
    private float currentSpeedBonus;
    // The number of ticks the player has been running on the current material
    private int ticksOnMaterial;
    // The resource location of the last block type (kept as RL for easier
    // serialization, or Block)
    // We'll use Block for runtime, but serialize as ResourceLocation
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

    public void reset() {
        this.currentSpeedBonus = 0.0f;
        this.ticksOnMaterial = 0;
        this.lastBlock = Blocks.AIR;
    }

    public void copyFrom(MomentumData other) {
        this.currentSpeedBonus = other.currentSpeedBonus;
        this.ticksOnMaterial = other.ticksOnMaterial;
        this.lastBlock = other.lastBlock;
    }

    // Serialization Codec
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
