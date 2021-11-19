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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import javax.imageio.ImageIO;

import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
/**
 *
 * @author M0rica
 */
public class ImageUtils {
    
    
    public static BufferedImage toBufferedImage(Image img){
        if(img instanceof BufferedImage){
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }
    
    public static BufferedImage resize(BufferedImage img, int w, int h){
        Image tmp = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return toBufferedImage(tmp);
    }
    
    public static BufferedImage resizeKeepAspect(BufferedImage img, int w, int h, boolean upRes){
        double ratio = scaleAspectRatio(img.getWidth(), img.getHeight(), w, h);
        if(ratio == 1){
            return img;
        } else {
            int origW = w;
            int origH = h;
            BufferedImage background = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            if(ratio < 1 || upRes){
                w = (int) (img.getWidth()*ratio);
                h = (int) (img.getHeight()*ratio);
                img = resize(img, w, h);
            } else {
                w = img.getWidth();
                h = img.getHeight();
            }
            
            Graphics2D bGr = background.createGraphics();
            bGr.drawImage(img, (origW/2)-(w/2), (origH/2)-(h/2), null);
            bGr.dispose();
            
            return background;
        }
        
    }
    
    private static double scaleAspectRatio(int oldW, int oldH, int newW, int newH){
        double widthRatio = (double) newW / oldW;
        double heightRatio = (double) newH / oldH;
        double ratio = Math.min(widthRatio, heightRatio);
        return ratio;
    }
    
    public static BufferedImage loadImage(String path){
        File f = new File(path);
        BufferedImage img;
        try {
            img = ImageIO.read(f);
            return img;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static BufferedImage loadImageFromURL(String path){
        try {
            URL url = new URL(path);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36");
            Image img = ImageIO.read(connection.getInputStream());
            BufferedImage out = toBufferedImage(img);
            BufferedImage newOut = new BufferedImage(out.getWidth(), out.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            newOut.createGraphics().drawImage(out, 0, 0, out.getWidth(), out.getHeight(), null);
            return newOut;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static BufferedImage sharpenImage(BufferedImage img, float factor){
        if(factor == 0){
            return img;
        }
        float[] normalKernel = new float[]{0, 0, 0, 0, 1, 0, 0, 0, 0};
        float[] kernelData = new float[]{-1, -1, -1, -1, 9, -1, -1, -1, -1};
        for(int i = 0; i<kernelData.length; i++){
            kernelData[i] = (kernelData[i] - normalKernel[i]) * factor + normalKernel[i];
        }
        Kernel kernel = new Kernel(3, 3, kernelData);
        BufferedImageOp op = new ConvolveOp(kernel);
        img = op.filter(img, null);
        return img;
    }
}
