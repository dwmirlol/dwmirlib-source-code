package dev.dwmir.lib;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import dev.dwmir.lib.api.GlassUi;
public final class WelcomeScreen extends Screen {
 private final Screen parent,target;private final String id;private final long started=System.nanoTime();
 private boolean report,ownership;private Action confirm;
 public WelcomeScreen(Screen parent,Screen target,String id){super(net.minecraft.text.Text.literal("dwmirLib"));this.parent=parent;this.target=target;this.id=id;}
 private String tr(String en,String tur){return client.options.language.startsWith("tr")?tur:en;}
 public int remainingSeconds(){return Math.max(0,5-(int)((System.nanoTime()-started)/1000000000L));}
 @Override protected void init(){int x=(width-650)/2,y=(height-410)/2;
  addDrawableChild(new Action(x+34,y+209,582,39,tr("I will report any bugs I find on Discord.","Bir bug bulursam Discord'a gelip raporlayacağım."),()->report=!report,1));
  addDrawableChild(new Action(x+34,y+256,582,39,tr("This mod belongs to dwmirlol.","Bu mod dwmirlol'e aittir."),()->ownership=!ownership,2));
  addDrawableChild(new Action(x+34,y+155,180,28,"YouTube",()->Socials.open("youtube"),0));addDrawableChild(new Action(x+235,y+155,180,28,"TikTok",()->Socials.open("tiktok"),0));addDrawableChild(new Action(x+436,y+155,180,28,"Discord",()->Socials.open("discord"),0));
  confirm=addDrawableChild(new Action(x+34,y+324,582,34,tr("Confirm","Onayla"),()->{if(remainingSeconds()==0&&report&&ownership){LibraryState.accept(id);client.setScreen(target);}},0));update();
 }
 private void update(){if(confirm!=null){confirm.active=remainingSeconds()==0&&report&&ownership;confirm.setMessage(net.minecraft.text.Text.literal(tr("Confirm","Onayla")+(remainingSeconds()>0?" ("+remainingSeconds()+"s)":"")));}}
 @Override public void tick(){update();}
 @Override public void render(DrawContext c,int mx,int my,float dt){update();int x=(width-650)/2,y=(height-410)/2;GlassUi.round(c,x,y,650,410,17,0x38ffffff);GlassUi.round(c,x+1,y+1,648,408,16,0xdd141418);
  GlassUi.playerHead(c,x+34,y+31,58);c.drawText(textRenderer,"dwmirLib / dwmirlol",x+111,y+34,GlassUi.accent(),false);String name=net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(id).map(m->m.getMetadata().getName()).orElse("dwmirLib");c.drawText(textRenderer,textRenderer.trimToWidth(name,490),x+111,y+56,0xffffffff,false);
  c.drawText(textRenderer,tr("Welcome. Make yourself at home.","Hoş geldin. Burada kendini evinde hisset."),x+34,y+111,0xffb8b8c1,false);super.render(c,mx,my,dt);
  c.drawText(textRenderer,tr("Read and acknowledge both statements to continue.","Devam etmek için iki kutucuğu da onayla."),x+34,y+377,0xff9999a2,false);
 }
 @Override public void close(){if(LibraryState.accepted(id))client.setScreen(parent);}
 @Override public boolean shouldPause(){return false;}
 private final class Action extends ButtonWidget {
  final int kind;Action(int x,int y,int w,int h,String title,Runnable action,int kind){super(x,y,w,h,net.minecraft.text.Text.literal(title),b->{GlassUi.press(b);action.run();},DEFAULT_NARRATION_SUPPLIER);this.kind=kind;}
  @Override protected void drawIcon(DrawContext c,int mx,int my,float dt){boolean checked=kind==1?report:kind==2&&ownership;GlassUi.button(c,this,getX(),getY(),getWidth(),getHeight(),active&&(isHovered()||isFocused()),checked);if(kind>0){GlassUi.round(c,getX()+13,getY()+12,15,15,4,checked?GlassUi.accent():0x50ffffff);if(checked)c.drawText(textRenderer,"+",getX()+18,getY()+16,0xff18181b,false);c.drawText(textRenderer,textRenderer.trimToWidth(getMessage().getString(),getWidth()-58),getX()+40,getY()+(getHeight()-textRenderer.fontHeight)/2,0xfff4f4f7,false);}else c.drawText(textRenderer,getMessage(),getX()+(getWidth()-textRenderer.getWidth(getMessage()))/2,getY()+(getHeight()-textRenderer.fontHeight)/2,active?0xfff5f5f7:0xff85858e,false);}
 }
}
