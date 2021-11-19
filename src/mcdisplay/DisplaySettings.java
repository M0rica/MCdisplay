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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author M0rica
 */
public class DisplaySettings {
    
    private Logger log;
    
    private String displayName;
    private String configPath;
    private String pluginPath = "plugins/MCdisplay";
    
    public Integer x, y, z;
    public int width, height;
    public float sharpnessFactor;
    public boolean upResImage;
    
    public DisplaySettings(String name, Settings settings, Logger l){
        displayName = name;
        log = l;
        configPath = String.format("%s/displays/%s.properties", pluginPath, displayName);
        
        x = null;
        y = null;
        z = null;
        
        width = settings.displayWidth;
        height = settings.displayHeight;
        sharpnessFactor = settings.sharpnessFactor;
        upResImage = settings.upResImage;
    }
    
    public static List<Field> getChangeableVariables(){
        List<Field> fields = Arrays.stream(DisplaySettings.class.getDeclaredFields())
            .filter(f -> Modifier.isPublic(f.getModifiers()))
            .collect(Collectors.toList());
        return fields;
    }
    
    public void save(){
        log.info(String.format("Saving settings for display %s", displayName));
       Properties p = new Properties();
       List<Field> fields = DisplaySettings.getChangeableVariables();
       for(Field field: fields){
           try {
               p.setProperty(field.getName(), String.valueOf(field.get(this)));
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       try {
            FileOutputStream output = new FileOutputStream(configPath);
            p.store(output, null);
            log.info(String.format("Successfully saved settings for display %s", displayName));
        } catch (Exception e) {
            e.printStackTrace();
            log.warning(String.format("Failed to save settings for display %s", displayName));
        }
    }
    
    public void load(){
        if(Utils.isFile(configPath)){
            log.info(String.format("Loading settings for display %s", displayName));
            Properties p = new Properties();
            try {
                InputStream input = new FileInputStream(configPath);
                p.load(input);
            } catch (Exception e) {
                log.warning(String.format("Failed to load settings for display %s", displayName));
            }
            if(!p.isEmpty()){
                List<Field> fields = DisplaySettings.getChangeableVariables();
                for(Field field: fields){
                    try{
                        field.setAccessible(true);
                        String value = p.getProperty(field.getName());
                        Class type = field.getType();
                        if(type.equals(double.class)){
                            field.set(this, Double.valueOf(value));
                        } else if(type.equals(float.class)){
                            field.set(this, Float.valueOf(value));
                        } else if(type.equals(Integer.class) || type.equals(int.class)){
                            field.set(this, Integer.valueOf(value));
                        } else if(type.equals(boolean.class)){
                            field.set(this, Boolean.valueOf(value));
                        } else if(type.equals(String.class)){
                            field.set(this, value);
                        }
                    } catch(Exception e){
                        log.warning(String.format("Faild to set value for field %s", field.getName()));
                    }
                }
                log.info(String.format("Successfully loaded settings for display %s", displayName));
            }
        }
    }
}
