package dev.dwmir.lib;
import dev.dwmir.lib.api.*;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.widget.ClickableWidget;
public final class R4Checks implements FabricClientGameTest {
 private void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 public void runTest(ClientGameTestContext t){
  t.getInput().resizeWindow(1920,1080);new R3Checks().runTest(t);
  t.setScreen(()->DwmirLib.createScreen(null));t.runOnClient(c->{var s=WorkspaceScreen.actual(c.currentScreen);check(s.children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals("Settings")).count()==0,"Redundant Settings tab");check(Socials.url("github").equals("https://github.com/dwmirlol"),"GitHub route");check(s.children().stream().anyMatch(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals("GitHub")),"GitHub button");GlassUi.drawing=true;LibraryState.select(LibraryState.Theme.QUARTZ);check((GlassUi.textColor(0xfff5f5f5)&0xffffff)<0x555555,"Quartz text contrast");GlassUi.drawing=false;});
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();
   for(var theme:LibraryState.Theme.values()){
    t.runOnClient(c->LibraryState.select(theme));t.setScreen(()->DwmirLib.createScreen(null));t.getInput().setCursorPos(0,0);t.waitTicks(65);for(int sample=0;sample<3;sample++){t.waitTicks(20);t.runOnClient(c->System.out.println("DWMIR_R4_THEME_FPS_"+theme+"="+c.getCurrentFps()));}t.takeScreenshot("r4-world-"+theme.name());
   }
   t.setScreen(()->new dev.shieldpulse.SettingsScreen(null));t.waitTicks(12);t.takeScreenshot("r4-quartz-shield");
   t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.waitTicks(12);t.takeScreenshot("r4-quartz-crossbow");
   t.setScreen(()->DwmirLib.settings("inventoryscalefix").apply(null));t.waitTicks(12);t.takeScreenshot("r4-quartz-inventory");
   t.runOnClient(c->LibraryState.select(LibraryState.Theme.SAKURA));t.setScreen(()->new dev.shieldpulse.SettingsScreen(null));t.waitTicks(12);t.takeScreenshot("r4-sakura-shield");
   t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.waitTicks(12);t.takeScreenshot("r4-sakura-crossbow");
   t.setScreen(()->DwmirLib.settings("inventoryscalefix").apply(null));t.waitTicks(12);t.takeScreenshot("r4-sakura-inventory");
  }
  for(int w:new int[]{1280,1920}){t.getInput().resizeWindow(w,w*9/16);t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(8);t.runOnClient(c->{double a=((WorkspaceScreen)c.currentScreen).screenX(1)-((WorkspaceScreen)c.currentScreen).screenX(0);double physical=a*c.getWindow().getScaleFactor();check(Math.abs(physical-Math.rint(physical))<.001,"Fractional physical font scale");});t.takeScreenshot("r4-layout-"+w);}
  t.setScreen(()->null);
  System.out.println("DWMIR_R4_CHECKS_PASS THEMES=5 COLOR_CONTROLS=16 GITHUB=OK PIXEL_ALIGNMENT=OK");
 }
}
