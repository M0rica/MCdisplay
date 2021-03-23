/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Der Gerät
 */
public class MapDisplay {
    String backend_path = "python plugins/MCdisplay/resize.py";
    
    Logger log;
    Plugin plugin;
    World world;
    Display display;
    
    int w = 256;
    int h = 144;
    
    int frames;
    int videoFrame;
    
    int videoID;
    String lastvid;
    
    VideoMapRenderer[] videoMaps;
    
    boolean videoPrepared = false;
    boolean isPaused = false;
    boolean videoPlays = false;
    
    public MapDisplay(Display d, Plugin p, Logger l, World w){
        display = d;
        plugin = p;
        log = l;
        world = w;
    }
    
    public void canRender(boolean b){
        boolean videoPlays = b;
    }
    
    public boolean processCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        log.info(args[0]);
        String path;
        switch (args[0]) {
            case "image":
                    path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    if(display.doesFileExists("image/" + path)){
                        if(!videoPlays){
                            drawImageMap(path, (Player) sender);
                        } else {
                            display.broadcastErr("There is a video playing or rendering, you can't render an image right now!");
                        }
                    }
                    return true;
                    
            case "video":
                path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if(display.doesFileExists("video/" + path)){
                    boolean doReplay = path.equals(lastvid);
                    if(!videoPlays){
                        drawVideoMap(path, (Player) sender, doReplay);
                    } else {
                        display.broadcastErr("There is a video playing or rendering, you can't render an image right now!");
                    }
                }
                return true;
            case "start":
                if(videoPrepared && !videoPlays){
                    videoPlays = true;
                    startMapVideo();
                }
                return true;
            case "pause":
                //log.info(String.valueOf(isVideoPlaying));
                if(videoPlays && !isPaused){
                    isPaused = true;
                    display.broadcastMsg("Video paused");
                } else {
                    isPaused = false;
                    display.broadcastMsg("Video resumed");
                }
                return true;
            case "stop":
                try{
                    videoFrame = frames;
                } catch(Exception e) {

                }
                return true;
            case "replay":
                if(videoPrepared && !videoPlays){
                    path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    drawVideoMap(path, (Player) sender, true);
                }
                return true;
            case "resolution":
                if(!videoPlays){
                    changeResolution(args[1]);
                    display.broadcastMsg("Successfully changed resolution for mapdisplay to " + args[1] + "!");
                }
                return true;
        }       
        return false;
    }
    
    public void blockActions(){
        videoPlays = true;
        display.canRender(false);
    }
    
    public void unblockActions(){
        videoPlays = false;
        display.canRender(true);
    }
    
    private void changeResolution(String res){
        String[] res_list = res.split("x");
        w = Integer.valueOf(res_list[0]);
        h = Integer.valueOf(res_list[1]);
    }
    
    private void drawImageMap(String path, Player player){
        blockActions();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
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
                    unblockActions();
                } catch(Exception e){
                    log.warning(String.valueOf(e));
                    unblockActions();
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
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
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
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
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
        blockActions();
        videoPrepared = false;
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
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
            @Override
            public void run(){
                Runtime rt = Runtime.getRuntime();
                try{
                    if(!replay){
                        log.info(String.format("Start rendering video: %s", vpath));
                        display.broadcastMsg(String.format("Start rendering video: %s", vpath));
                        int res = num_maps*128;
                        Process pr = rt.exec(String.format("%s \"plugins/MCdisplay/video/%s\" %dx%d --fps 20", backend_path, vpath, res, res));
                        int p = pr.waitFor();
                        log.info("Rendered video");
                        display.broadcastMsg("Rendered video");

                        log.info("Reading video data");
                        JSONParser jsonParser = new JSONParser();
                        FileReader reader = new FileReader("plugins/MCdisplay/resized/video.json");
                        Object obj = jsonParser.parse(reader);
                        JSONObject data = (JSONObject) obj;
                        long lframes = (long)data.get("frames");
                        frames = Math.toIntExact(lframes);
                        //mapVideoFrames = frames;
                        log.info(String.format("Number of frames: %d", frames));
                        display.broadcastMsg(String.format("Number of frames: %d", frames));
                        reader.close();

                        long start = System.currentTimeMillis();
                        String path = vpath.split("\\.")[0];

                        display.broadcastMsg("Loading video into maps...");

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
                                display.broadcastMsg(String.format("Colormapped %d frames", videoFrame));
                            }
                        }
                        log.info(String.format("Time taken for colormapping: %dms", System.currentTimeMillis() - start));
                        videoFrame = 0;
                        /*for(int i = 0; i<maps; i++){
                            videoMaps[i].setVideo(videos[i], frames, res);
                        }*/
                        display.broadcastMsg(String.format("Time taken for colormapping: %ds", (System.currentTimeMillis() - start)/1000));
                    }

                    display.broadcastMsg("MapVideo prepared, use '/mapdisplay start' to start playback.");
                    videoPrepared = true;
                    unblockActions();
                }
                catch(Exception e){
                    display.broadcastErr(String.valueOf(e));
                    e.printStackTrace();
                    log.info(String.format("Frame: %d", videoFrame));
                    videoFrame = 0;
                    isPaused = false;
                    videoPrepared = false;
                    unblockActions();
                }
            }
        });
    }
    
    private void startMapVideo(){
        blockActions();
        videoFrame = 0;
        for(VideoMapRenderer r: videoMaps){
            r.start();
        }
        this.videoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                //log.info(String.valueOf(isPaused));
                if(!isPaused){
                    for(VideoMapRenderer r: videoMaps){
                        r.setFinished(false);
                    }
                    if(videoFrame>=frames){
                        //video = null;
                        for(VideoMapRenderer r: videoMaps){
                            r.setFinished(true);
                        }
                        Bukkit.getServer().getScheduler().cancelTask(videoID);
                        videoPlays = false;
                        videoFrame = 0;
                        display.broadcastMsg("Done playing video");
                    }
                    videoFrame++;

                } else {
                    for(VideoMapRenderer r: videoMaps){
                        r.setFinished(true);
                    }
                    unblockActions();
                }
            }
        }, 100L, 1L);
    }
}
