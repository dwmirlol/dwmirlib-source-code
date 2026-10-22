package dev.shieldpulse;
import dev.dwmir.lib.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import java.util.*;
public final class R2Checks implements FabricClientGameTest {
 private void require(boolean b,String message){if(!b)throw new AssertionError(message);}
 private net.minecraft.client.gui.screen.Screen actual(MinecraftClient c){return WorkspaceScreen.actual(c.currentScreen);}
 private void point(MinecraftClient c,double x,double y){var screen=c.currentScreen;if(screen instanceof WorkspaceScreen w){x=w.screenX(x);y=w.screenY(y);}var click=new Click(x,y,new MouseInput(0,0));require(screen.mouseClicked(click,false),"Rejected click");if(c.currentScreen!=null)c.currentScreen.mouseReleased(click);}
 private void button(MinecraftClient c,String label){var b=actual(c).children().stream().filter(e->e instanceof ClickableWidget w&&w.getMessage().getString().equals(label)).map(e->(ClickableWidget)e).findFirst().orElseThrow(()->new AssertionError("Missing "+label));if(actual(c) instanceof SettingsScreen s){int[] bounds=s.controlsBounds();int delta=b.getY()<bounds[1]?b.getY()-bounds[1]:b.getY()+b.getHeight()>bounds[1]+bounds[3]?b.getY()+b.getHeight()-(bounds[1]+bounds[3]):0;if(delta!=0)s.mouseScrolled(bounds[0]+10,bounds[1]+10,0,-delta/28.0);}point(c,b.getX()+b.getWidth()/2.,b.getY()+b.getHeight()/2.);}
 @Override public void runTest(ClientGameTestContext t){
  new BenchmarkChecks().runTest(t);
  t.runOnClient(c->{ShieldPulse.CONFIG=new Config();c.setScreen(new SettingsScreen(null));((SettingsScreen)actual(c)).showPage(3);button(c,"Break effect color");require(actual(c) instanceof SpectrumScreen,"Spectrum route missing");});t.waitTicks(4);t.takeScreenshot("r2-spectrum-rainbow");
  t.runOnClient(c->{int old=ShieldPulse.CONFIG.visualColor;point(c,(860-390)/2+204,(520-356)/2+102);require(((SpectrumScreen)actual(c)).selectedColor()!=old,"Spectrum input ignored");button(c,"Cancel");require(ShieldPulse.CONFIG.visualColor==old,"Cancel changed color");button(c,"Break effect color");});t.waitTicks(3);
  final int[] chosen={0};t.runOnClient(c->{point(c,(860-390)/2+206,(520-356)/2+108);chosen[0]=((SpectrumScreen)actual(c)).selectedColor();});t.waitTicks(20);t.runOnClient(c->{require(((SpectrumScreen)actual(c)).uploads==1,"Picker reuploads every frame");button(c,"Apply");require(ShieldPulse.CONFIG.visualColor==chosen[0],"Effect color not applied");});
  for(String name:new String[]{"Recovery bar color","Recovery text color","Recovery background color"}){t.runOnClient(c->button(c,name));t.waitTicks(2);t.runOnClient(c->{point(c,(860-390)/2+74,(520-356)/2+128);button(c,"Apply");});}
  t.runOnClient(c->{ShieldPulse.CONFIG.save();var saved=Config.load();require(saved.visualColor==chosen[0],"Effect color not saved");require(saved.recoveryColor==ShieldPulse.CONFIG.recoveryColor&&saved.recoveryTextColor==ShieldPulse.CONFIG.recoveryTextColor&&saved.recoveryBackgroundColor==ShieldPulse.CONFIG.recoveryBackgroundColor,"Recovery colors not saved");});t.takeScreenshot("r2-effects-color-buttons");t.setScreen(()->null);
  try(var world=t.worldBuilder().create()){
   world.getClientWorld().waitForChunksRender();t.runOnClient(c->{c.options.getMainArm().setValue(Arm.RIGHT);c.player.setStackInHand(Hand.MAIN_HAND,ItemStack.EMPTY);c.player.setStackInHand(Hand.OFF_HAND,new ItemStack(Items.SHIELD));var v=ShieldPulse.CONFIG.firstPerson;v.x=.32;v.y=.2;v.rotation=12;v.reanchor(true);for(int i=0;i<8;i++)c.gameRenderer.firstPersonRenderer.updateHeldItems();});t.waitTicks(8);t.takeScreenshot("r2-shield-offhand");
   t.runOnClient(c->{require(ShieldView.lastFirstPersonLeft&&Math.abs(ShieldView.lastFirstPersonOffset-.32)<.001,"Offhand offset incorrect");c.player.setStackInHand(Hand.OFF_HAND,ItemStack.EMPTY);c.player.setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.SHIELD));for(int i=0;i<8;i++)c.gameRenderer.firstPersonRenderer.updateHeldItems();});t.waitTicks(8);t.takeScreenshot("r2-shield-mainhand");
   t.runOnClient(c->{require(!ShieldView.lastFirstPersonLeft&&Math.abs(ShieldView.lastFirstPersonOffset+.32)<.001,"Hand swap did not mirror offset");});
   final Opponent[] target={null};t.runOnClient(c->{c.player.setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.DIAMOND_AXE));var p=new Opponent(c.world,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"LocalEvidence"));p.setId(93323);p.setPosition(c.player.getX(),c.player.getY(),c.player.getZ()+2.5);p.setYaw(180);p.setHeadYaw(180);p.setStackInHand(Hand.OFF_HAND,new ItemStack(Items.SHIELD));c.world.addEntity(p);p.block(true);target[0]=p;ShieldPulse.CONFIG.localInference=true;});t.waitTicks(25);
   t.runOnClient(c->{ShieldPulse.attack(c.player,target[0]);ShieldPulse.status(target[0],(byte)29);});t.waitTicks(6);t.runOnClient(c->{require(ShieldPulse.state(target[0])==2,"Blocked axe falsely disabled active shield");ShieldPulse.attack(c.player,target[0]);ShieldPulse.status(target[0],(byte)29);target[0].block(false);});t.waitTicks(3);
   t.runOnClient(c->{require(ShieldPulse.TRACKER.inferred(target[0].getUuid(),ShieldPulse.tick),"Correlated release estimate missing");target[0].block(true);});t.waitTicks(2);t.runOnClient(c->{require(!ShieldPulse.TRACKER.inferred(target[0].getUuid(),ShieldPulse.tick),"Fresh block did not cancel estimate");target[0].block(false);ShieldPulse.status(target[0],(byte)30);require(ShieldPulse.state(target[0])==3,"Authoritative break missing");ShieldPulse.TRACKER.clear();ShieldPulse.sound("minecraft:item.shield.break",target[0].getEntityPos().add(0,1.5,0),null);require(ShieldPulse.state(target[0])==3,"Torso-height shield sound missed");ShieldPulse.CONFIG.visualColor=0x9955ff;BreakFeedback.clear();BreakFeedback.burst(target[0]);});t.waitTicks(6);t.takeScreenshot("r2-break-effect-custom-color");
  }
  System.out.println("DWMIR_R2_CHECKS_PASS");
 }
 private static final class Opponent extends OtherClientPlayerEntity {
  Opponent(net.minecraft.client.world.ClientWorld w,com.mojang.authlib.GameProfile p){super(w,p);}
  void block(boolean value){setLivingFlag(2,value);setLivingFlag(1,value);}
 }
}
