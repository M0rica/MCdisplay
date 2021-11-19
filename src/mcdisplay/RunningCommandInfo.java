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

import java.util.UUID;
import org.bukkit.Bukkit;

/**
 *
 * @author M0rica
 */
public class RunningCommandInfo {
    
    public String commandName, playerName;
    private long startTime;
    
    public RunningCommandInfo(String cn, String pID, long st){
        commandName = cn;
        UUID id = UUID.fromString(pID);
        playerName = Bukkit.getPlayer(id).getName();
        startTime = st;
    }
    
    public double getTimeElapsed(){
        return ((double) System.currentTimeMillis() - startTime)/1000;
    }
    
    @Override
    public String toString(){
        return String.format("Command /%s by player %s (running since %.2fs)", commandName, playerName, getTimeElapsed());
    }
    
    public String onComplete(){
        return String.format("Command /%s by player %s completed in %.2fs", commandName, playerName, getTimeElapsed());
    }
    
    public String onFail(){
        return String.format("Command /%s by player %s failed", commandName, playerName);
    }
}
