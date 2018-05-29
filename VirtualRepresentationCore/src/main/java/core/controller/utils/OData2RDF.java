package core.controller.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a conversion possibility from OData to RDF.
 * @author Jan-Peter.Schmidt
 */
public class OData2RDF {
    
    /**
     * This methods calls does a shell call on a library that does OData to RDF conversion.
     * This is done by using an xml configuration file. We reccomend using RMLMapper for this.
     * @param jarPath Path to conversion archive (.jar)
     * @param xmlFile File to convert
     * @param configPath Path to configuration xml.
     * @return File with RDF Triples that are derviced from XML File.
     */
    public static File convert(String jarPath, File xmlFile, String configPath) {
        
        /**
         * java -jar RML-Mapper.jar -m ~/Documents/7_Abschlussarbeit/Jan-Peter_Master/ODataAuszug/protokolldb-mapping.rml.ttl 
         * -f turtle -o ~/Documents/7_Abschlussarbeit/Jan-Peter_Master/ODataAuszug/graph.ttl 
         */
        
        try {        
        
            File rdfFile = File.createTempFile("convertedRDFFile", ".ttl");

            List<String> argList = new ArrayList();
            /*argList.add("cmd.exe");
            argList.add("/c");
            argList.add("start");
            argList.add("\"XML2RDF Converter - " + xmlFile.getName() + "\"");*/
            //argList.add("/B");
            argList.add("java");
            argList.add("-jar");
            argList.add(jarPath);
            argList.add("-m");
            argList.add(configPath);
            argList.add("-f turtle");
            argList.add("-o");
            argList.add(rdfFile.getAbsolutePath());
            
            /*System.out.println(String.join(" ", argList));
            
            System.out.println("RDF File: " + rdfFile.getAbsolutePath());
            System.out.println("XML File: " + xmlFile.getParentFile().getAbsolutePath());
            System.out.println("Config file: " + configPath);
            
            System.out.println("Converted file stored under: " + rdfFile.getAbsolutePath());*/

            ProcessBuilder pb = new ProcessBuilder(argList);
            pb.inheritIO();
            pb.directory(xmlFile.getParentFile());
            Process p = pb.start();
            System.out.println("Process started");
            if(p.waitFor(30, TimeUnit.SECONDS)) {
                System.out.println("Process ends");
                return rdfFile;
            } else {
                System.out.println("Process killed.");
            }
            
        } catch (Exception ex) {
            Logger.getLogger(OData2RDF.class.getName()).log(Level.SEVERE, "Error von OData2RDF Conversion.");
        }
        
        return null;
        
    }
    
}
