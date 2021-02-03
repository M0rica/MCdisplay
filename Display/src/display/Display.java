/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Der Gerät
 */
public class Display extends JavaPlugin{
    
    Logger log = this.getLogger();
    int w = 256;
    int h = 144;
            
    public void onEnable(){
        log.info("Plugin enabled");
    }
    
    public void onDisable(){
        //despawnDisplay();
        log.info("Plugin disabled");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("display")){
            log.info(args[0]);
            if(args[0].equals("on")){;
                spawnDisplay();
                return true;
            } else if(args[0].equals("off")){
                despawnDisplay();
                return true;
            } else if(args[0].equals("image")){
                drawImage(args[1]);
                return true;
            }
            else if(args[0].equals("resolution")){
                changeResolution(args[1]);
                return true;
            }
        }
        return false;
    }
    
    private void changeResolution(String res){
        despawnDisplay();
        String[] res_list = res.split("x");
        w = Integer.valueOf(res_list[0]);
        h = Integer.valueOf(res_list[1]);
        spawnDisplay();
    }
    
    private void drawImage(String path){
        log.info("Working directory: " + System.getProperty("user.dir"));
        log.info(String.format("Start rendering new image: %s", path));
        Runtime rt = Runtime.getRuntime();
        try{
            log.info("Resizing image");
            Process pr = rt.exec(String.format("python plugins/Display/resize.py plugins/Display/image/%s %dx%d", path, w, h));
            int p = pr.waitFor();
            log.info("Reading resized image " + path);
            File f = new File("plugins/Display/resized/" + path);
            long start = System.currentTimeMillis();
            BufferedImage img = ImageIO.read(f);
            int[][] pixels = new int[w][h];
            for(int i = 0; i < w; i++){
                for(int j = 0; j < h; j++){
                    pixels[i][j] = img.getRGB(i, j);
                }
            }
            //log.info(String.valueOf(pixels[0][0]));
            //log.info(String.valueOf(pixels[23][4]));
            //log.info(String.valueOf(pixels[200][1]));
            //log.info(String.valueOf(pixels[69][12]));
            
            log.info(String.format("Rendering image %s to display", path));
            renderImage(pixels);
            log.info(String.format("Rendering time: %dms", System.currentTimeMillis() - start));
        }
        catch (Exception e){
            log.warning(String.valueOf(e));
        }
    }
    
    private void renderImage(int[][] img){
        World world = Bukkit.getServer().getWorld("display_test");
        for(int i=0; i<img.length; i++){
            for (int j=0; j<img[0].length; j++){
                int diff = Math.abs(img[i][j] * 1);
                //log.info(String.valueOf(diff));
                //max 16581375, 1/16 = 1036335
                // orange = 21825 (226, 99, 0)
                // magenta = 1155453
                Block block = world.getBlockAt(i,10,j);
                Material m;
                if(diff <= 8000000){ //1841616
                    m = Material.WHITE_CONCRETE;
                /*} else if(diff > 1036335 && diff < 2072670){
                    world.getBlockAt(i,10,j).setType(Material.ORANGE_CONCRETE);
                } else if(diff > 2072670 && diff < 3109005){
                    world.getBlockAt(i,10,j).setType(Material.MAGENTA_CONCRETE);*/
                } else if(diff > 8000000 && diff <= 9000000) { //8257152
                    m = Material.LIGHT_GRAY_CONCRETE;
                } else if(diff > 9000000 && diff <= 12500000) { //8257152
                    m = Material.GRAY_CONCRETE;
                } else {
                    m = Material.BLACK_CONCRETE;
                }
                if (m != block.getType()){
                    block.setType(m);
                }
            }
        }
    }
    
    private void spawnDisplay(){
        World world = Bukkit.getServer().getWorld("display_test");
        log.info("Spawning display");
        for(int i=0; i<w; i++){

            for(int j=0; j<h; j++){

                world.getBlockAt(i,10,j).setType(Material.BLACK_CONCRETE);

            }
            //log.info(String.format("Spawend row %d", i));
        }
        log.info("Spawned display");
    }
    
    private void despawnDisplay(){
        World world = Bukkit.getServer().getWorld("display_test");
        log.info("Spawning display");
        for(int i=0; i<w; i++){
            
            for(int j=0; j<h; j++){
                
                world.getBlockAt(i,10,j).setType(Material.AIR);
            
            }
            //log.info(String.format("Despawend row %d", i));
        }
        log.info("Despawned display");
    }
    
}
