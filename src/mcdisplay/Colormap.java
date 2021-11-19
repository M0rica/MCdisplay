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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;

/**
 *
 * @author M0rica
 */
public class Colormap {
    
    
    private class ColorBlock {
    
        Material material;
        int color;
        float red, green, blue;
        float[] colorLab;
    
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
            colorLab = colorConv.fromRGB(new float[]{r, g, b});
        }
    }
    
    private String colormapName;
    private ColorBlock[] blocks;
    private CIELab colorConv = new CIELab();
    private HashMap<List<Float>, Material> cache = new HashMap<>();
    private Logger log;
    
    public Colormap(Logger l){
        log = l;
        try{
            loadColormap("latest");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void loadColormap(String colormap) throws FileNotFoundException, IOException{
        log.info(String.format("Attempting to load colormap \"%s\"", colormap));
        clearCache();
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
    
    public Material map(float[] color){
        float r = color[0];
        float g = color[1];
        float b = color[2];
        List<Float> colors = Arrays.asList(r, g, b);
        Material cached = cache.get(colors);
        if(cached != null){
            return cached;
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
        double tempDiff;
        float[] tempLab;

        for(ColorBlock cb: blocks){
            tempLab = colorConv.fromRGB(new float[]{r, g, b});
            tempDiff = sqrt((tempLab[0]-cb.colorLab[0])*(tempLab[0]-cb.colorLab[0])+(tempLab[1]-cb.colorLab[1])*(tempLab[1]-cb.colorLab[1])+(tempLab[2]-cb.colorLab[2])*(tempLab[2]-cb.colorLab[2]));
            if(tempDiff < difference){
                matchedMaterial = cb.material;
                difference = tempDiff;
            }
        }
        
        cache.put(colors, matchedMaterial);
        return matchedMaterial;
    }
}
