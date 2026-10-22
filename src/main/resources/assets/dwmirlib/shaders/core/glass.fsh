#version 150
// Rounded SDF / rim-light approach informed by WalksyLib liquid_glass_gui.fsh (MIT).
// Compact independent implementation; see META-INF/licenses/WalksyLib-MIT.txt.
uniform sampler2D Sampler0;
in vec2 localUV;
in vec4 tint;
flat in vec2 panelSize;
flat in float radius;
flat in float lightTheme;
flat in float themeKind;
out vec4 fragColor;
void main(){
 vec2 halfSize=panelSize*.5;
 float r=min(radius,min(halfSize.x,halfSize.y));
 vec2 p=(localUV-.5)*panelSize,q=abs(p)-(halfSize-r);
 float distance=length(max(q,0.0))+min(max(q.x,q.y),0.0)-r;
 float aa=max(fwidth(distance),.5),mask=1.0-smoothstep(-aa,aa,distance);
 if(mask<.002)discard;
 vec2 n=normalize(max(q,0.0)+vec2(.0001))*sign(p);
 if(max(q.x,q.y)<0.0)n=q.x>q.y?vec2(sign(p.x),0):vec2(0,sign(p.y));
 vec2 texel=1.0/vec2(textureSize(Sampler0,0));
 float edge=exp(-abs(distance)/5.0);
 vec2 uv=gl_FragCoord.xy*texel+vec2(n.x,-n.y)*texel*edge*3.2;
 vec2 stepUV=texel*(panelSize.y>80.0?3.5:1.5);
 vec3 scene=texture(Sampler0,uv).rgb*.24;
 scene+=(texture(Sampler0,uv+vec2(stepUV.x,0)).rgb+texture(Sampler0,uv-vec2(stepUV.x,0)).rgb+texture(Sampler0,uv+vec2(0,stepUV.y)).rgb+texture(Sampler0,uv-vec2(0,stepUV.y)).rgb)*.12;
 scene+=(texture(Sampler0,uv+stepUV).rgb+texture(Sampler0,uv-stepUV).rgb+texture(Sampler0,uv+vec2(stepUV.x,-stepUV.y)).rgb+texture(Sampler0,uv+vec2(-stepUV.x,stepUV.y)).rgb)*.07;
 vec3 surface=lightTheme>.5?mix(vec3(.95,.94,.97),scene,.12):scene*.34+vec3(.025,.027,.035);
 if(themeKind==2.)surface=mix(surface,vec3(.20,.085,.15),.30);
 if(themeKind==3.)surface=mix(surface,vec3(.22,.045,.025),.28);
 if(themeKind==4.)surface=mix(surface,vec3(.06,.16,.22),.23);
 surface=mix(surface,tint.rgb,clamp(tint.a*.22,0.0,.20));
 float rim=exp(-abs(distance+.8)*1.8);
 float light=.25+.75*max(0.0,dot(n,normalize(vec2(-.45,-.85))));
 float sheen=pow(max(0.0,1.0-localUV.y),5.0)*.026;
 surface+=vec3(rim*light*(lightTheme>.5?.055:.22)+edge*.009+sheen*(lightTheme>.5?.2:1.));
 fragColor=vec4(surface,mask);
}
