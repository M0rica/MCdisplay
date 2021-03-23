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
public class DisplayTabExecuter implements TabExecutor{
    
    Display plugin;
    
    public DisplayTabExecuter(Display p){
        plugin = p;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        
        if(args.length == 1){
            
            commands.add("on");
            commands.add("off");
            commands.add("image");
            commands.add("video");
            commands.add("replay");
            commands.add("pause");
            commands.add("stop");
            commands.add("resolution");
            commands.add("start");
            commands.add("tp");
            commands.add("colormap");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if(args.length == 2){
            switch (args[0]) {
                case "resolution":
                    commands.add("128x72");
                    commands.add("256x144");
                    commands.add("384x216");
                    commands.add("512x288");
                    commands.add("640x360");
                    commands.add("768x432");
                    commands.add("896x504");
                    commands.add("1024x576");
                    break;
                case "image":
                {
                    File[] folder = new File("plugins/MCdisplay/image").listFiles();
                    for(File f: folder){
                        if(f.isFile()){
                            commands.add(f.getName());
                        }
                    } break;
                }
                case "video":
                {
                    File[] folder = new File("plugins/MCdisplay/video").listFiles();
                    for(File f: folder){
                        if(f.isFile()){
                            commands.add(f.getName());
                        }
                    } break;
                }
                case "colormap":
                    File[] folder = new File("plugins/MCdisplay/colormaps").listFiles();
                    for(File f: folder){
                        if(f.isFile()){
                            commands.add(f.getName().split("\\.")[0]);
                        }
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
