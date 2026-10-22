package dev.dwmir.lib.api;
import net.minecraft.client.gui.DrawContext;import java.util.function.*;
public record HudElement(String name,IntSupplier x,IntSupplier y,BiConsumer<Integer,Integer> position,DoubleSupplier scale,DoubleConsumer resize,DoubleSupplier opacity,DoubleConsumer alpha,IntSupplier color,IntConsumer tint,int baseWidth,int baseHeight,Consumer<DrawContext> render) {
 public int width(){return Math.max(8,(int)Math.round(baseWidth*scale.getAsDouble()));}public int height(){return Math.max(8,(int)Math.round(baseHeight*scale.getAsDouble()));}
}
