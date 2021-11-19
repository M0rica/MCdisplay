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

import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author M0rica
 */
public class MCDisplay extends JavaPlugin{
    
    private Logger log;
    
    private DisplayManager displayManager;
    private Settings settings;
    private Chat chat;
    private TaskManager taskManager;
    
    @Override
    public void onEnable(){
        ImageIO.scanForPlugins();
        
        log = this.getLogger();
        Utils.createMissingDirs();
        
        chat = new Chat(this);
        settings = new Settings(log);
        settings.load();
        displayManager = new DisplayManager(log, this, settings, chat);
        taskManager = new TaskManager(this, chat, log);
        
        displayManager.loadDisplaySettings();
        
        TabExecutor tabExecutor = new DisplayTabExecutor(this, settings, displayManager);
        this.getCommand("display").setExecutor(tabExecutor);
        this.getCommand("display").setTabCompleter(tabExecutor);
        log.info("Plugin enabled");
    }
    
    @Override
    public void onDisable(){
        settings.save();
        displayManager.saveDisplaySettings();
        log.info("Plugin disabled");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(sender instanceof Player){
            return taskManager.runCommand(sender, args);
        } else {
            chat.error("You have to be a player to use MCDisplay commands");
            return true;
        }
    }
    
    public DisplayManager getDisplayManager(){
        return displayManager;
    }
    
    public Settings getSettings(){
        return settings;
    }
}
