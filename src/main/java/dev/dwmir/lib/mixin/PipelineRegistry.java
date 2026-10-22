package dev.dwmir.lib.mixin;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(RenderPipelines.class)
public interface PipelineRegistry {
 @Invoker("register") static RenderPipeline dwmir$register(RenderPipeline pipeline){throw new AssertionError();}
}
