package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import dev.dwmir.sprinter.*;
import net.customcrossbowsounds.client.HitmarkerRenderer;
import net.customcrossbowsounds.config.CustomCrossbowSoundsConfig;
import me.shedaniel.autoconfig.AutoConfig;
public final class R7Checks implements FabricClientGameTest {
 private static volatile boolean lastTickSprint,lastTickSneak;
 private void check(boolean value,String reason){if(!value)throw new AssertionError(reason);}
 private void click(net.minecraft.client.MinecraftClient c,String label){var s=WorkspaceScreen.actual(c.currentScreen);var b=s.children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().startsWith(label)).map(e->(ClickableWidget)e).findFirst().orElseThrow();double x=b.getX()+b.getWidth()/2.,y=b.getY()+b.getHeight()/2.;if(c.currentScreen instanceof WorkspaceScreen w){x=w.screenX(x);y=w.screenY(y);}var point=new Click(x,y,new MouseInput(0,0));check(c.currentScreen.mouseClicked(point,false),"Click rejected: "+label);if(c.currentScreen!=null)c.currentScreen.mouseReleased(point);}
 public void runTest(ClientGameTestContext t){
  t.runOnClient(c->{net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc->{lastTickSprint=mc.player!=null&&mc.player.isSprinting();lastTickSneak=mc.options.sneakKey.isPressed();});System.out.println("SPRINTER_RUNTIME="+Sprinter.class.getProtectionDomain().getCodeSource().getLocation());});t.getInput().resizeWindow(1440,900);t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");LibraryState.select(LibraryState.Theme.SAKURA);c.options.language="en_us";c.options.getGuiScale().setValue(2);c.onResolutionChanged();check(DwmirLib.settings("sprinter")!=null,"Sprinter not registered");Preferences.current=new Preferences();});
  for(int page=0;page<6;page++){final int p=page;t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.runOnClient(c->((net.customcrossbowsounds.client.SettingsScreen)WorkspaceScreen.actual(c.currentScreen)).selectPage(p));t.waitTicks(8);t.takeScreenshot("r7-crossbow-page-"+p);}
  t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(8);t.takeScreenshot("r7-library-sprinter");
  t.setScreen(()->new dev.dwmir.sprinter.SettingsScreen(null));t.waitTicks(8);t.takeScreenshot("r7-sprinter-settings");t.runOnClient(c->{click(c,"Auto sprint:");check(!Preferences.current.autoSprint,"Auto sprint toggle did not apply");});t.waitTicks(3);t.runOnClient(c->{click(c,"Auto sprint:");check(Preferences.current.autoSprint,"Auto sprint toggle did not restore");});
  for(int width:new int[]{1440,1442}){t.getInput().resizeWindow(width,900);for(boolean marker:new boolean[]{false,true}){t.setScreen(()->new Aim(marker));t.waitTicks(4);t.takeScreenshot("r7-aim-"+width+(marker?"-marker":"-vanilla"));}}
  t.getInput().resizeWindow(1440,900);t.setScreen(()->null);
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");c.setScreen(null);Preferences.current.autoSprint=true;c.player.getHungerManager().setFoodLevel(20);});
   t.getInput().holdKey(o->o.forwardKey);t.waitTicks(8);t.runOnClient(c->check(c.player.isSprinting(),"Auto sprint did not start moving forward"));
   t.getInput().pressKey(o->o.jumpKey);t.waitTicks(4);t.runOnClient(c->check(c.player.isSprinting(),"Jump cancelled auto sprint"));
   t.getInput().holdKey(o->o.sneakKey);t.waitTicks(4);t.runOnClient(c->{System.out.println("SPRINTER_SNEAK_DEBUG forward="+c.options.forwardKey.isPressed()+" sneak="+c.options.sneakKey.isPressed()+" sprintKey="+c.options.sprintKey.isPressed()+" sprint="+c.player.isSprinting()+" entitySneak="+c.player.isSneaking()+" endTickSprint="+lastTickSprint+" endTickSneak="+lastTickSneak);check(!lastTickSprint&&lastTickSneak,"Auto sprint ignored sneak at tick end");});t.getInput().releaseKey(o->o.sneakKey);t.waitTicks(4);t.runOnClient(c->check(c.player.isSprinting(),"Sprint did not resume"));
   t.getInput().releaseKey(o->o.forwardKey);t.waitTicks(8);t.runOnClient(c->{check(!c.player.isSprinting(),"Forward release left sprint active");Preferences.current.autoSprint=false;c.player.setSprinting(false);});
   t.getInput().holdKey(o->o.forwardKey);t.waitTicks(6);t.runOnClient(c->check(!c.player.isSprinting(),"Disabled auto sprint still running"));t.getInput().holdKey(o->o.sprintKey);t.waitTicks(4);t.runOnClient(c->check(c.player.isSprinting(),"Manual sprint broken with auto disabled"));t.getInput().releaseKey(o->o.sprintKey);t.getInput().releaseKey(o->o.forwardKey);
   t.setScreen(()->new HudEditor(null));t.waitTicks(3);t.runOnClient(c->{Screen s=c.currentScreen;var a=new Click(100,120,new MouseInput(0,0));check(s.mouseClicked(a,false),"HUD drag rejected");var b=new Click(260,170,new MouseInput(0,0));s.mouseDragged(b,160,50);s.mouseReleased(b);int x=Preferences.current.hudX,y=Preferences.current.hudY;Preferences.load();check(Preferences.current.hudX==x&&Preferences.current.hudY==y,"HUD position did not persist");check(x>240&&x<260&&y>150&&y<170,"HUD drag coordinates incorrect");});t.waitTicks(3);t.takeScreenshot("r7-sprinter-hud-editor");
   t.setScreen(()->new net.customcrossbowsounds.client.SettingsScreen(null));t.runOnClient(c->((net.customcrossbowsounds.client.SettingsScreen)WorkspaceScreen.actual(c.currentScreen)).selectPage(1));t.waitTicks(8);t.takeScreenshot("r7-crossbow-world");
   t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(8);t.takeScreenshot("r7-library-world");
  }
  t.setScreen(()->null);System.out.println("DWMIR_R7_CHECKS_PASS AUTO_SPRINT=OK MANUAL_SPRINT=OK JUMP=OK SNEAK=OK HUD_DRAG_SAVE=OK");
 }
 private static final class Aim extends Screen {
  private final boolean marker;Aim(boolean marker){super(Text.literal("Crosshair alignment"));this.marker=marker;}
  public void renderBackground(DrawContext c,int x,int y,float dt){}
  public void render(DrawContext c,int x,int y,float dt){c.fill(0,0,width,height,0xff202020);c.drawGuiTexture(RenderPipelines.GUI_TEXTURED,Identifier.ofVanilla("hud/crosshair"),(width-15)/2,(height-15)/2,15,15);if(marker){var p=AutoConfig.getConfigHolder(CustomCrossbowSoundsConfig.class).getConfig();p.enabled=true;p.hitmarkerEnabled=true;p.hitmarkerShape=CustomCrossbowSoundsConfig.MarkerShape.DOT;p.hitmarkerColor=0xff0000;p.hitmarkerOpacity=100;HitmarkerRenderer.render(c,true);}}
 }
}
