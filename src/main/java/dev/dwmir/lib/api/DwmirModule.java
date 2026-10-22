package dev.dwmir.lib.api;
import net.minecraft.client.gui.screen.Screen;
/** Implement as a dwmirlib entrypoint in your client mod metadata. */
public interface DwmirModule {
    String modId();
    default boolean itemTooltipsEnabled(){return true;}
    default void toggleItemTooltips(){}
    default java.util.List<String> profileFiles(){return java.util.List.of();}
    default void flushProfile(){}
    default void reloadProfile(){}
    Screen createConfigScreen(Screen parent);
}
