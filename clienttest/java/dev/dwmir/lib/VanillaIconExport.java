package dev.dwmir.lib;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.*;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.gametest.v1.*;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import java.nio.*;
import java.nio.file.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
public final class VanillaIconExport implements FabricClientGameTest {
 private static Object field(Object target,String name){try{var f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}catch(Exception e){throw new AssertionError(e);}}
 public void runTest(ClientGameTestContext t){
  t.getInput().resizeWindow(768,768);t.runOnClient(c->{c.options.getGuiScale().setValue(1);c.onResolutionChanged();});
  String[] ids={"playerglowing","betterhighlighter"};Item[] items={Items.PLAYER_HEAD,Items.ENCHANTED_BOOK};
  for(int i=0;i<ids.length;i++){final String id=ids[i];final Item item=items[i];t.setScreen(()->new Capture(item));t.waitTicks(10);
   t.runOnClient(c->{var renderer=field(c.gameRenderer,"guiRenderer");var texture=(GlTexture)field(renderer,"itemAtlasTexture");var cache=(java.util.Map<?,?>)field(renderer,"renderedItems");Object tile=cache.values().stream().max(java.util.Comparator.comparingInt(o->(int)field(o,"frame"))).orElseThrow();int x=(int)field(tile,"x"),y=(int)field(tile,"y"),size=1024;ByteBuffer bytes=MemoryUtil.memAlloc(size*size*4).order(ByteOrder.nativeOrder());
    try(var image=new NativeImage(size,size,false)){
     GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER,0);GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,4);GL45.glGetTextureSubImage(texture.getGlId(),0,x,y,0,size,size,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,bytes);
     int error=GL11.glGetError();if(error!=GL11.GL_NO_ERROR)throw new AssertionError("GPU export error "+error);
     int transparent=0;for(int py=0;py<size;py++)for(int px=0;px<size;px++){int pixel=bytes.getInt(((size-1-py)*size+px)*4);image.setColor(px,py,pixel);if((pixel>>>24)==0)transparent++;}
     if(transparent<100000)throw new AssertionError("Atlas transparency missing");Path file=Path.of("../../native-icons-r6/"+id+".png").toAbsolutePath().normalize();Files.createDirectories(file.getParent());image.writeTo(file);System.out.println("DWMIR_NATIVE_EXPORT "+id+"=1024x1024 TILE="+x+","+y+" ALPHA_ZERO="+transparent+" FILE="+file);
    }catch(java.io.IOException e){throw new AssertionError(e);}finally{MemoryUtil.memFree(bytes);}
   });
  }
  t.setScreen(()->null);System.out.println("DWMIR_VANILLA_EXPORT_PASS");
 }
 private static final class Capture extends Screen {
  private final Item item;Capture(Item item){super(Text.literal("Shield icon capture"));this.item=item;}
  @Override public void renderBackground(DrawContext c,int x,int y,float dt){}
  @Override public void render(DrawContext c,int x,int y,float dt){c.fill(0,0,width,height,0xffeeeaf2);c.getMatrices().pushMatrix();c.getMatrices().translate(width/2f,height/2f);c.getMatrices().scale(32,32);c.drawItem(new ItemStack(item),-8,-8);c.getMatrices().popMatrix();}
 }
}
