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

/**
 *
 * @author M0rica
 */
public abstract class Task {
    
    public MCDisplay mcdisplay;
    public boolean running;
    
    public Task(MCDisplay mcd){
        mcdisplay = mcd;
        running = false;
    }
    
    public boolean isRunning(){
        return running;
    }
    
    public abstract void run(String[] args);
    public abstract boolean checkArgs(String[] args);
    public abstract String getSyntax();
}
