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
        //lastBlock = new ColorBlock(Material.BLACK_CONCRETE, 50, 50, 50);
        //new ColorBlock(Material., 0, 0, 0),

        /*blocks = new ColorBlock[]{
            
        // concrete
        new ColorBlock(Material.BLACK_CONCRETE, 10, 12, 17),
        new ColorBlock(Material.WHITE_CONCRETE, 207, 213, 214),
        new ColorBlock(Material.ORANGE_CONCRETE, 224, 97, 1),
        new ColorBlock(Material.MAGENTA_CONCRETE, 169, 48, 159),
        new ColorBlock(Material.LIGHT_BLUE_CONCRETE, 36, 137, 199),
        new ColorBlock(Material.YELLOW_CONCRETE, 241, 175, 21),
        new ColorBlock(Material.LIME_CONCRETE, 94, 169, 24),
        new ColorBlock(Material.PINK_CONCRETE, 214, 101, 143),
        new ColorBlock(Material.GRAY_CONCRETE, 55, 58, 62),
        new ColorBlock(Material.LIGHT_GRAY_CONCRETE, 125, 125, 115),
        new ColorBlock(Material.CYAN_CONCRETE, 21, 119, 136),
        new ColorBlock(Material.PURPLE_CONCRETE, 100, 32, 156),
        new ColorBlock(Material.BLUE_CONCRETE, 45, 47, 143),
        new ColorBlock(Material.BROWN_CONCRETE, 96, 60, 32),
        new ColorBlock(Material.GREEN_CONCRETE, 73, 91, 36),
        new ColorBlock(Material.RED_CONCRETE, 142, 33, 33),
        
        // wool
        new ColorBlock(Material.BLACK_WOOL, 21, 21, 26),
        new ColorBlock(Material.WHITE_WOOL, 234, 236, 237),
        new ColorBlock(Material.ORANGE_WOOL, 241, 118, 20),
        new ColorBlock(Material.MAGENTA_WOOL, 190, 69, 180),
        new ColorBlock(Material.LIGHT_BLUE_WOOL, 58, 175, 217),
        new ColorBlock(Material.YELLOW_WOOL, 249, 198, 40),
        new ColorBlock(Material.LIME_WOOL, 112, 185, 26),
        new ColorBlock(Material.PINK_WOOL, 238, 141, 172),
        new ColorBlock(Material.GRAY_WOOL, 63, 68, 72),
        new ColorBlock(Material.LIGHT_GRAY_WOOL, 142, 142, 135),
        new ColorBlock(Material.CYAN_WOOL, 21, 138, 145),
        new ColorBlock(Material.PURPLE_WOOL, 122, 42, 173),
        new ColorBlock(Material.BLUE_WOOL, 53, 57, 157),
        new ColorBlock(Material.BROWN_WOOL, 114, 72, 41),
        new ColorBlock(Material.GREEN_WOOL, 85, 110, 28),
        new ColorBlock(Material.RED_WOOL, 161, 39, 35),
        
        // terracotta
        new ColorBlock(Material.TERRACOTTA, 152, 94, 68),
        new ColorBlock(Material.BLACK_TERRACOTTA, 37, 23, 16),
        new ColorBlock(Material.BLUE_TERRACOTTA, 74, 60, 91),
        new ColorBlock(Material.BROWN_TERRACOTTA, 77, 51, 36),
        new ColorBlock(Material.CYAN_TERRACOTTA, 87, 91, 91),
        new ColorBlock(Material.GRAY_TERRACOTTA, 58, 42, 36),
        new ColorBlock(Material.GREEN_TERRACOTTA, 76, 83, 42),
        new ColorBlock(Material.LIGHT_BLUE_TERRACOTTA, 113, 109, 138),
        new ColorBlock(Material.LIGHT_GRAY_TERRACOTTA, 135, 107, 98),
        new ColorBlock(Material.LIME_TERRACOTTA, 104, 118, 53),
        new ColorBlock(Material.MAGENTA_TERRACOTTA, 150, 88, 109),
        new ColorBlock(Material.ORANGE_TERRACOTTA, 162, 84, 38),
        new ColorBlock(Material.PINK_TERRACOTTA, 162, 78, 79),
        new ColorBlock(Material.PURPLE_TERRACOTTA, 118, 70, 86),
        new ColorBlock(Material.RED_TERRACOTTA, 143, 61, 47),
        new ColorBlock(Material.WHITE_TERRACOTTA, 210, 178, 161),
        new ColorBlock(Material.YELLOW_TERRACOTTA, 186, 133, 35),
        
        // wood
        new ColorBlock(Material.OAK_PLANKS, 162, 131, 79),
        new ColorBlock(Material.SPRUCE_PLANKS, 115, 85, 49),
        new ColorBlock(Material.BIRCH_PLANKS, 192, 175, 121),
        new ColorBlock(Material.JUNGLE_PLANKS, 160, 115, 81),
        new ColorBlock(Material.ACACIA_PLANKS, 168, 90, 50),
        new ColorBlock(Material.DARK_OAK_PLANKS, 67, 43, 20),
        new ColorBlock(Material.CRIMSON_PLANKS, 101, 49, 71),
        new ColorBlock(Material.WARPED_PLANKS, 43, 105, 99),
        
        // stones
        new ColorBlock(Material.STONE, 119, 119, 119),
        new ColorBlock(Material.COBBLESTONE, 128, 127, 128),
        new ColorBlock(Material.MOSSY_COBBLESTONE, 110, 118, 95),
        new ColorBlock(Material.SMOOTH_STONE, 159, 159, 159),
        
        // andesite etc.
        new ColorBlock(Material.ANDESITE, 136, 136, 137),
        new ColorBlock(Material.POLISHED_ANDESITE, 132, 135, 134),
        
        new ColorBlock(Material.DIORITE, 189, 189, 189),
        new ColorBlock(Material.POLISHED_DIORITE, 193, 193, 195),
        
        new ColorBlock(Material.GRANITE, 149, 103, 86),
        new ColorBlock(Material.POLISHED_GRANITE, 154, 107, 89),
        };*/
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

