package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.MouseInput;
import java.util.*;
public final class InventoryChecks {
 private static Map<String,Object> original;
 private static Object handler(){try{return Class.forName("net.dwmirlol.inventoryscalefix.config.InventoryScaleConfig").getField("CONFIG").get(null);}catch(Exception e){throw new AssertionError(e);}}
 private static Object config(){try{return Class.forName("dev.isxander.yacl3.config.v2.api.ConfigClassHandler").getMethod("instance").invoke(handler());}catch(Exception e){throw new AssertionError(e);}}
 private static void action(String name){try{Class.forName("dev.isxander.yacl3.config.v2.api.ConfigClassHandler").getMethod(name).invoke(handler());}catch(Exception e){throw new AssertionError(e);}}
 private static Object get(String name){try{return config().getClass().getField(name).get(config());}catch(Exception e){throw new AssertionError(e);}}
 private static void set(String name,Object value){try{config().getClass().getField(name).set(config(),value);}catch(Exception e){throw new AssertionError(e);}}
 private void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 private void number(String field,float expected){require(Math.abs((float)get(field)-expected)<.011,"Wrong "+field+": "+get(field));}
 private void page(MinecraftClient c,int value){try{c.currentScreen.getClass().getMethod("selectPage",int.class).invoke(c.currentScreen,value);}catch(Exception e){throw new AssertionError(e);}}
 private ClickableWidget widget(MinecraftClient c,String label){return c.currentScreen.children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals(label)).map(e->(ClickableWidget)e).findFirst().orElseThrow(()->new AssertionError("Missing "+label));}
 private void click(MinecraftClient c,String label){var w=widget(c,label);clickAt(c,w.getX()+w.getWidth()/2.,w.getY()+w.getHeight()/2.);}
 private void clickAt(MinecraftClient c,double x,double y){var event=new Click(x,y,new MouseInput(0,0));require(c.currentScreen.mouseClicked(event,false),"Click rejected");c.currentScreen.mouseReleased(event);}
 private void slider(MinecraftClient c,String label,double fraction){var w=c.currentScreen.children().stream().filter(e->e instanceof SliderWidget s&&s.getMessage().getString().startsWith(label+":")).map(e->(ClickableWidget)e).findFirst().orElseThrow(()->new AssertionError("Missing slider "+label));clickAt(c,w.getX()+12+(w.getWidth()-24)*fraction,w.getY()+25);}
 private void open(ClientGameTestContext t){t.clickScreenButton("dwmirlols Inventory Scale Fix");}
 public void run(ClientGameTestContext t){
  t.runOnClient(c->{original=new HashMap<>();for(var f:config().getClass().getFields())if(!java.lang.reflect.Modifier.isStatic(f.getModifiers()))try{original.put(f.getName(),f.get(config()));}catch(Exception e){throw new AssertionError(e);}require(DwmirLib.settings("inventoryscalefix")!=null,"Inventory module registration failed");});
  open(t);t.waitTicks(5);t.takeScreenshot("inventory-scaling");
  t.runOnClient(c->{require(c.currentScreen.getClass().getName().endsWith("InventorySettingsScreen"),"Wrong inventory screen");slider(c,"Inventory GUI size",(3.25-1)/7);click(c,"Limit size to fit the screen");slider(c,"Tooltip GUI size",2.5/8);require(get("inventoryGuiScale").equals(original.get("inventoryGuiScale")),"Draft modified live config before save");page(c,1);
   slider(c,"Panel opacity",.5);slider(c,"Player preview opacity",.35);slider(c,"Background darkness",.2);click(c,"Hide inventory player model");click(c,"Hide recipe book and button");});
  t.waitTicks(4);t.takeScreenshot("inventory-appearance");
  t.runOnClient(c->{page(c,2);click(c,"Enable panel color");var input=c.currentScreen.children().stream().filter(e->e instanceof TextFieldWidget).map(e->(TextFieldWidget)e).findFirst().orElseThrow();input.setText("bad");click(c,"Apply");input=c.currentScreen.children().stream().filter(e->e instanceof TextFieldWidget).map(e->(TextFieldWidget)e).findFirst().orElseThrow();input.setText("#6688CC");click(c,"Apply");});
  t.waitTicks(4);t.takeScreenshot("inventory-color");
  t.runOnClient(c->{click(c,"Save");require(c.currentScreen instanceof LibraryScreen,"Save did not restore library");number("inventoryGuiScale",3.25f);number("tooltipGuiScale",2.5f);number("panelOpacity",.5f);number("avatarOpacity",.35f);number("backgroundOpacity",.2f);
   for(String flag:new String[]{"fitToScreen","hidePlayer","hideRecipeBook","panelTintEnabled"})require(!get(flag).equals(original.get(flag)),"Toggle failed: "+flag);
   require((((java.awt.Color)get("panelColor")).getRGB()&0xffffff)==0x6688cc,"HEX color failed");
   for(String field:new String[]{"containerSize","tooltipSize","scaleMode","absoluteScale","fractionalSmoothing"})require(get(field).equals(original.get(field)),"Legacy field changed: "+field);
   action("load");number("inventoryGuiScale",3.25f);number("panelOpacity",.5f);
  });
  open(t);t.runOnClient(c->{slider(c,"Inventory GUI size",(6.5-1)/7);click(c,"Cancel");number("inventoryGuiScale",3.25f);});
  open(t);t.runOnClient(c->{click(c,"Reset page");click(c,"Save");number("inventoryGuiScale",4f);number("tooltipGuiScale",0f);});
  open(t);t.runOnClient(c->{c.options.getGuiScale().setValue(3);c.onResolutionChanged();page(c,1);});t.waitTicks(4);t.takeScreenshot("inventory-compact");
  t.runOnClient(c->{c.currentScreen.close();c.options.getGuiScale().setValue(2);c.onResolutionChanged();for(var e:original.entrySet())set(e.getKey(),e.getValue());action("save");});
 }
 public void world(ClientGameTestContext t){
  final int[] gui={0};t.runOnClient(c->{gui[0]=c.options.getGuiScale().getValue();set("inventoryGuiScale",3.25f);set("fitToScreen",false);set("panelOpacity",.5f);set("avatarOpacity",.35f);set("hidePlayer",false);set("panelTintEnabled",true);set("panelColor",new java.awt.Color(0x6688cc));c.setScreen(new InventoryScreen(c.player));});
  t.waitTicks(8);t.takeScreenshot("inventory-in-game");
  t.runOnClient(c->{try{float factor=(float)Class.forName("net.dwmirlol.inventoryscalefix.Scale").getMethod("inventoryScale").invoke(null);require(Math.abs(factor-3.25)<.01,"Inventory size changed");}catch(Exception e){throw new AssertionError(e);}require(c.options.getGuiScale().getValue()==gui[0],"Normal HUD scale changed");c.currentScreen.close();for(var e:original.entrySet())set(e.getKey(),e.getValue());action("save");});
 }
}
