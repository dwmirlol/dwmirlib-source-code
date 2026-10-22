package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.MouseInput;
import dev.dwmir.lib.api.ColorPickerScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.customcrossbowsounds.config.CustomCrossbowSoundsConfig;
public final class R8Checks implements FabricClientGameTest {
 private void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
 private void click(MinecraftClient c,double x,double y){if(c.currentScreen instanceof WorkspaceScreen w){x=w.screenX(x);y=w.screenY(y);}var event=new Click(x,y,new MouseInput(0,0));check(c.currentScreen.mouseClicked(event,false),"Click rejected");c.currentScreen.mouseReleased(event);}
 private double field(Object obj,String name){try{var f=SliderWidget.class.getDeclaredField(name);f.setAccessible(true);return f.getDouble(obj);}catch(Exception e){throw new AssertionError(e);}}
 private void endpoints(MinecraftClient c){Screen s=WorkspaceScreen.actual(c.currentScreen);var slider=s.children().stream().filter(e->e instanceof SliderWidget).map(e->(SliderWidget)e).findFirst().orElseThrow();double initial=field(slider,"value");int inset=s.getClass().getName().startsWith("dev.dwmir.sprinter")?14:12;for(double p:new double[]{0,1,.5,initial}){click(c,slider.getX()+inset+(slider.getWidth()-2*inset)*p,slider.getY()+25);check(Math.abs(field(slider,"value")-p)<.001,"Slider endpoint mismatch "+s.getClass().getName()+" "+p);}}
 public void runTest(ClientGameTestContext t){
  t.getInput().resizeWindow(1440,900);t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");LibraryState.select(LibraryState.Theme.SAKURA);c.options.language="en_us";c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();
   for(int page=0;page<6;page++){final int p=page;t.getInput().setCursorPos(0,0);t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.runOnClient(c->((net.customcrossbowsounds.client.SettingsScreen)WorkspaceScreen.actual(c.currentScreen)).selectPage(p));t.waitTicks(8);t.takeScreenshot("r8-crossbow-"+p);}
   t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.runOnClient(c->{var s=(net.customcrossbowsounds.client.SettingsScreen)WorkspaceScreen.actual(c.currentScreen);s.selectPage(1);var cfg=AutoConfig.getConfigHolder(CustomCrossbowSoundsConfig.class).getConfig();int original=cfg.hitmarkerOpacity;try{for(int value:new int[]{0,100,50}){float scale=Math.min(1f,Math.min((s.width-16)/700f,(s.height-16)/420f)),ox=(s.width-700*scale)/2,oy=(s.height-420*scale)/2;click(c,ox+(168+308*value/100.)*scale,oy+244*scale);check(Math.abs(cfg.hitmarkerOpacity-value)<=1,"Crossbow track/input mismatch");}}finally{cfg.hitmarkerOpacity=original;net.customcrossbowsounds.client.SettingsScreen.save();}});
   for(int page:new int[]{0,1,3}){final int p=page;t.setScreen(()->new dev.shieldpulse.SettingsScreen(null));t.runOnClient(c->((dev.shieldpulse.SettingsScreen)WorkspaceScreen.actual(c.currentScreen)).showPage(p));t.waitTicks(8);t.takeScreenshot("r8-shield-"+p);if(page==0)t.runOnClient(this::endpoints);}
   for(int page=0;page<3;page++){final int p=page;t.setScreen(()->DwmirLib.settings("inventoryscalefix").apply(null));t.runOnClient(c->{try{WorkspaceScreen.actual(c.currentScreen).getClass().getMethod("selectPage",int.class).invoke(WorkspaceScreen.actual(c.currentScreen),p);}catch(Exception e){throw new AssertionError(e);}});t.waitTicks(8);t.takeScreenshot("r8-inventory-"+p);if(page==1)t.runOnClient(this::endpoints);}
   t.setScreen(()->new dev.dwmir.sprinter.SettingsScreen(null));t.runOnClient(this::endpoints);t.waitTicks(8);t.takeScreenshot("r8-sprinter");
   t.setScreen(()->new net.customcrossbowsounds.client.SoundPickerScreen(null));t.waitTicks(8);t.takeScreenshot("r8-sound-picker");
   t.setScreen(()->new net.customcrossbowsounds.client.hud.ArrowHudEditScreen(null));t.waitTicks(8);t.takeScreenshot("r8-arrow-editor");
   t.setScreen(()->new ColorPickerScreen(null,"Color picker",0xffb4d1,rgb->{}));t.waitTicks(8);t.takeScreenshot("r8-picker");
   t.getInput().setCursorPos(0,0);t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(10);t.takeScreenshot("r8-library");
   t.runOnClient(c->LibraryState.select(LibraryState.Theme.QUARTZ));t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.waitTicks(8);t.takeScreenshot("r8-quartz");
   t.getInput().resizeWindow(1100,720);t.runOnClient(c->{LibraryState.select(LibraryState.Theme.ONYX);c.options.language="tr_tr";c.setScreen(new net.customcrossbowsounds.client.SettingsScreen(null));});t.waitTicks(8);t.takeScreenshot("r8-compact-tr");
  }
  t.setScreen(()->null);System.out.println("DWMIR_R8_CHECKS_PASS SLIDER_ENDPOINTS=OK ALL_MODULE_MENUS=OK");
 }
}
