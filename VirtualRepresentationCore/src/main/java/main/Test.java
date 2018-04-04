/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class Test {
    
    public static void main(String args[]) {
        
        String path =  "C:\\Users\\JP_Schmidt\\Desktop\\MaschineSmall.n3";

        File file = new File(path);

        Model model = ModelFactory.createDefaultModel();
        model.read(file.getAbsolutePath());
        System.out.println("MS: " + model.getGraph().size());
        
        Property prop = ResourceFactory.createProperty("https://w3id.org/saref", "Unit_of_measure");
        
        model.listObjectsOfProperty(prop).toList().forEach((object) -> {
            
            Model model2 = ModelFactory.createDefaultModel();
            model2.read(object.toString());
            
            System.out.println(model2.getGraph().size());

            
        });
     
    }
    
}
