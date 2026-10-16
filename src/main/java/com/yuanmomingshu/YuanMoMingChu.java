package com.yuanmomingshu;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(YuanMoMingChu.MOD_ID)
public final class YuanMoMingChu {
    public static final String MOD_ID = "create_wuthering_waves_crash";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public YuanMoMingChu() {
        LOGGER.info("机械动力：原末鸣初已加载，等待客户端检查接龙条件。");
    }
}
