#version 150
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
out vec2 localUV;
out vec4 tint;
flat out vec2 panelSize;
flat out float radius;
flat out float lightTheme;
flat out float themeKind;
void main(){lightTheme=float(UV1.y==1);themeKind=float(UV1.y);gl_Position=ProjMat*ModelViewMat*vec4(Position,1.0);localUV=UV0;tint=Color;panelSize=vec2(UV2);radius=float(UV1.x);}
