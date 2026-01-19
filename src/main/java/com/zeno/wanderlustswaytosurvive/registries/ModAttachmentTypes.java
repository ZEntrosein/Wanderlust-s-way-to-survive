package com.zeno.wanderlustswaytosurvive.registries;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.attachment.MomentumData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister
            .create(NeoForgeRegistries.ATTACHMENT_TYPES, WanderlustsWayToSurvive.MOD_ID);

    public static final Supplier<AttachmentType<MomentumData>> MOMENTUM = ATTACHMENT_TYPES.register(
            "momentum",
            () -> AttachmentType.builder(MomentumData::new)
                    .serialize(MomentumData.CODEC)
                    .copyOnDeath() // Keep speed on death? Probably not, but maybe? Default is usually lost. Let's
                                   // keep it clean on death.
                    // Actually, let's NOT copy on death for speed. It should reset.
                    // But we likely want copyOnDeath() to be false (default behavior).
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
