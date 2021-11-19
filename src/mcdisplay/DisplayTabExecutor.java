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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

/**
 *
 * @author M0rica
 */
public class DisplayTabExecutor implements TabExecutor{
    
    private MCDisplay plugin;
    private Settings settings;
    private DisplayManager displayManager;
    
    public DisplayTabExecutor(MCDisplay mcd, Settings s, DisplayManager dm){
        plugin = mcd;
        settings = s;
        displayManager = dm;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        
        if(args.length == 1){
            
            commands.add("select");
            commands.add("deselect");
            commands.add("image");
            commands.add("resolution");
            commands.add("center");
            commands.add("colormap");
            commands.add("create");
            commands.add("position");
            commands.add("settings");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if(args.length == 2){
            switch (args[0]) {
                case "select":
                    for(String name: displayManager.getDisplayNames()){
                        commands.add(name);
                    }
                    break;
                case "resolution":
                    commands.add("128x72");
                    commands.add("256x144");
                    commands.add("384x216");
                    commands.add("512x288");
                    commands.add("640x360");
                    commands.add("768x432");
                    commands.add("896x504");
                    commands.add("1024x576");
                    commands.add("1280x720");
                    break;
                case "image":
                {
                    File[] folder = new File(settings.imgPath).listFiles();
                    for(File f: folder){
                        if(f.isFile()){
                            commands.add(f.getName());
                        }
                    } 
                    break;
                }
                case "video":
                {
                    File[] folder = new File(settings.vidPath).listFiles();
                    for(File f: folder){
                        if(f.isFile()){
                            commands.add(f.getName());
                        }
                    } 
                    break;
                }
                case "colormap":
                    File[] folder = new File("plugins/MCdisplay/colormaps").listFiles();
                    for(File f: folder){
                        if(f.isFile()){
                            commands.add(f.getName().split("\\.")[0]);
                        }
                    }
                    break;
                case "settings":
                    for(String var: displayManager.getAllDisplaySettings()){
                        commands.add(var);
                    }
                default:
                    break;
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        return plugin.onCommand(cs, cmnd, string, strings);
    }
    
}
