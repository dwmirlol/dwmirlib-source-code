package dev.dwmir.lib.api;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.gl.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.*;

/** One GPU scene copy per frame; each panel is one rounded, refractive quad. */
public final class GlassRenderer {
 private static GpuTexture scene;private static GpuTextureView view;private static boolean available,disabled;
 public static long captures,panels;
 private static final RenderPipeline PIPELINE=dev.dwmir.lib.mixin.PipelineRegistry.dwmir$register(RenderPipeline.builder()
  .withLocation(Identifier.of("dwmirlib","pipeline/glass"))
  .withVertexShader(Identifier.of("dwmirlib","core/glass"))
  .withFragmentShader(Identifier.of("dwmirlib","core/glass"))
  .withUniform("DynamicTransforms",UniformType.UNIFORM_BUFFER).withUniform("Projection",UniformType.UNIFORM_BUFFER)
  .withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT)
  .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false)
  .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,VertexFormat.DrawMode.QUADS).build());
 public static void capture(){available=false;if(disabled)return;try{var framebuffer=MinecraftClient.getInstance().getFramebuffer();var source=framebuffer.getColorAttachment();if(source==null)return;int w=source.getWidth(0),h=source.getHeight(0);if(w<1||h<1)return;if(scene==null||scene.getWidth(0)!=w||scene.getHeight(0)!=h){close();scene=RenderSystem.getDevice().createTexture("dwmirLib glass scene",GpuTexture.USAGE_TEXTURE_BINDING|GpuTexture.USAGE_COPY_DST,source.getFormat(),w,h,1,1);view=RenderSystem.getDevice().createTextureView(scene);}RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(source,scene,0,0,0,0,0,w,h);captures++;available=true;}catch(RuntimeException e){disabled=true;org.slf4j.LoggerFactory.getLogger("dwmirlib").warn("Glass capture unavailable; using translucent surfaces",e);}}
 public static void end(){available=false;}
 public static void close(){available=false;if(view!=null){view.close();view=null;}if(scene!=null){scene.close();scene=null;}}
 public static boolean draw(DrawContext c,int x,int y,int w,int h,int radius,int color){if(!available||view==null||w<40||h<20)return false;var access=(GlassContext)c;var pose=new Matrix3x2f(c.getMatrices());var clip=access.dwmir$clip();var bounds=new ScreenRect(x,y,w,h).transformEachVertex(pose);if(clip!=null){bounds=bounds.intersection(clip);if(bounds==null)return true;}var texture=TextureSetup.of(view,RenderSystem.getSamplerCache().get(FilterMode.LINEAR));access.dwmir$state().addSimpleElement(new Panel(pose,x,y,w,h,radius,color,texture,clip,bounds));panels++;return true;}
 private record Panel(Matrix3x2f pose,int x,int y,int w,int h,int radius,int color,TextureSetup textureSetup,ScreenRect scissorArea,ScreenRect bounds) implements SimpleGuiElementRenderState {
  public RenderPipeline pipeline(){return PIPELINE;}
  private void vertex(VertexConsumer v,float xx,float yy,float u,float t){v.vertex(pose,xx,yy).color(color).texture(u,t).overlay(radius,switch(dev.dwmir.lib.LibraryState.theme){case QUARTZ->1;case SAKURA->2;case INFERNO->3;case FROST->4;default->0;}).light(w,h).normal(0,0,1);}
  public void setupVertices(VertexConsumer v){vertex(v,x,y,0,0);vertex(v,x,y+h,0,1);vertex(v,x+w,y+h,1,1);vertex(v,x+w,y,1,0);}
 }
}
