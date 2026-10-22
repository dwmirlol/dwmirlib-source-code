package dev.dwmir.lib;
import dev.dwmir.lib.api.DwmirModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import java.util.*;
import java.util.function.Function;
import org.slf4j.LoggerFactory;
public final class DwmirLib implements ClientModInitializer {
    public static final Map<String,DwmirModule> MODULES=new java.util.LinkedHashMap<>();
    private static final Map<String, Function<Screen, Screen>> SCREENS = new HashMap<>();
    @Override public void onInitializeClient() {
        for (var entry : FabricLoader.getInstance().getEntrypointContainers("dwmirlib", DwmirModule.class)) {
            try {
                var module = entry.getEntrypoint();
                if (!entry.getProvider().getMetadata().getId().equals(module.modId()))
                    throw new IllegalArgumentException("Module id must match its provider");
                register(module.modId(), module::createConfigScreen);MODULES.put(module.modId(),module);
            } catch (RuntimeException e) { LoggerFactory.getLogger("dwmirlib").error("Unable to register module from {}", entry.getProvider().getMetadata().getId(), e); }
        }
    }
    public static void register(String modId, Function<Screen, Screen> factory) {
        Objects.requireNonNull(factory);
        if (!FabricLoader.getInstance().isModLoaded(modId)) throw new IllegalArgumentException("Mod is not loaded: " + modId);
        if (SCREENS.putIfAbsent(modId, factory) != null) throw new IllegalStateException("Duplicate settings provider: " + modId);
    }
    public static Function<Screen, Screen> settings(String id) { return SCREENS.get(id); }
    public static Screen createScreen(Screen parent) { return new LibraryScreen(parent); }
}
