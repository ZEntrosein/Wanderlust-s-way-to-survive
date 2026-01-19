package com.zeno.wanderlustswaytosurvive.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 骑乘末影珍珠传送数据类
 * 存储玩家传送时需要跟随的坐骑信息
 */
public class MountedPearlData {
    // 坐骑实体 ID（-1 表示无待传送坐骑）
    private int vehicleId;
    // 目标传送坐标
    private double targetX;
    private double targetY;
    private double targetZ;
    // 剩余同步 tick 数
    private int ticksRemaining;

    public MountedPearlData() {
        this.vehicleId = -1;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.ticksRemaining = 0;
    }

    public MountedPearlData(int vehicleId, double targetX, double targetY, double targetZ, int ticksRemaining) {
        this.vehicleId = vehicleId;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.ticksRemaining = ticksRemaining;
    }

    // Getters and Setters
    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }

    public void setTargetPosition(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void setTicksRemaining(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    /**
     * 检查是否有待传送的坐骑
     */
    public boolean hasPendingVehicle() {
        return vehicleId > 0;
    }

    /**
     * 重置所有数据
     */
    public void reset() {
        this.vehicleId = -1;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.ticksRemaining = 0;
    }

    // 序列化编解码器
    public static final Codec<MountedPearlData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("vehicleId").forGetter(MountedPearlData::getVehicleId),
            Codec.DOUBLE.fieldOf("targetX").forGetter(MountedPearlData::getTargetX),
            Codec.DOUBLE.fieldOf("targetY").forGetter(MountedPearlData::getTargetY),
            Codec.DOUBLE.fieldOf("targetZ").forGetter(MountedPearlData::getTargetZ),
            Codec.INT.fieldOf("ticksRemaining").forGetter(MountedPearlData::getTicksRemaining))
            .apply(instance, MountedPearlData::new));
}
