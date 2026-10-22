package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
public final class BenchmarkChecks implements FabricClientGameTest {
 private void sample(ClientGameTestContext t,String label){t.waitTicks(80);for(int i=0;i<3;i++){t.waitTicks(20);t.runOnClient(c->System.out.println("DWMIR_BENCH_"+label+"="+c.getCurrentFps()));}}
 @Override public void runTest(ClientGameTestContext t){
  t.runOnClient(c->{LibraryState.accept("library-onboarding-v2");LibraryState.select(LibraryState.Theme.ONYX);c.options.getGuiScale().setValue(2);c.options.getEnableVsync().setValue(false);c.options.getMaxFps().setValue(260);c.onResolutionChanged();c.onWindowFocusChanged(true);});
  t.setScreen(()->DwmirLib.createScreen(null));sample(t,"MENU");t.setScreen(()->null);
  try(var world=t.worldBuilder().create()){world.getClientWorld().waitForChunksRender();sample(t,"GAME");t.setScreen(()->DwmirLib.createScreen(null));sample(t,"WORLD_MENU");t.takeScreenshot("fps-library");}
  System.out.println("DWMIR_BENCH_PASS");
 }
}
