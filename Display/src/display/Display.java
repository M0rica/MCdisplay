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
import java.util.Arrays;
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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.map.MapPalette;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author M0rica
 */

public class Display extends JavaPlugin{
    
    String backend_path = "python plugins/MCdisplay/resize.py";
    
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
    
    public void onEnable(){
        colormap = new BlockColor();
        TabExecutor tabExecutor = new DisplayTabExecuter(this);
        this.getCommand("display").setExecutor(tabExecutor);
        this.getCommand("display").setTabCompleter(tabExecutor);
        world = Bukkit.getWorlds().get(0);
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
                    broadcastMsg("Display turned on!");
                    return true;
                case "off":
                    despawnDisplay();
                    broadcastMsg("Display turned off!");
                    return true;
                case "image":
                    String path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if(doesFileExists("image/" + path)){
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
                case "mapimage":
                    path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if(doesFileExists("image/" + path)){
                        if(!videoPlays){
                            drawImageMap(path, (Player) sender);
                        } else {
                            broadcastErr("There is a video playing or rendering, you can't render an image right now!");
                        }
                    }
                    return true;
                    
                case "mapvideo":
                    path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if(doesFileExists("video/" + path)){
                        boolean doReplay = path.equals(lastvid);
                        if(!videoPlays){
                            drawVideoMap(path, (Player) sender, doReplay);
                        } else {
                            broadcastErr("There is a video playing or rendering, you can't render an image right now!");
                        }
                    }
                    return true;
                case "start":
                    if(mapVideoPrepared && !videoPlays){
                        videoPlays = true;
                        startMapVideo();
                    }
                default:
                    break;
            }
        }
        return false;
    }
    
    private boolean doesFileExists(String file){
        log.info(String.format("\"plugins/MCdisplay/%s\"", file));
        boolean isFile = new File(String.format("plugins/MCdisplay/%s", file)).isFile();
        if(!isFile){
            broadcastErr(String.format("The file %s does not exist!", file));
        }
        return isFile;
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
    
    private void drawImageMap(String path, Player player){
        
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable(){
            @Override
            public void run(){
                long start = System.currentTimeMillis();
                log.info(String.format("Start rendering new image: %s", path));
                Runtime rt = Runtime.getRuntime();
                try{
                    int num_maps = getNumMaps();
                    int res = 128*num_maps;
                    log.info("Resizing image");
                    Process pr = rt.exec(String.format("%s \"plugins/MCdisplay/image/%s\" %dx%d", backend_path, path, res, res));
                    log.info(String.format("%s \"plugins/MCdisplay/image/%s\" %dx%d", backend_path, path, res, res));
                    int p = pr.waitFor();
                    log.info("Reading resized image " + path);
                    File f = new File("plugins/MCdisplay/resized/" + path);
                    BufferedImage img = ImageIO.read(f);
                    if(num_maps > 1){
                        renderImageMultipleMaps(img, num_maps, player);
                    } else {
                        renderImageMap(img, player);
                    }
                } catch(Exception e){
                    log.warning(String.valueOf(e));
                }
            }
        });
    }
    
    private int getNumMaps(){
        int num_maps = Math.max(w/128, h/128);
        if(num_maps == 0){
            num_maps = 1;
        }
        return num_maps;
    }
    
    private void renderImageMap(BufferedImage img, Player player){
        Bukkit.getScheduler().runTaskLater(this, new Runnable(){
            public void run(){
                MapView mv;
                boolean giveItem = true;
                ItemStack player_item = player.getInventory().getItemInMainHand();
                if(player_item.getType() == Material.FILLED_MAP){
                    mv = ((MapMeta)player_item.getItemMeta()).getMapView();
                    giveItem = false;
                } else {
                    mv = Bukkit.createMap(world);
                }
                for(MapRenderer r : mv.getRenderers())
                    mv.removeRenderer(r);
                //ImageMapRenderer renderer = new ImageMapRenderer(img);
                mv.addRenderer(new ImageMapRenderer(img));
                ItemStack i = new ItemStack(Material.FILLED_MAP, 1, (short) mv.getId());
                MapMeta mm = (MapMeta) i.getItemMeta();
                mm.setMapView(mv);
                i.setItemMeta(mm);
                if(giveItem){
                    player.getInventory().addItem(i);
                }
            }
        }, 1L);
    }
    
    private void renderImageMultipleMaps(BufferedImage img, int maps, Player player){
        Bukkit.getScheduler().runTaskLater(this, new Runnable(){
            public void run(){
                MapView mv;
                BufferedImage tempImg;
                int num_maps = maps*maps;
                int wmap = 1;
                int hmap = 1;
                int x, y;
                for(int i = 1; i<=num_maps; i++){
                    x = 128*(wmap-1);
                    y = 128*(hmap-1);
                    //log.info(String.format("x:%d, y:%d, w:%d, h:%d", x, y, 128, 128));
                    tempImg = img.getSubimage(x, y, 128, 128);
                    mv = Bukkit.createMap(world);
                    for(MapRenderer r : mv.getRenderers())
                        mv.removeRenderer(r);
                    mv.addRenderer(new ImageMapRenderer(tempImg));
                    ItemStack item = new ItemStack(Material.FILLED_MAP, 1, (short) mv.getId());
                    MapMeta mm = (MapMeta) item.getItemMeta();
                    mm.setMapView(mv);
                    item.setItemMeta(mm);
                    player.getInventory().addItem(item);
                    wmap++;
                    if(wmap > maps){
                        wmap = 1;
                        hmap++;
                    }
                }
            }
        }, 1L);
    }
    
    private void drawVideoMap(String vpath, Player player, boolean replay){
        mapVideoPrepared = false;
        int num_maps = getNumMaps();
        log.info(String.valueOf(num_maps));
        int maps = num_maps*num_maps;
        int wmap = 1, hmap = 1;
        MapView mv;
        videoMaps = new VideoMapRenderer[maps];
        for(int i = 0; i<maps; i++){
            mv = Bukkit.createMap(world);
            for(MapRenderer r : mv.getRenderers())
                mv.removeRenderer(r);
            VideoMapRenderer renderer = new VideoMapRenderer(String.format("%d_%d", wmap, hmap));
            mv.addRenderer(renderer);
            videoMaps[i] = renderer;
            ItemStack item = new ItemStack(Material.FILLED_MAP, 1, (short) mv.getId());
            MapMeta mm = (MapMeta) item.getItemMeta();
            mm.setMapView(mv);
            item.setItemMeta(mm);
            player.getInventory().addItem(item);
            wmap++;
            if(wmap == num_maps+1){
                wmap = 1;
                hmap++;
            }
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable(){
            @Override
            public void run(){
                Runtime rt = Runtime.getRuntime();
                try{
                    if(!replay){
                        log.info(String.format("Start rendering video: %s", vpath));
                        broadcastMsg(String.format("Start rendering video: %s", vpath));
                        int res = num_maps*128;
                        Process pr = rt.exec(String.format("%s \"plugins/MCdisplay/video/%s\" %dx%d --fps 20", backend_path, vpath, res, res));
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
                        mapVideoFrames = frames;
                        log.info(String.format("Number of frames: %d", frames));
                        broadcastMsg(String.format("Number of frames: %d", frames));
                        reader.close();

                        long start = System.currentTimeMillis();
                        String path = vpath.split("\\.")[0];

                        broadcastMsg("Loading video into maps...");

                        for(int i = 0; i<maps; i++){
                            videoMaps[i].initVideo(frames, res);
                        }
                        
                        BufferedImage img;
                        File f;
                        BufferedImage tempImg;
                        byte[][][] videos =  new byte[maps][frames][num_maps*128];
                        int mapw = 0, maph = 0;
                        for(videoFrame = 0; videoFrame<frames; videoFrame++){
                            maph = 0;
                            mapw = 0;
                            f = new File(String.format("plugins/MCdisplay/resized/%s_%d.jpg", path, videoFrame));
                            img = ImageIO.read(f);

                            int x, y;
                            for(int i = 0; i<maps; i++){
                                x = 128*mapw;
                                y = 128*maph;
                                //log.info(String.format("x: %d, y: %d", x, y));
                                //log.info(String.format("x:%d, y:%d, w:%d, h:%d", x, y, 128, 128));
                                tempImg = img.getSubimage(x, y, 128, 128);
                                //videos[i][videoFrame] = MapPalette.imageToBytes(tempImg);
                                videoMaps[i].addFrame(MapPalette.imageToBytes(tempImg));
                                mapw++;
                                if(mapw == num_maps){
                                    mapw = 0;
                                    maph++;
                                }
                            }

                            if(videoFrame % 100 == 0){
                                log.info(String.format("Colormapped %d frames", videoFrame));
                                broadcastMsg(String.format("Colormapped %d frames", videoFrame));
                            }
                        }
                        log.info(String.format("Time taken for colormapping: %dms", System.currentTimeMillis() - start));
                        videoFrame = 0;
                        /*for(int i = 0; i<maps; i++){
                            videoMaps[i].setVideo(videos[i], frames, res);
                        }*/
                        broadcastMsg(String.format("Time taken for colormapping: %ds", (System.currentTimeMillis() - start)/1000));
                    }

                    broadcastMsg("MapVideo prepared, use '/display start' to start playback.");
                    mapVideoPrepared = true;
                }
                catch(Exception e){
                    broadcastErr(String.valueOf(e));
                    e.printStackTrace();
                    log.info(String.format("Frame: %d, Width: %d, Height: %d", videoFrame, pixelW, pixelH));
                    videoFrame = 0;
                    videoPlays = false;
                    isPaused = false;
                    isVideoPlaying = false;
                    mapVideoPrepared = false;
                }
            }
        });
    }
    
    private void startMapVideo(){
        videoFrame = 0;
        isVideoPlaying = true;
        for(VideoMapRenderer r: videoMaps){
            r.start();
        }
        this.videoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                //log.info(String.valueOf(isPaused));
                if(!isPaused){
                    for(VideoMapRenderer r: videoMaps){
                        r.setFinished(false);
                    }
                    if(videoFrame>=mapVideoFrames){
                        //video = null;
                        for(VideoMapRenderer r: videoMaps){
                            r.setFinished(true);
                        }
                        Bukkit.getServer().getScheduler().cancelTask(videoID);
                        videoPlays = false;
                        videoFrame = 0;
                        isVideoPlaying = false;
                        broadcastMsg("Done playing video");
                    }
                    videoFrame++;

                } else {
                    for(VideoMapRenderer r: videoMaps){
                        r.setFinished(true);
                    }
                }
            }
        }, 100L, 1L);
    }
    
    private void changeResolution(String res){
        despawnDisplay();
        String[] res_list = res.split("x");
        w = Integer.valueOf(res_list[0]);
        h = Integer.valueOf(res_list[1]);
        spawnDisplay();
    }
    
    private void drawImage(String path){
        broadcastMsg("Rendering image " + path);
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
                    Process pr = rt.exec(String.format("%s \"plugins/MCdisplay/image/%s\" %dx%d", backend_path, path, w, h));
                    log.info(String.format("%s \"plugins/MCdisplay/image/%s\" %dx%d", backend_path, path, w, h));
                    int p = pr.waitFor();
                    log.info("Reading resized image " + path);
                    File f = new File("plugins/MCdisplay/resized/" + path);
                    BufferedImage img = ImageIO.read(f);
                    log.info("Colormapping " + path);
                    Material[][] pixels = new Material[w][h];
                    for(int i = 0; i < w; i++){
                        for(int j = 0; j < h; j++){
                            pixels[i][j] = colormap.matchColor(-img.getRGB(i, j));
                        }
                    }
                    colormap.clearCache();
                    log.info(String.format("Rendering image %s to display", path));
                    
                    renderImage(pixels);
                    log.info(String.format("Rendering time: %dms", System.currentTimeMillis() - start));
                    broadcastMsg(String.format("Rendered image in %.2fs", ((double)System.currentTimeMillis() - start)/1000));
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
            log.info(String.format("Time taken for frame #%d: %dms", videoFrame, System.currentTimeMillis()-start));
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
