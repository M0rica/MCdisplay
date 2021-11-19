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

/**
 *
 * @author M0rica
 */
public class Utils {
    
    public static boolean isURL(String path){
        return path.startsWith("https://") || path.startsWith("http://");
    }
    
    public static boolean isFile(String path){
        return new File(path).isFile();
    }
    
    public static void createMissingDirs(){
        String[] dirs = new String[]{"image", "video", "displays"};
        for(String dir: dirs){
            File directory = new File(String.format("plugins/MCdisplay/%s", dir));
            if(!directory.exists()){
                directory.mkdir();
            }
        }
    }
}
