package dev.dwmir.lib.api;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.*;
import net.minecraft.util.Identifier;
/** Two cached quads; saturation/value dragging never rebuilds or uploads the texture. */
public final class HsvTexture implements AutoCloseable {
 private final Identifier id=Identifier.of("dwmirlib","picker/"+java.util.UUID.randomUUID());
 private final NativeImageBackedTexture texture=new NativeImageBackedTexture("dwmirLib HSV",272,128,false);
 private int hueKey=-1;public int uploads;
 public HsvTexture(){MinecraftClient.getInstance().getTextureManager().registerTexture(id,texture);}
 public void draw(DrawContext c,float hue,int x,int y,int w,int h,int hueX){int key=Math.round(hue*360);if(key!=hueKey){NativeImage image=texture.getImage();for(int yy=0;yy<128;yy++){for(int xx=0;xx<256;xx++)image.setColorArgb(xx,yy,java.awt.Color.HSBtoRGB(key/360f,xx/255f,1-yy/127f));for(int xx=256;xx<272;xx++)image.setColorArgb(xx,yy,java.awt.Color.HSBtoRGB(yy/127f,1,1));}texture.upload();hueKey=key;uploads++;}c.drawTexture(RenderPipelines.GUI_TEXTURED,id,x,y,0,0,w,h,256,128,272,128);c.drawTexture(RenderPipelines.GUI_TEXTURED,id,hueX,y,256,0,16,h,16,128,272,128);}
 @Override public void close(){MinecraftClient.getInstance().getTextureManager().destroyTexture(id);}
}
