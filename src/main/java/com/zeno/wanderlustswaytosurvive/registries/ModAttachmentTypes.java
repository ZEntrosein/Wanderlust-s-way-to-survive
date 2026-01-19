package com.zeno.wanderlustswaytosurvive.registries;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.attachment.MomentumData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 数据附件类型注册类
 * 用于注册附加到玩家实体上的自定义数据
 */
public class ModAttachmentTypes {
    // 附件类型延迟注册器
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, WanderlustsWayToSurvive.MOD_ID);

    // 动量数据附件 - 存储玩家的速度加成状态
    // 注意：死亡时不保留速度加成，应该重置
    public static final Supplier<AttachmentType<MomentumData>> MOMENTUM = ATTACHMENT_TYPES.register(
            "momentum",
            () -> AttachmentType.builder(MomentumData::new)
                    .serialize(MomentumData.CODEC)
                    // 死亡时不复制数据（速度在死亡时重置）
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
