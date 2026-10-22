package dev.dwmir.lib.mixin;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(DrawContext.class)
public abstract class ThemeTextMixin {
 @ModifyVariable(method="drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)V",at=@At("HEAD"),argsOnly=true,ordinal=2)
 private int dwmir$ink(int color){return dev.dwmir.lib.api.GlassUi.textColor(color);}
}
