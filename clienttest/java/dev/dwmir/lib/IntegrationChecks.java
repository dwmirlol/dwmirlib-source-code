package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.MouseInput;
public final class IntegrationChecks {
 private void require(boolean v,String message){if(!v)throw new AssertionError(message);}
 private void call(Object target,String method,int value){try{target.getClass().getMethod(method,int.class).invoke(target,value);}catch(Exception e){throw new AssertionError(e);}}
 private Screen create(String type,Screen parent){try{return (Screen)Class.forName(type).getConstructor(Screen.class).newInstance(parent);}catch(Exception e){throw new AssertionError(e);}}
 private Object config(){try{var configClass=Class.forName("net.customcrossbowsounds.config.CustomCrossbowSoundsConfig");var holder=Class.forName("me.shedaniel.autoconfig.AutoConfig").getMethod("getConfigHolder",Class.class).invoke(null,configClass);return Class.forName("me.shedaniel.autoconfig.ConfigHolder").getMethod("getConfig").invoke(holder);}catch(Exception e){throw new AssertionError(e);}}
 private Object get(Object target,String name){try{return target.getClass().getField(name).get(target);}catch(Exception e){throw new AssertionError(e);}}
 private void virtualClick(net.minecraft.client.MinecraftClient c,double x,double y){
  var s=c.currentScreen;float scale=Math.min((s.width-16)/700f,(s.height-16)/420f);float ox=(s.width-700*scale)/2,oy=(s.height-420*scale)/2;
  var click=new Click(ox+x*scale,oy+y*scale,new MouseInput(0,0));require(s.mouseClicked(click,false),"Screen click rejected at "+x+","+y);s.mouseReleased(click);
 }
 public void run(ClientGameTestContext t){
  t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();require(DwmirLib.settings("shieldpulse")!=null&&DwmirLib.settings("customcrossbowsounds")!=null,"Real modules did not register");});
  t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(8);t.takeScreenshot("dwmirlib-connected");
  if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("inventoryscalefix"))new InventoryChecks().run(t);
  t.clickScreenButton("Custom Shield Status");
  t.runOnClient(c->require(c.currentScreen.getClass().getName().equals("dev.shieldpulse.SettingsScreen"),"Wrong shield screen"));
  for(int i=0;i<5;i++){final int page=i;t.runOnClient(c->call(c.currentScreen,"showPage",page));t.waitTicks(4);t.takeScreenshot("shield-page-"+i);}
  t.runOnClient(c->{c.currentScreen.close();require(c.currentScreen instanceof LibraryScreen,"Shield parent lost");});
  t.clickScreenButton("Custom Crossbow Sounds");
  t.runOnClient(c->{require(c.currentScreen.getClass().getName().endsWith("SettingsScreen"),"Wrong crossbow screen");boolean before=(boolean)get(config(),"enabled");virtualClick(c,647,34);require((boolean)get(config(),"enabled")!=before,"Enabled toggle failed");virtualClick(c,647,34);require((boolean)get(config(),"enabled")==before,"Enabled toggle restore failed");});
  for(int i=0;i<6;i++){final int page=i;t.runOnClient(c->call(c.currentScreen,"selectPage",page));t.waitTicks(4);t.takeScreenshot("crossbow-page-"+i);}
  t.runOnClient(c->{call(c.currentScreen,"selectPage",1);boolean before=(boolean)get(config(),"hitmarkerEnabled");virtualClick(c,460,116);require((boolean)get(config(),"hitmarkerEnabled")!=before,"Hitmarker toggle failed");virtualClick(c,460,116);virtualClick(c,460,192);});
  t.waitTicks(3);t.takeScreenshot("crossbow-color-picker");
  t.runOnClient(c->{virtualClick(c,284,334);call(c.currentScreen,"selectPage",4);virtualClick(c,460,154);});t.waitTicks(4);t.takeScreenshot("crossbow-sound-library");
  t.runOnClient(c->c.currentScreen.close());
  t.runOnClient(c->{call(c.currentScreen,"selectPage",5);c.setScreen(create("net.customcrossbowsounds.client.hud.ArrowHudEditScreen",c.currentScreen));});t.waitTicks(5);t.takeScreenshot("crossbow-hud-editor");
  t.runOnClient(c->{c.currentScreen.close();c.currentScreen.close();require(c.currentScreen instanceof LibraryScreen,"Crossbow parent lost");c.options.getGuiScale().setValue(3);c.onResolutionChanged();});t.waitTicks(4);t.takeScreenshot("dwmirlib-connected-compact");
  t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();});t.setScreen(()->null);
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(6);t.takeScreenshot("dwmirlib-in-world");
   t.clickScreenButton("Custom Shield Status");t.waitTicks(6);t.takeScreenshot("shield-in-world");t.runOnClient(c->c.currentScreen.close());
   t.clickScreenButton("Custom Crossbow Sounds");t.waitTicks(6);t.takeScreenshot("crossbow-in-world");t.setScreen(()->null);
   if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("inventoryscalefix"))new InventoryChecks().world(t);
  }
 }
}
