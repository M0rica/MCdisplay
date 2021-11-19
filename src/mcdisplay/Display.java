/*
 * Copyright (C) 2021 M0rica
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mcdisplay;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author M0rica
 */
public class Display {
    
    private Logger log;
    
    private String imgPath;
    private BufferedImage img;
    private String name;
    
    private BukkitScheduler scheduler;
    private MCDisplay plugin;
    private World world;
    private Colormap colormap;
    private Settings settings;
    private Chat chat;
    private DisplayManager displayManager;
    private DisplaySettings displaySettings;
    
    public Display(MCDisplay mcd, DisplayManager dm, Colormap cmap, Settings set, Chat c, String n, Logger l){
        imgPath = "";
        img = null;
        plugin = mcd;
        displayManager = dm;
        colormap = cmap;
        settings = set;
        world = Bukkit.getWorlds().get(0);
        scheduler = Bukkit.getScheduler();
        chat = c;
        name = n;
        log = l;
        
        displaySettings = new DisplaySettings(name, settings, log);
        displaySettings.load();
    }
    
    public String getName(){
        return name;
    }
    
    public DisplaySettings getSettings(){
        return displaySettings;
    }
    
    public boolean setSetting(String var, String value){
        try{
            Field field = displaySettings.getClass().getDeclaredField(var);
            Class type = field.getType();
            if(type.equals(double.class)){
                field.set(displaySettings, Double.valueOf(value));
            } else if(type.equals(float.class)){
                field.set(displaySettings, Float.valueOf(value));
            } else if(type.equals(int.class)){
                field.set(displaySettings, Integer.valueOf(value));
            } else if(type.equals(boolean.class)){
                field.set(displaySettings, Boolean.valueOf(value));
            } else if(type.equals(String.class)){
                field.set(displaySettings, value);
            }
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    public void setPos(int[] newPos){
        displaySettings.x = newPos[0];
        displaySettings.y = newPos[1];
        displaySettings.z = newPos[2];
    }
    
    public void setResolution(int[] res){
        displaySettings.width = res[0];
        displaySettings.height = res[1];
    }
    
    public void center(Player player){
        scheduler.runTaskLater(plugin, new Runnable(){
            @Override
            public void run(){
                int w = displaySettings.width;
                int h = displaySettings.height;
                int[] pos = new int[]{displaySettings.x, displaySettings.y, displaySettings.z};
                Location location = new Location(world, (w/2)+pos[0], ((w+h)/2/4)+3, (h/2)+pos[2], -180, 90);
                player.teleport(location);
            }
        }, 1L);
    }
    
    public void renderImage(String path){
        if(displaySettings.x != null && displaySettings.y != null && displaySettings.z != null){
            BufferedImage image = null;
            if(path.equals(imgPath) && img != null){
                chat.info("Using cached image");
                image = img;
            } else if(Utils.isURL(path)){
                chat.info("Downloading image from URL");
                image = ImageUtils.loadImageFromURL(path);
            } else {
                String filePath = settings.imgPath + "/" + path;
                if(Utils.isFile(filePath)){
                    chat.info("Loading image");
                    image = ImageUtils.loadImage(filePath);
                }
            }
            if(image != null){
                img = image;
                imgPath = path;
                image = processImage(image);
                renderToBlocks(imgToMaterial(image));
            } else {
                chat.error("Failed to render image to display: couldn't load image, porbably unsupported file type");
                displayManager.reportActionFailed();
            }
        } else {
            chat.error("Failed to render image to display: no possition set yet");
            displayManager.reportActionFailed();
        }
    }
    
    private BufferedImage processImage(BufferedImage image){
        chat.info("Processing image");
        int w = displaySettings.width;
        int h = displaySettings.height;
        image = ImageUtils.resizeKeepAspect(image, w, h, displaySettings.upResImage);
        image = ImageUtils.sharpenImage(image, displaySettings.sharpnessFactor);
        return image;
    }
    
    private Material[][] imgToMaterial(BufferedImage image){
        chat.info("Converting image to blocks");
        int w = displaySettings.width;
        int h = displaySettings.height;
        float[] rgb = new float[3];
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Material[][] imgMaterial = new Material[w][h];
        int x = 0;
        int y = 0;
        for(int pixel = 0; pixel+2<pixels.length; pixel+=3){
            rgb[2] = (float) (pixels[pixel] & 0xff);
            rgb[1] = (float) (pixels[pixel+1] & 0xff);
            rgb[0] = (float) (pixels[pixel+2] & 0xff);
            imgMaterial[x][y] = colormap.map(rgb);
            x++;
            if(x == w){
                x = 0;
                y++;
            }
        }
        //colormap.clearCache();
        return imgMaterial;
    }
    
    private void renderToBlocks(Material[][] image){
        chat.info("Placing blocks");
        scheduler.runTaskLater(plugin, new Runnable(){
            @Override
            public void run(){
                int[] pos = new int[]{displaySettings.x, displaySettings.y, displaySettings.z};
                for(int i=0; i<image.length; i++){
                    for(int j=0; j<image[0].length; j++){
                        Block block = world.getBlockAt(pos[0]+i,pos[1],pos[2]+j);
                        Material m = image[i][j];
                        if (m != block.getType()){
                            block.setType(m);
                        }
                    }
                }
                displayManager.reportActionComplete();
            }
        }, 1L);
    }
}
