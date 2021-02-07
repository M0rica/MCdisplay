/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package display;

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
 * @author Der Gerät
 */
public class DisplayTabExecuter implements TabExecutor{
    
    Display plugin;
    
    public DisplayTabExecuter(Display p){
        this.plugin = p;
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
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if(args.length == 2){
            if(args[0].equals("resolution")){
                commands.add("128x72");
                commands.add("256x144");
                commands.add("384x216");
                commands.add("512x288");
                commands.add("640x360");
                commands.add("768x432");
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        return this.plugin.processCommand(cs, cmnd, string, strings);
    }
    
}