package com.yuanmomingshu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional target: Create: Genshin Impact need not be installed. */
@Pseudo
@Mixin(targets = "create_genshin_impact.procedures.DownloadGenshinImpactProcedure", remap = false)
public abstract class DownloadGenshinProcedureMixin {
    @Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private static void yuanMoMingChu$cancelGenshinDownload(CallbackInfo callback) {
        callback.cancel();
    }
}
