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

import mcdisplay.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author M0rica
 */
public class ResolutionDisplay extends Task{
    DisplayManager displayManager;
    
    public ResolutionDisplay(MCDisplay mcd){
        super(mcd);
        displayManager = mcdisplay.getDisplayManager();
    }

    @Override
    public void run(String[] args){
        running = true;
        int[] resolution = toResolution(args);
        displayManager.setDisplayResolution(args[0], resolution);
        running = false;
    }

    @Override
    public boolean checkArgs(String[] args){
        if(args.length >= 2){
            try{
                toResolution(args);
                return true;
            } catch(Exception e){
                return false;
            }
        }
        return false;
    }

    @Override
    public String getSyntax(){
        return "/display resolution <width>x<height>";
    }
    
    private int[] toResolution(String[] args){
        int[] resolution = new int[2];
        String[] res_list = args[1].split("x");
        resolution[0] = Integer.valueOf(res_list[0]);
        resolution[1] = Integer.valueOf(res_list[1]);
        return resolution;
    }
}
