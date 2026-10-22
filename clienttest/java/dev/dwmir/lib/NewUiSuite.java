package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;
import dev.dwmir.lib.api.GlassUi;
import java.util.*;
public final class NewUiSuite implements FabricClientGameTest {
 private void require(boolean v,String message){if(!v)throw new AssertionError(message);}
 private Screen actual(MinecraftClient c){return WorkspaceScreen.actual(c.currentScreen);}
 private Object call(Object target,String method,Class<?>[] types,Object... args){try{return target.getClass().getMethod(method,types).invoke(target,args);}catch(Exception e){throw new AssertionError(e);}}
 private void point(MinecraftClient c,double x,double y){if(c.currentScreen instanceof WorkspaceScreen w){x=w.screenX(x);y=w.screenY(y);}var event=new Click(x,y,new MouseInput(0,0));require(c.currentScreen.mouseClicked(event,false),"Click rejected at "+x+","+y);c.currentScreen.mouseReleased(event);}
 private void button(MinecraftClient c,String label){var w=actual(c).children().stream().filter(e->e instanceof ClickableWidget b&&b.getMessage().getString().equals(label)).map(e->(ClickableWidget)e).findFirst().orElseThrow(()->new AssertionError("Missing button "+label));point(c,w.getX()+w.getWidth()/2.,w.getY()+w.getHeight()/2.);}
 private void crossbowPoint(MinecraftClient c,double x,double y){var s=actual(c);float scale=Math.min((s.width-16)/700f,(s.height-16)/420f);point(c,(s.width-700*scale)/2+x*scale,(s.height-420*scale)/2+y*scale);}
 private void consent(ClientGameTestContext t,String id){
  t.runOnClient(c->{require(actual(c) instanceof WelcomeScreen,"First open did not show welcome for "+id);var w=(WelcomeScreen)actual(c);require(w.remainingSeconds()>0,"Countdown absent");button(c,"I will report any bugs I find on Discord.");button(c,"This mod belongs to dwmirlol.");require(!LibraryState.accepted(id),"Accepted before confirmation");var confirm=w.children().stream().filter(e->e instanceof ButtonWidget b&&b.getMessage().getString().startsWith("Confirm")).map(e->(ButtonWidget)e).findFirst().orElseThrow();require(!confirm.active,"Confirm enabled before five seconds");});
  t.waitTicks(7);t.takeScreenshot("welcome-"+id);t.waitFor(c->actual(c) instanceof WelcomeScreen w&&w.remainingSeconds()==0,180);
  t.waitTicks(2);t.runOnClient(c->{button(c,"Confirm");require(LibraryState.accepted(id),"Consent was not persisted");});t.waitTicks(9);
 }
 @Override public void runTest(ClientGameTestContext t){
  t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();LibraryState.load();try{var f=LibraryState.class.getDeclaredField("accepted");f.setAccessible(true);((Set<?>)f.get(null)).clear();}catch(Exception e){throw new AssertionError(e);}LibraryState.select(LibraryState.Theme.ONYX);});
  t.setScreen(()->DwmirLib.createScreen(null));consent(t,"dwmirlib");t.takeScreenshot("library-native-icons");
  String[] ids={"customcrossbowsounds","shieldpulse","inventoryscalefix"};String[] names={"Custom Crossbow Sounds","Custom Shield Status","dwmirlols Inventory Scale Fix"};String[] integrations={"net.customcrossbowsounds.config.ModMenuIntegration","dev.shieldpulse.MenuIntegration","net.dwmirlol.inventoryscalefix.config.ModMenuIntegration"};
  for(int i=0;i<ids.length;i++){final int index=i;t.runOnClient(c->button(c,names[index]));consent(t,ids[index]);t.takeScreenshot("module-open-"+ids[index]);t.runOnClient(c->{require(actual(c).width==860&&actual(c).height==520,"Wrong library canvas size");actual(c).close();require(actual(c) instanceof LibraryScreen,"Back navigation failed");try{var menu=(com.terraformersmc.modmenu.api.ModMenuApi)Class.forName(integrations[index]).getConstructor().newInstance();c.setScreen(menu.getModConfigScreenFactory().create(c.currentScreen));}catch(Exception e){throw new AssertionError(e);}require(!(actual(c) instanceof WelcomeScreen),"Welcome repeated after acceptance");require(actual(c).width==860&&actual(c).height==520,"Mod Menu uses different canvas dimensions");});t.waitTicks(9);t.takeScreenshot("modmenu-open-"+ids[index]);t.runOnClient(c->actual(c).close());}
  for(var theme:LibraryState.Theme.values()){t.runOnClient(c->LibraryState.select(theme));t.waitTicks(10);t.takeScreenshot("theme-"+theme.name().toLowerCase(Locale.ROOT));}
  t.runOnClient(c->{require(Socials.url("youtube").equals("https://www.youtube.com/@dwmirlol"),"Wrong YouTube URL");require(Socials.url("discord").equals("https://discord.gg/VtVagpUtSf"),"Wrong Discord invite");LibraryState.select(LibraryState.Theme.ONYX);button(c,"Custom Crossbow Sounds");call(actual(c),"selectPage",new Class<?>[]{int.class},1);});t.waitTicks(8);
  t.runOnClient(c->crossbowPoint(c,460,192));t.waitTicks(35);t.takeScreenshot("crossbow-picker-cached");
  final int[] uploads={0};t.runOnClient(c->uploads[0]=(int)call(actual(c),"pickerUploads",new Class<?>[]{}));t.waitTicks(60);
  t.runOnClient(c->{require((int)call(actual(c),"pickerUploads",new Class<?>[]{})==uploads[0],"Idle picker re-uploaded its texture");crossbowPoint(c,320,165);});t.waitTicks(25);
  t.runOnClient(c->{require((int)call(actual(c),"pickerUploads",new Class<?>[]{})==uploads[0],"SV dragging re-uploaded unchanged hue texture");System.out.println("DWMIR_CACHED_PICKER_FPS="+c.getCurrentFps()+" UPLOADS="+uploads[0]+" COLOR_QUADS=2");crossbowPoint(c,284,334);actual(c).close();});
  t.runOnClient(c->{c.options.getGuiScale().setValue(3);c.onResolutionChanged();});t.waitTicks(10);t.takeScreenshot("library-gui-scale-3");
  t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();require(LibraryState.accepted("inventoryscalefix"),"Acknowledgement lost after resize");});
  try(var world=t.worldBuilder().create()) {world.getClientWorld().waitForChunksRender();t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(10);t.takeScreenshot("library-in-world");t.runOnClient(c->button(c,"dwmirlols Inventory Scale Fix"));t.waitTicks(10);t.takeScreenshot("inventory-workspace-in-world");t.runOnClient(c->actual(c).close());}
  t.runOnClient(c->{LibraryState.select(LibraryState.Theme.ONYX);});t.getInput().resizeWindow(512,512);t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  for(String id:new String[]{"customcrossbowsounds","shieldpulse","inventoryscalefix","dwmirlib"}){t.setScreen(()->new NativeIcon(id));t.waitTicks(5);t.takeScreenshot("native-avatar-"+id);}
  t.setScreen(()->null);
 }
 private static final class NativeIcon extends Screen {
  final String id;NativeIcon(String id){super(Text.literal("Minecraft native icon"));this.id=id;}
  @Override public void renderBackground(DrawContext c,int x,int y,float dt){}
  @Override public void render(DrawContext c,int x,int y,float dt){c.fill(0,0,width,height,0xff17171c);GlassUi.rawRound(c,8,8,width-16,height-16,24,0xff212128);int size=(int)(Math.min(width,height)*.68);GlassUi.moduleIcon(c,id,(width-size)/2,(height-size)/2,size);}
 }
}

