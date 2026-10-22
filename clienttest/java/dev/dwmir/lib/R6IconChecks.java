package dev.dwmir.lib;
import dev.dwmir.lib.api.GlassUi;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
public final class R6IconChecks implements FabricClientGameTest {
 public void runTest(ClientGameTestContext t){
  t.getInput().resizeWindow(1440,900);t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  for(var theme:new LibraryState.Theme[]{LibraryState.Theme.SAKURA,LibraryState.Theme.QUARTZ}){t.runOnClient(c->LibraryState.select(theme));t.setScreen(()->DwmirLib.createScreen(null));t.getInput().setCursorPos(0,0);t.waitTicks(12);t.takeScreenshot("r6-icons-"+theme);}
  for(boolean light:new boolean[]{true,false}){t.setScreen(()->new Preview(light));t.waitTicks(8);t.takeScreenshot(light?"r6-transparent-light":"r6-transparent-dark");}
  new IconChecks().runTest(t);t.setScreen(()->null);System.out.println("DWMIR_R6_ICON_CHECKS_PASS");
 }
 private static final class Preview extends Screen {
  private final boolean light;Preview(boolean light){super(Text.literal("Transparent icons"));this.light=light;}
  @Override public void renderBackground(DrawContext c,int x,int y,float dt){}
  @Override public void render(DrawContext c,int x,int y,float dt){c.fill(0,0,width,height,light?0xffeeeaf2:0xff20212b);String[] ids={"customcrossbowsounds","shieldpulse","inventoryscalefix"};for(int i=0;i<3;i++)GlassUi.moduleIcon(c,ids[i],width/2-240+i*176,height/2-64,128);}
 }
}
