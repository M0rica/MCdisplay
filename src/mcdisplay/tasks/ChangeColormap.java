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

/**
 *
 * @author M0rica
 */
public class ChangeColormap extends Task{
    DisplayManager displayManager;
    Chat chat;
    
    public ChangeColormap(MCDisplay mcd){
        super(mcd);
        displayManager = mcdisplay.getDisplayManager();
        chat = displayManager.getChat();
    }

    @Override
    public void run(String[] args){
        running = true;
        try{
            displayManager.getColormap().loadColormap(args[1]);
            chat.info(String.format("Successfully loaded colormap %s", args[1]));
        } catch(Exception e){
            chat.error(String.format("Could not load colormap %s", args[1]));
            e.printStackTrace();
        }
        running = false;
    }

    @Override
    public boolean checkArgs(String[] args){
        return args.length >= 2;
    }

    @Override
    public String getSyntax(){
        return "/display create <name of display>";
    }
}
