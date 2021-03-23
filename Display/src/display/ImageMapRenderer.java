/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.Image;
import java.awt.image.BufferedImage;
 
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

/**
 *
 * @author M0rica
 */
public class ImageMapRenderer extends MapRenderer{
    
    BufferedImage img;
    boolean rendered = false;
    
    public ImageMapRenderer(BufferedImage img){
        //super();
        System.out.println("Init new ImageMapRenderer");
        this.img = img;
    }
    
    @Override
    public void render(MapView mv, MapCanvas mc, Player p){
        if(rendered){
            return;
        }
        System.out.println("Rendering image");
        mc.drawImage(0, 0, (Image) img);
        rendered = true;
    }
}
