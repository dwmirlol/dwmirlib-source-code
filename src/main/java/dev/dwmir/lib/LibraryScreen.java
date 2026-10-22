package dev.dwmir.lib;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.*;

/** A translucent collection view; all entries come from the active loader. */
public final class LibraryScreen extends Screen {
    private static final int WHITE=0xfff5f5f5, MUTED=0xffaaaaaf, ACCENT=0xffffffff;
    private final Screen parent;
    private final List<Card> cards=new ArrayList<>();
    private List<ModContainer> modules=List.of();private List<ModContainer> visibleModules=List.of();
    private TextFieldWidget search;
    private QuietButton previous,next;
    private int x,y,w,h,rail,bodyX,bodyW,columns,rows,offset,contentTop,cardHeight;
    private String query="",error="";
    private boolean configurable;
    private static final net.minecraft.text.Style UI_STYLE=net.minecraft.text.Style.EMPTY;
    private static net.minecraft.text.MutableText label(String value){return net.minecraft.text.Text.literal(value).setStyle(UI_STYLE);}
    private static net.minecraft.text.MutableText tr(String key,Object... args){return net.minecraft.text.Text.translatable(key,args).setStyle(UI_STYLE);}
    public LibraryScreen(Screen parent){super(tr("dwmirlib.title"));this.parent=parent;}
    @Override protected void init(){
        w=Math.min(780,width-32);h=Math.min(460,height-32);x=(width-w)/2;y=(height-h)/2;
        rail=w>=540?146:62;bodyX=x+rail+24;bodyW=w-rail-48;
        columns=bodyW>=360?2:1;contentTop=h<270?99:133;cardHeight=Math.min(82,Math.max(48,h-contentTop-58));rows=Math.max(1,(h-47-contentTop+12)/(cardHeight+12));
        modules=FabricLoader.getInstance().getAllMods().stream()
            .filter(m->!m.getMetadata().getId().equals("dwmirlib") &&
                (DwmirLib.settings(m.getMetadata().getId())!=null || m.getMetadata().getDependencies().stream().anyMatch(d->d.getModId().equals("dwmirlib"))))
            .sorted(Comparator.comparing(m->m.getMetadata().getName(),String.CASE_INSENSITIVE_ORDER)).toList();
        search=addDrawableChild(new TextFieldWidget(textRenderer,bodyX+27,y+(h<270?63:91),bodyW-39,18,tr("dwmirlib.search")));
        search.setDrawsBackground(false);search.setTextShadow(false);search.addFormatter((value,start)->label(value).asOrderedText());search.setEditableColor(WHITE);search.setUneditableColor(MUTED);
        search.setMaxLength(80);search.setText(query);search.setPlaceholder(tr("dwmirlib.search").styled(style->style.withColor(LibraryState.theme==LibraryState.Theme.QUARTZ?0x665e70:0xbdb6c6)));
        search.setChangedListener(value->{query=value;offset=0;refresh();});
        addDrawableChild(new QuietButton(x+12,y+90,rail-24,30,tr(rail>100?"dwmirlib.collection":"dwmirlib.all"),()->{configurable=false;offset=0;refresh();},1));

        addDrawableChild(new QuietButton(x+12,y+126,rail-24,27,label("Profiles"),()->client.setScreen(new ProfilesScreen(this)),0));
        addDrawableChild(new QuietButton(x+w-42,y+13,27,26,label("x"),this::close,0));
        previous=addDrawableChild(new QuietButton(bodyX+bodyW-62,y+h-37,28,23,label("<"),()->{offset=Math.max(0,offset-capacity());refresh();},0));
        next=addDrawableChild(new QuietButton(bodyX+bodyW-28,y+h-37,28,23,label(">"),()->{offset+=capacity();refresh();},0));
        for(int i=0;i<LibraryState.Theme.values().length;i++){final LibraryState.Theme theme=LibraryState.Theme.values()[i];QuietButton b=new QuietButton(x+w-323+i*55,y+18,50,23,label(new String[]{"Onyx","Inferno","Sakura","Frost","Quartz"}[i]),()->{LibraryState.select(theme);search.setPlaceholder(tr("dwmirlib.search").styled(style->style.withColor(theme==LibraryState.Theme.QUARTZ?0x665e70:0xbdb6c6)));},0);b.themeChoice=theme;addDrawableChild(b);}
        if(rail>100){String[] labels={"YouTube","TikTok","Discord","GitHub","Modrinth"},keys={"youtube","tiktok","discord","github","modrinth"};for(int i=0;i<labels.length;i++){final String key=keys[i];addDrawableChild(new QuietButton(x+12,y+188+i*26,rail-24,24,label(labels[i]),()->Socials.open(key),0));}}
        refresh();
    }
    private int capacity(){return rows*columns;}
    private List<ModContainer> filtered(){String q=query.toLowerCase(Locale.ROOT);return modules.stream()
        .filter(m->!configurable||DwmirLib.settings(m.getMetadata().getId())!=null)
        .filter(m->(m.getMetadata().getName()+" "+m.getMetadata().getId()).toLowerCase(Locale.ROOT).contains(q)).toList();}
    private void refresh(){
        for(var card:cards)remove(card);cards.clear();var list=filtered();visibleModules=list;
        offset=Math.min(offset,list.isEmpty()?0:((list.size()-1)/capacity())*capacity());
        int cw=(bodyW-(columns-1)*12)/columns;
        for(int i=offset;i<Math.min(list.size(),offset+capacity());i++){
            int n=i-offset;cards.add(addDrawableChild(new Card(bodyX+(n%columns)*(cw+12),y+contentTop+(n/columns)*(cardHeight+12),cw,cardHeight,list.get(i))));
        }
        previous.active=offset>0;next.active=offset+capacity()<list.size();
    }
    @Override public void renderBackground(DrawContext c,int mx,int my,float dt){
        if(client.world==null)renderPanoramaBackground(c,dt);
        // Preserve the world/panorama instead of replacing it with an opaque screen.
        c.fillGradient(0,0,width,height,0x450a0a0c,0x850a0a0c);
    }
    @Override public void render(DrawContext c,int mx,int my,float dt){
        // Soft outer shadow, hairline rim, then two layers of tinted glass.
        for(int i=6;i>0;i--)round(c,x-i,y-i,w+2*i,h+2*i,17+i,0x04000000);
        round(c,x,y,w,h,17,0x38ffffff);round(c,x+1,y+1,w-2,h-2,16,0x88111114);
        round(c,x+1,y+1,rail-1,h-2,16,0x480b0b0e);
        c.fill(x+rail,y+18,x+rail+1,y+h-18,0x16ffffff);
        c.getMatrices().pushMatrix();c.getMatrices().translate(x+(rail>100?20:11),y+25);float brandScale=1f;c.getMatrices().scale(brandScale,brandScale);
        c.drawText(textRenderer,label("dwmirLib"),0,0,WHITE,false);c.getMatrices().popMatrix();
        if(rail>100){dev.dwmir.lib.api.GlassUi.playerHead(c,x+20,y+49,26);c.drawText(textRenderer,textRenderer.trimToWidth(client.getSession().getUsername(),82),x+53,y+58,0xffd4d4dc,false);
            if(h>=390){round(c,x+16,y+h-68,rail-32,48,9,0x12ffffff);
            c.drawText(textRenderer,label("Fabric 1.21.11"),x+26,y+h-54,0xffc2c2c6,false);
            c.drawText(textRenderer,label("dwmirLib 1.0.0"),x+26,y+h-37,0xff85858b,false);}
        }
        if(rail>100)c.drawText(textRenderer,label("SOCIALS"),x+20,y+165,MUTED,false);
        if(h>=270)c.drawText(textRenderer,tr("dwmirlib.eyebrow"),bodyX,y+22,ACCENT,false);
        c.getMatrices().pushMatrix();c.getMatrices().translate(bodyX,y+(h<270?23:40));c.getMatrices().scale(1f,1f);
        c.drawText(textRenderer,tr(configurable?"dwmirlib.configurable":"dwmirlib.collection"),0,0,WHITE,false);c.getMatrices().popMatrix();
        if(h>=270)c.drawText(textRenderer,tr("dwmirlib.subtitle"),bodyX,y+65,MUTED,false);
        round(c,bodyX,y+(h<270?57:85),bodyW,30,8,search.isFocused()?0x48ffffff:0x24ffffff);
        round(c,bodyX+1,y+(h<270?58:86),bodyW-2,28,7,0x450d0d10);
        c.drawGuiTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,net.minecraft.util.Identifier.of("dwmirlib","search"),bodyX+7,y+(h<270?64:92),16,16,MUTED);
        super.render(c,mx,my,dt);
        if(visibleModules.isEmpty())empty(c);
        c.fill(bodyX,y+h-47,bodyX+bodyW,y+h-46,0x18ffffff);
        c.drawText(textRenderer,error.isEmpty()?tr("dwmirlib.count",visibleModules.size()):label(error),bodyX,y+h-29,error.isEmpty()?MUTED:0xffffa5a5,false);
        for(var card:cards)if(card.isHovered())dev.dwmir.lib.api.GlassUi.tooltip(c,card.mod.getMetadata().getDescription(),mx,my,width,height);
        if(!visibleModules.isEmpty())center(c,label((offset/capacity()+1)+" / "+Math.max(1,(visibleModules.size()+capacity()-1)/capacity())),bodyX+bodyW-94,y+h-29,MUTED);
    }
    private void empty(DrawContext c){
        int cx=bodyX+bodyW/2,cy=h<320?y+contentTop-(h<270?18:6):y+133+(h-184)/2;
        center(c,tr(query.isEmpty()?"dwmirlib.empty_title":"dwmirlib.no_results"),cx,cy+19,WHITE);
        var lines=textRenderer.wrapLines(tr(query.isEmpty()?"dwmirlib.empty":"dwmirlib.search_hint"),Math.min(280,bodyW-20));
        for(int i=0;i<lines.size();i++)center(c,lines.get(i),cx,cy+39+i*12,MUTED);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(mx>=bodyX&&mx<=bodyX+bodyW&&my>=y+contentTop-10&&my<y+h-47&&vertical!=0){
            offset+=vertical<0?capacity():-capacity();offset=Math.max(0,offset);refresh();return true;
        }return super.mouseScrolled(mx,my,horizontal,vertical);
    }
    static void round(DrawContext c,int x,int y,int w,int h,int r,int color){dev.dwmir.lib.api.GlassUi.round(c,x,y,w,h,r,color);}
    private void center(DrawContext c,net.minecraft.text.Text text,int x,int y,int color){c.drawText(textRenderer,text,x-textRenderer.getWidth(text)/2,y,color,false);}
    private void center(DrawContext c,net.minecraft.text.OrderedText text,int x,int y,int color){c.drawText(textRenderer,text,x-textRenderer.getWidth(text)/2,y,color,false);}
    @Override public void close(){client.setScreen(parent);}
    @Override public boolean shouldPause(){return false;}
    private final class QuietButton extends ButtonWidget {
        final int tab;float hoverAmount;long hoverTime=System.nanoTime();LibraryState.Theme themeChoice;
        QuietButton(int x,int y,int w,int h,net.minecraft.text.Text label,Runnable action,int tab){super(x,y,w,h,label,b->{dev.dwmir.lib.api.GlassUi.press(b);action.run();},DEFAULT_NARRATION_SUPPLIER);this.tab=tab;}
        @Override protected void drawIcon(DrawContext c,int mx,int my,float dt){
            boolean selected=tab==1&&!configurable||tab==2&&configurable||themeChoice!=null&&themeChoice==LibraryState.theme,hover=active&&(isHovered()||isFocused());
            long now=System.nanoTime();hoverAmount+=( (hover?1:0)-hoverAmount)*Math.min(1,(now-hoverTime)/85000000f);hoverTime=now;
            dev.dwmir.lib.api.GlassUi.button(c,this,getX(),getY(),getWidth(),getHeight(),hover,selected);
            String social=getMessage().getString().toLowerCase(Locale.ROOT);if(social.equals("youtube")||social.equals("discord")||social.equals("tiktok")||social.equals("github")||social.equals("modrinth"))dev.dwmir.lib.api.GlassUi.socialIcon(c,social,getX()+10,getY()+(getHeight()-12)/2,12);
            int color=active?(selected?ACCENT:WHITE):0xff64646b;
            boolean hasIcon=java.util.Set.of("youtube","tiktok","discord","github","modrinth").contains(social);dev.dwmir.lib.api.GlassUi.label(c,getMessage().getString(),getX()+(hasIcon?30:8),getY(),getWidth()-(hasIcon?38:16),getHeight(),color);
            if(isFocused())c.fill(getX()+8,getY()+getHeight()-3,getX()+getWidth()-8,getY()+getHeight()-2,ACCENT);
        }
    }
    private final class Card extends ButtonWidget {
        final ModContainer mod;
        float hover;
        long last=System.nanoTime();
        Card(int x,int y,int w,int h,ModContainer mod){super(x,y,w,h,label(mod.getMetadata().getName()),b->{},DEFAULT_NARRATION_SUPPLIER);this.mod=mod;
            active=DwmirLib.settings(mod.getMetadata().getId())!=null;
            
        }
        @Override public void onPress(net.minecraft.client.input.AbstractInput input){
            var factory=DwmirLib.settings(mod.getMetadata().getId());if(factory==null)return;
            try{var screen=factory.apply(LibraryScreen.this);if(screen!=null)client.setScreen(screen);else error=tr("dwmirlib.failed").getString();}
            catch(RuntimeException e){org.slf4j.LoggerFactory.getLogger("dwmirlib").error("Cannot open settings for {}",mod.getMetadata().getId(),e);error=tr("dwmirlib.failed").getString();}
        }
        @Override protected void drawIcon(DrawContext c,int mx,int my,float dt){
            long now=System.nanoTime();float step=Math.min(1,(now-last)/100000000f);last=now;hover+=((isHovered()||isFocused()?1:0)-hover)*step;
            int xx=getX(),yy=getY(),ww=getWidth();
            dev.dwmir.lib.api.GlassUi.button(c,this,xx,yy,ww,getHeight(),isHovered()||isFocused(),false);
            round(c,xx+1,yy+1,ww-2,getHeight()-2,9,0x48131316);

            String name=mod.getMetadata().getName();dev.dwmir.lib.api.GlassUi.moduleIcon(c,mod.getMetadata().getId(),xx+10,yy+10,52);
            c.drawText(textRenderer,label(textRenderer.trimToWidth(name,ww-91)),xx+73,yy+14,WHITE,false);
            if(getHeight()>=65)c.drawText(textRenderer,label(textRenderer.trimToWidth("v"+mod.getMetadata().getVersion().getFriendlyString(),ww-91)),xx+73,yy+29,MUTED,false);
            c.drawText(textRenderer,tr(active?"dwmirlib.open_settings":"dwmirlib.no_settings"),xx+73,yy+(getHeight()<65?32:getHeight()-21),active?ACCENT:MUTED,false);
            if(active)c.drawText(textRenderer,label(">"),xx+ww-20,yy+(getHeight()<65?32:getHeight()-21),ACCENT,false);
        }
    }
}


