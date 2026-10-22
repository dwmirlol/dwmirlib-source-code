package dev.dwmir.lib;
import dev.dwmir.lib.api.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.MouseInput;
import java.util.*;
import dev.shieldpulse.*;
public final class R3Checks implements FabricClientGameTest {
 private void require(boolean v,String message){if(!v)throw new AssertionError(message);}
 private net.minecraft.client.gui.screen.Screen actual(MinecraftClient c){return WorkspaceScreen.actual(c.currentScreen);}
 private Object field(Object obj,String name){try{var f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);}catch(Exception e){throw new AssertionError(e);}}
 private void call(Object obj,String name,Class<?>[] types,Object...args){try{var m=obj.getClass().getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(obj,args);}catch(Exception e){throw new AssertionError(e);}}
 private void point(MinecraftClient c,double x,double y){var screen=c.currentScreen;if(screen instanceof WorkspaceScreen w){x=w.screenX(x);y=w.screenY(y);}var e=new Click(x,y,new MouseInput(0,0));require(screen.mouseClicked(e,false),"Click rejected at "+x+","+y);if(c.currentScreen!=null)c.currentScreen.mouseReleased(e);}
 private void button(MinecraftClient c,String text){var screen=actual(c);var b=screen.children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals(text)).map(e->(ClickableWidget)e).findFirst().orElseThrow(()->new AssertionError("Missing "+text));if(screen instanceof dev.shieldpulse.SettingsScreen){try{var m=screen.getClass().getDeclaredMethod("controlsBounds");m.setAccessible(true);int[] bounds=(int[])m.invoke(screen);int delta=b.getY()<bounds[1]?b.getY()-bounds[1]:b.getY()+b.getHeight()>bounds[1]+bounds[3]?b.getY()+b.getHeight()-(bounds[1]+bounds[3]):0;if(delta!=0)screen.mouseScrolled(bounds[0]+10,bounds[1]+10,0,-delta/28.0);}catch(Exception e){throw new AssertionError(e);}}point(c,b.getX()+b.getWidth()/2.,b.getY()+b.getHeight()/2.);}
 private int choose(MinecraftClient c){require(actual(c).getClass()==ColorPickerScreen.class,"Not the common picker: "+actual(c).getClass());point(c,(actual(c).width-390)/2+20+185,(actual(c).height-356)/2+63+54);return ((ColorPickerScreen)actual(c)).selectedColor();}
 private Object inventory(){try{Object handler=Class.forName("net.dwmirlol.inventoryscalefix.config.InventoryScaleConfig").getField("CONFIG").get(null);return Class.forName("dev.isxander.yacl3.config.v2.api.ConfigClassHandler").getMethod("instance").invoke(handler);}catch(Exception e){throw new AssertionError(e);}}
 private int invRgb(){try{return ((java.awt.Color)inventory().getClass().getField("panelColor").get(inventory())).getRGB()&0xffffff;}catch(Exception e){throw new AssertionError(e);}}
 @Override public void runTest(ClientGameTestContext t){
  t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");c.options.getGuiScale().setValue(2);c.onResolutionChanged();});t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(10);t.runOnClient(c->{require(GlassRenderer.captures>0&&GlassRenderer.panels>0,"Glass GPU path inactive");});t.takeScreenshot("r3-library-glass");
  for(var theme:LibraryState.Theme.values()){t.runOnClient(c->LibraryState.select(theme));t.waitTicks(6);t.takeScreenshot("r3-theme-"+theme.name());}t.runOnClient(c->LibraryState.select(LibraryState.Theme.ONYX));
  int checked=0;
  for(int page:new int[]{0,1,2,3,5}){String[] labels=page==0?new String[]{"Effect color"}:page==1?new String[]{"Color"}:page==2?new String[]{"Timer color","Background color"}:page==3?new String[]{"Timer color"}:new String[]{"Indicator color"};
   for(String label:labels){final int p=page;t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.runOnClient(c->((net.customcrossbowsounds.client.SettingsScreen)actual(c)).selectPage(p));t.waitTicks(3);
    final java.util.function.IntSupplier[] getter=new java.util.function.IntSupplier[1];final int[] color={0};t.runOnClient(c->{Object row=((List<?>)field(actual(c),"rows")).stream().filter(r->field(r,"title").equals(label)).findFirst().orElseThrow();getter[0]=(java.util.function.IntSupplier)field(row,"color");var s=actual(c);float scale=Math.min(1f,Math.min((s.width-16)/700f,(s.height-16)/420f));double x=(s.width-700*scale)/2+450*scale,y=(s.height-420*scale)/2+((float)field(row,"top")+18)*scale;point(c,x,y);color[0]=choose(c);});t.waitTicks(4);if(checked==0)t.takeScreenshot("r3-crossbow-picker");t.runOnClient(c->{require(((ColorPickerScreen)actual(c)).uploads==1,"Picker repeatedly uploaded");button(c,"Apply");require(getter[0].getAsInt()==color[0],"Crossbow color not applied");require(actual(c) instanceof net.customcrossbowsounds.client.SettingsScreen,"Wrong picker parent");});checked++;
   }
  }
  t.setScreen(()->new dev.shieldpulse.SettingsScreen(null));t.runOnClient(c->{ShieldPulse.CONFIG=new Config();((dev.shieldpulse.SettingsScreen)actual(c)).showPage(1);});
  int state=1;for(String label:new String[]{"Holding","Blocking","Disabled"}){final int s=state++;final int[] chosen={0};final double[] alpha={0};t.runOnClient(c->{button(c,label);alpha[0]=ShieldPulse.CONFIG.alpha(s);button(c,label+" color picker");chosen[0]=choose(c);});t.waitTicks(3);t.runOnClient(c->{button(c,"Apply");require(ShieldPulse.CONFIG.color(s)==chosen[0],"State color not applied");require(ShieldPulse.CONFIG.alpha(s)==alpha[0],"Color picker changed opacity");});checked++;}
  t.runOnClient(c->((dev.shieldpulse.SettingsScreen)actual(c)).showPage(3));for(String name:new String[]{"Break effect color","Recovery bar color","Recovery text color","Recovery background color"}){t.runOnClient(c->{button(c,name);choose(c);button(c,"Apply");});checked++;}t.waitTicks(5);t.takeScreenshot("r3-shield-glass");
  t.runOnClient(c->((dev.shieldpulse.SettingsScreen)actual(c)).showPage(2));for(String prefix:new String[]{"Banner base: ","Banner pattern: "}){t.runOnClient(c->{String label=actual(c).children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().startsWith(prefix)).map(e->((ClickableWidget)e).getMessage().getString()).findFirst().orElseThrow();button(c,label);choose(c);button(c,"Apply");});checked++;}
  t.setScreen(()->DwmirLib.settings("inventoryscalefix").apply(null));final int[] old={0},changed={0};t.runOnClient(c->{call(actual(c),"selectPage",new Class<?>[]{int.class},2);old[0]=invRgb();button(c,"Panel color picker");changed[0]=choose(c);});t.waitTicks(4);t.takeScreenshot("r3-inventory-picker");t.runOnClient(c->{button(c,"Apply");require(invRgb()==old[0],"Picker bypassed inventory staged edits");button(c,"Cancel");require(invRgb()==old[0],"Inventory cancel saved color");c.setScreen(DwmirLib.settings("inventoryscalefix").apply(null));call(actual(c),"selectPage",new Class<?>[]{int.class},2);button(c,"Panel color picker");changed[0]=choose(c);button(c,"Apply");button(c,"Save");require(invRgb()==changed[0],"Inventory color save failed");});checked++;
  t.setScreen(()->DwmirLib.settings("inventoryscalefix").apply(null));t.waitTicks(5);t.takeScreenshot("r3-inventory-glass");t.runOnClient(c->{c.options.getGuiScale().setValue(3);c.onResolutionChanged();});t.waitTicks(5);t.takeScreenshot("r3-glass-gui-scale-3");t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  new IconChecks().runTest(t);new BenchmarkChecks().runTest(t);
  System.out.println("DWMIR_R3_CHECKS_PASS COLOR_CONTROLS="+checked+" CAPTURES="+GlassRenderer.captures+" PANELS="+GlassRenderer.panels);
 }
}
