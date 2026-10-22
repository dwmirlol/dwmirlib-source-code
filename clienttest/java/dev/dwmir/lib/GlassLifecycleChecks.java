package dev.dwmir.lib;
import dev.dwmir.lib.api.*;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
public final class GlassLifecycleChecks implements FabricClientGameTest {
 public void runTest(ClientGameTestContext t){
  t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");LibraryState.select(LibraryState.Theme.ONYX);c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(8);
  t.getInput().resizeWindow(1280,720);t.waitTicks(8);
  t.runOnClient(c->{if(GlassRenderer.captures<2)throw new AssertionError("Glass resize failed");});
  t.runOnClient(c->c.reloadResources());t.waitTicks(120);
  t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(10);
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();
   for(boolean recovery:new boolean[]{false,true}){
    t.setScreen(()->new dev.shieldpulse.ShieldEditorScreen(null,recovery));t.waitTicks(8);t.takeScreenshot(recovery?"r3-recovery-editor":"r3-shield-editor");
   }
   t.setScreen(()->new net.customcrossbowsounds.client.hud.ArrowHudEditScreen(null));t.waitTicks(8);t.takeScreenshot("r3-arrow-editor");
   t.setScreen(()->new dev.shieldpulse.SettingsScreen(null));t.runOnClient(c->((dev.shieldpulse.SettingsScreen)WorkspaceScreen.actual(c.currentScreen)).showPage(3));t.waitTicks(8);t.takeScreenshot("r3-shield-final");
   t.setScreen(()->DwmirLib.createScreen(null));t.getInput().setCursorPos(0,0);t.waitTicks(20);t.takeScreenshot("r3-world-final");
   t.runOnClient(c->{if(GlassRenderer.panels<20)throw new AssertionError("Glass panels inactive");});
  }
  System.out.println("DWMIR_R3_LIFECYCLE_PASS");
 }
}
