/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author M0rica
 */

public class Display extends JavaPlugin{
    
    String backend_path = "python plugins/MCdisplay/resize.py";
    String base_path = "plugins/MCdisplay";
    
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
    
    VideoMapRenderer[] videoMaps;
    boolean mapVideoPrepared = false;
    int mapVideoFrames;
    
    BlockColor colormap;
    World world;
    MapDisplay mapdisplay;
    
    public void onEnable(){
        log.info("Rendering backend: " + backend_path);
        world = Bukkit.getWorlds().get(0);
        checkDirs();
        colormap = new BlockColor(log);
        mapdisplay = new MapDisplay(this, (Plugin)this, log, world);
        TabExecutor tabExecutor = new DisplayTabExecuter(this);
        TabExecutor tabExecutorMap = new MapDisplayTabExecuter(mapdisplay);
        this.getCommand("display").setExecutor(tabExecutor);
        this.getCommand("display").setTabCompleter(tabExecutor);
        this.getCommand("mapdisplay").setExecutor(tabExecutorMap);
        this.getCommand("mapdisplay").setTabCompleter(tabExecutorMap);
        log.info("Plugin enabled");
    }
    
    public void onDisable(){
        log.info("Plugin disabled");
    }
    
    public boolean processCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("display")){
            log.info(args[0]);
            switch (args[0]) {
                case "on":
                    spawnDisplay();
                    broadcastMsg("Display turned on!");
                    return true;
                case "off":
                    despawnDisplay();
                    broadcastMsg("Display turned off!");
                    return true;
                case "image":
                    String path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if(isURL(path) || doesFileExists("image/" + path)){
                        if(!videoPlays){
                            drawImage(path);
                        } else {
                            broadcastErr("There is a video playing or rendering, you can't render an image right now!");
                        }
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
                    try{
                        videoFrame = video.length;
                    } catch(Exception e){
                        videoFrame = frames;
                    }
                    broadcastMsg("Stopped video");
                    return true;
                case "resolution":
                    if(args.length == 1){
                        broadcastMsg(String.format("Current display resolution: %dx%d", w, h));
                    } else {
                        if(!videoPlays){
                            changeResolution(args[1]);
                            broadcastMsg("Successfully changed resolution to " + args[1] + "!");
                        } else {
                            broadcastErr("Can't change reolution while playing a video!");
                        }
                    }
                    return true;
                case "video":
                    path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if(doesFileExists("video/" + path)){
                        if(!videoPlays){
                            videoPlays = true;
                            if(w>256 || h>251){
                                changeResolution("256x144");
                            }
                            if(!vertical){
                                despawnDisplay();
                                vertical = true;
                                spawnDisplay();
                            }
                            broadcastMsg("Preparing video " + path + ", may take a while!");
                            Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                                @Override
                                public void run() {
                                    if(path.equals(lastvid) && w == lastW && h == lastH){
                                        drawVideo(path, true);
                                    } else {
                                        lastW = w;
                                        lastH = h;
                                        lastvid = path;
                                        drawVideo(path, false);
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
                                    lastW = w;
                                    lastH = h;
                                    drawVideo(lastvid, false);
                                }
                                }
                            });
                    } else {
                        broadcastErr("There is no video in memory to play!");
                    }
                    return true;
                case "colormap":
                    if(!videoPlays){
                        if(args.length == 2){
                            try{
                                colormap.loadColormap(args[1]);
                                broadcastMsg("Changed colormap to " + args[1]);
                            } catch(Exception e){
                                e.printStackTrace();
                                broadcastErr(String.format("An error occurred while trying to load colormap \"%s\"", args[1]));
                            }
                        } else {
                            broadcastMsg("Current colormap: " + colormap.getName());
                        }
                    }
                    return true;
                case "tp": case "center":
                    teleportPlayer((Player) sender);
                    return true;
                default:
                    break;
            }
        } else if(cmd.getName().equalsIgnoreCase("mapdisplay")){
            return mapdisplay.processCommand(sender, cmd, commandLabel, args);
        }
        return false;
    }
    
    private void checkDirs(){
        String[] dirs = new String[]{"image", "video", "downloads", "resized"};
        for(String dir: dirs){
            File directory = new File(String.format("%s/%s", base_path, dir));
            if(!directory.exists()){
                directory.mkdir();
            }
        }
        File colormapDir = new File(String.format("%s/colormaps", base_path));
        if(!colormapDir.exists()){
            broadcastErr("No colormap directory found, you won't be able to render any image or video until you add a colormap!");
        }
    }
    
    public void canRender(boolean b){
        b = !b;
        log.info(String.valueOf(b));
        videoPlays = b;
        isVideoPlaying = b;
    }
    
    public boolean doesFileExists(String file){
        log.info(String.format("\"plugins/MCdisplay/%s\"", file));
        boolean isFile = new File(String.format("plugins/MCdisplay/%s", file)).isFile();
        if(!isFile){
            broadcastErr(String.format("The file %s does not exist!", file));
        }
        return isFile;
    }
    
    private boolean isURL(String path){
        return path.startsWith("https://") || path.startsWith("http://");
    }
    
    public static BufferedImage toBufferedImage(Image img){
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
    
    private boolean downloadImg(String path){
        broadcastMsg("Downloading image from " + path);
        try{
            URL url = new URL(path);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
            Image img = ImageIO.read(connection.getInputStream());
            BufferedImage out = toBufferedImage(img);
            ImageIO.write(out, "jpg", new File("plugins/MCdisplay/downloads/download.jpg"));
            broadcastMsg("Download successful!");
            return true;
        } catch(Exception e){
            e.printStackTrace();
            broadcastErr("Failed to download image from given url!");
            return false;
        }
    }
    
    public void broadcastMsg(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Display] " + ChatColor.LIGHT_PURPLE + msg);
            }
        });
    }
    
    public void broadcastWarn(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.YELLOW + "[Display] Warning: " + ChatColor.GOLD + msg);
            }
        });
    }
    
    public void broadcastErr(String msg){
        Bukkit.getScheduler().runTask(this, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "[Display] Error: " + ChatColor.RED + msg);
            }
        });
    }
    
    private void teleportPlayer(Player player){
        Location location = new Location(world, w/2, (w+h)/2/4, h/2, -180, 90);
        player.teleport(location);
    }
    
    private void changeResolution(String res){
        despawnDisplay();
        String[] res_list = res.split("x");
        w = Integer.valueOf(res_list[0]);
        h = Integer.valueOf(res_list[1]);
        spawnDisplay();
    }
    
    private void drawImage(String path){
        if(!isURL(path)){
            broadcastMsg("Rendering image " + path);
        }
        if(vertical){
            despawnDisplay();
            vertical = false;
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable(){
            @Override
            public void run(){
                long start = System.currentTimeMillis();
                log.info(String.format("Start rendering new image: %s", path));
                Runtime rt = Runtime.getRuntime();
                try{
                    String readPath = path;
                    String prString = String.format("%s \"plugins/MCdisplay/image/%s\" %dx%d", backend_path, path, w, h);
                    if(isURL(path)){
                        boolean success = downloadImg(path);
                        if(!success){
                            return;
                        }
                        broadcastMsg("Rendering downloaded image");
                        prString = String.format("%s \"plugins/MCdisplay/downloads/download.jpg\" %dx%d", backend_path, w, h);
                        readPath = "plugins/MCdisplay/resized/download.jpg";
                    }
                    log.info("Resizing image");
                    Process pr = rt.exec(prString);
                    log.info(prString);
                    int p = pr.waitFor();
                    if(path.endsWith(".webp")){
                        readPath = path.split("\\.")[0] + ".jpg";
                    }
                    log.info("Reading resized image");
                    File f = new File("plugins/MCdisplay/resized/resized.jpg");
                    BufferedImage img = ImageIO.read(f);
                    log.info("Colormapping " + readPath);
                    Material[][] pixels = new Material[w][h];
                    for(int i = 0; i < w; i++){
                        for(int j = 0; j < h; j++){
                            pixels[i][j] = colormap.matchColor(-img.getRGB(i, j));
                        }
                    }
                    colormap.clearCache();
                    log.info(String.format("Rendering image %s to display", readPath));
                    
                    renderImage(pixels);
                    log.info(String.format("Rendering time: %dms", System.currentTimeMillis() - start));
                    broadcastMsg(String.format("Rendered image in %.2fs", ((double)System.currentTimeMillis() - start)/1000));
                }
                catch (Exception e){
                    log.warning(String.valueOf(e));
                    broadcastErr("Something went wrong, " + e.getMessage());
                }
            }
        });
    }
    
    private void drawVideo(String path, boolean replay){
        mapdisplay.blockActions();
        Runtime rt = Runtime.getRuntime();
        try{
            if(!replay){
                log.info("New Video");
                log.info(String.format("Start rendering video: %s", path));
                broadcastMsg(String.format("Start rendering video: %s", path));
                Process pr;
                if(w*h<10000){
                    pr = rt.exec(String.format("%s \"plugins/MCdisplay/video/%s\" %dx%d --fps 20", backend_path, path, w, h));
                } else {
                    pr = rt.exec(String.format("%s \"plugins/MCdisplay/video/%s\" %dx%d --fps 10", backend_path, path, w, h));
                }
                int p = pr.waitFor();
                log.info("Rendered video");
                broadcastMsg("Rendered video");
                
                log.info("Reading video data");
                JSONParser jsonParser = new JSONParser();
                FileReader reader = new FileReader("plugins/MCdisplay/resized/video.json");
                Object obj = jsonParser.parse(reader);
                JSONObject data = (JSONObject) obj;
                long lframes = (long)data.get("frames");
                frames = Math.toIntExact(lframes);
                log.info(String.format("Number of frames: %d", frames));
                broadcastMsg(String.format("Number of frames: %d", frames));
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
                    f = new File(String.format("plugins/MCdisplay/resized/%s_%d.jpg", path, videoFrame));
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
                        log.info(String.format("Colormapped %d frames", videoFrame));
                        broadcastMsg(String.format("Colormapped %d frames", videoFrame));
                    }
                }
                colormap.clearCache();
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
            //mapdisplay.unblockActions();
            this.videoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                //log.info(String.valueOf(isPaused));
                if(!isPaused){
                    if(videoFrame>=video.length){
                        //video = null;
                        Bukkit.getServer().getScheduler().cancelTask(videoID);
                        videoPlays = false;
                        videoFrame = 0;
                        isVideoPlaying = false;
                        mapdisplay.unblockActions();
                        broadcastMsg("Done playing video");
                    }
                    try{
                        renderVideoFrame();
                    } catch (Exception e){
                        broadcastErr(String.valueOf(e));
                        e.printStackTrace();
                        videoFrame = video.length;
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
            mapdisplay.unblockActions();
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
            long timeTaken = System.currentTimeMillis()-start;
            String infoString = String.format("Time taken for frame #%d: %dms", videoFrame, timeTaken);
            log.info(infoString);
            if(timeTaken > 6){
                broadcastWarn(infoString);
            }
        }
    }
    
    private void renderImage(Material[][] img){
        Bukkit.getScheduler().runTaskLater(this, new Runnable(){
            public void run(){
                Block block;
                Material m;
                for(int i=0; i<img.length; i++){
                    for (int j=0; j<img[0].length; j++){
                        block = world.getBlockAt(i,3,j);
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
        log.info("Spawning display");
        for(int i=0; i<w; i++){

            for(int j=0; j<h; j++){
                
                if(vertical){
                    world.getBlockAt(i,h-j+5,0).setType(Material.BLACK_CONCRETE);
                } else {
                    world.getBlockAt(i,3,j).setType(Material.BLACK_CONCRETE);
                }
            }
            //log.info(String.format("Spawend row %d", i));
        }
        log.info("Spawned display");
    }
    
    private void despawnDisplay(){
        log.info("Despawning display");
        for(int i=0; i<w; i++){
            
            for(int j=0; j<h; j++){
                
                if(vertical){
                    world.getBlockAt(i,h-j+5,0).setType(Material.AIR);
                } else {
                    world.getBlockAt(i,3,j).setType(Material.AIR);
                }
            }
            //log.info(String.format("Despawend row %d", i));
        }
        log.info("Despawned display");
    }
    
}
