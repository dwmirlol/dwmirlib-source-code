package dev.dwmir.lib;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.*;
import net.minecraft.client.input.*;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import java.util.*;
public final class WorkspaceScreen extends Screen implements dev.dwmir.lib.api.PickerHost {
 public static final int VW=860,VH=520;
 public final Screen content;
 private dev.dwmir.lib.api.ColorPickerScreen picker;
 public boolean hasPicker(){return picker!=null;}
 public void openPicker(dev.dwmir.lib.api.ColorPickerScreen p){if(picker!=null)picker.removed();picker=p;p.dismiss=()->{p.removed();picker=null;};p.init(logicalWidth,logicalHeight);}
 private float factor,left,top;private int logicalWidth=VW,logicalHeight=VH;
 private final long opened=System.nanoTime();
 private WorkspaceScreen(Screen content){super(content.getTitle());this.content=content;}
 public static Screen actual(Screen screen){return screen instanceof WorkspaceScreen w?w.content:screen;}
 public static String module(Screen s){String n=s.getClass().getName();if(n.equals("dev.dwmir.glowing.SettingsScreen"))return "playerglowing";if(n.equals("dev.dwmir.highlighter.SettingsScreen"))return "betterhighlighter";if(n.equals("dev.dwmir.countit.SettingsScreen"))return "countit";if(n.equals("dev.dwmir.sprinter.SettingsScreen"))return "sprinter";if(n.equals("dev.dwmir.lib.LibraryScreen"))return "dwmirlib";if(n.equals("dev.shieldpulse.SettingsScreen"))return "shieldpulse";if(n.equals("net.customcrossbowsounds.client.SettingsScreen"))return "customcrossbowsounds";if(n.equals("net.dwmirlol.inventoryscalefix.config.InventorySettingsScreen"))return "inventoryscalefix";return null;}
 public static Screen intercept(Screen target,Screen parent){
  if(target==null||target instanceof WorkspaceScreen)return target;
  if(target instanceof dev.dwmir.lib.api.ColorPickerScreen&&parent instanceof dev.dwmir.lib.api.PickerHost)return target;
  String id=module(target);boolean auxiliary=target.getClass().getName().equals("net.customcrossbowsounds.client.SoundPickerScreen")||target instanceof dev.dwmir.lib.api.ColorPickerScreen||target instanceof ProfilesScreen||target instanceof WelcomeScreen;
  if(id==null&&!auxiliary)return target;
  LibraryState.load();
  return new WorkspaceScreen(target);
 }
 @Override protected void init(){layout();content.init(logicalWidth,logicalHeight);if(picker!=null)picker.init(logicalWidth,logicalHeight);}
 private void layout(){double gui=MinecraftClient.getInstance().getWindow().getScaleFactor();int physical=Math.max(1,Math.min(2,(int)Math.floor(Math.min(width*gui/640.,height*gui/360.))));factor=(float)(physical/gui);logicalWidth=Math.round(width/factor);logicalHeight=Math.round(height/factor);left=0;top=0;}

 public double screenX(double x){return left+x*factor;}public double screenY(double y){return top+y*factor;}
 private Click local(Click c){return new Click((c.x()-left)/factor,(c.y()-top)/factor,c.buttonInfo());}
 @Override public void renderBackground(DrawContext c,int mx,int my,float dt){dev.dwmir.lib.api.GlassRenderer.capture();if(client.world==null)c.fill(0,0,width,height,0xff15151c);dev.dwmir.lib.api.GlassUi.backdrop(c,width,height);}
 @Override public void render(DrawContext c,int mx,int my,float dt){
  float t=Math.min(1,(System.nanoTime()-opened)/280000000f),ease=1-(float)Math.pow(1-t,3),zoom=1;
  c.getMatrices().pushMatrix();c.getMatrices().translate(left+VW*factor*(1-zoom)/2,top+(1-ease)*12);c.getMatrices().scale(factor*zoom,factor*zoom);
  int localX=(int)((mx-left)/factor),localY=(int)((my-top)/factor);dev.dwmir.lib.api.GlassUi.drawing=true;content.render(c,picker==null?localX:-100,picker==null?localY:-100,dt);if(picker!=null){c.fill(0,0,logicalWidth,logicalHeight,0x50000000);picker.render(c,localX,localY,dt);}dev.dwmir.lib.api.GlassUi.drawing=false;dev.dwmir.lib.api.GlassUi.glow(c,localX,localY);dev.dwmir.lib.api.GlassRenderer.end();c.getMatrices().popMatrix();
 }
 @Override public void tick(){content.tick();}
 @Override public void removed(){if(picker!=null)picker.removed();content.removed();}
 @Override public void close(){if(picker!=null)picker.close();else content.close();}
 @Override public boolean shouldPause(){return content.shouldPause();}
 @Override public java.util.List<? extends Element> children(){return content.children();}
 @Override public boolean mouseClicked(Click c,boolean d){Click point=local(c);if(picker!=null)return picker.mouseClicked(point,d);for(var child:content.children())if(child.isMouseOver(point.x(),point.y()))dev.dwmir.lib.api.GlassUi.press(child);boolean handled=content.mouseClicked(point,d);if(handled&&content.getClass().getName().equals("net.customcrossbowsounds.client.SettingsScreen"))dev.dwmir.lib.api.GlassUi.click();return handled;}
 @Override public boolean mouseReleased(Click c){return picker!=null?picker.mouseReleased(local(c)):content.mouseReleased(local(c));}
 @Override public boolean mouseDragged(Click c,double dx,double dy){return picker!=null?picker.mouseDragged(local(c),dx/factor,dy/factor):content.mouseDragged(local(c),dx/factor,dy/factor);}
 @Override public void mouseMoved(double x,double y){if(picker==null)content.mouseMoved((x-left)/factor,(y-top)/factor);}
 @Override public boolean mouseScrolled(double x,double y,double h,double v){return picker!=null?picker.mouseScrolled((x-left)/factor,(y-top)/factor,h,v):content.mouseScrolled((x-left)/factor,(y-top)/factor,h,v);}
 @Override public boolean keyPressed(KeyInput k){return picker!=null?picker.keyPressed(k):content.keyPressed(k);}
 @Override public boolean keyReleased(KeyInput k){return content.keyReleased(k);}
 @Override public boolean charTyped(CharInput c){return picker!=null?picker.charTyped(c):content.charTyped(c);}
 @Override public void onFilesDropped(java.util.List<java.nio.file.Path> paths){content.onFilesDropped(paths);}
}
