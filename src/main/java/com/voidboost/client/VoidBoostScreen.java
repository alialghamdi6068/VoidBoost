package com.voidboost.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** High-end VoidBoost control center. */
public final class VoidBoostScreen extends Screen {
    private static final int BG=0xFF07090D,SHELL=0xFF0B0F16,SIDE=0xFF090C12,CARD=0xFF101620,HOVER=0xFF171E2A,SEL=0xFF1D1730,BORDER=0xFF252D3A,ACCENT=0xFF9B7CFF,ACCENT_SOFT=0xFF5E4A8A,TEXT=0xFFF5F6FA,MUTED=0xFF8791A4,GOOD=0xFF75E0A4;
    private final Screen parent;
    private int page;
    private double scroll;
    private long openedAt;

    public VoidBoostScreen(Screen parent){super(Component.literal("VoidBoost"));this.parent=parent;}

    @Override protected void init(){
        if(openedAt==0)openedAt=System.currentTimeMillis();
        rebuildWidgets();
    }

    @Override protected void rebuildWidgets(){
        clearWidgets();
        int left=24,sideRight=Math.min(204,width/3),content=sideRight+30,right=width-30;
        int top=104-(int)scroll;
        nav(left+12,118,"General",0);
        nav(left+12,160,"Visual",1);
        nav(left+12,202,"Performance",2);
        nav(left+12,244,"Advanced",3);
        if(page==0)general(content,right,top); else if(page==1)visual(content,right,top); else if(page==2)performance(content,right,top); else advanced(content,right,top);
        button(left+12,height-70,88,38,"Reset","",()->{scroll=0;VoidBoostConfig.applyBalancedPreset();rebuildWidgets();},false,true,"Balanced");
        button(right-116,height-70,116,38,"Done","SAVE",()->{VoidBoostConfig.save();Minecraft.getInstance().setScreen(parent);},true,true,"Save");
    }

    private int maxScroll(){return switch(page){case 0->184;case 1->118;case 2->118;default->158;};}

    @Override public boolean mouseScrolled(double mx,double my,double sx,double sy){
        if(mx<204||my<88||my>height-82)return super.mouseScrolled(mx,my,sx,sy);
        double next=Math.max(0,Math.min(maxScroll(),scroll-sy*34));
        if(next!=scroll){scroll=next;rebuildWidgets();}
        return true;
    }

    private void nav(int x,int y,String name,int p){button(x,y,150,34,name,p==page?"ACTIVE":"",()->{page=p;scroll=0;rebuildWidgets();},p==page,true,"");}

    private void general(int l,int r,int y){
        int w=(r-l-14)/2;
        card(l,y,w,82,"Balanced","STABLE",VoidBoostConfig::applyBalancedPreset,preset("balanced"),"Reliable everyday profile");
        card(l+w+14,y,w,82,"Competitive","PVP",VoidBoostConfig::applyCompetitivePreset,preset("competitive"),"Low-latency rendering");
        card(l,y+94,w,82,"MAX FPS","FAST",VoidBoostConfig::applyMaxFpsPreset,preset("max"),"Aggressive workload reduction");
        card(l+w+14,y+94,w,82,"ULTIMATE FPS","MAX",VoidBoostConfig::applyUltimateLockedPreset,preset("ultimate"),"Maximum performance profile");
        card(l,y+188,r-l,66,"Custom","MANUAL",this::custom,preset("custom"),"Tune every available option yourself");
        setting(l,y+268,r,"Dynamic Render Distance",state("dynamic"),"Adaptive chunk workload",()->toggle("dynamic"));
        setting(l,y+318,r,"Adaptive Target",VoidBoostConfig.get().dynamicTargetFps+" FPS","Local adaptive engine target",this::target);
        setting(l,y+368,r,"Performance Mode",state("performance"),"Optimize supported vanilla settings",()->toggle("performance"));
    }

    private void visual(int l,int r,int y){
        setting(l,y,r,"Particles",state("particles"),"All, reduced, or minimal particle workload",()->toggle("particles"));
        setting(l,y+50,r,"Entity Shadows",state("shadows"),"Shadow rendering pass",()->toggle("shadows"));
        setting(l,y+100,r,"Weather Effects",state("weather"),"Weather rendering workload",()->toggle("weather"));
        setting(l,y+150,r,"Cloud Optimization",state("clouds"),"Reduce cloud rendering work",()->toggle("clouds"));
        setting(l,y+200,r,"Animation Optimization",state("animations"),"Reduce animation workload",()->toggle("animations"));
        setting(l,y+250,r,"Fog Optimization",state("fog"),"Reduce fog workload",()->toggle("fog"));
        setting(l,y+300,r,"Vignette Optimization",state("vignette"),"Remove the vignette render pass",()->toggle("vignette"));
        setting(l,y+350,r,"Ambient Occlusion",state("ao"),"Reduce ambient occlusion workload",()->toggle("ao"));
    }

    private void performance(int l,int r,int y){
        setting(l,y,r,"Entity Optimization",state("entities"),"Cull distant non-critical entities",()->toggle("entities"));
        setting(l,y+50,r,"Entity Distance",VoidBoostConfig.get().maxEntityDistance+" BLOCKS","Maximum entity render distance",this::entityDistance);
        setting(l,y+100,r,"Render Distance",VoidBoostConfig.get().maxRenderDistance+" CHUNKS","Maximum chunk render distance",this::renderDistance);
        setting(l,y+150,r,"Dynamic Render Distance",state("dynamic"),"AI-controlled render workload",()->toggle("dynamic"));
        setting(l,y+200,r,"Performance Monitor",state("monitor"),"Low-overhead FPS, RAM and pressure telemetry",()->toggle("monitor"));
        setting(l,y+250,r,"View Bobbing",state("viewbob"),"Reduce camera animation work",()->toggle("viewbob"));
        setting(l,y+300,r,"FPS Limit",VoidBoostConfig.get().targetFps+" FPS","Client frame-rate limit",this::fpsLimit);
    }

    private void advanced(int l,int r,int y){
        setting(l,y,r,"Mipmap Optimization",state("mipmap"),"Reduce texture sampling workload",()->toggle("mipmap"));
        setting(l,y+50,r,"Biome Blend Optimization",state("biome"),"Reduce biome color blending work",()->toggle("biome"));
        setting(l,y+100,r,"VSync Optimization",state("vsync"),"Avoid forced synchronization when enabled",()->toggle("vsync"));
        setting(l,y+150,r,"Particle Budget",VoidBoostConfig.get().particleLimitPercent+"%","Sampling budget for reduced particles",this::particleBudget);
        info(l,y+216,r,"ADAPTIVE ENGINE","The local engine reacts to FPS, frame pressure, RAM pressure and entity load.");
        info(l,y+282,r,"CLIENT ONLY","VoidBoost settings are stored locally and do not require a network service.");
    }

    private void card(int x,int y,int w,int h,String title,String value,Runnable action,boolean selected,String desc){addRenderableWidget(new PremiumButton(x,y,w,h,title,value,desc,action,selected,false));}
    private void setting(int l,int y,int r,String title,String value,String desc,Runnable action){addRenderableWidget(new PremiumButton(l,y,r-l,42,title,value,desc,action,false,false));}
    private void info(int l,int y,int r,String title,String desc){addRenderableWidget(new PremiumButton(l,y,r-l,56,title,"",desc,()->{},true,false));}
    private void button(int x,int y,int w,int h,String title,String value,Runnable action,boolean selected,boolean compact,String desc){addRenderableWidget(new PremiumButton(x,y,w,h,title,value,desc,action,selected,compact));}

    private void custom(){
        VoidBoostConfig c=VoidBoostConfig.get();
        c.maxFpsPreset=false;c.competitiveMode=false;c.ultimateLocked=false;c.markDirty();rebuildWidgets();
    }

    private void toggle(String k){
        VoidBoostConfig c=VoidBoostConfig.get();
        if(c.ultimateLocked&&!k.equals("performance"))return;
        switch(k){
            case "particles"->{if(c.disableParticles){c.disableParticles=false;c.reducedParticles=true;}else if(c.reducedParticles)c.reducedParticles=false;else c.disableParticles=true;}
            case "dynamic"->c.dynamicRenderDistance=!c.dynamicRenderDistance;
            case "entities"->c.entityRenderOptimization=!c.entityRenderOptimization;
            case "shadows"->c.entityShadows=!c.entityShadows;
            case "weather"->c.weatherEffects=!c.weatherEffects;
            case "clouds"->c.cloudOptimization=!c.cloudOptimization;
            case "animations"->c.animationOptimization=!c.animationOptimization;
            case "fog"->c.fogOptimization=!c.fogOptimization;
            case "vignette"->c.vignetteOptimization=!c.vignetteOptimization;
            case "ao"->c.ambientOcclusionOptimization=!c.ambientOcclusionOptimization;
            case "mipmap"->c.mipmapOptimization=!c.mipmapOptimization;
            case "biome"->c.biomeBlendOptimization=!c.biomeBlendOptimization;
            case "vsync"->c.vsyncOptimization=!c.vsyncOptimization;
            case "viewbob"->c.viewBobOptimization=!c.viewBobOptimization;
            case "performance"->{c.performanceMode=!c.performanceMode;if(!c.performanceMode)c.ultimateLocked=false;}
            case "monitor"->c.performanceMonitor=!c.performanceMonitor;
            default-> {return;}
        }
        c.maxFpsPreset=false;c.competitiveMode=false;if(!k.equals("performance"))c.ultimateLocked=false;c.markDirty();rebuildWidgets();
    }

    private void target(){VoidBoostConfig c=VoidBoostConfig.get();if(c.ultimateLocked)return;c.dynamicTargetFps=c.dynamicTargetFps>=240?60:c.dynamicTargetFps+30;c.maxFpsPreset=false;c.competitiveMode=false;c.markDirty();rebuildWidgets();}
    private void entityDistance(){VoidBoostConfig c=VoidBoostConfig.get();if(c.ultimateLocked)return;c.maxEntityDistance=c.maxEntityDistance>=128?32:c.maxEntityDistance+16;c.maxFpsPreset=false;c.competitiveMode=false;c.markDirty();rebuildWidgets();}
    private void renderDistance(){VoidBoostConfig c=VoidBoostConfig.get();if(c.ultimateLocked)return;c.maxRenderDistance=c.maxRenderDistance>=12?4:c.maxRenderDistance+2;c.maxFpsPreset=false;c.competitiveMode=false;c.markDirty();rebuildWidgets();}
    private void particleBudget(){VoidBoostConfig c=VoidBoostConfig.get();if(c.ultimateLocked)return;c.particleLimitPercent=c.particleLimitPercent>=100?10:c.particleLimitPercent+10;c.maxFpsPreset=false;c.competitiveMode=false;c.markDirty();rebuildWidgets();}
    private void fpsLimit(){VoidBoostConfig c=VoidBoostConfig.get();if(c.ultimateLocked)return;int[] v={60,120,144,165,240,360,1000};int n=60;for(int x:v)if(x>c.targetFps){n=x;break;}c.targetFps=n;c.maxFpsPreset=false;c.competitiveMode=false;c.markDirty();rebuildWidgets();}

    private static String state(String k){
        VoidBoostConfig c=VoidBoostConfig.get();
        return switch(k){
            case "particles"->c.disableParticles?"MINIMAL":c.reducedParticles?"REDUCED":"ALL";
            case "dynamic"->c.dynamicRenderDistance?"ON":"OFF";
            case "entities"->c.entityRenderOptimization?"ON":"OFF";
            case "shadows"->c.entityShadows?"ON":"OFF";
            case "weather"->c.weatherEffects?"ON":"OFF";
            case "clouds"->c.cloudOptimization?"ON":"OFF";
            case "animations"->c.animationOptimization?"ON":"OFF";
            case "fog"->c.fogOptimization?"ON":"OFF";
            case "vignette"->c.vignetteOptimization?"ON":"OFF";
            case "ao"->c.ambientOcclusionOptimization?"ON":"OFF";
            case "mipmap"->c.mipmapOptimization?"ON":"OFF";
            case "biome"->c.biomeBlendOptimization?"ON":"OFF";
            case "vsync"->c.vsyncOptimization?"ON":"OFF";
            case "viewbob"->c.viewBobOptimization?"ON":"OFF";
            case "performance"->c.performanceMode?"ON":"OFF";
            case "monitor"->c.performanceMonitor?"ON":"OFF";
            default->"OFF";
        };
    }

    private static boolean preset(String n){
        VoidBoostConfig c=VoidBoostConfig.get();
        return switch(n){
            case "ultimate"->c.ultimateLocked;
            case "competitive"->c.competitiveMode&&!c.maxFpsPreset&&!c.ultimateLocked;
            case "max"->c.maxFpsPreset&&!c.competitiveMode&&!c.ultimateLocked;
            case "balanced"->!c.competitiveMode&&!c.maxFpsPreset&&!c.ultimateLocked&&c.performanceMode&&!c.disableParticles&&c.reducedParticles&&!c.entityShadows&&!c.weatherEffects&&c.animationOptimization&&c.fogOptimization&&c.entityRenderOptimization&&c.dynamicRenderDistance&&c.cloudOptimization&&c.vignetteOptimization&&c.ambientOcclusionOptimization&&c.mipmapOptimization&&c.biomeBlendOptimization&&!c.viewBobOptimization&&c.vsyncOptimization&&c.dynamicTargetFps==120&&c.maxEntityDistance==56&&c.maxRenderDistance==10;
            case "custom"->!preset("balanced")&&!c.competitiveMode&&!c.maxFpsPreset&&!c.ultimateLocked;
            default->false;
        };
    }

    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        int left=24,top=22,right=width-24,bottom=height-22,side=Math.min(204,width/3),content=side+30;
        g.fill(0,0,width,height,BG);
        g.fill(left+4,top+5,right+4,bottom+5,0xFF05070A);
        g.fill(left,top,right,bottom,SHELL);
        g.fill(left,top,side,bottom,SIDE);
        g.fill(left,top,right,top+2,ACCENT);
        g.fill(side,top+2,side+1,bottom,BORDER);

        // Reserved logo frame. Intentionally blank until the real VoidBoost logo is supplied.
        g.fill(left+14,39,left+52,77,0xFF141923);
        g.fill(left+14,39,left+17,77,ACCENT);
        g.fill(left+17,39,left+52,40,ACCENT_SOFT);

        g.drawString(font,Component.literal("VoidBoost"),left+64,41,TEXT,false);
        g.drawString(font,Component.literal("PERFORMANCE SUITE"),left+64,57,MUTED,false);
        g.drawString(font,Component.literal("SETTINGS"),left+14,94,MUTED,false);
        g.drawString(font,Component.literal("Made by VoidFlame"),left+14,bottom-29,MUTED,false);
        g.drawString(font,Component.literal("O"),left+side-28,bottom-29,ACCENT,false);

        String title=switch(page){case 0->"General";case 1->"Visual";case 2->"Performance";default->"Advanced"};
        String sub=switch(page){case 0->"Profiles and adaptive controls";case 1->"Rendering workload";case 2->"Frame-time and entity control";default->"Advanced performance controls"};
        g.drawString(font,Component.literal(title),content,41,TEXT,false);
        g.drawString(font,Component.literal(sub),content,57,MUTED,false);

        int statusX=right-158;
        g.fill(statusX,38,right,68,0xFF0F151E);
        g.fill(statusX,38,statusX+2,68,GOOD);
        g.drawString(font,Component.literal("ENGINE"),statusX+12,44,MUTED,false);
        g.drawString(font,Component.literal("ACTIVE"),statusX+12,56,GOOD,false);
        g.drawString(font,Component.literal("LOCAL"),right-48,44,ACCENT,false);

        int clipBottom=bottom-48;
        g.enableScissor(0,82,width,clipBottom);
        super.render(g,mx,my,delta);
        g.disableScissor();

        int trackTop=84,trackBottom=clipBottom,trackH=trackBottom-trackTop,max=maxScroll();
        if(max>0){
            int thumb=Math.max(28,trackH*trackH/(trackH+max));
            int ty=trackTop+(int)((trackH-thumb)*(scroll/(double)max));
            g.fill(right-17,trackTop,right-14,trackBottom,BORDER);
            g.fill(right-17,ty,right-14,ty+thumb,ACCENT);
        }
        g.fill(content,bottom-46,right-12,bottom-45,BORDER);
        g.drawString(font,Component.literal("FABRIC • MINECRAFT 1.21.11"),content,bottom-29,MUTED,false);
    }

    private static final class PremiumButton extends AbstractWidget{
        private final String title,value,description;
        private final Runnable action;
        private final boolean selected,compact;
        PremiumButton(int x,int y,int w,int h,String t,String v,String d,Runnable a,boolean s,boolean c){super(x,y,w,h,Component.literal(t));title=t;value=v;description=d;action=a;selected=s;compact=c;}

        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float delta){
            boolean hover=isHovered();
            int fill=selected?SEL:hover?HOVER:CARD;
            int edge=selected?ACCENT:hover?0xFF3B4658:BORDER;
            g.fill(getX()+2,getY()+2,getX()+width+2,getY()+height+2,0xFF070A0F);
            g.fill(getX(),getY(),getX()+width,getY()+height,fill);
            g.fill(getX(),getY(),getX()+3,getY()+height,edge);
            if(selected)g.fill(getX()+3,getY(),getX()+width,getY()+2,ACCENT);
            if(hover&&!selected)g.fill(getX()+3,getY()+height-2,getX()+width,getY()+height,0xFF343D4C);
            int ty=getY()+(compact?11:9);
            int titleColor=hover||selected?TEXT:0xFFE6E9EF;
            g.drawString(Minecraft.getInstance().font,Component.literal(title),getX()+14,ty,titleColor,false);
            if(!description.isEmpty()&&!compact)g.drawString(Minecraft.getInstance().font,Component.literal(description),getX()+14,ty+16,MUTED,false);
            if(!value.isEmpty()){
                int vx=getX()+width-Minecraft.getInstance().font.width(value)-14;
                g.drawString(Minecraft.getInstance().font,Component.literal(value),vx,ty,selected?GOOD:MUTED,false);
            }
        }

        @Override public void onClick(MouseButtonEvent event,boolean isDoubleClick){action.run();}
        @Override protected void updateWidgetNarration(NarrationElementOutput out){defaultButtonNarrationText(out);}
    }
}
