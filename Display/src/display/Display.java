/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
    int frames = 0;
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
        TabExecutor tabExecutor = new DisplayTabExecuter(this);
        this.getCommand("display").setExecutor(tabExecutor);
        this.getCommand("display").setTabCompleter(tabExecutor);
        log.info("Plugin enabled");
    }
    
    public void onDisable(){
        //despawnDisplay();
        log.info("Plugin disabled");
    }
    
    public boolean processCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("display")){
            log.info(args[0]);
            switch (args[0]) {
                case "on":
                    spawnDisplay();
                    broadcastMsg("Turned display on!");
                    return true;
                case "off":
                    despawnDisplay();
                    broadcastMsg("Turned display off!");
                    return true;
                case "image":
                    if(doesFileExists("image/" + args[1])){
                        if(!videoPlays){
                            broadcastMsg("Rendering image " + args[1]);
                            drawImage(args[1]);
                        } else {
                            broadcastErr("There is a video playing or rendering, you can't render an image right now!");
                        }
                    } else {
                        broadcastErr(String.format("The file %s does not exist!", args[1]));
                    }
                    return true;
                case "pause":
                    log.info(String.valueOf(isVideoPlaying));
                    if(isVideoPlaying && !isPaused){
                        isPaused = true;
                        broadcastMsg("Video paused");
                    } else {
                        isPaused = false;
                        broadcastMsg("Video resumed");
                    }
                    return true;
                case "stop":
                    videoFrame = video.length;
                    broadcastMsg("Stopped video");
                    return true;
                case "resolution":
                    if(!videoPlays){
                        changeResolution(args[1]);
                    } else {
                        broadcastErr("Can't change reolution while playing a video!");
                    }
                    broadcastMsg("Successfully changed resolution to " + args[1] + "!");
                    return true;
                case "video":
                    if(doesFileExists("video/" + args[1])){
                        if(!videoPlays){
                            videoPlays = true;
                            if(w>256 || h>251){
                                changeResolution("256x144");
                            }
                            if(!vertical){
                                despawnDisplay();
                                vertical = true;
                            }
                            broadcastMsg("Preparing video " + args[1] + ", may take a while!");
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
                            broadcastErr("There is allready a video playing or in preparation!");
                        }
                    } else {
                        broadcastErr(String.format("The file %s does not exist!", args[1]));
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
                        broadcastMsg("Playing last video");
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
                        broadcastErr("There is no last video to play!");
                    }
                    return true;
                default:
                    break;
            }
        }
        return false;
    }
    
    private boolean doesFileExists(String file){
        return new File(String.format("plugins/MCdisplay/%s", file)).isFile();
    }
    
    private void broadcastMsg(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Display] " + ChatColor.LIGHT_PURPLE + msg);
            }
        });
    }
    
    private void broadcastErr(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "[Display] " + ChatColor.RED + msg);
            }
        });
    }
    
    private void changeResolution(String res){
        despawnDisplay();
        String[] res_list = res.split("x");
        w = Integer.valueOf(res_list[0]);
        h = Integer.valueOf(res_list[1]);
        spawnDisplay();
    }
    
    private void drawImage(String path){
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable(){
            @Override
            public void run(){
                if(vertical){
                    despawnDisplay();
                    vertical = false;
                }
                long start = System.currentTimeMillis();
                log.info(String.format("Start rendering new image: %s", path));
                Runtime rt = Runtime.getRuntime();
                try{
                    log.info("Resizing image");
                    Process pr = rt.exec(String.format("python plugins/MCdisplay/resize.py plugins/MCdisplay/image/%s %dx%d", path, w, h));
                    int p = pr.waitFor();
                    log.info("Reading resized image " + path);
                    File f = new File("plugins/MCdisplay/resized/" + path);
                    BufferedImage img = ImageIO.read(f);
                    Material[][] pixels = new Material[w][h];
                    for(int i = 0; i < w; i++){
                        for(int j = 0; j < h; j++){
                            pixels[i][j] = colormap.matchColor(-img.getRGB(i, j));
                        }
                    }
                    //log.info(String.valueOf(pixels[0][0]));
                    //log.info(String.valueOf(pixels[23][4]));
                    //log.info(String.valueOf(pixels[200][1]));
                    //log.info(String.valueOf(pixels[69][12]));

                    
                    log.info(String.format("Rendering image %s to display", path));
                    
                    renderImage(pixels);
                    log.info(String.format("Rendering time: %dms", System.currentTimeMillis() - start));
                    broadcastMsg(String.format("Rendered image in: %.2fs", ((double)System.currentTimeMillis() - start)/1000));
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
                broadcastMsg(String.format("Start rendering video: %s", path));
                Process pr;
                if(w*h<10000){
                    pr = rt.exec(String.format("python plugins/Display/resize.py plugins/Display/video/%s %dx%d", path, w, h));
                } else {
                    pr = rt.exec(String.format("python plugins/Display/resize.py plugins/Display/video/%s %dx%d --fps 10", path, w, h));
                }
                int p = pr.waitFor();
                log.info("Rendered video");
                broadcastMsg("Rendered video");
                
                log.info("Reading video data");
                JSONParser jsonParser = new JSONParser();
                FileReader reader = new FileReader("plugins/Display/resized/video.json");
                Object obj = jsonParser.parse(reader);
                JSONObject data = (JSONObject) obj;
                long lframes = (long)data.get("frames");
                frames = Math.toIntExact(lframes);
                log.info(String.format("Number of Frames: %d", frames));
                broadcastMsg(String.format("Number of Frames: %d", frames));
                reader.close();
                
                long start = System.currentTimeMillis();
                video = new Material[frames][h][w];
                path = path.split("\\.")[0];
                int width, height;
                float[] rgb = new float[3];
                
                BufferedImage img;
                File f;
                broadcastMsg("Colormapping frames...");
                for(videoFrame = 0; videoFrame<frames; videoFrame++){
                    f = new File(String.format("plugins/Display/resized/%s_%d.jpg", path, videoFrame));
                    img = ImageIO.read(f);
                    //img = images.get(videoFrame);
                    //images.remove(0);
                    byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                    width = img.getWidth();
                    height = img.getHeight();
                    pixelH = 0;
                    pixelW = 0;
                    for(int pixel = 0; pixel+2<pixels.length; pixel+=3){
                        rgb[0] = (float) (pixels[pixel] & 0xff);
                        rgb[1] = (float) (pixels[pixel+1] & 0xff);
                        rgb[2] = (float) (pixels[pixel+2] & 0xff);
                        createVideoArray(rgb);
                        pixelW++;
                        if(pixelW == width){
                            pixelW = 0;
                            pixelH++;
                        }
                    }
                    if(videoFrame % 100 == 0){
                        log.info(String.format("Rendered %d frames", videoFrame));
                        broadcastMsg(String.format("Colormaped %d frames", videoFrame));
                    }
                }
                log.info(String.format("Time taken for colormapping: %dms", System.currentTimeMillis() - start));
                broadcastMsg(String.format("Time taken for colormapping: %ds", (System.currentTimeMillis() - start)/1000));
            }
            //log.info(String.valueOf(video[0][0][0]));
            videoFrame = 0;
            pixelW = 0;
            pixelH = 0;
            isVideoPlaying = true;
            long ticks = 1L;
            if(w*h>10000){
                ticks = 2L;
            }
            broadcastMsg("Starting video in 5s");
            this.videoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                //log.info(String.valueOf(isPaused));
                if(!isPaused){
                    try{
                        renderVideoFrame();
                    } catch (Exception e){
                        broadcastErr(String.valueOf(e));
                        e.printStackTrace();
                        videoFrame = video.length;
                    }
                    if(videoFrame>=video.length){
                        //video = null;
                        Bukkit.getServer().getScheduler().cancelTask(videoID);
                        videoPlays = false;
                        videoFrame = 0;
                        isVideoPlaying = false;
                        broadcastMsg("Done playing video");
                    }
                }
            }
        }, 100L, ticks);
        }
        catch(Exception e){
            broadcastErr(String.valueOf(e));
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
    
    
    public void createVideoArray(float[] pixel){
        Material m = colormap.matchColor(pixel);
        if(m == null){
            log.info("No Material!");
        }
        video[videoFrame][pixelH][pixelW] = m;
    }
    
    private void renderVideoFrame(){
        long start = System.currentTimeMillis();
        World world = Bukkit.getServer().getWorld("display_test");
        Material[][] frame = video[videoFrame];
        Material blockmaterial;
        Material newMaterial = Material.BLACK_CONCRETE;
        Block block;
        if(videoFrame<video.length){
            for(int i=0; i<frame.length; i++){
                for(int j=0; j<frame[0].length; j++){
                    block = world.getBlockAt(j,h-i+5,0);
                    blockmaterial = block.getType();
                    newMaterial = frame[i][j];
                    //log.info(String.valueOf(newMaterial == null));
                    //log.info(newMaterial.toString());
                    if(newMaterial != blockmaterial){
                        block.setType(newMaterial);
                    }
                }
            }
            videoFrame++;
            log.info(String.format("Time taken for frame #%d: %dms", videoFrame, System.currentTimeMillis()-start));
        }
    }
    
    private void renderImage(Material[][] img){
        Bukkit.getScheduler().runTaskLater(this, new Runnable(){
            public void run(){
                Block block;
                Material m;
                World world = Bukkit.getServer().getWorld("display_test");
                for(int i=0; i<img.length; i++){
                    for (int j=0; j<img[0].length; j++){
                        block = world.getBlockAt(i,5,j);
                        m = img[i][j];
                        if (m != block.getType()){
                            block.setType(m);
                        }
                    }
                }
            }
        }, 1L);
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
