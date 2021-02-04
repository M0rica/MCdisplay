/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Der Gerät
 */
public class Display extends JavaPlugin{
    
    Logger log = this.getLogger();
    int w = 256;
    int h = 144;
    
    int videoFrame = 0;
    Material[][][] video = null;
    int pixelW = 0;
    int pixelH = 0;
    int videoID;
    boolean videoPlays = false;
    boolean isPaused = false;
    boolean isVideoPlaying = false;
    String lastvid;
            
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
            switch (args[0]) {
                case "on":
                    spawnDisplay();
                    return true;
                case "off":
                    despawnDisplay();
                    return true;
                case "image":
                    Bukkit.broadcastMessage("[Display] Rendering image " + args[1]);
                    drawImage(args[1]);
                    return true;
                case "pause":
                    log.info(String.valueOf(isVideoPlaying));
                    if(isVideoPlaying && !isPaused){
                        isPaused = true;
                    } else {
                        isPaused = false;
                    }
                    return true;
                case "stop":
                    videoFrame = video.length;
                    Bukkit.broadcastMessage("Stopped video");
                    return true;
                case "resolution":
                    changeResolution(args[1]);
                    return true;
                case "video":
                    if(!videoPlays){
                        videoPlays = true;
                        changeResolution("128x72");
                        Bukkit.broadcastMessage("[Display] Preparing video " + args[1] + ", this may take a while (up to several minutes depending on the length of the video)!");
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                if(args[1].equals(lastvid)){
                                    drawVideo(args[1], true);
                                } else {
                                    log.info("New Video");
                                    lastvid = args[1];
                                    drawVideo(args[1], false);
                                }
                            }
                        });
                    } else {
                        Bukkit.broadcastMessage("[Display] There is allready a video playing or in preparation!");
                    }
                    return true;
                case "replay":
                    Bukkit.broadcastMessage("[Display] Playing last video");
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                drawVideo("placeholder", true);
                            }
                        });
                    return true;
                default:
                    break;
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
    
    private void drawVideo(String path, boolean replay){
        Runtime rt = Runtime.getRuntime();
        try{
            if(!replay){
                log.info(String.format("Start rendering video: %s", path));
                Process pr = rt.exec(String.format("python plugins/Display/resize.py plugins/Display/video/%s %dx%d", path, w, h));
                int p = pr.waitFor();
                log.info("Rendered video");
            }
            log.info("Reading video data");
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("plugins/Display/resized/video.json");
            Object obj = jsonParser.parse(reader);
            JSONObject data = (JSONObject) obj;
            JSONArray pixels = (JSONArray) data.get("data");
            long lframes = (long)data.get("frames");
            int frames = Math.toIntExact(lframes);
            log.info(String.format("Number of Frames: %d", frames));
            video = new Material[frames][h][w];
            pixels.forEach(i -> parseVideoArray((JSONArray)i));
            //log.info(String.valueOf(pixels));
            videoFrame = 0;
            pixelW = 0;
            pixelH = 0;
            isVideoPlaying = true;
            this.videoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                //log.info(String.valueOf(isPaused));
                if(!isPaused){
                    try{
                        renderVideoFrame();
                    } catch (Exception e){
                        e.printStackTrace();
                        videoFrame = video.length;
                    }
                    if(videoFrame>=video.length){
                        video = null;
                        Bukkit.getServer().getScheduler().cancelTask(videoID);
                        videoPlays = false;
                        videoFrame = 0;
                        isVideoPlaying = false;
                        Bukkit.broadcastMessage("[Display] Done playing video");
                    }
                }
            }
        }, 0L, 1L);
        }
        catch(Exception e){
            e.printStackTrace();
            log.info(String.format("Frame: %d, Width: %d, Height: %d", videoFrame, pixelW, pixelH));
        }
    }
    
    private void parseVideoArray(JSONArray pixels){
        pixels.forEach(p -> parseVideoArray2((JSONArray)p));
        videoFrame++;
        pixelW = 0;
        pixelH = 0;
    }
    
    private void parseVideoArray2(JSONArray pixels){
        pixelW = 0;
        pixels.forEach(p -> createVideoArray((long)p));
        pixelH++;
    }
    
    public void createVideoArray(long lpixel){
        int pixel = Math.toIntExact(lpixel);
        Material m = Material.BLACK_CONCRETE;
        if(pixel <= 8000000){ //1841616
            m = Material.WHITE_CONCRETE;
        /*} else if(diff > 1036335 && diff < 2072670){
            world.getBlockAt(i,10,j).setType(Material.ORANGE_CONCRETE);
        } else if(diff > 2072670 && diff < 3109005){
            world.getBlockAt(i,10,j).setType(Material.MAGENTA_CONCRETE);*/
        } else if(pixel > 8000000 && pixel <= 9000000) { //8257152
            m = Material.LIGHT_GRAY_CONCRETE;
        } else if(pixel > 9000000 && pixel <= 12500000) { //8257152
            m = Material.GRAY_CONCRETE;
        } else {
            m = Material.BLACK_CONCRETE;
        }
        video[videoFrame][pixelH][pixelW] = m;
        pixelW++;
    }
    
    private void renderVideoFrame(){
        long start = System.currentTimeMillis();
        World world = Bukkit.getServer().getWorld("display_test");
        Material[][] frame = video[videoFrame];
        if(videoFrame<video.length){
            for(int i=0; i<frame.length; i++){
                for(int j=0; j<frame[0].length; j++){
                    world.getBlockAt(j,10,i).setType(frame[i][j]);
                }
            }
            videoFrame++;
            log.info(String.format("Time taken for frame #%d: %dms", videoFrame, System.currentTimeMillis()-start));
        }
    }
    
    private void renderImage(int[][] img){
        World world = Bukkit.getServer().getWorld("display_test");
        for(int i=0; i<img.length; i++){
            for (int j=0; j<img[0].length; j++){
                //int diff = Math.abs(img[i][j] * 1);
                int diff = -img[i][j];
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
