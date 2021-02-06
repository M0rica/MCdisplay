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
 * @author M0rica
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
    boolean vertical = false;
    int lastW, lastH;
    String lastvid;
    
    BlockColor colormap;
            
    public void onEnable(){
        colormap = new BlockColor();
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
                    if(!videoPlays){
                        changeResolution(args[1]);
                    } else {
                        Bukkit.broadcastMessage("Can't change reolution while playing a video!");
                    }
                    return true;
                case "video":
                    if(!videoPlays){
                        videoPlays = true;
                        if(w>256 || h>251){
                            changeResolution("256x144");
                        }
                        if(!vertical){
                            despawnDisplay();
                            vertical = true;
                        }
                        Bukkit.broadcastMessage("[Display] Preparing video " + args[1] + ", may take a while depending on the length of the video!");
                        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                            @Override
                            public void run() {
                                if(args[1].equals(lastvid) && w == lastW && h == lastH){
                                    drawVideo(args[1], true);
                                } else {
                                    log.info("New Video");
                                    lastW = w;
                                    lastH = h;
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
                    if(!lastvid.matches("") && !videoPlays){
                        videoPlays = true;
                        if(w>256 || h>251){
                            changeResolution("256x144");
                        }
                        if(!vertical){
                            despawnDisplay();
                            vertical = true;
                        }
                        Bukkit.broadcastMessage("[Display] Playing last video");
                            Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                                @Override
                                public void run() {
                                    if(w == lastW && h == lastH){
                                    drawVideo(lastvid, true);
                                } else {
                                    log.info("New Video");
                                    lastW = w;
                                    lastH = h;
                                    lastvid = args[1];
                                    drawVideo(lastvid, false);
                                }
                                }
                            });
                    } else{
                        Bukkit.broadcastMessage("There is no last video to play!");
                    }
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
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                if(vertical){
                    despawnDisplay();
                    vertical = false;
                }
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
        });
    }
    
    private void drawVideo(String path, boolean replay){
        Runtime rt = Runtime.getRuntime();
        try{
            if(!replay){
                log.info(String.format("Start rendering video: %s", path));
                Process pr;
                if(w*h<10000){
                    pr = rt.exec(String.format("python plugins/Display/resize.py plugins/Display/video/%s %dx%d", path, w, h));
                } else {
                    pr = rt.exec(String.format("python plugins/Display/resize.py plugins/Display/video/%s %dx%d --fps 10", path, w, h));
                }
                int p = pr.waitFor();
                log.info("Rendered video");
            }
            log.info("Reading video data");
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader("plugins/Display/resized/video.json");
            Object obj = jsonParser.parse(reader);
            JSONObject data = (JSONObject) obj;
            long lframes = (long)data.get("frames");
            int frames = Math.toIntExact(lframes);
            log.info(String.format("Number of Frames: %d", frames));
            reader.close();
            video = new Material[frames][h][w];
            path = path.split("\\.")[0];
            for(videoFrame = 0; videoFrame<frames; videoFrame++){
                File f = new File(String.format("plugins/Display/resized/%s_%d.jpg", path, videoFrame));
                BufferedImage img = ImageIO.read(f);
                for(pixelH = 0; pixelH<h; pixelH++){
                    for(pixelW = 0; pixelW<w; pixelW++){
                        createVideoArray(-img.getRGB(pixelW, pixelH));
                        //log.info(String.valueOf(img.getRGB(pixelW, pixelH)));
                        //log.info(String.valueOf(video[videoFrame][pixelH][pixelW]));
                    }
                }
            }
            //log.info(String.valueOf(video));
            videoFrame = 0;
            pixelW = 0;
            pixelH = 0;
            isVideoPlaying = true;
            long ticks = 1L;
            if(w*h>10000){
                ticks = 2L;
            }
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
        }, 0L, ticks);
        }
        catch(Exception e){
            e.printStackTrace();
            log.info(String.format("Frame: %d, Width: %d, Height: %d", videoFrame, pixelW, pixelH));
            videoFrame = 0;
            video = null;
            pixelW = 0;
            pixelH = 0;
            videoPlays = false;
            isPaused = false;
            isVideoPlaying = false;
        }
    }
    
    /*private void parseVideoArray(JSONArray pixels){
        pixels.forEach(p -> parseVideoArray2((JSONArray)p));
        videoFrame++;
        pixelW = 0;
        pixelH = 0;
    }*/
    
    /*private void parseVideoArray2(JSONArray pixels){
        pixelW = 0;
        pixels.forEach(p -> createVideoArray((long)p));
        pixelH++;
    }*/
    
    public void createVideoArray(int pixel){
        //int pixel = Math.toIntExact(lpixel);
        Material m = Material.BLACK_CONCRETE;
        if(pixel <= 8000000){ //1841616
            m = Material.WHITE_CONCRETE;
        /*} else if(diff > 1036335 && diff < 2072670){
            world.getBlockAt(i,10,j).setType(Material.ORANGE_CONCRETE);
        } else if(diff > 2072670 && diff < 3109005){
            world.getBlockAt(i,10,j).setType(Material.MAGENTA_CONCRETE);*/
        } else if(pixel > 8000000 && pixel <= 9000000) { //8257152
            m = Material.LIGHT_GRAY_CONCRETE;
        } else if(pixel > 8800000 && pixel <= 9000000) { //8257152
                    m = Material.CYAN_CONCRETE;
        } else if(pixel > 9000000 && pixel <= 14000000) { //8257152
            m = Material.GRAY_CONCRETE;
        } else {
            m = Material.BLACK_CONCRETE;
        }
        video[videoFrame][pixelH][pixelW] = m;
    }
    
    private void renderVideoFrame(){
        long start = System.currentTimeMillis();
        World world = Bukkit.getServer().getWorld("display_test");
        Material[][] frame = video[videoFrame];
        Material blockmaterial;
        Material newMaterial;
        Block block;
        if(videoFrame<video.length){
            for(int i=0; i<frame.length; i++){
                for(int j=0; j<frame[0].length; j++){
                    block = world.getBlockAt(j,h-i+5,0);
                    blockmaterial = block.getType();
                    newMaterial = frame[i][j];
                    if(newMaterial != blockmaterial){
                        block.setType(newMaterial);
                    }
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
                //log.info(String.valueOf(diff));
                //max 16581375, 1/16 = 1036335
                // orange = 21825 (226, 99, 0)
                // magenta = 1155453
                Block block = world.getBlockAt(i,5,j);
                Material m;
                /*if(diff <= 8000000){ //1841616
                    m = Material.WHITE_CONCRETE;
                /*} else if(diff > 1036335 && diff < 2072670){
                    world.getBlockAt(i,10,j).setType(Material.ORANGE_CONCRETE);
                } else if(diff > 2072670 && diff < 3109005){
                    world.getBlockAt(i,10,j).setType(Material.MAGENTA_CONCRETE);
                } else if(diff > 8000000 && diff <= 9000000) { //8257152
                    m = Material.LIGHT_GRAY_CONCRETE;
                } else if(diff > 8600000 && diff <= 9000000) { //8257152
                    m = Material.CYAN_CONCRETE;
                } else if(diff > 9000000 && diff <= 14000000) { //8257152
                    m = Material.GRAY_CONCRETE;
                } else {
                    m = Material.BLACK_CONCRETE;
                }*/
                m = colormap.matchColor(diff);
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
                
                if(vertical){
                    world.getBlockAt(i,h-j+5,0).setType(Material.BLACK_CONCRETE);
                } else {
                    world.getBlockAt(i,5,j).setType(Material.BLACK_CONCRETE);
                }
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
                
                if(vertical){
                    world.getBlockAt(i,h-j+5,0).setType(Material.AIR);
                } else {
                    world.getBlockAt(i,5,j).setType(Material.AIR);
                }
            }
            //log.info(String.format("Despawend row %d", i));
        }
        log.info("Despawned display");
    }
    
}
