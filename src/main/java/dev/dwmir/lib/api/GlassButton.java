package dev.dwmir.lib.api;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
/** Shared native-font button for the unscaled HUD positioning editors. */
public final class GlassButton extends ButtonWidget {
 public GlassButton(int x,int y,int width,int height,net.minecraft.text.Text title,Runnable action){super(x,y,width,height,title,b->{GlassUi.press(b);action.run();},DEFAULT_NARRATION_SUPPLIER);}
 @Override protected void drawIcon(DrawContext c,int mx,int my,float dt){var tr=net.minecraft.client.MinecraftClient.getInstance().textRenderer;int alpha=(int)(38+GlassUi.hover(this,isHovered()||isFocused())*28);GlassUi.button(c,this,getX(),getY(),getWidth(),getHeight(),isHovered()||isFocused(),false);GlassUi.label(c,getMessage().getString(),getX()+10,getY(),getWidth()-20,getHeight(),active?GlassUi.TEXT:GlassUi.MUTED);}
}
