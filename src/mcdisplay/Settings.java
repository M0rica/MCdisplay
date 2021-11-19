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
public class Settings {
    
    private Logger log;
    private String configPath;
    public String pluginPath = "plugins/MCdisplay";
    
    public String imgPath = "plugins/MCdisplay/image";
    public String vidPath = "plugins/MCdisplay/video";
    public String displaysPath = "plugins/MCdisplay/displays";
    
    public int displayWidth = 128;
    public int displayHeight = 72;
    public float sharpnessFactor = (float) 0.3;
    public boolean upResImage = false;
    
    public Settings(Logger l){
        log = l;
        configPath = String.format("%s/settings.properties", pluginPath);
    }
    
    public void load(){
        if(Utils.isFile(configPath)){
            log.info("Loading settings");
            Properties p = new Properties();
            try {
                InputStream input = new FileInputStream(configPath);
                p.load(input);
            } catch (Exception e) {
                log.warning("Error when loading settings");
            }
            if(!p.isEmpty()){
                List<Field> fields = getPublicFields();
                for(Field field: fields){
                    try{
                        field.setAccessible(true);
                        String value = p.getProperty(field.getName());
                        Class type = field.getType();
                        if(type.equals(double.class)){
                            field.set(this, Double.valueOf(value));
                        } else if(type.equals(float.class)){
                            field.set(this, Float.valueOf(value));
                        } else if(type.equals(int.class)){
                            field.set(this, Integer.valueOf(value));
                        } else if(type.equals(boolean.class)){
                            field.set(this, Boolean.valueOf(value));
                        } else if(type.equals(String.class)){
                            field.set(this, value);
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                log.info("Successfully loaded settings");
            }
        } else {
            log.info("No settings.properties found, skip loading settings");
        }
    }
    
    public void save(){
        log.info("Saving settings");
        Properties p = new Properties();
        
        List<Field> fields = getPublicFields();
        for(Field field: fields){
            try {
                p.setProperty(field.getName(), String.valueOf(field.get(this)));
            } catch (Exception e) {
                log.warning(String.format("Could not get value for field %s", field.getName()));
            }
        }
        
        try {
            FileOutputStream output = new FileOutputStream(configPath);
            p.store(output, null);
            log.info("Successfully saved settings to settings.properties");
        } catch (Exception e) {
            log.warning("Failed to save settings to settings.properties");
        }
    }
    
    public List<Field> getPublicFields(){
        List<Field> fields = Arrays.stream(Settings.class.getDeclaredFields())
            .filter(f -> Modifier.isPublic(f.getModifiers()))
            .collect(Collectors.toList());
        return fields;
    }
    
}
