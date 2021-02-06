/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import static java.lang.Math.sqrt;
import java.util.ArrayList;
import org.bukkit.Material;
/**
 *
 * @author M0rica
 */

public class BlockColor{
   
    private class ColorBlock {
    
        Material material;
        int color;
        int red, green, blue;
        float[] color_lab;
    
        private ColorBlock(Material m, int r, int g, int b){
            material = m;
            /*if(r == 0){
                r = 1;
            }
            if(g == 0){
                g = 1;
            }
            if(b == 0){
                b = 1;
            }*/
            //System.out.println(String.format("Block %s color initialized: %d %d %d", m.toString(), r, g, b));
            red = r;
            green = g;
            blue = b;
            color = r*g*b;
            color_lab = colorConv.fromRGB(new float[]{r, g, b});
        }
    }
    
    ArrayList<ColorBlock> blocks = new ArrayList<ColorBlock>();
    CIELab colorConv = new CIELab();
    
    public BlockColor(){
        
        //blocks.add(new ColorBlock(Material., 0, 0, 0));
        // Concrete
        blocks.add(new ColorBlock(Material.WHITE_CONCRETE, 207, 213, 214));
        blocks.add(new ColorBlock(Material.ORANGE_CONCRETE, 233, 96, 0));
        blocks.add(new ColorBlock(Material.MAGENTA_CONCRETE, 170, 45, 160));
        blocks.add(new ColorBlock(Material.LIGHT_BLUE_CONCRETE, 30, 137, 199));
        blocks.add(new ColorBlock(Material.YELLOW_CONCRETE, 243, 178, 15));
        blocks.add(new ColorBlock(Material.LIME_CONCRETE, 93, 168, 16));
        blocks.add(new ColorBlock(Material.PINK_CONCRETE, 214, 100, 143));
        blocks.add(new ColorBlock(Material.GRAY_CONCRETE, 51, 55, 59));
        blocks.add(new ColorBlock(Material.LIGHT_GRAY_CONCRETE, 125, 125, 115));
        blocks.add(new ColorBlock(Material.CYAN_CONCRETE, 15, 121, 138));
        blocks.add(new ColorBlock(Material.PURPLE_CONCRETE, 100, 25, 157));
        blocks.add(new ColorBlock(Material.BLUE_CONCRETE, 42, 45, 145));
        blocks.add(new ColorBlock(Material.BROWN_CONCRETE, 97, 58, 26));
        blocks.add(new ColorBlock(Material.GREEN_CONCRETE, 72, 91, 31));
        blocks.add(new ColorBlock(Material.RED_CONCRETE, 144, 30, 30));
        blocks.add(new ColorBlock(Material.BLACK_CONCRETE, 10, 10, 10));
        
        blocks.add(new ColorBlock(Material.STONE, 119, 119, 119));
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
        Material matchedMaterial = Material.WHITE_CONCRETE;
        double difference = 16777216;
        double temp_diff;
        float[] temp_lab;
        //System.out.println("Color to match: " + " " + r + " " + g + " " + b);
        for(ColorBlock cb: blocks){
            //temp_diff = Math.abs(color-cb.color);
            //temp_diff = (int) (r-cb.red)^2+(g-cb.green)^2+(b-cb.blue)^2;
            temp_lab = colorConv.fromRGB(new float[]{r, g, b});
            temp_diff = sqrt((temp_lab[0]-cb.color_lab[0])*(temp_lab[0]-cb.color_lab[0])+(temp_lab[1]-cb.color_lab[1])*(temp_lab[1]-cb.color_lab[1])+(temp_lab[2]-cb.color_lab[2])*(temp_lab[2]-cb.color_lab[2]));
            //temp_diff = Math.abs(((cb.red-r)^2)+((cb.green-g)^2)+((cb.blue-b)^2));
            //System.out.println("Compared block: " + cb.material + ", Block color: " + cb.color + ", Difference: " + temp_diff);
            if(temp_diff < difference){
                matchedMaterial = cb.material;
                difference = temp_diff;
            }
        }
        
        return matchedMaterial;
    }
}

