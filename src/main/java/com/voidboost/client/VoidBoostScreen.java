package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** VoidBoost settings screen matching the supplied reference artwork. */
public final class VoidBoostScreen extends Screen {
    private static final int BG = 0x99040A12, PANEL = 0xE60B1420, SIDE = 0xE30A121D;
    private static final int CARD = 0xD9141E2B, HOVER = 0xE51B2A38, BORDER = 0x663B5368;
    private static final int CYAN = 0xFF31D8FF, CYAN_DIM = 0x6631D8FF, TEXT = 0xFFF2F8FC, MUTED = 0xFF91A8B8;
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("voidboost", "textures/gui/voidboost_logo.png");
    private final Screen parent;
    private int page = 1;
    private boolean draggingRender, draggingSimulation, draggingFps;
    private int renderDistance = 12, simulationDistance = 8, fps = 240;

    public VoidBoostScreen(Screen parent) { super(Component.literal("VoidBoost")); this.parent = parent; }

    @Override protected void init() {
        VoidBoostConfig c = VoidBoostConfig.get();
        renderDistance = clamp(c.maxRenderDistance, 4, 32);
        fps = clamp(c.targetFps, 30, 1000);
        rebuildWidgets();
    }

    @Override protected void rebuildWidgets() {
        clearWidgets();
        int[] r = layout();
        int sx=r[0], sy=r[1], sw=r[2], mx=r[3], my=r[4], mw=r[5], h=r[6];
        nav(sx+12, sy+92, sw-24, "General", 0);
        nav(sx+12, sy+138, sw-24, "Performance", 1);
        nav(sx+12, sy+184, sw-24, "Visuals", 2);
        nav(sx+12, sy+230, sw-24, "PvP", 3);
        nav(sx+12, sy+276, sw-24, "HUD", 4);
        nav(sx+12, sy+322, sw-24, "Advanced", 5);
        if (page == 1) performance(mx, my, mw, h);
        else if (page == 0) general(mx, my, mw, h);
        else simplePage(mx, my, mw, h, page);
    }

    private int[] layout() {
        int shellW = Math.min(width - 64, 1432), shellX=(width-shellW)/2, shellY=52;
        int sideW = Math.min(306, Math.max(270, shellW/5));
        int mainX=shellX+sideW+20, mainW=shellW-sideW-20;
        return new int[]{shellX,shellY,sideW,mainX,shellY,mainW,Math.min(768,height-shellY-78)};
    }

    private void nav(int x,int y,int w,String label,int p) {
        addRenderableWidget(new NavButton(x,y,w,38,label,p==page,()->{page=p;rebuildWidgets();}));
    }

    private void performance(int x,int y,int w,int h) {
        int rowY=y+112;
        addRenderableWidget(new SliderRow(x,rowY,w,56,"Render Distance","How far you can see in the world.",renderDistance+" Chunks",4,32,renderDistance,v->{
            renderDistance=v; VoidBoostConfig.get().maxRenderDistance=v; VoidBoostConfig.get().markDirty();
        },v->{renderDistance=v;},0));
        rowY+=72;
        addRenderableWidget(new SliderRow(x,rowY,w,56,"Simulation Distance","How far game mechanics are simulated.",simulationDistance+" Chunks",2,32,simulationDistance,v->simulationDistance=v,v->simulationDistance=v,1));
        rowY+=72;
        addRenderableWidget(new SliderRow(x,rowY,w,56,"Max Framerate","Limits the FPS to reduce heat and lag.",fps+" FPS",30,1000,fps,v->{
            fps=v; VoidBoostConfig.get().targetFps=v; VoidBoostConfig.get().markDirty();
        },v->fps=v,2));
        rowY+=72;
        toggleRow(x,rowY,w,"VSync","Synchronize frames with your monitor.","OFF",false,()->toggleVsync());
        rowY+=72;
        addRenderableWidget(new ChoiceRow(x,rowY,w,"Particles","Reduce particle effects for better performance.",particles(),()->cycleParticles()));
        rowY+=72;
        toggleRow(x,rowY,w,"Entity Shadows","Disable entity shadows to save resources.","OFF",false,()->{VoidBoostConfig.get().entityShadows=!VoidBoostConfig.get().entityShadows;VoidBoostConfig.get().markDirty();});
        rowY+=72;
        toggleRow(x,rowY,w,"FPS Boost","Applies additional optimizations (experimental).","ON",true,()->{VoidBoostConfig.get().performanceMode=!VoidBoostConfig.get().performanceMode;VoidBoostConfig.get().markDirty();});
    }

    private void general(int x,int y,int w,int h) {
        int yy=y+112;
        addRenderableWidget(new ProfileRow(x,yy,w,"Balanced","Stable everyday performance",VoidBoostConfig::applyBalancedPreset));
        yy+=72;
        addRenderableWidget(new ProfileRow(x,yy,w,"Competitive","Low-latency PvP profile",VoidBoostConfig::applyCompetitivePreset));
        yy+=72;
        addRenderableWidget(new ProfileRow(x,yy,w,"MAX FPS","Aggressive performance profile",VoidBoostConfig::applyMaxFpsPreset));
        yy+=72;
        addRenderableWidget(new ProfileRow(x,yy,w,"ULTIMATE FPS","Maximum performance profile",VoidBoostConfig::applyUltimateLockedPreset));
    }

    private void simplePage(int x,int y,int w,int h,int p) {
        String title=switch(p){case 2->"Visuals";case 3->"PvP";case 4->"HUD";default->"Advanced";};
        String desc=switch(p){case 2->"Visual rendering controls and optimization.";case 3->"Low-latency combat settings.";case 4->"On-screen performance information.";default->"Advanced client-side controls.";};
        addRenderableWidget(new InfoRow(x,y+112,w,title,desc));
    }

    private void toggleRow(int x,int y,int w,String title,String desc,String value,boolean on,Runnable a) {
        addRenderableWidget(new ToggleRow(x,y,w,56,title,desc,value,on,a));
    }

    private void toggleVsync() {
        VoidBoostConfig.get().vsyncOptimization=!VoidBoostConfig.get().vsyncOptimization;
        VoidBoostConfig.get().markDirty(); rebuildWidgets();
    }

    private String particles() {
        VoidBoostConfig c=VoidBoostConfig.get();
        return c.disableParticles?"Minimal":c.reducedParticles?"Reduced":"All";
    }
    private void cycleParticles() {
        VoidBoostConfig c=VoidBoostConfig.get();
        if(c.disableParticles){c.disableParticles=false;c.reducedParticles=false;}
        else if(!c.reducedParticles)c.reducedParticles=true;
        else c.disableParticles=true;
        c.markDirty(); rebuildWidgets();
    }

    @Override public boolean mouseClicked(MouseButtonEvent e, boolean d) {
        if (e.x() >= width-78 && e.y() <= 78) { Minecraft.getInstance().setScreen(parent); return true; }
        return super.mouseClicked(e,d);
    }

    @Override public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        if(draggingRender||draggingSimulation||draggingFps) {
            int[] r=layout(); int x=r[3]+22, w=r[5]-44;
            int v=(int)Math.round(((e.x()-x)/(double)w)*(draggingFps?970:28)+(draggingFps?30:draggingSimulation?2:4));
            v=clamp(v,draggingFps?30:draggingSimulation?2:4,draggingFps?1000:32);
            if(draggingRender){renderDistance=v;VoidBoostConfig.get().maxRenderDistance=v;}
            if(draggingFps){fps=v;VoidBoostConfig.get().targetFps=v;}
            if(draggingSimulation)simulationDistance=v;
            return true;
        }
        return super.mouseDragged(e,dx,dy);
    }

    @Override public boolean mouseReleased(MouseButtonEvent e) {
        draggingRender=draggingSimulation=draggingFps=false;
        return super.mouseReleased(e);
    }

    @Override public void render(GuiGraphics g,int mx,int my,float delta) {
        int[] r=layout(); int sx=r[0],sy=r[1],sw=r[2],main=r[3],mw=r[5],bottom=sy+r[6];
        g.fill(0,0,width,height,BG);
        g.fill(sx,sy,sx+sw+mw+20,bottom,0xB807101A);
        g.fill(sx,sy,sx+sw,bottom,SIDE);
        g.fill(main,sy,main+mw,bottom,PANEL);
        g.fill(sx,sy,sx+sw,sy+3,CYAN);
        g.fill(main,sy,main+mw,sy+3,CYAN);
        g.blit(LOGO,sx+22,sy+20,0,0,48,48,64,64);
        g.drawString(font,Component.literal("VOIDBOOST"),sx+80,sy+21,TEXT,false);
        g.drawString(font,Component.literal("More FPS • Smoother • Better"),sx+80,sy+39,MUTED,false);
        g.drawString(font,Component.literal("Minecraft 1.21.11 | Fabric"),main+22,sy+28,MUTED,false);
        g.drawString(font,Component.literal("×"),width-62,sy+19,TEXT,false);
        g.drawString(font,Component.literal("VOIDBOOST"),sx+22,bottom-54,MUTED,false);
        g.drawString(font,Component.literal("Boost your game"),sx+22,bottom-37,MUTED,false);

        String title=switch(page){case 0->"General";case 1->"Performance";case 2->"Visuals";case 3->"PvP";case 4->"HUD";default->"Advanced";};
        String sub=page==1?"Optimize your game for higher FPS and smoother gameplay.":"VoidBoost client-side controls.";
        g.drawString(font,Component.literal(title),main+28,sy+82,TEXT,false);
        g.drawString(font,Component.literal(sub),main+28,sy+101,MUTED,false);
        g.fill(main+22,sy+128,main+mw-22,sy+129,BORDER);
        g.fill(main+mw-14,sy+128,main+mw-10,bottom-18,0x44314A5A);
        super.render(g,mx,my,delta);
        g.fill(0,bottom,width,height,0x55040A12);
    }

    private static int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}

    private abstract static class Base extends AbstractWidget {
        Base(int x,int y,int w,int h,String s){super(x,y,w,h,Component.literal(s));}
        protected void box(GuiGraphics g){g.fill(getX(),getY(),getX()+width,getY()+height,isHovered()?HOVER:CARD);g.fill(getX(),getY(),getX()+1,getY()+height,isHovered()?CYAN_DIM:BORDER);}
        @Override protected void updateWidgetNarration(NarrationElementOutput o){defaultButtonNarrationText(o);}
        @Override public void onClick(MouseButtonEvent e,boolean d){}
    }

    private final class NavButton extends Base {
        final String label; final boolean selected; final Runnable action;
        NavButton(int x,int y,int w,String l,boolean s,Runnable a){super(x,y,w,38,l);label=l;selected=s;action=a;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float d){
            g.fill(getX(),getY(),getX()+width,getY()+height,selected?0xCC132A35:(isHovered()?HOVER:SIDE));
            if(selected)g.fill(getX(),getY(),getX()+3,getY()+height,CYAN);
            g.drawString(font,Component.literal(label),getX()+42,getY()+13,selected?TEXT:MUTED,false);
            drawIcon(g,getX()+15,getY()+12,label,selected?CYAN:MUTED);
        }
        @Override public void onClick(MouseButtonEvent e,boolean d){action.run();}
    }

    private final class ToggleRow extends Base {
        final String title,desc,value; final boolean on; final Runnable action;
        ToggleRow(int x,int y,int w,int h,String t,String d,String v,boolean o,Runnable a){super(x,y,w,h,t);title=t;desc=d;value=v;on=o;action=a;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float d){box(g);g.drawString(font,Component.literal(title),getX()+16,getY()+12,TEXT,false);g.drawString(font,Component.literal(desc),getX()+16,getY()+30,MUTED,false);drawToggle(g,getX()+width-70,getY()+15,on);}
        @Override public void onClick(MouseButtonEvent e,boolean d){action.run();}
    }

    private final class ChoiceRow extends Base {
        final String title,desc; String value; final Runnable action;
        ChoiceRow(int x,int y,int w,String t,String d,String v,Runnable a){super(x,y, w,56,t);title=t;desc=d;value=v;action=a;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float d){box(g);g.drawString(font,Component.literal(title),getX()+16,getY()+12,TEXT,false);g.drawString(font,Component.literal(desc),getX()+16,getY()+30,MUTED,false);g.drawString(font,Component.literal(value+"  ›"),getX()+width-104,getY()+21,CYAN,false);}
        @Override public void onClick(MouseButtonEvent e,boolean d){action.run();}
    }

    private final class SliderRow extends Base {
        final String title,desc; String value; int min,max,current; final java.util.function.IntConsumer change,preview; final int kind;
        SliderRow(int x,int y,int w,int h,String t,String d,String v,int mi,int ma,int c,java.util.function.IntConsumer ch,java.util.function.IntConsumer pr,int k){super(x,y,w,h,t);title=t;desc=d;value=v;min=mi;max=ma;current=c;change=ch;preview=pr;kind=k;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float d){
            box(g);g.drawString(font,Component.literal(title),getX()+16,getY()+10,TEXT,false);g.drawString(font,Component.literal(desc),getX()+16,getY()+28,MUTED,false);g.drawString(font,Component.literal(value),getX()+width-100,getY()+11,CYAN,false);
            int tx=getX()+16,tw=width-32,ty=getY()+45;g.fill(tx,ty,tx+tw,ty+3,0xFF263A49);int knob=tx+(int)((current-min)/(double)(max-min)*tw);g.fill(tx,ty,knob,ty+3,CYAN);g.fill(knob-4,ty-4,knob+5,ty+9,CYAN);
        }
        @Override public void onClick(MouseButtonEvent e,boolean d){current=clampFromMouse(e.x()); if(kind==0)draggingRender=true;if(kind==1)draggingSimulation=true;if(kind==2)draggingFps=true;preview.accept(current);change.accept(current);rebuildWidgets();}
        private int clampFromMouse(double xx){int tx=getX()+16,tw=width-32;return clamp((int)Math.round(min+(xx-tx)/tw*(max-min)),min,max);}
    }

    private final class ProfileRow extends Base {
        final String title,desc; final Runnable action;
        ProfileRow(int x,int y,int w,String t,String d,Runnable a){super(x,y,w,56,t);title=t;desc=d;action=a;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float d){box(g);g.drawString(font,Component.literal(title),getX()+16,getY()+12,TEXT,false);g.drawString(font,Component.literal(desc),getX()+16,getY()+30,MUTED,false);g.drawString(font,Component.literal("APPLY  ›"),getX()+width-90,getY()+21,CYAN,false);}
        @Override public void onClick(MouseButtonEvent e,boolean d){action.run();rebuildWidgets();}
    }

    private final class InfoRow extends Base {
        final String title,desc; InfoRow(int x,int y,int w,String t,String d){super(x,y,w,56,t);title=t;desc=d;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float d){box(g);g.drawString(font,Component.literal(title),getX()+16,getY()+12,TEXT,false);g.drawString(font,Component.literal(desc),getX()+16,getY()+30,MUTED,false);}
    }

    private static void drawToggle(GuiGraphics g,int x,int y,boolean on){g.fill(x,y,x+48,y+24,on?CYAN_DIM:0xFF1A2631);g.fill(x+(on?26:2),y+2,x+(on?46:22),y+22,on?CYAN:0xFF71818B);}
    private static void drawIcon(GuiGraphics g,int x,int y,String s,int c){
        int w=14; g.fill(x,y+5,x+w,y+7,c);
        if(s.equals("Performance")){g.fill(x+2,y+2,x+4,y+5,c);g.fill(x+10,y+1,x+12,y+5,c);}
        else if(s.equals("General")){g.fill(x+5,y+1,x+9,y+13,c);}
        else if(s.equals("Visuals")){g.fill(x+2,y+4,x+12,y+10,c);}
        else if(s.equals("PvP")){g.fill(x+3,y+2,x+5,y+12,c);g.fill(x+9,y+2,x+11,y+12,c);}
        else if(s.equals("HUD")){g.fill(x+3,y+2,x+11,y+11,c);g.fill(x+5,y+12,x+9,y+13,c);}
        else {g.fill(x+1,y+2,x+13,y+4,c);g.fill(x+1,y+6,x+13,y+8,c);g.fill(x+1,y+10,x+13,y+12,c);}
    }
}
