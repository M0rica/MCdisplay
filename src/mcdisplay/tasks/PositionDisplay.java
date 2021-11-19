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
package mcdisplay.tasks;
import java.util.UUID;
import mcdisplay.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
/**
 *
 * @author M0rica
 */
public class PositionDisplay extends Task{
    DisplayManager displayManager;
    
    public PositionDisplay(MCDisplay mcd){
        super(mcd);
        displayManager = mcdisplay.getDisplayManager();
    }

    @Override
    public void run(String[] args){
        running = true;
        if(args.length >= 4){
            displayManager.setDisplayPos(args[0], toInt(args));
        } else {
            Player player = Bukkit.getPlayer(UUID.fromString(args[0]));
            displayManager.setDisplayPos(player);
        }
        running = false;
    }

    @Override
    public boolean checkArgs(String[] args){
        if(args.length >= 4){
            try{
                toInt(args);
            } catch(Exception e){
                return false;
            }
        } else if(args.length >= 1){
            return true;
        }
        return false;
    }

    @Override
    public String getSyntax(){
        return "/display position <x> <y> <z> OR /display position (takes the block you're looking at)";
    }
    
    private int[] toInt(String[] args){
        int[] pos = new int[3];
        pos[0] = Integer.parseInt(args[1]);
        pos[1] = Integer.parseInt(args[2]);
        pos[2] = Integer.parseInt(args[3]);
        return pos;
    }
}
