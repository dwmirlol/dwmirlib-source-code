package dev.dwmir.lib.mixin;
import dev.dwmir.lib.api.GlassContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(DrawContext.class)
public abstract class GlassContextMixin implements GlassContext {
 @Shadow @Final private GuiRenderState state;
 @Unique private final java.util.Deque<ScreenRect> dwmir$clips=new java.util.ArrayDeque<>();
 public GuiRenderState dwmir$state(){return state;}
 public ScreenRect dwmir$clip(){return dwmir$clips.peek();}
 @Inject(method="enableScissor",at=@At("RETURN")) private void dwmir$push(int x,int y,int x2,int y2,CallbackInfo ci){var c=(DrawContext)(Object)this;ScreenRect clip=new ScreenRect(x,y,Math.max(0,x2-x),Math.max(0,y2-y)).transformEachVertex(c.getMatrices());if(!dwmir$clips.isEmpty()){clip=clip.intersection(dwmir$clips.peek());if(clip==null)clip=ScreenRect.empty();}dwmir$clips.push(clip);}
 @Inject(method="disableScissor",at=@At("RETURN")) private void dwmir$pop(CallbackInfo ci){if(!dwmir$clips.isEmpty())dwmir$clips.pop();}
}
