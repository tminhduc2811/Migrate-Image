package com.ducta.MigrateImageProperties.helper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class StringHelper {
    public static String rewriteHtml(String html, RestTemplate restTemplate) throws IOException, InterruptedException {
        String[] lines = html.split(System.getProperty("line.separator"));
        ArrayList<String> results = new ArrayList<>();

        for (String line : lines) {
            if (line.contains("img") && !line.contains("width") && !line.contains("height")) {
                String src = getImageSrc(line);
                if(src.contains("firebasestorage")) {
                    results.add(line);
                } else {
                    byte[] imageBytes = restTemplate.getForObject(src, byte[].class);
                    assert imageBytes != null;

                    System.out.println("Starting to read image " + src);
                    try {
                        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                        int width = bufferedImage.getWidth();
                        int height = bufferedImage.getHeight();

                        System.out.println("Got width=" + width + ", height=" + height);
                        results.add(addDimension(line, width, height));
                        Thread.sleep(200);
                    } catch (Exception e) {
                        System.out.println("Error during reading file");
                        results.add(line);
                    }
                }
            } else {
                results.add(line);
            }
        }

        return String.join(System.getProperty("line.separator"), results);
    }

    public static String getImageSrc(String imgTag) {
        return imgTag.split("src=\"")[1].split("\"")[0];
    }

    public static String addDimension(String line, int width, int height) {
        String[] subLines = line.split("<img ");
        return subLines[0] + "<img width=\"" + width + "\" height=\"" + height + "\" " + subLines[1];
    }

}
