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
public class CreateDisplay extends Task{
    DisplayManager displayManager;
    
    public CreateDisplay(MCDisplay mcd){
        super(mcd);
        displayManager = mcdisplay.getDisplayManager();
    }

    @Override
    public void run(String[] args){
        running = true;
        displayManager.createDisplay(args[1]);
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
