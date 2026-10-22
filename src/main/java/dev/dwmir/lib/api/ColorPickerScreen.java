package dev.dwmir.lib.api;



import dev.dwmir.lib.api.GlassUi;

import net.minecraft.client.gui.Click;

import net.minecraft.client.gui.DrawContext;

import net.minecraft.client.gui.screen.Screen;

import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.texture.*;

import net.minecraft.client.gl.RenderPipelines;

import net.minecraft.text.Text;

import net.minecraft.util.Identifier;

import java.util.function.IntConsumer;



/** A horizontal rainbow fading to white, with a separate brightness strip. */

public class ColorPickerScreen extends Screen {

 public Runnable dismiss;
 private final Screen parent;private final IntConsumer apply;private final int original;

 private float hue,saturation,value;private int zone=-1,x,y;private Identifier id;

 public int uploads;

 public ColorPickerScreen(Screen parent,String title,int color,IntConsumer apply){super(Text.literal(title));this.parent=parent;this.original=color;this.apply=apply;float[] hsv=java.awt.Color.RGBtoHSB(color>>16&255,color>>8&255,color&255,null);hue=hsv[0];saturation=hsv[1];value=hsv[2];}

 @Override protected void init(){x=(width-390)/2;y=(height-356)/2;addDrawableChild(new Soft(x+20,y+306,165,"Cancel",this::close));addDrawableChild(new Soft(x+205,y+306,165,"Apply",()->{apply.accept(selectedColor());close();}));}

 public int selectedColor(){return java.awt.Color.HSBtoRGB(hue,saturation,value)&0xffffff;}

 private void texture(){if(id!=null)return;NativeImage image=new NativeImage(272,128,false);for(int py=0;py<128;py++){for(int px=0;px<256;px++)image.setColorArgb(px,py,java.awt.Color.HSBtoRGB(px/255f,1-py/127f,1));int b=Math.round(255*(1-py/127f));for(int px=256;px<272;px++)image.setColorArgb(px,py,0xff000000|b<<16|b<<8|b);}id=Identifier.of("dwmirlib","spectrum/"+java.util.UUID.randomUUID());client.getTextureManager().registerTexture(id,new NativeImageBackedTexture(()->"Shield color spectrum",image));uploads++;}

 @Override public void renderBackground(DrawContext c,int mx,int my,float dt){}

 @Override public void render(DrawContext c,int mx,int my,float dt){texture();GlassUi.round(c,x,y,390,356,14,0xb01d1d25);c.drawText(textRenderer,textRenderer.trimToWidth(getTitle().getString(),350),x+20,y+18,GlassUi.TEXT,false);c.drawText(textRenderer,"Choose a color · Brightness on the right",x+20,y+38,GlassUi.MUTED,false);

  int b=Math.round(value*255),tint=0xff000000|b<<16|b<<8|b;c.drawTexture(RenderPipelines.GUI_TEXTURED,id,x+20,y+63,0,0,308,190,256,128,272,128,tint);

  int base=java.awt.Color.HSBtoRGB(hue,saturation,1);c.drawTexture(RenderPipelines.GUI_TEXTURED,id,x+346,y+63,256,0,24,190,16,128,272,128,base);

  c.drawStrokedRectangle(x+17+Math.round(hue*308),y+60+Math.round((1-saturation)*190),7,7,0xffffffff);c.drawStrokedRectangle(x+343,y+60+Math.round((1-value)*190),30,7,0xffffffff);

  GlassUi.rawRound(c,x+20,y+270,94,22,5,0xff000000|original);GlassUi.rawRound(c,x+126,y+270,244,22,5,0xff000000|selectedColor());int rgb=selectedColor();double lum=((rgb>>16&255)*.2126+(rgb>>8&255)*.7152+(rgb&255)*.0722);GlassUi.label(c,String.format(java.util.Locale.ROOT,"#%06X",rgb),x+126,y+270,244,22,lum>150?0xff16161a:0xffffffff);super.render(c,mx,my,dt);

 }

 private boolean update(double mx,double my){if(zone==0){hue=(float)Math.clamp((mx-x-20)/308,0,1);saturation=1-(float)Math.clamp((my-y-63)/190,0,1);}else if(zone==1)value=1-(float)Math.clamp((my-y-63)/190,0,1);return zone>=0;}

 @Override public boolean mouseClicked(Click click,boolean doubled){if(super.mouseClicked(click,doubled))return true;if(click.button()!=0)return false;if(click.y()>=y+63&&click.y()<=y+253){if(click.x()>=x+20&&click.x()<=x+328)zone=0;else if(click.x()>=x+346&&click.x()<=x+370)zone=1;}return update(click.x(),click.y());}

 @Override public boolean mouseDragged(Click click,double dx,double dy){return zone>=0?update(click.x(),click.y()):super.mouseDragged(click,dx,dy);}

 @Override public boolean mouseReleased(Click click){if(zone>=0){zone=-1;return true;}return super.mouseReleased(click);}

 @Override public void close(){if(dismiss!=null)dismiss.run();else client.setScreen(parent);}

 @Override public void removed(){if(id!=null){client.getTextureManager().destroyTexture(id);id=null;}}

 @Override public boolean shouldPause(){return false;}

 private final class Soft extends ButtonWidget {

  Soft(int x,int y,int w,String label,Runnable task){super(x,y,w,28,net.minecraft.text.Text.literal(label),b->{GlassUi.press(b);task.run();},DEFAULT_NARRATION_SUPPLIER);}

  @Override protected void drawIcon(DrawContext c,int mx,int my,float dt){GlassUi.button(c,this,getX(),getY(),getWidth(),getHeight(),isHovered(),false);GlassUi.label(c,getMessage().getString(),getX()+10,getY(),getWidth()-20,getHeight(),GlassUi.TEXT);}

 }

}

