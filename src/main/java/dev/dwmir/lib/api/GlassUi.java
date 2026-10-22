package dev.dwmir.lib.api;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import dev.dwmir.lib.LibraryState;
public final class GlassUi {
 public static final int PANEL=0x88111114,CARD=0x26ffffff,ROW=0x20ffffff,TEXT=0xfff5f5f5,MUTED=0xffaaaab0,ACCENT=0xffffffff;
 private static final Identifier[] CORNERS=new Identifier[25];
 public static final Identifier SKIN=Identifier.of("dwmirlib","textures/creator_skin.png");
 static {for(int i=1;i<CORNERS.length;i++)CORNERS[i]=Identifier.of("dwmirlib","round_"+i);}
 public static boolean drawing;
 private GlassUi(){}
 public static void image(DrawContext c,Identifier texture,int x,int y,int w,int h,int tint){var access=(GlassContext)c;var view=MinecraftClient.getInstance().getTextureManager().getTexture(texture).getGlTextureView();var setup=net.minecraft.client.texture.TextureSetup.of(view,com.mojang.blaze3d.systems.RenderSystem.getSamplerCache().get(com.mojang.blaze3d.textures.FilterMode.LINEAR));access.dwmir$state().addSimpleElement(new net.minecraft.client.gui.render.state.TexturedQuadGuiElementRenderState(RenderPipelines.GUI_TEXTURED,setup,new org.joml.Matrix3x2f(c.getMatrices()),x,y,x+w,y+h,0,1,0,1,tint,access.dwmir$clip()));}
 private static final java.util.Map<Object,Long> PRESSES=new java.util.WeakHashMap<>();
 private static final java.util.Map<Object,double[]> MOTION=new java.util.WeakHashMap<>();
 public static void press(Object key){PRESSES.put(key,System.nanoTime());}
 public static float pulse(Object key){Long t=PRESSES.get(key);if(t==null)return 0;float age=(System.nanoTime()-t)/220000000f;return age>=1?0:(float)Math.sin(age*Math.PI);}
 public static double animate(Object key,double target){long now=System.nanoTime();double[] s=MOTION.computeIfAbsent(key,k->new double[]{target,now});double dt=Math.clamp((now-s[1])/1e9,0,.1);s[0]+=(target-s[0])*(1-Math.exp(-dt*22));s[1]=now;return s[0];}
 public static void button(DrawContext c,Object key,int x,int y,int w,int h,boolean over,boolean selected){float hover=hover(key,over),pressed=pulse(key);int inset=Math.round(pressed*2);if(selected){int rgb=LibraryState.theme==LibraryState.Theme.QUARTZ?0x887a99:accent()&0xffffff;rawRound(c,x-3,y-3,w+6,h+6,h/2+3,0x0c000000|rgb);rawRound(c,x-1,y-1,w+2,h+2,h/2+1,0x24000000|rgb);}round(c,x+inset,y+inset,w-inset*2,h-inset*2,h/2,((int)(selected?65:24+hover*24+pressed*30)<<24)|0xffffff);}
 /** Draw a native-font label within its own content rectangle. */
 public static void label(DrawContext c,String text,int x,int y,int width,int height,int color){var tr=MinecraftClient.getInstance().textRenderer;String shown=tr.trimToWidth(text,Math.max(0,width));c.drawText(tr,shown,x+(width-tr.getWidth(shown))/2,y+(height-tr.fontHeight)/2,color,false);}
 public static void slider(DrawContext c,Object key,int x,int y,int width,double value,boolean over){value=Double.isFinite(value)?Math.clamp(value,0,1):0;float activity=hover(key,over);int thumb=10+Math.round(activity*2);rawRound(c,x,y,width,3,1,LibraryState.theme==LibraryState.Theme.QUARTZ?0x30484255:0x40ffffff);rawRound(c,x,y,Math.max(1,(int)(width*value)),3,1,accent());int cx=x+(int)(width*value);rawRound(c,cx-thumb/2-2,y-(thumb-3)/2-2,width>0?thumb+4:0,thumb+4,8,((int)(activity*30)<<24)|(accent()&0xffffff));rawRound(c,cx-thumb/2,y-(thumb-3)/2,thumb,thumb,6,LibraryState.theme==LibraryState.Theme.QUARTZ?0xff6a6178:0xfff4f1f8);}
 public static int textColor(int color){if(!drawing||LibraryState.theme!=LibraryState.Theme.QUARTZ)return color;int r=color>>16&255,g=color>>8&255,b=color&255;if(Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))<40&&r>90){int v=r>220?42:85;return (color&0xff000000)|v<<16|v<<8|v;}return color;}

 private static final java.util.Map<Object,float[]> HOVERS=new java.util.WeakHashMap<>();
 public static float hover(Object key,boolean active){float now=(System.nanoTime()%1000000000000L)/1000000f;float[] state=HOVERS.computeIfAbsent(key,k->new float[]{0,now});float dt=Math.max(0,Math.min(.1f,(now-state[1])/1000f));state[0]+=( (active?1:0)-state[0])*(1-(float)Math.exp(-dt*18));state[1]=now;return state[0];}
 public static void click(){var c=MinecraftClient.getInstance();c.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK,1f));}
 public static void playerHead(DrawContext c,int x,int y,int size){var mc=MinecraftClient.getInstance();var skin=mc.player!=null?mc.player.getSkin():net.minecraft.client.util.DefaultSkinHelper.getSkinTextures(mc.getSession().getUuidOrNull());Identifier texture=skin.body().texturePath();c.drawTexture(RenderPipelines.GUI_TEXTURED,texture,x,y,8,8,size,size,8,8,64,64);c.drawTexture(RenderPipelines.GUI_TEXTURED,texture,x,y,40,8,size,size,8,8,64,64);}
 public static void tooltip(DrawContext c,String text,int mx,int my,int width,int height){if(!LibraryState.tooltips)return;var tr=MinecraftClient.getInstance().textRenderer;var lines=tr.wrapLines(net.minecraft.text.Text.literal(text),230);if(lines.isEmpty())return;int tw=lines.stream().mapToInt(tr::getWidth).max().orElse(0),th=lines.size()*11;int x=Math.max(4,Math.min(mx+10,width-tw-17)),y=Math.max(4,Math.min(my+13,height-th-15));rawRound(c,x-5,y-5,tw+10,th+8,5,LibraryState.theme==LibraryState.Theme.QUARTZ?0xf0f7f3fc:0xf0202029);for(int i=0;i<lines.size();i++)c.drawText(tr,lines.get(i),x,y+i*11,0xffeeeef3,false);}
 private static final Identifier GLOW=Identifier.of("dwmirlib","pointer-glow");private static boolean glowReady;
 public static void glow(DrawContext c,int x,int y){if(!glowReady){var img=new net.minecraft.client.texture.NativeImage(64,64,false);for(int py=0;py<64;py++)for(int px=0;px<64;px++){double r=Math.hypot(px-31.5,py-31.5)/32;int a=(int)(Math.pow(Math.max(0,1-r),2)*19);img.setColorArgb(px,py,a<<24|0xffffff);}MinecraftClient.getInstance().getTextureManager().registerTexture(GLOW,new net.minecraft.client.texture.NativeImageBackedTexture(()->"dwmir cursor light",img));glowReady=true;}c.drawTexture(RenderPipelines.GUI_TEXTURED,GLOW,x-100,y-100,0,0,200,200,64,64,64,64);}
 private static final Identifier PETAL=Identifier.of("dwmirlib","sakura-petal");private static boolean petalReady;
 private static void petalTexture(){if(petalReady)return;var image=new net.minecraft.client.texture.NativeImage(64,64,false);for(int y=0;y<64;y++)for(int x=0;x<64;x++){double u=(x-31.5)/27,v=(y-31.5)/28;double shape=Math.sqrt(Math.pow(u/(.72+.12*v),2)+v*v);double notch=Math.exp(-u*u*180)*Math.max(0,-v-.55)*.55;double alpha=Math.clamp((1-shape-notch)*18,0,1);double edge=Math.clamp(shape,0,1),vein=Math.exp(-u*u*220)*.1;int red=255,green=(int)(224-edge*41-vein*28),blue=(int)(237-edge*22-vein*12);image.setColorArgb(x,y,(int)(alpha*235)<<24|red<<16|green<<8|blue);}MinecraftClient.getInstance().getTextureManager().registerTexture(PETAL,new net.minecraft.client.texture.NativeImageBackedTexture(()->"Sakura petals",image));petalReady=true;}
 public static void socialIcon(DrawContext c,String kind,int x,int y,int size){if(!java.util.Set.of("youtube","discord","tiktok","github","modrinth").contains(kind))return;image(c,Identifier.of("dwmirlib","textures/social/"+kind+".png"),x,y,size,size,LibraryState.theme==LibraryState.Theme.QUARTZ?0xff383642:0xffeeeef4);}

 public static int accent(){return switch(LibraryState.theme){case INFERNO->0xffff7766;case SAKURA->0xffffb4d1;case FROST->0xffa8d8ff;case QUARTZ->0xffd6d2df;default->0xffffffff;};}
 private static int themed(int color){int a=color>>>24,r=color>>16&255,g=color>>8&255,b=color&255;if(LibraryState.theme==LibraryState.Theme.ONYX||a>=220||a<10||r<12||Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))>14)return color;int rgb=r<60?switch(LibraryState.theme){case INFERNO->0x30100f;case SAKURA->0x301b29;case FROST->0x142331;case QUARTZ->0xeceaf1;default->0x111114;}:accent()&0xffffff;return a<<24|rgb;}
 public static void round(DrawContext c,int x,int y,int w,int h,int r,int color){int styled=themed(color),alpha=styled>>>24;int radius=h>=300?32:Math.max(r,Math.min(22,h/2));if(alpha>=12&&GlassRenderer.draw(c,x,y,w,h,radius,styled))return;rawRound(c,x,y,w,h,Math.min(24,radius),styled);}
 public static void rawRound(DrawContext c,int x,int y,int w,int h,int r,int color){if(w<=0||h<=0||(color>>>24)==0)return;if(r<=0||Math.min(w,h)<4){c.fill(x,y,x+w,y+h,color);return;}r=Math.max(1,Math.min(24,Math.min(r,Math.min(w,h)/2-1)));c.drawGuiTexture(RenderPipelines.GUI_TEXTURED,CORNERS[r],x,y,w,h,color);}
 public static void head(DrawContext c,int x,int y,int size){c.drawTexture(RenderPipelines.GUI_TEXTURED,SKIN,x,y,8,8,size,size,8,8,64,64);c.drawTexture(RenderPipelines.GUI_TEXTURED,SKIN,x,y,40,8,size,size,8,8,64,64);}
 public static void moduleIcon(DrawContext c,String id,int x,int y,int size){if(id.equals("dwmirlib")){head(c,x,y,size);return;}if(!id.equals("native-preview")){String ns=switch(id){case "customcrossbowsounds","shieldpulse","inventoryscalefix","sprinter","countit","playerglowing","betterhighlighter"->id;default->"dwmirlib";};image(c,Identifier.of(ns,"icon.png"),x,y,size,size,0xffffffff);return;}Item item=switch(id){case "customcrossbowsounds"->Items.CROSSBOW;case "shieldpulse"->Items.SHIELD;case "inventoryscalefix"->Items.CHEST;default->Items.BOOK;};c.getMatrices().pushMatrix();c.getMatrices().translate(x,y);c.getMatrices().scale(size/16f,size/16f);c.drawItem(new ItemStack(item),0,0);c.getMatrices().popMatrix();}
 public static void backdrop(DrawContext c,int w,int h){
  int top=switch(LibraryState.theme){case INFERNO->0x85300907;case SAKURA->0x852d1727;case FROST->0x85112432;case QUARTZ->0xc9f4f1f8;default->0x450a0a0c;};c.fillGradient(0,0,w,h,top,LibraryState.theme==LibraryState.Theme.QUARTZ?0xb8e6e2ed:0xb009090d);ThemeEffects.draw(c,w,h);
  if(LibraryState.theme==LibraryState.Theme.SAKURA){
   boolean sakura=LibraryState.theme==LibraryState.Theme.SAKURA;Identifier sprite=Identifier.ofVanilla(sakura?"textures/particle/cherry_0.png":"textures/particle/flame.png");double t=System.nanoTime()/1e9;
   for(int i=0;i<18;i++){double speed=sakura?8:10;float xx=(float)((i*97+Math.sin(t*.45+i)*22+w)%w),yy=(float)((i*61+(sakura?t:-t)*speed+h*100)%h);
    if(sakura){petalTexture();c.getMatrices().pushMatrix();c.getMatrices().translate(xx,yy);c.getMatrices().rotate((float)(t*.35+i));c.getMatrices().scale((float)(.7+Math.abs(Math.sin(t*.7+i))*.5),1);image(c,PETAL,-7,-7,14,14,0x95ffffff);c.getMatrices().popMatrix();}
    else c.drawTexture(RenderPipelines.GUI_TEXTURED,sprite,(int)xx,(int)yy,0,0,5,5,16,16,0x60ffffff);
   }
  }
 }
}
