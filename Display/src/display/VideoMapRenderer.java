/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
 
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

/**
 *
 * @author M0rica
 */
public class VideoMapRenderer extends MapRenderer{
    
    List<byte[][]> video;
    boolean finished = false;
    boolean prepare = true;
    int frame = 0;
    int length = 0;
    int resolution;
    String mapNumber;
    
    public VideoMapRenderer(String mapNumber){
        this.mapNumber = mapNumber;
        System.out.println("Init new VideoMapRenderer");
    }
    
    @Override
    public void render(MapView mv, MapCanvas mc, Player p){
        if(finished){
            return;
        }
        if(prepare){
            mc.drawText(64, 64, MinecraftFont.Font, "§12;" + mapNumber);
            finished = true;
            return;
        }
        if(frame >= length){
           finished = true;
           return;
        }
        byte[][] videoframe = video.get(frame);
        for(int h = 0; h<videoframe.length; h++){
            for(int w = 0; w<videoframe[0].length; w++){
                mc.setPixel(h, w, videoframe[h][w]);
            }
        }
        frame++;
    }
    
    /*public void setVideo(byte[][] vid, int frames, int res){
        video = new byte[frames][res][res];
        length = frames;
        int f = 0, h = 0, w = 0;
        for(byte[] frame: vid){
            w = 0;
            h = 0;
            for(byte color: frame){
                this.video[f][h][w] = color;
                h++;
                if(h == 128){
                    h = 0;
                    w++;
                }
            }
            f++;
        }
        frame = 0;
        prepare = true;
    }*/
    
    public void initVideo(int frames, int res){
        video = new ArrayList<byte[][]>();
        length = frames;
        resolution = res;
        prepare = true;
        frame = 0;
    }
    
    public void addFrame(byte[] f){
        byte[][] tempFrame = new byte[resolution][resolution];
        int h = 0, w = 0;
        w = 0;
        h = 0;
        for(byte color: f){
            tempFrame[h][w] = color;
            h++;
            if(h == 128){
                h = 0;
                w++;
            }
        }
        video.add(tempFrame);
    }
    
    public void start(){
        finished = false;
        frame = 0;
        prepare = false;
    }
    
    public void setFinished(boolean f){
        finished = f;
    }
}

