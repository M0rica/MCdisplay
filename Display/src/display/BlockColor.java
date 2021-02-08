/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    
    ColorBlock[] blocks;
    Material lastMaterial = Material.BLACK_CONCRETE;
    float lastR, lastG, lastB = 0;
    CIELab colorConv = new CIELab();
    HashMap<List<Float>, Material> cache = new HashMap<>();
    
    public BlockColor(){
        
        //lastBlock = new ColorBlock(Material.BLACK_CONCRETE, 50, 50, 50);
        //new ColorBlock(Material., 0, 0, 0),
        // concrete
        blocks = new ColorBlock[]{
        new ColorBlock(Material.BLACK_CONCRETE, 20, 20, 20),
        new ColorBlock(Material.WHITE_CONCRETE, 230, 230, 230),
        new ColorBlock(Material.ORANGE_CONCRETE, 233, 96, 0),
        new ColorBlock(Material.MAGENTA_CONCRETE, 170, 45, 160),
        new ColorBlock(Material.LIGHT_BLUE_CONCRETE, 30, 137, 199),
        new ColorBlock(Material.YELLOW_CONCRETE, 243, 178, 15),
        new ColorBlock(Material.LIME_CONCRETE, 90, 168, 16),
        new ColorBlock(Material.PINK_CONCRETE, 214, 100, 143),
        new ColorBlock(Material.GRAY_CONCRETE, 55, 55, 55),
        new ColorBlock(Material.LIGHT_GRAY_CONCRETE, 125, 125, 115),
        new ColorBlock(Material.CYAN_CONCRETE, 15, 121, 138),
        new ColorBlock(Material.PURPLE_CONCRETE, 100, 25, 157),
        new ColorBlock(Material.BLUE_CONCRETE, 42, 45, 145),
        new ColorBlock(Material.BROWN_CONCRETE, 97, 58, 26),
        new ColorBlock(Material.GREEN_CONCRETE, 72, 91, 31),
        new ColorBlock(Material.RED_CONCRETE, 144, 30, 30),
        
        // wool
        new ColorBlock(Material.BLACK_WOOL, 40, 40, 40),
        new ColorBlock(Material.WHITE_WOOL, 250, 250, 250),
        new ColorBlock(Material.ORANGE_WOOL, 235, 108, 2),
        new ColorBlock(Material.MAGENTA_WOOL, 180, 60, 170),
        new ColorBlock(Material.LIGHT_BLUE_WOOL, 50, 150, 200),
        new ColorBlock(Material.YELLOW_WOOL, 250, 200, 50),
        new ColorBlock(Material.LIME_WOOL, 100, 180, 20),
        new ColorBlock(Material.PINK_WOOL, 240, 130, 150),
        new ColorBlock(Material.GRAY_WOOL, 65, 65, 65),
        new ColorBlock(Material.LIGHT_GRAY_WOOL, 140, 140, 140),
        new ColorBlock(Material.CYAN_WOOL, 20, 145, 145),
        new ColorBlock(Material.PURPLE_WOOL, 130, 42, 180),
        new ColorBlock(Material.BLUE_WOOL, 55, 60, 160),
        new ColorBlock(Material.BROWN_WOOL, 120, 76, 45),
        new ColorBlock(Material.GREEN_WOOL, 90, 120, 15),
        new ColorBlock(Material.RED_WOOL, 170, 40, 30),
        
        new ColorBlock(Material.STONE, 119, 119, 119),
        };
    }
    
    public void clearCache(){
        cache.clear();
    }
    
    public Material matchColor(float[] color){
        //color = 16777216 - color;
        //int r =   (color & 0x00ff0000) >> 16;
        //int g = (color & 0x0000ff00) >> 8;
        //int b =   color & 0x000000ff;
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
        
        //lastMaterial = matchedMaterial;
        //lastR = r;
        //lastG = g;
        //lastB = b;
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

