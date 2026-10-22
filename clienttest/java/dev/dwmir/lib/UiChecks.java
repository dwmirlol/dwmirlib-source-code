package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
public final class UiChecks implements FabricClientGameTest {
 private void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 @Override public void runTest(ClientGameTestContext t){
  if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("customcrossbowsounds")){new IntegrationChecks().run(t);return;}
  t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  t.setScreen(()->new MenuIntegration().getModConfigScreenFactory().create(null));
  t.waitTicks(8);t.takeScreenshot("dwmirlib-empty");
  t.runOnClient(c->DwmirLib.register("dwmirlib_test",parent->new Screen(Text.literal("Fixture settings")){
   @Override public void close(){client.setScreen(parent);}
  }));
  t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(8);t.takeScreenshot("dwmirlib-collection");
  t.clickScreenButton("Studio Controls");
  t.runOnClient(c->{require(c.currentScreen.getTitle().getString().equals("Fixture settings"),"Settings routing failed");c.currentScreen.close();require(c.currentScreen instanceof LibraryScreen,"Settings back navigation failed");});
  t.runOnClient(c->{var field=c.currentScreen.children().stream().filter(e->e instanceof TextFieldWidget).map(e->(TextFieldWidget)e).findFirst().orElseThrow();field.setText("no-such-mod");});
  t.waitTicks(3);t.takeScreenshot("dwmirlib-search-empty");
  t.runOnClient(c->require(c.currentScreen.children().stream().noneMatch(e->e instanceof ButtonWidget b&&b.getMessage().getString().equals("Studio Controls")),"Search did not remove card"));
  t.runOnClient(c->{c.options.getGuiScale().setValue(3);c.onResolutionChanged();});t.waitTicks(3);t.takeScreenshot("dwmirlib-compact");
  t.runOnClient(c->{c.options.getGuiScale().setValue(4);c.onResolutionChanged();
   c.currentScreen.children().stream().filter(e->e instanceof TextFieldWidget).map(e->(TextFieldWidget)e).findFirst().orElseThrow().setText("");
  });t.waitTicks(3);t.takeScreenshot("dwmirlib-smallest");
  t.runOnClient(c->{for(var e:c.currentScreen.children())if(e instanceof ClickableWidget b){
   require(b.getX()>=0&&b.getY()>=0&&b.getRight()<=c.currentScreen.width&&b.getBottom()<=c.currentScreen.height,"Widget outside compact screen: "+b.getMessage().getString());
  }});
  t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();});
  t.setScreen(()->null);
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();
   t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(10);t.takeScreenshot("dwmirlib-in-world");
   t.runOnClient(c->{c.currentScreen.close();require(c.currentScreen==null,"Close did not restore parent");});
  }
 }
}
