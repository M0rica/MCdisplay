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
import java.util.HashMap;
import java.util.logging.Logger;
import mcdisplay.tasks.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
/**
 *
 * @author M0rica
 */
public class TaskManager {
    
    private Logger log;
    private HashMap<String, Task> tasks;
    private HashMap<String, Task> runningTasks;
    
    private MCDisplay mcdisplay;
    private Chat chat;
    
    private BukkitScheduler scheduler;
    
    public TaskManager(MCDisplay mcd, Chat c, Logger l){
        log = l;
        log.info("Setting up TaskManager");
        mcdisplay = mcd;
        chat = c;
        scheduler = Bukkit.getScheduler();
        
        tasks = new HashMap<>();
        runningTasks = new HashMap<>();
        
        tasks.put("image", new RenderImage(mcdisplay));
        tasks.put("create", new CreateDisplay(mcdisplay));
        tasks.put("select", new SelectDisplay(mcdisplay));
        tasks.put("deselect", new DeselectDisplay(mcdisplay));
        tasks.put("position", new PositionDisplay(mcdisplay));
        tasks.put("resolution", new ResolutionDisplay(mcdisplay));
        tasks.put("center", new CenterDisplay(mcdisplay));
        tasks.put("settings", new SettingsDisplay(mcdisplay));
        tasks.put("colormap", new ChangeColormap(mcdisplay));
    }
    
    public boolean runCommand(CommandSender sender, String[] args){
        Player player = (Player) sender;
        String playerID = player.getUniqueId().toString();
        
        String taskName = args[0];
        Task task = getTaskByName(taskName);
        if(task != null){
            if(!task.isRunning()){
                args[0] = playerID;
                if(task.checkArgs(args)){
                    scheduler.runTaskAsynchronously(mcdisplay, new Runnable() {
                        @Override
                        public void run() {
                            runTask(task, args);
                        }
                    });
                    return true;
                } else {
                    chat.info(String.format("Failed to run command %s: incorrect syntax. Correct syntax for this command is %s", taskName, task.getSyntax()));
                }
            } else {
                chat.info(String.format("Failed to run command %s: allready running", taskName));
            }
        } else {
            chat.info(String.format("Failed to run command %s: does not exist", taskName));
        }
        
        return true;
    }
    
    public Task getTaskByName(String name){
        return (Task) tasks.get(name);
    }
    
    public void runTask(Task task, String[] args){
        task.run(args);
    }
        
}
