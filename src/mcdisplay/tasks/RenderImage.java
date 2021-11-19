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
import java.util.Arrays;
import mcdisplay.*;
/**
 *
 * @author M0rica
 */
public class RenderImage extends Task{
    
    DisplayManager displayManager;
    Settings settings;
    
    public RenderImage(MCDisplay mcd){
        super(mcd);
        displayManager = mcdisplay.getDisplayManager();
        settings = mcdisplay.getSettings();
    }

    @Override
    public void run(String[] args){
        running = true;
        String path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        displayManager.renderImage(args[0], path);
        running = false;
    }

    @Override
    public boolean checkArgs(String[] args){
        if(args.length >= 2){
            String path = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if(Utils.isURL(path)){
                return true;
            } else {
                path = settings.imgPath + "/" + path;
                if(Utils.isFile(path)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getSyntax(){
        return "/display image <URL or FILEPATH>";
    }
    
}
