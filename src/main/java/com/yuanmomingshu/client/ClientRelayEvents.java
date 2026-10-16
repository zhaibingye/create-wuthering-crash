package com.yuanmomingshu.client;

import com.yuanmomingshu.RelayAction;
import com.yuanmomingshu.YuanMoMingChu;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.net.URI;

@Mod.EventBusSubscriber(modid = YuanMoMingChu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT)
public final class ClientRelayEvents {
    private static final RelayAction RELAY = new RelayAction();

    private ClientRelayEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || RELAY.isChecked()) {
            return;
        }
        ModList mods = ModList.get();
        RELAY.runIfMatched(mods.isLoaded("create"), mods.isLoaded("create_genshin_impact"), () -> {
            YuanMoMingChu.LOGGER.warn("接龙条件已满足：已拦截原神链接，即将打开鸣潮下载链接并主动崩溃。URL={}",
                    RelayAction.DOWNLOAD_URL);
            Util.getPlatform().openUri(URI.create(RelayAction.DOWNLOAD_URL));
        });
    }
}
