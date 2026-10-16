package com.yuanmomingshu;

/** The launch decision and one-shot guard do not depend on Minecraft classes. */
public final class RelayAction {
    public static final String DOWNLOAD_URL =
            "https://download.kurogames.com/transfer/mc_WnGtDn85y8lJB4mTmYHYuNjIl9n6YGVm/official/cn/zh-Hans/pc_app";
    public static final String CRASH_MESSAGE =
            "机械动力引来了原神，原神被拦下了，但鸣潮来了。"
            + " [create_wuthering_waves_crash: intentional mod relay crash]";

    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void runIfMatched(boolean createLoaded, boolean genshinModLoaded, Runnable openDownload) {
        if (checked) {
            return;
        }
        // Set the guard before opening the browser, including the failure path.
        checked = true;
        if (!createLoaded || !genshinModLoaded) {
            return;
        }

        RuntimeException openFailure = null;
        try {
            openDownload.run();
        } catch (RuntimeException exception) {
            openFailure = exception;
        }
        // Throw on the client tick thread so Minecraft writes its normal crash report.
        throw new IntentionalRelayCrash(openFailure);
    }

    public static final class IntentionalRelayCrash extends RuntimeException {
        private IntentionalRelayCrash(Throwable cause) {
            super(CRASH_MESSAGE, cause);
        }
    }
}
