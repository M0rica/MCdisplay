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

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author M0rica
 */
public class DisplayManager {
    
    private Logger log;
    private BukkitScheduler scheduler;
    
    private HashMap<String, Display> displays;
    private HashMap<String, String> players;
    private RunningCommandInfo runningCommandInfo;
    
    private MCDisplay plugin;
    private Settings settings;
    private Colormap colormap;
    private Chat chat;
            
    public DisplayManager(Logger l, MCDisplay mcd, Settings s, Chat c){
        log = l;
        plugin = mcd;
        settings = s;
        chat = c;
        log.info("Setting up DisplayManager");
        displays = new HashMap<>();
        players = new HashMap<>();
        colormap = new Colormap(log);
        scheduler = Bukkit.getScheduler();
    }
    
    /**
     * Get the names of all currently registered displays.
     * @return names of all displays
     */
    public String[] getDisplayNames(){
        Set<String> set = displays.keySet();
        return set.toArray(new String[0]);
    }
    
    public List<String> getAllDisplaySettings(){
        List<Field> vars = DisplaySettings.getChangeableVariables();
        List<String> varNames = new ArrayList<>();
        for(Field field: vars){
            varNames.add(field.getName());
        }
        return varNames;
    }
    
    public Colormap getColormap(){
        return colormap;
    }
    
    public Chat getChat(){
        return chat;
    }
    
    public boolean startAction(String player, String command){
        if(runningCommandInfo == null){
            runningCommandInfo = new RunningCommandInfo(command, player, System.currentTimeMillis());
            return true;
        } else {
            chat.info(String.format("Failed to execute command %s because another command is allready running: %s", command, runningCommandInfo.toString()));
            return false;
        }
    }
    
    public void reportActionComplete(){
        chat.info(runningCommandInfo.onComplete());
        runningCommandInfo = null;
    }
    
    public void reportActionFailed(){
        chat.info(runningCommandInfo.onFail());
        runningCommandInfo = null;
    }
    
    /**
     * Selects a display for the player to redirect all display commands to
     * @param playerID the player that wants to select a display
     * @param displayName the name of the display the player wants to select
     */
    public void selectDisplay(String playerID, String displayName){
        if(displays.containsKey(displayName)){
            if(!hasDisplaySelected(playerID)){
                players.put(playerID, displayName);
                chat.info(String.format("Selected display %s", displayName));
            } else {
                deselectDisplay(playerID);
                selectDisplay(playerID, displayName);
            }
        } else {
            chat.error(String.format("Failed to select display %s: does not exist", displayName));
        }
    }
    
    /**
     * Deselcts the current display of player if possible
     * @param playerID the player that wants to deselct his current display
     */
    public void deselectDisplay(String playerID){
        if(players.containsKey(playerID)){
            players.remove(playerID);
        }
    }
    
    /**
     * Check if a player has selected a display
     * @param playerID the player's name
     * @return
     */
    private boolean hasDisplaySelected(String playerID){
        return players.containsKey(playerID);
    }
    
    private Display resolvePlayerID(String playerID){
        String displayName = players.get(playerID);
        return displays.get(displayName);
    }
    
    /**
     * Create a new display with the given name if not allready exists
     * @param name name for the new display
     */
    public void createDisplay(String name){
        if(!displays.containsKey(name)){
            displays.put(name, new Display(plugin, this, colormap, settings, chat, name, log));
            chat.info(String.format("Created new Display %s", name));
        } else {
            chat.error(String.format("Failed to create display %s as it allready exists", name));
        }
    }
    
    public void setDisplayPos(String playerID, int[] pos){
        if(hasDisplaySelected(playerID)){
            Display display = resolvePlayerID(playerID);
            display.setPos(pos);
            chat.info(String.format("Set position for display %s to %d %d %d", display.getName(), pos[0], pos[1], pos[2]));
        } else {
            chat.error("Failed to set position of display: no display selected");
        }
    }
    
    public void setDisplayPos(Player player){
        Location loc = player.getTargetBlock(null, 25).getLocation();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int[] pos = new int[]{x, y, z};
        String playerID = player.getUniqueId().toString();
        setDisplayPos(playerID, pos);
    }
    
    public void setDisplayResolution(String playerID, int[] resolution){
        if(hasDisplaySelected(playerID)){
            Display display = resolvePlayerID(playerID);
            display.setResolution(resolution);
            chat.info(String.format("Set resolution for display %s to %d %d", display.getName(), resolution[0], resolution[1]));
        } else {
            chat.error("Failed to change display resolution: no display selected");
        }
    }
    
    public void centerPlayer(String playerID){
        if(hasDisplaySelected(playerID)){
            Player player = Bukkit.getPlayer(UUID.fromString(playerID));
            Display display = resolvePlayerID(playerID);
            display.center(player);
        } else {
            chat.error("Failed to center display: no display selected");
        }
    }
    
    public void setDisplaySetting(String playerID, String var, String value){
        if(hasDisplaySelected(playerID)){
            Display display = resolvePlayerID(playerID);
            if(display.setSetting(var, value)){
                chat.info(String.format("Sucessfully set setting %s for display %s to %s", var, display.getName(), value));
            } else {
                chat.info(String.format("Failed to set setting %s for display %s, pobably because it's an invalid value", var, display.getName()));
            }
        } else {
            chat.info("Failed to set setting: no display selected");
        }
    }
    
    public void loadDisplaySettings(){
        File[] folder = new File(settings.displaysPath).listFiles();
        if(folder != null){
            log.info("Loading saved display data");
            for(File f: folder){
                if(f.isFile()){
                    createDisplay(f.getName().split("\\.")[0]);
                }
            }
        } else {
            log.info("No saved display data found, skip loading displays");
        }
    }
    
    public void renderImage(String playerID, String path){
        if(hasDisplaySelected(playerID)){    
            if(startAction(playerID, "image")){
                Display display = resolvePlayerID(playerID);
                display.renderImage(path);
            }
        } else {
            chat.error("Failed to render image to display: no display selected");
        }
    }
    
    public void saveDisplaySettings(){
        for(Display display: displays.values()){
            display.getSettings().save();
        }
    }
    
}
