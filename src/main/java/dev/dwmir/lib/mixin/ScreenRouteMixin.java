package dev.dwmir.lib.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(MinecraftClient.class)
public abstract class ScreenRouteMixin {
 @Inject(method="close",at=@At("HEAD")) private void dwmirlib$releaseGlass(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){dev.dwmir.lib.api.GlassRenderer.close();}

 @Inject(method="tick",at=@At("TAIL"))
 private void dwmirlib$welcome(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){
  var c=(MinecraftClient)(Object)this;
  if(c.world!=null&&c.player!=null&&c.currentScreen==null&&!dev.dwmir.lib.LibraryState.accepted("library-onboarding-v2"))
   c.setScreen(new dev.dwmir.lib.WelcomeScreen(null,null,"library-onboarding-v2"));
 }

 @Inject(method="setScreen",at=@At("HEAD"),cancellable=true)
 private void dwmirlib$picker(Screen screen,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){if(screen instanceof dev.dwmir.lib.api.ColorPickerScreen p&&((MinecraftClient)(Object)this).currentScreen instanceof dev.dwmir.lib.api.PickerHost host){host.openPicker(p);ci.cancel();}}

 @ModifyVariable(method="setScreen",at=@At("HEAD"),argsOnly=true)
 private Screen dwmirlib$route(Screen screen){return dev.dwmir.lib.WorkspaceScreen.intercept(screen,((MinecraftClient)(Object)this).currentScreen);}
}
