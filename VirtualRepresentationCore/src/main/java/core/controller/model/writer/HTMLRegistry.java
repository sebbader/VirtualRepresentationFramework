/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.controller.model.writer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.LangBuilder;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;

/**
 *
 * @author Jan-Peter.Schmidt
 */
public class HTMLRegistry {
    
    public static void registerHTMLWriter() {
        
        Lang lang = LangBuilder.create("HTML", "text/html").addFileExtensions("html").build();        
        
        if(!RDFWriterRegistry.contains(lang)) {
            
            System.out.println("HTMLWriter wurde registriert!");
        
            RDFLanguages.register(lang);

            RDFFormat format = new RDFFormat(lang) ;
            RDFWriterRegistry.register(lang, format)  ;
            RDFWriterRegistry.register(format, new HTMLWriterFactory()) ; 
        
        } else {
            System.out.println("Ist schon..");
        }
  
        
    }
    
}
