package dev.dwmir.lib.api;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
public interface GlassContext {
 GuiRenderState dwmir$state();
 ScreenRect dwmir$clip();
}
