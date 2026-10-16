# 机械动力：原末鸣初

Minecraft 模组接龙作品，适用于 **Minecraft 1.20.1、Forge 47.x、Java 17**。

安装本模组后，同时检测到 `create`（机械动力）和 `create_genshin_impact`（Create: Genshin Impact）时：

1. 使用 Mixin 取消原模组的原神下载方法，阻止打开原神链接。
2. 在客户端首次 tick 打开鸣潮官方下载链接。
3. 在同一个客户端线程主动抛出异常，由 Minecraft 的崩溃处理流程生成报告并退出。

鸣潮下载地址：

<https://download.kurogames.com/transfer/mc_WnGtDn85y8lJB4mTmYHYuNjIl9n6YGVm/official/cn/zh-Hans/pc_app>

这里的“下载”沿用原模组的行为：调用系统默认浏览器打开官方链接，由浏览器处理后续下载。打开请求提交后立即触发游戏崩溃，不等待文件下载完成。不会自动运行或安装下载的程序。

## 安装

将 `create_wuthering_waves_crash-1.0.0-forge-1.20.1.jar` 放入对应游戏实例的 `mods` 文件夹。

不需要把“不下载原神修复”作为前置；也可以同时安装它。鸣潮触发逻辑使用独立的客户端事件监听，不依赖原神下载方法继续执行。

| 其他已安装模组 | 行为 |
| --- | --- |
| 只有机械动力 | 正常启动，不打开链接、不触发崩溃 |
| 只有 Create: Genshin Impact | 正常启动，不打开链接、不触发崩溃 |
| 机械动力 + Create: Genshin Impact | 拦截原神链接，打开鸣潮链接，主动崩溃 |
| 上述两者 + 不下载原神修复 | 拦截原神链接，打开鸣潮链接，主动崩溃 |
| 两者都没有 | 正常启动，不打开链接、不触发崩溃 |
| 独立服务器 | 不注册客户端下载和崩溃逻辑 |

每个游戏进程只检查一次。下次重新启动游戏时，条件仍满足就会再次触发。

## 报错识别

预期异常为 `RelayAction$IntentionalRelayCrash`，消息为：

> 机械动力引来了原神，原神被拦下了，但鸣潮来了。 [create_wuthering_waves_crash: intentional mod relay crash]

日志会记录鸣潮 URL。若系统无法打开浏览器，Minecraft 自带的 URL 打开方法会记录错误；本模组仍然触发预定的崩溃。

## 构建

安装 JDK 17 并设置 `JAVA_HOME`，在本目录执行：

```powershell
.\gradlew.bat test build
```

Linux/macOS：

```sh
sh ./gradlew test build
```

成品位于 `build/libs/`。首次构建会下载 Gradle、Forge 和 Minecraft 开发依赖。Gradle 版本和发行包 SHA-256 校验已固定。

自动测试覆盖缺少任一模组时无操作、先打开链接后抛出异常、单次触发，以及打开链接抛出异常时保留原始原因。测试使用替代回调，不打开真实浏览器。

本仓库可独立构建和运行逻辑测试，无需提供其他模组的源码或 JAR。

可选的 Mixin 检查：

```powershell
.\gradlew.bat verifyMixin
```

默认只检查目标模组缺失时能否正常跳过。有需要时，可自行提供外部 JAR 的路径以启用额外检查，无需将它们提交到仓库：

```powershell
.\gradlew.bat verifyMixin "-PgenshinModJar=C:/mods/create_genshin_impact-1.0.0-forge-1.20.1.jar" "-PoldFixJar=C:/mods/fix_create_genshin_hoax-1.0.0.jar"
```

额外检查覆盖原神下载拦截、与旧修复的两种配置顺序兼容，以及服务端跳过客户端注入。所有检查均不会启动游戏或打开浏览器。

## 实现与许可

本项目采用 [Apache-2.0 许可证](LICENSE)。

联动模组为 **“加载机械动力就下载原神”**（Create: Genshin Impact，Mod ID：`create_genshin_impact`）。

拦截原神下载的方法参考 **“不下载原神修复”**（`fix_create_genshin_hoax`，RhineIris，MIT License），保留其 [MIT 许可声明](licenses/fix_create_genshin_hoax-MIT.txt)。

目标类使用 `@Pseudo` 和字符串名称声明为可选 Mixin 目标，未安装原神下载模组时跳过；所有注入均 `remap = false`，通过 JAR 的 `MixinConfigs` 注册。客户端事件类仅在 `Dist.CLIENT` 加载。

本模组不打包机械动力、原神下载模组或旧修复模组。
