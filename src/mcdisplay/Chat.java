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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author M0rica
 */
public class Chat {
    
    private MCDisplay plugin;
    private BukkitScheduler scheduler;
    
    public Chat(MCDisplay mcd){
        plugin = mcd;
        scheduler = Bukkit.getScheduler();
    }
    
    public void info(String msg){
        scheduler.runTask(plugin, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Display] " + ChatColor.LIGHT_PURPLE + msg);
            }
        });
    }
    
    public void warning(String msg){
        scheduler.runTask(plugin, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.YELLOW + "[Display] Warning: " + ChatColor.GOLD + msg);
            }
        });
    }
    
    public void error(String msg){
        scheduler.runTask(plugin, new Runnable(){
            @Override
            public void run(){
                Bukkit.broadcastMessage(ChatColor.DARK_RED + "[Display] Error: " + ChatColor.RED + msg);
            }
        });
    }
}
