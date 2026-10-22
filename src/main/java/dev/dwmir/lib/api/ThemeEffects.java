package dev.dwmir.lib.api;
import dev.dwmir.lib.LibraryState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
/** Cached botanical illustration; animation only transforms finished textures. */
public final class ThemeEffects {
 private static final Identifier BRANCH=Identifier.of("dwmirlib","sakura-branch");
 private static boolean ready;
 private static final Identifier ORB=Identifier.of("dwmirlib","theme-orb");private static boolean orbReady;
 private static void orb(){if(orbReady)return;var image=new NativeImage(64,64,false);for(int y=0;y<64;y++)for(int x=0;x<64;x++){double d=Math.hypot(x-31.5,y-31.5)/32.;int alpha=(int)(Math.pow(Math.max(0,1-d),2.5)*255);image.setColorArgb(x,y,alpha<<24|0xffffff);}MinecraftClient.getInstance().getTextureManager().registerTexture(ORB,new NativeImageBackedTexture(()->"Theme atmosphere",image));orbReady=true;}
 private static void pixel(NativeImage image,int x,int y,int rgb,double opacity){if(x<0||y<0||x>=384||y>=256)return;int old=image.getColorArgb(x,y);double a=Math.clamp(opacity,0,1),oa=(old>>>24)/255.,out=a+oa*(1-a);if(out<=0)return;int color=(int)(out*255)<<24;for(int shift:new int[]{16,8,0})color|=(int)(((rgb>>shift&255)*a+(old>>shift&255)*oa*(1-a))/out)<<shift;image.setColorArgb(x,y,color);}
 private static void stem(NativeImage image,double ax,double ay,double bx,double by,double width){int steps=(int)(Math.hypot(bx-ax,by-ay)*2);for(int s=0;s<=steps;s++){double t=s/(double)steps,cx=ax+(bx-ax)*t,cy=ay+(by-ay)*t,r=width*(1-.65*t);for(int y=(int)(cy-r-1);y<=cy+r+1;y++)for(int x=(int)(cx-r-1);x<=cx+r+1;x++)pixel(image,x,y,0x563641,Math.clamp(r+.5-Math.hypot(x-cx,y-cy),0,1)*.8);}}
 private static void flower(NativeImage image,int cx,int cy,double radius,double rotation){for(int y=(int)(cy-radius-2);y<=cy+radius+2;y++)for(int x=(int)(cx-radius-2);x<=cx+radius+2;x++){double dx=(x-cx)/radius,dy=(y-cy)/radius,d=Math.hypot(dx,dy),angle=Math.atan2(dy,dx)+rotation;double petal=.73+.24*Math.cos(angle*5)-.035*Math.cos(angle*10);double a=Math.clamp((petal-d)*radius,0,1);if(a<=0)continue;double shade=Math.clamp(d,0,1);int green=(int)(233-shade*47),blue=(int)(243-shade*26);pixel(image,x,y,0xff0000|green<<8|blue,a*.96);if(d<.23)pixel(image,x,y,0xca718e,(.23-d)*3);if(d>.12&&d<.38&&Math.sin(angle*10)>.92)pixel(image,x,y,0xc79065,.8);}}
 private static void prepare(){if(ready)return;var image=new NativeImage(384,256,false);stem(image,0,245,340,50,7);stem(image,53,212,153,30,4);stem(image,152,160,367,142,4);stem(image,225,111,253,5,3);stem(image,104,188,45,81,3);stem(image,281,85,370,15,2);
  int[][] points={{44,90},{70,125},{100,150},{131,70},{151,35},{154,98},{185,140},{212,129},{242,91},{252,24},{275,77},{302,63},{335,50},{366,22},{293,151},{332,145},{365,143},{85,210},{36,230},{177,167},{223,154},{120,185}};
  for(int i=0;i<points.length;i++)flower(image,points[i][0],points[i][1],9+i%4*2,i*.93);MinecraftClient.getInstance().getTextureManager().registerTexture(BRANCH,new NativeImageBackedTexture(()->"Sakura blossom branch",image));ready=true;
 }
 public static void draw(DrawContext c,int w,int h){if(LibraryState.theme==LibraryState.Theme.SAKURA){prepare();GlassUi.image(c,BRANCH,0,h-220,330,220,0xb8ffffff);c.getMatrices().pushMatrix();c.getMatrices().translate(w,0);c.getMatrices().scale(-1,-1);GlassUi.image(c,BRANCH,0,-180,270,180,0x85ffffff);c.getMatrices().popMatrix();}
  if(LibraryState.theme==LibraryState.Theme.INFERNO||LibraryState.theme==LibraryState.Theme.FROST){orb();double t=System.nanoTime()/1e9;boolean fire=LibraryState.theme==LibraryState.Theme.INFERNO;for(int i=0;i<16;i++){int x=(int)((i*139+Math.sin(t*.25+i)*30+w)%w),y=(int)((i*67+(fire?-t*12:t*3)+h*10000)%h);int size=fire?7+i%4:25+i%5*7;GlassUi.image(c,ORB,x,y,size,fire?size*2:size,fire?0x99ff9a58:0x25b4e4ff);}if(fire)GlassUi.image(c,ORB,-w/3,h-w/3,w,w,0x60ed4b26);}
  if(LibraryState.theme==LibraryState.Theme.QUARTZ){c.fillGradient(0,0,w,h/3,0x40ffffff,0x00ffffff);c.fillGradient(0,h*2/3,w,h,0x00d7cfe3,0x30d7cfe3);}
 }
}
