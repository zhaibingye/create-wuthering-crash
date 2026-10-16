package verification;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/** Applies real Mixin 0.8.5 to the supplied original JAR, without starting Minecraft. */
public final class MixinSmokeMain {
    public static void main(String[] args) throws Exception {
        String scenario = args[0];
        List<URL> urls = new ArrayList<>();
        List<URL> modUrls = new ArrayList<>();
        for (String element : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path path = Path.of(element);
            // Use the packaged mod, not the un-reobfuscated main class directory.
            if (!path.endsWith(Path.of("classes", "java", "main"))
                    && !path.endsWith(Path.of("resources", "main"))) {
                urls.add(path.toUri().toURL());
            }
        }
        require(Files.isRegularFile(Path.of(args[1])), "Built mod JAR is missing");
        modUrls.add(Path.of(args[1]).toUri().toURL());
        if (!scenario.equals("absent")) {
            require(Files.isRegularFile(Path.of(args[2])), "Original mod JAR is missing");
            modUrls.add(Path.of(args[2]).toUri().toURL());
        }
        boolean oldFix = scenario.equals("new-first") || scenario.equals("old-first");
        if (oldFix) {
            require(Files.isRegularFile(Path.of(args[3])), "Old fix JAR is missing");
            modUrls.add(Path.of(args[3]).toUri().toURL());
        }

        Launch.blackboard = new HashMap<>();
        Launch.blackboard.put("TweakClasses", new ArrayList<String>());
        Launch.blackboard.put("Tweaks", new ArrayList<>());
        Launch.classLoader = new LaunchClassLoader(urls.toArray(URL[]::new));
        Thread.currentThread().setContextClassLoader(Launch.classLoader);
        // Keep the Mixin service and its ASM dependencies in one classloader.
        Launch.classLoader.addClassLoaderExclusion("org.spongepowered.asm.");
        Launch.classLoader.addClassLoaderExclusion("org.objectweb.asm.");
        MixinBootstrap.init();
        // Register configs explicitly below, without manifest discovery changing their order.
        for (URL modUrl : modUrls) {
            Launch.classLoader.addURL(modUrl);
        }
        MixinEnvironment environment = MixinEnvironment.getDefaultEnvironment();
        environment.setSide(scenario.equals("server")
                ? MixinEnvironment.Side.SERVER : MixinEnvironment.Side.CLIENT);

        if (scenario.equals("old-first")) {
            Mixins.addConfiguration("mixins.fix_create_genshin_hoax.json");
        }
        Mixins.addConfiguration("mixins.create_wuthering_waves_crash.json");
        if (scenario.equals("new-first")) {
            Mixins.addConfiguration("mixins.fix_create_genshin_hoax.json");
        }
        Method gotoPhase = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
        gotoPhase.setAccessible(true);
        gotoPhase.invoke(null, MixinEnvironment.Phase.DEFAULT);
        Launch.classLoader.registerTransformer("org.spongepowered.asm.mixin.transformer.Proxy");
        require(!Launch.classLoader.getTransformers().isEmpty(), "Mixin transformer was not registered");

        if (scenario.equals("absent")) {
            require(Launch.classLoader.getClassBytes(
                    "create_genshin_impact.procedures.DownloadGenshinImpactProcedure") == null,
                    "Original target must be absent in this scenario");
            Class<?> probe = Launch.classLoader.loadClass("verification.OptionalProbe");
            require(probe.getMethod("value").invoke(null).equals(42), "Optional target prevented startup");
        } else {
            Class<?> target = Launch.classLoader.loadClass(
                    "create_genshin_impact.procedures.DownloadGenshinImpactProcedure");
            boolean newHandler = Arrays.stream(target.getDeclaredMethods())
                    .anyMatch(method -> method.getName().contains("cancelGenshinDownload"));
            require(newHandler != scenario.equals("server"), "Wrong client/server Mixin selection");
            if (!scenario.equals("server")) {
                // Without successful cancellation this hits uninitialized ModList and fails.
                target.getMethod("execute").invoke(null);
                if (oldFix) {
                    require(Arrays.stream(target.getDeclaredMethods())
                            .anyMatch(method -> method.getName().contains("onExecute")),
                            "Old fix Mixin was not applied");
                }
            }
        }
        System.out.println("MIXIN_SMOKE_PASS " + scenario);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
