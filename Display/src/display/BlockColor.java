/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Material;
/**
 *
 * @author M0rica
 */

public class BlockColor{
   
    private class ColorBlock {
    
        Material material;
        int color;
        float red, green, blue;
        float[] color_lab;
    
        private ColorBlock(Material m, float r, float g, float b){
            material = m;
            if(r == 0){
                r = 1;
            }
            if(g == 0){
                g = 1;
            }
            if(b == 0){
                b = 1;
            }
            red = r;
            green = g;
            blue = b;
            color_lab = colorConv.fromRGB(new float[]{r, g, b});
        }
    }
    
    String colormapName;
    ColorBlock[] blocks;
    Material lastMaterial = Material.BLACK_CONCRETE;
    float lastR, lastG, lastB = 0;
    CIELab colorConv = new CIELab();
    HashMap<List<Float>, Material> cache = new HashMap<>();
    Logger log;
    
    public BlockColor(Logger l){
        
        log = l;
        try{
            loadColormap("latest");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void loadColormap(String colormap) throws FileNotFoundException, IOException{
        log.info(String.format("Attempting to load colormap \"%s\"", colormap));
        BufferedReader br = new BufferedReader(new FileReader(String.format("plugins/MCdisplay/colormaps/%s.txt", colormap)));
        try {
            String colorData;
            List<String[]> allData = new ArrayList<String[]>();
            while((colorData = br.readLine()) != null){
                if(colorData.equals("") || colorData.startsWith("//")){
                    continue;
                }
                allData.add(colorData.split(", "));
            }
            ColorBlock[] temp = new ColorBlock[allData.size()];
            int i = 0;
            log.info("Processing colormap data");
            for(String[] data: allData){
                try {
                    temp[i] = new ColorBlock(Material.matchMaterial(data[0]), Float.valueOf(data[1]), Float.valueOf(data[2]), Float.valueOf(data[3]));
                    i++;
                } catch(Exception e){
                    log.warning("Incorrect color data: " + String.join(" ", data));
                }
            }
            blocks = temp;
            colormapName = colormap;
            log.info(String.format("Sucessfully loaded colormap \"%s\"", colormap));
            
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            br.close();
        }
    }
    
    public String getName(){
        return colormapName;
    }
    
    public void clearCache(){
        cache.clear();
    }
    
    public Material matchColor(float[] color){
        float r = color[0];
        float g = color[1];
        float b = color[2];
        List<Float> colors = Arrays.asList(r, g, b);
        if(cache.containsKey(colors)){
            return cache.get(colors);
        }
        
        if(r == 0){
            r = 1;
        }
        if(g == 0){
            g = 1;
        }
        if(b == 0){
            b = 1;
        }
        Material matchedMaterial = Material.WHITE_CONCRETE;
        double difference = 16777216;
        double temp_diff;
        float[] temp_lab;
        //System.out.println("Color to match: " + " " + r + " " + g + " " + b);
        for(ColorBlock cb: blocks){
            temp_lab = colorConv.fromRGB(new float[]{r, g, b});
            temp_diff = sqrt((temp_lab[0]-cb.color_lab[0])*(temp_lab[0]-cb.color_lab[0])+(temp_lab[1]-cb.color_lab[1])*(temp_lab[1]-cb.color_lab[1])+(temp_lab[2]-cb.color_lab[2])*(temp_lab[2]-cb.color_lab[2]));
            if(temp_diff < difference){
                matchedMaterial = cb.material;
                difference = temp_diff;
            } if(temp_diff == 0){
                break;
            }
        }
        
        cache.put(colors, matchedMaterial);
        return matchedMaterial;
    }
    public Material matchColor(int color){
        color = 16777216 - color;
        int r =   (color & 0x00ff0000) >> 16;
        int g = (color & 0x0000ff00) >> 8;
        int b =   color & 0x000000ff;
        if(r == 0){
            r = 1;
        }
        if(g == 0){
            g = 1;
        }
        if(b == 0){
            b = 1;
        }
        List<Float> colors = Arrays.asList((float)r, (float)g, (float)b);
        if(cache.containsKey(colors)){
            return cache.get(colors);
        }
        Material matchedMaterial = Material.WHITE_CONCRETE;
        double difference = 16777216;
        double temp_diff;
        float[] temp_lab;
        //System.out.println("Color to match: " + " " + r + " " + g + " " + b);
        for(ColorBlock cb: blocks){
            temp_lab = colorConv.fromRGB(new float[]{r, g, b});
            temp_diff = sqrt((temp_lab[0]-cb.color_lab[0])*(temp_lab[0]-cb.color_lab[0])+(temp_lab[1]-cb.color_lab[1])*(temp_lab[1]-cb.color_lab[1])+(temp_lab[2]-cb.color_lab[2])*(temp_lab[2]-cb.color_lab[2]));
            if(temp_diff < difference){
                matchedMaterial = cb.material;
                difference = temp_diff;
            }
            if(temp_diff == 0){
                break;
            }
        }
        cache.put(colors, matchedMaterial);
        return matchedMaterial;
    }
}

