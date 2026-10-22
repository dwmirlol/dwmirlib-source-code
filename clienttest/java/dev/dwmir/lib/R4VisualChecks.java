package dev.dwmir.lib;
import dev.dwmir.lib.api.*;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
public final class R4VisualChecks implements FabricClientGameTest {
 public void runTest(ClientGameTestContext t){
  t.getInput().resizeWindow(1920,1080);t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");c.options.getGuiScale().setValue(2);c.options.getEnableVsync().setValue(false);c.options.getMaxFps().setValue(260);c.options.getInactivityFpsLimit().setValue(net.minecraft.client.option.InactivityFpsLimit.MINIMIZED);c.getInactivityFpsLimiter().onInput();c.onResolutionChanged();});
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();
   for(var theme:LibraryState.Theme.values()){
    t.runOnClient(c->LibraryState.select(theme));t.setScreen(()->DwmirLib.createScreen(null));t.getInput().setCursorPos(0,0);t.waitTicks(65);
    for(int i=0;i<3;i++){t.waitTicks(20);t.runOnClient(c->{c.getInactivityFpsLimiter().onInput();if(c.getInactivityFpsLimiter().getLimitReason()!=net.minecraft.client.option.InactivityFpsLimiter.LimitReason.NONE)throw new AssertionError("FPS sample capped: "+c.getInactivityFpsLimiter().getLimitReason());System.out.println("DWMIR_R4_THEME_FPS_"+theme+"="+c.getCurrentFps());System.out.println("DWMIR_R4_LIMIT=NONE");});}
    t.takeScreenshot("r4-final-"+theme);
   }
   t.runOnClient(c->LibraryState.select(LibraryState.Theme.SAKURA));
   t.setScreen(()->new dev.shieldpulse.SettingsScreen(null));t.waitTicks(8);t.takeScreenshot("r4-final-shield");
   t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.waitTicks(8);t.takeScreenshot("r4-final-crossbow");
   t.setScreen(()->DwmirLib.settings("inventoryscalefix").apply(null));t.waitTicks(8);t.takeScreenshot("r4-final-inventory");
   t.setScreen(()->DwmirLib.createScreen(null));t.runOnClient(c->c.reloadResources());t.waitTicks(120);t.takeScreenshot("r4-final-reloaded");
  }
  t.setScreen(()->null);System.out.println("DWMIR_R4_VISUAL_PASS");
 }
}
