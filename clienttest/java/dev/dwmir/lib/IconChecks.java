package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.widget.TextFieldWidget;
public final class IconChecks implements FabricClientGameTest {
 public void runTest(ClientGameTestContext t){t.runOnClient(c->{c.options.getGuiScale().setValue(2);c.onResolutionChanged();for(String id:new String[]{"dwmirlib","shieldpulse","customcrossbowsounds","inventoryscalefix"}){var container=FabricLoader.getInstance().getModContainer(id).orElseThrow();var icon=new FabricIconHandler().createIcon(container,container.getMetadata().getIconPath(64).orElseThrow());if(icon==null)throw new AssertionError("Broken icon: "+id);System.out.println("DWMIR_ICON_"+id+"="+icon.getImage().getWidth()+"x"+icon.getImage().getHeight());}});
  t.setScreen(()->new ModsScreen(null));t.waitTicks(10);t.takeScreenshot("modmenu-icons-before");
  for(String query:new String[]{"dwmirLib","Custom Shield Status","Custom Crossbow Sounds","Inventory Scale Fix"}){t.runOnClient(c->{c.currentScreen.children().stream().filter(e->e instanceof TextFieldWidget).map(e->(TextFieldWidget)e).findFirst().orElseThrow().setText(query);});t.waitTicks(4);t.takeScreenshot("modmenu-"+query.replace(' ','-'));}t.setScreen(()->null);
 }
}
