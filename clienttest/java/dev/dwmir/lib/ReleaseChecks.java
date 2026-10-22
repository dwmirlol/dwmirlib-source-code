package dev.dwmir.lib;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.MouseInput;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import java.util.*;
import dev.shieldpulse.*;
import net.customcrossbowsounds.client.*;
import net.customcrossbowsounds.client.hud.ArrowHudEditScreen;
import net.customcrossbowsounds.config.CustomCrossbowSoundsConfig;
import me.shedaniel.autoconfig.AutoConfig;

public final class ReleaseChecks implements FabricClientGameTest {
 private void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 private Screen actual(MinecraftClient c){return WorkspaceScreen.actual(c.currentScreen);}
 private Click click(double x,double y){return new Click(x,y,new MouseInput(0,0));}
 private void point(MinecraftClient c,double x,double y){if(c.currentScreen instanceof WorkspaceScreen w){x=w.screenX(x);y=w.screenY(y);}Click e=click(x,y);require(c.currentScreen.mouseClicked(e,false),"Rejected click "+x+","+y);if(c.currentScreen!=null)c.currentScreen.mouseReleased(e);}
 private void button(MinecraftClient c,String text){var b=actual(c).children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals(text)).map(e->(ClickableWidget)e).findFirst().orElseThrow(()->new AssertionError("Missing "+text));point(c,b.getX()+b.getWidth()/2.,b.getY()+b.getHeight()/2.);}
 private Object invoke(Object object,String method,Class<?>[] types,Object...args){try{return object.getClass().getMethod(method,types).invoke(object,args);}catch(Exception e){throw new AssertionError(e);}}
 private Object config(){try{Object h=Class.forName("net.dwmirlol.inventoryscalefix.config.InventoryScaleConfig").getField("CONFIG").get(null);return Class.forName("dev.isxander.yacl3.config.v2.api.ConfigClassHandler").getMethod("instance").invoke(h);}catch(Exception e){throw new AssertionError(e);}}
 private void set(String name,Object value){try{config().getClass().getField(name).set(config(),value);}catch(Exception e){throw new AssertionError(e);}}
 private Object staticCall(String name,String method,Class<?>[] types,Object...args){try{return Class.forName(name).getMethod(method,types).invoke(null,args);}catch(Exception e){throw new AssertionError(e);}}
 private Set<String> accepted(){try{var f=LibraryState.class.getDeclaredField("accepted");f.setAccessible(true);return (Set<String>)f.get(null);}catch(Exception e){throw new AssertionError(e);}}
 private void drag(MinecraftClient c,double x,double y,double endX,double endY){require(c.currentScreen.mouseClicked(click(x,y),false),"Drag start rejected");require(c.currentScreen.mouseDragged(click(endX,endY),endX-x,endY-y),"Drag event rejected");c.currentScreen.mouseReleased(click(endX,endY));}
 @Override public void runTest(ClientGameTestContext t){
  t.runOnClient(c->{LibraryState.load();accepted().clear();LibraryState.select(LibraryState.Theme.ONYX);c.options.getGuiScale().setValue(2);c.options.getEnableVsync().setValue(false);c.options.getMaxFps().setValue(260);c.onResolutionChanged();});
  t.setScreen(()->DwmirLib.createScreen(null));t.waitTicks(80);t.runOnClient(c->{require(actual(c) instanceof LibraryScreen,"Library still asks per-mod consent");System.out.println("DWMIR_LIBRARY_FPS="+c.getCurrentFps());});t.takeScreenshot("v1-library-player-socials");
  t.runOnClient(c->{var lib=actual(c);var card=lib.children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals("Custom Crossbow Sounds")).map(e->(ClickableWidget)e).findFirst().orElseThrow();var w=(WorkspaceScreen)c.currentScreen;c.currentScreen.mouseMoved(w.screenX(card.getX()+10),w.screenY(card.getY()+10));});
  t.getInput().setCursorPos(440,330);t.waitTicks(3);t.takeScreenshot("v1-tooltip-aligned");
  for(String name:new String[]{"Custom Crossbow Sounds","Custom Shield Status","dwmirlols Inventory Scale Fix"}){t.runOnClient(c->{button(c,name);require(!(actual(c) instanceof WelcomeScreen),"Per-mod welcome returned");require(actual(c).width==860,"Canvas mismatch");});t.waitTicks(20);t.takeScreenshot("v1-settings-"+name.replace(' ','-'));t.runOnClient(c->{if(actual(c) instanceof dev.shieldpulse.SettingsScreen)require(ShieldView.shieldDraws>0,"Shield preview did not render");actual(c).close();});}
  for(var theme:LibraryState.Theme.values()){t.runOnClient(c->LibraryState.select(theme));t.waitTicks(7);t.takeScreenshot("v1-theme-"+theme.name());}
  t.runOnClient(c->{LibraryState.select(LibraryState.Theme.ONYX);LibraryState.accept("library-onboarding-v2");c.setScreen(null);});
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();t.runOnClient(c->{accepted().remove("library-onboarding-v2");c.setScreen(null);});t.waitFor(c->actual(c) instanceof WelcomeScreen,80);
   t.runOnClient(c->{var screen=(WelcomeScreen)actual(c);require(screen.remainingSeconds()>0,"Five-second wait missing");button(c,"I will report any bugs I find on Discord.");button(c,"This mod belongs to dwmirlol.");require(!LibraryState.accepted("library-onboarding-v2"),"Early acceptance");});t.takeScreenshot("v1-once-on-world-entry");
   t.waitFor(c->actual(c) instanceof WelcomeScreen w&&w.remainingSeconds()==0,180);t.waitTicks(2);t.runOnClient(c->{button(c,"Confirm");require(c.currentScreen==null,"Welcome did not return to gameplay");require(LibraryState.accepted("library-onboarding-v2"),"Onboarding not saved");});t.waitTicks(5);t.runOnClient(c->require(c.currentScreen==null,"Onboarding repeated"));
   t.runOnClient(c->{ShieldPulse.CONFIG=new Config();c.player.setStackInHand(Hand.OFF_HAND,new ItemStack(Items.SHIELD));for(int i=0;i<8;i++)c.gameRenderer.firstPersonRenderer.updateHeldItems();c.setScreen(new ShieldEditorScreen(null,false));double before=ShieldPulse.CONFIG.firstPerson.x;drag(c,200,190,270,215);require(ShieldPulse.CONFIG.firstPerson.x>before,"First-person drag failed");});t.waitTicks(10);t.takeScreenshot("v1-first-person-shield-position");
   t.runOnClient(c->{c.setScreen(new ShieldEditorScreen(null,true));drag(c,300,200,480,280);c.currentScreen.mouseScrolled(480,280,0,2);require(ShieldPulse.CONFIG.recoveryX>.6&&ShieldPulse.CONFIG.recoveryScale>1,"Recovery drag/size failed");ShieldPulse.CONFIG.recoveryColor=0xff6699;ShieldPulse.CONFIG.recoveryBackgroundOpacity=.3;ShieldPulse.CONFIG.save();require(Config.load().recoveryColor==0xff6699,"Recovery preferences not saved");});t.waitTicks(6);t.takeScreenshot("v1-recovery-customizable");
   t.runOnClient(c->{c.setScreen(new ArrowHudEditScreen(null));var cfg=AutoConfig.getConfigHolder(CustomCrossbowSoundsConfig.class).getConfig();cfg.arrowHudEnabled=true;cfg.arrowHudX=20;cfg.arrowHudY=70;drag(c,40,80,250,190);require(cfg.arrowHudX>200&&cfg.arrowHudY>150,"Arrow counter drag failed");});t.waitTicks(6);t.takeScreenshot("v1-arrow-counter-drag");
   t.runOnClient(c->{set("fitToScreen",false);set("inventoryGuiScale",2.5f);set("fixedGuiScale",false);float scale=(float)staticCall("net.dwmirlol.inventoryscalefix.Scale","inventoryScale",new Class<?>[]{});require(Math.abs(scale-2.5)<.01,"Fractional scale failed");set("inventoryGuiScale",4.7f);require(Math.abs((float)staticCall("net.dwmirlol.inventoryscalefix.Scale","inventoryScale",new Class<?>[]{})-4.7)<.01,"4.7 scale failed");set("fixedGuiScale",true);set("inventoryGuiScale",2.5f);require((float)staticCall("net.dwmirlol.inventoryscalefix.Scale","inventoryScale",new Class<?>[]{})==3,"Fixed scale not integer");set("fixedGuiScale",false);set("inventoryGuiScale",2.5f);
    String file=java.nio.file.Path.of("../src/main/resources/assets/dwmirlib/icon.png").toAbsolutePath().normalize().toString();String imported=(String)staticCall("net.dwmirlol.inventoryscalefix.CustomInventoryTexture","importPng",new Class<?>[]{String.class},file);set("customPng",imported);set("customPngEnabled",true);set("panelOpacity",.85f);c.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(c.player));});t.waitTicks(8);t.takeScreenshot("v1-custom-inventory-png");
   t.runOnClient(c->{c.setScreen(null);EffectTracker.tick(c);var target=new net.minecraft.client.network.OtherClientPlayerEntity(c.world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ArrowTarget"));target.setId(92123);target.setPosition(c.player.getX(),c.player.getY(),c.player.getZ()+3);c.world.addEntity(target);EffectTracker.confirmed(target.getId(),net.minecraft.entity.effect.StatusEffects.SPEED,0,300);require(EffectTracker.entries(target).isEmpty(),"Unrelated potion accepted");EffectTracker.confirmed(c.player.getId(),net.minecraft.entity.effect.StatusEffects.SPEED,0,300);require(EffectTracker.entries(c.player).isEmpty(),"Self effects visible");
    try{var type=Class.forName("net.customcrossbowsounds.client.EffectTracker$Shot");var constructor=type.getDeclaredConstructors()[0];constructor.setAccessible(true);var f=EffectTracker.class.getDeclaredField("shots");f.setAccessible(true);var shots=(Map<Integer,Object>)f.get(null);shots.put(92124,constructor.newInstance(new ItemStack(Items.SPECTRAL_ARROW),1000L));EffectTracker.damage(new net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket(target,c.world.getDamageSources().arrow(new net.minecraft.entity.projectile.ArrowEntity(c.world,c.player,new ItemStack(Items.SPECTRAL_ARROW),new ItemStack(Items.BOW)),c.player)));
     var source=c.world.getDamageSources().arrow(new net.minecraft.entity.projectile.ArrowEntity(c.world,c.player,new ItemStack(Items.SPECTRAL_ARROW),new ItemStack(Items.BOW)),c.player);EffectTracker.damage(new net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket(target.getId(),source.getTypeRegistryEntry(),c.player.getId(),92124,Optional.empty()));require(!EffectTracker.entries(target).isEmpty(),"Own arrow effect missing");require(!EffectTracker.label(target,true).getString().contains("~"),"Estimate marker remains");EffectTracker.confirmed(target.getId(),net.minecraft.entity.effect.StatusEffects.POISON,0,300);require(EffectTracker.entries(target).size()==1,"Unrelated confirmed effect leaked");
    }catch(Exception e){throw new AssertionError(e);}c.setScreen(DwmirLib.createScreen(null));});t.waitTicks(30);t.runOnClient(c->{require(!(actual(c) instanceof WelcomeScreen),"Consent repeated in library");System.out.println("DWMIR_WORLD_LIBRARY_FPS="+c.getCurrentFps());});t.takeScreenshot("v1-library-in-world");
  }
  t.setScreen(()->null);System.out.println("DWMIR_V1_RELEASE_CHECKS_PASS");
 }
}
