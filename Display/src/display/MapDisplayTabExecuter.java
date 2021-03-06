/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
/**
 *
 * @author M0rica
 */
public class MapDisplayTabExecuter implements TabExecutor{
    
    MapDisplay plugin;
    
    public MapDisplayTabExecuter(MapDisplay p){
        plugin = p;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        
        if(args.length == 1){
            
            commands.add("image");
            commands.add("video");
            commands.add("pause");
            commands.add("stop");
            commands.add("resolution");
            commands.add("start");
            StringUtil.copyPartialMatches(args[0], commands, completions);
            
        } else if(args.length == 2){
            switch (args[0]) {
                case "resolution":
                    commands.add("128x72");
                    commands.add("256x144");
                    commands.add("384x216");
                    commands.add("512x288");
                    break;
                case "image":
                    {
                        File[] folder = new File("plugins/MCdisplay/image").listFiles();
                        for(File f: folder){
                            if(f.isFile()){
                                commands.add(f.getName());
                            }
                        }       break;
                    }
                case "video":
                    {
                        File[] folder = new File("plugins/MCdisplay/video").listFiles();
                        for(File f: folder){
                            if(f.isFile()){
                                commands.add(f.getName());
                            }
                        }       break;
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
        return plugin.processCommand(cs, cmnd, string, strings);
    }
    
}
