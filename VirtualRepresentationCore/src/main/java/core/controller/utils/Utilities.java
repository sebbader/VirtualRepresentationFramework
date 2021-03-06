/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

/**
 * This class provides useful helping methods that are needed somehwere in
 * the project
 * @author Jan-Peter.Schmidt
 */
public class Utilities {
    
    /**
     * Utility method to save InputStream data to target location/file
     * 
     * @param inStream InputStream to be saved
     * @param fileDetails Further details on the file stored in inStream.
     * @return File that contains content of inStream
     * @throws java.io.IOException Throws IO Exception if file is not accesible
     */
    public static File saveToFile(InputStream inStream, FormDataContentDisposition fileDetails) throws IOException {

        String target = "\\virtrepframework\\temp\\";
        File folder = new File(target);
        folder.mkdirs();
        OutputStream out = null;
        int read = 0;
        byte[] bytes = new byte[1024];
        File file = File.createTempFile("uploadedFile", fileDetails.getFileName(), folder);
        out = new FileOutputStream(file);
        while ((read = inStream.read(bytes)) != -1) {
            System.out.println("Writing...");
                out.write(bytes, 0, read);
        }
        out.flush();
        out.close();

        return file;
    }
    
    /**
     * Returns the suffix of a file
     * @param file File for suffix determination
     * @return Suffix of file
     */
    public static String getEndung(File file) {
        
        if(file!=null) {
            String[] split = file.getAbsolutePath().split("\\.");       
            return "." + split[split.length-1];
        } else {
            return "";
        }
        
    }
    
    /**
     * Taken from https://stackoverflow.com/a/326440
     * Converts a File to a String
     * @param path Path to file
     * @param encoding Encoding of file
     * @return String that is contained in file
     */
    public static String readFile(String path, Charset encoding) {
        
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return "";
        
    }
    
    /**
     * Writes File from String
     * @param fileString String that will be written to file
     * @param endung suffix of file
     * @param encoding encoding of file
     * @return File with specified properties that contains fileString.
     */
    public static File writeFile(String fileString, String endung, Charset encoding) {
        
        try {
            
            byte[] bytes = fileString.getBytes(encoding);
            File file = File.createTempFile("encodedFile", endung);
            Files.write(file.toPath(), bytes);
            return file;
            
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        
        return null;
        
    }
    
}
